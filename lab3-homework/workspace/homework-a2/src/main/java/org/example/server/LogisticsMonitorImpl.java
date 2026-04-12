package org.example.server;

import io.grpc.Context;
import io.grpc.stub.StreamObserver;
import sr.grpc.gen.Location;
import sr.grpc.gen.LogisticsMonitorGrpc.LogisticsMonitorImplBase;
import sr.grpc.gen.SubscriptionRequest;
import sr.grpc.gen.TransportEvent;
import sr.grpc.gen.UnsubscribeRequest;
import sr.grpc.gen.UnsubscribeResponse;
import sr.grpc.gen.VehicleType;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Deque;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.TimeUnit;

public class LogisticsMonitorImpl extends LogisticsMonitorImplBase {
    private static final int STREAM_INTERVAL_SECONDS = 7;
    private static final int BUFFER_TTL_SECONDS = 90; // Czas zycia eventu w bufforze
    private static final int MAX_BUFFER_PER_SUBSCRIPTION = 100; // Max liczba zaleglych eventow na subskrybcje

    // zapisane subskrypcje (trwają, dopóki nie zrobimy unsubscribe)
    private final Map<String, SubscriptionRequest> subscriptionsById = new ConcurrentHashMap<>();
    // to jest teraz online i ma aktywny stream
    private final Map<String, StreamObserver<TransportEvent>> liveObserversBySubscriptionId = new ConcurrentHashMap<>();
    // kolejka zaległych eventów dla offline klienta
    private final Map<String, Deque<BufferedEvent>> pendingBySubscriptionId = new ConcurrentHashMap<>();
    private final ScheduledExecutorService generatorScheduler = Executors.newSingleThreadScheduledExecutor();

    public LogisticsMonitorImpl() {
        // scheduler co 7 sekund woła metodę, która generuje i rozsyła eventy.
        generatorScheduler.scheduleAtFixedRate(
                this::generateAndDispatchForAllSubscriptions,
                0,
                STREAM_INTERVAL_SECONDS,
                TimeUnit.SECONDS
        );
    }

    @Override
    public void subscribeTransport(SubscriptionRequest request, StreamObserver<TransportEvent> responseObserver) {
        if (request.getClientId().isBlank() || request.getSubscriptionId().isBlank()) {
            responseObserver.onError(io.grpc.Status.INVALID_ARGUMENT
                    .withDescription("client_id and subscription_id are required")
                    .asRuntimeException());
            return;
        }

        // rejestruje subskrypcję i oznacza klienta jako online.
        subscriptionsById.put(request.getSubscriptionId(), request);
        liveObserversBySubscriptionId.put(request.getSubscriptionId(), responseObserver);
        System.out.println("SUBSCRIBE client=" + request.getClientId() + " sub=" + request.getSubscriptionId());
        // odsyła zaległe eventy z bufora
        replayBufferedEvents(request.getSubscriptionId(), responseObserver);

        //Dodaje listener na zamknięcie kontekstu: gdy stream padnie, klient jest zdejmowany z mapy online, ale subskrypcja zostaje.
        Context.current().addListener(context -> {
            liveObserversBySubscriptionId.remove(request.getSubscriptionId());
            System.out.println("STREAM CLOSED sub=" + request.getSubscriptionId());
        }, Runnable::run);
    }

    @Override
    public void unsubscribeTransport(UnsubscribeRequest request, StreamObserver<UnsubscribeResponse> responseObserver) {
        SubscriptionRequest existing = subscriptionsById.get(request.getSubscriptionId());

        boolean removed = false;
        if (existing != null && existing.getClientId().equals(request.getClientId())) {
            subscriptionsById.remove(request.getSubscriptionId());
            pendingBySubscriptionId.remove(request.getSubscriptionId());
            StreamObserver<TransportEvent> streamObserver = liveObserversBySubscriptionId.remove(request.getSubscriptionId());
            if (streamObserver != null) {
                synchronized (streamObserver) {
                    streamObserver.onCompleted();
                }
            }
            removed = true;
        }

        UnsubscribeResponse response = UnsubscribeResponse.newBuilder()
                .setRemoved(removed)
                .setMessage(removed ? "Subscription removed" : "Subscription not found")
                .build();
        responseObserver.onNext(response);
        responseObserver.onCompleted();
    }

    private void generateAndDispatchForAllSubscriptions() {
        for (Map.Entry<String, SubscriptionRequest> entry : subscriptionsById.entrySet()) {
            String subscriptionId = entry.getKey();
            SubscriptionRequest request = entry.getValue();
            TransportEvent event = generateEventFor(request);
            if (!matchesFilter(request, event)) {
                continue;
            }

            StreamObserver<TransportEvent> liveObserver = liveObserversBySubscriptionId.get(subscriptionId);
            if (liveObserver == null) {
                enqueueBufferedEvent(subscriptionId, event);
                continue;
            }

            try {
                pushEvent(liveObserver, event);
            } catch (RuntimeException ex) {
                liveObserversBySubscriptionId.remove(subscriptionId);
                enqueueBufferedEvent(subscriptionId, event);
            }
        }
    }

    private void replayBufferedEvents(String subscriptionId, StreamObserver<TransportEvent> observer) {
        Deque<BufferedEvent> queue = pendingBySubscriptionId.get(subscriptionId);
        if (queue == null) {
            return;
        }

        pruneExpired(queue);
        while (!queue.isEmpty()) {
            BufferedEvent buffered = queue.pollFirst();
            if (buffered == null) {
                break;
            }
            if (isExpired(buffered)) {
                continue;
            }
            try {
                pushEvent(observer, buffered.event());
            } catch (RuntimeException ex) {
                queue.addFirst(buffered);
                liveObserversBySubscriptionId.remove(subscriptionId);
                return;
            }
        }
    }

    private void enqueueBufferedEvent(String subscriptionId, TransportEvent event) {
        Deque<BufferedEvent> queue = pendingBySubscriptionId.computeIfAbsent(subscriptionId, key -> new LinkedList<>());
        pruneExpired(queue);
        if (queue.size() >= MAX_BUFFER_PER_SUBSCRIPTION) {
            queue.pollFirst();
        }
        queue.addLast(new BufferedEvent(event, Instant.now().getEpochSecond()));
    }

    private void pruneExpired(Deque<BufferedEvent> queue) {
        while (!queue.isEmpty() && isExpired(queue.peekFirst())) {
            queue.pollFirst();
        }
    }

    private boolean isExpired(BufferedEvent buffered) {
        long now = Instant.now().getEpochSecond();
        return now - buffered.createdAtUnix() > BUFFER_TTL_SECONDS;
    }

    private void pushEvent(StreamObserver<TransportEvent> observer, TransportEvent event) {
        synchronized (observer) {
            observer.onNext(event);
        }
    }

    private boolean matchesFilter(SubscriptionRequest request, TransportEvent event) {
        if (request.getTypeFilter() != VehicleType.UNKNOWN && request.getTypeFilter() != event.getVehicle()) {
            return false;
        }

        List<String> requestedAreas = request.getAreaCodesList();
        if (requestedAreas.isEmpty()) {
            return true;
        }

        String eventAreaCode = event.getCurrentLocation().getAreaCode();
        return requestedAreas.contains(eventAreaCode);
    }

    private TransportEvent generateEventFor(SubscriptionRequest request) {
        ThreadLocalRandom random = ThreadLocalRandom.current();
        String areaCode = pickAreaCode(request.getAreaCodesList());
        VehicleType vehicle = request.getTypeFilter() == VehicleType.UNKNOWN
                ? pickVehicleType()
                : request.getTypeFilter();

        Location location = Location.newBuilder()
                .setLatitude(random.nextDouble(49.0, 54.9))
                .setLongitude(random.nextDouble(14.0, 24.1))
                .setCity("PL_CITY_" + areaCode)
                .setAreaCode(areaCode)
                .build();

        TransportEvent.Builder builder = TransportEvent.newBuilder()
                .setEventId(UUID.randomUUID().toString())
                .setShipmentId("SHP-" + random.nextInt(1000, 9999))
                .setVehicle(vehicle)
                .setCurrentLocation(location)
                .setEventUnixTime(Instant.now().getEpochSecond())
                .addAllStatusHistory(defaultStatuses());

        int alertType = random.nextInt(3);
        if (alertType == 0) {
            builder.setDelayReason("Traffic congestion");
        } else if (alertType == 1) {
            builder.setSensorThreshold(random.nextInt(1, 100));
        } else {
            builder.setSecurityAlert("Suspicious route deviation");
        }

        return builder.build();
    }

    private String pickAreaCode(List<String> requestedAreas) {
        if (!requestedAreas.isEmpty()) {
            int idx = ThreadLocalRandom.current().nextInt(requestedAreas.size());
            return requestedAreas.get(idx);
        }
        String[] defaults = {"WAW", "KRK", "GDN", "POZ"};
        return defaults[ThreadLocalRandom.current().nextInt(defaults.length)];
    }

    private VehicleType pickVehicleType() {
        VehicleType[] candidates = {VehicleType.DELIVERY_VAN, VehicleType.HEAVY_TRUCK, VehicleType.CARGO_PLANE};
        return candidates[ThreadLocalRandom.current().nextInt(candidates.length)];
    }

    private List<String> defaultStatuses() {
        List<String> values = new ArrayList<>();
        values.add("CREATED");
        values.add("IN_TRANSIT");
        return values;
    }

    private record BufferedEvent(TransportEvent event, long createdAtUnix) {
    }
}
