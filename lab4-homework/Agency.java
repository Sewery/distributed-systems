import com.rabbitmq.client.*;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.concurrent.atomic.AtomicInteger;

public class Agency {

    static final String EXCHANGE_ORDERS    = "orders";
    static final String EXCHANGE_CONFIRM   = "confirmations";
    static final String EXCHANGE_ADMIN     = "admin_copy";
    static final String EXCHANGE_BROADCAST = "broadcast";

    public static void main(String[] args) throws Exception {

        BufferedReader br = new BufferedReader(new InputStreamReader(System.in));

        System.out.print("nazwa: ");
        String agencyName = br.readLine().trim();

        ConnectionFactory factory = new ConnectionFactory();
        factory.setHost("localhost");
        Connection connection = factory.newConnection();
        Channel channel = connection.createChannel();

        channel.exchangeDeclare(EXCHANGE_ORDERS,    BuiltinExchangeType.DIRECT, true);
        channel.exchangeDeclare(EXCHANGE_CONFIRM,   BuiltinExchangeType.DIRECT, true);
        channel.exchangeDeclare(EXCHANGE_ADMIN,     BuiltinExchangeType.FANOUT, true);
        channel.exchangeDeclare(EXCHANGE_BROADCAST, BuiltinExchangeType.TOPIC,  true);

        String confirmQueue = "confirm." + agencyName;
        channel.queueDeclare(confirmQueue, true, false, false, null);        channel.queueBind(confirmQueue, EXCHANGE_CONFIRM, agencyName);

        String broadcastQueue = channel.queueDeclare().getQueue();
        channel.queueBind(broadcastQueue, EXCHANGE_BROADCAST, "agencies");
        channel.queueBind(broadcastQueue, EXCHANGE_BROADCAST, "all");

        System.out.println("[" + agencyName + "] up");

        Consumer confirmConsumer = new DefaultConsumer(channel) {
            @Override
            public void handleDelivery(String tag, Envelope env,
                                       AMQP.BasicProperties props, byte[] body) throws IOException {
                System.out.println("[ack] " + new String(body, "UTF-8"));
            }
        };
        channel.basicConsume(confirmQueue, true, confirmConsumer);

        Consumer broadcastConsumer = new DefaultConsumer(channel) {
            @Override
            public void handleDelivery(String tag, Envelope env,
                                       AMQP.BasicProperties props, byte[] body) throws IOException {
                System.out.println("[admin] " + new String(body, "UTF-8"));
            }
        };
        channel.basicConsume(broadcastQueue, true, broadcastConsumer);

        AtomicInteger orderCounter = new AtomicInteger(1);

        System.out.println("1=osoby 2=ladunek 3=satelita exit");
        while (true) {
            System.out.print("> ");
            String input = br.readLine().trim();
            if ("exit".equalsIgnoreCase(input)) break;

            String serviceType;
            String routingKey;
            switch (input) {
                case "1": serviceType = "osoby";    routingKey = "people";    break;
                case "2": serviceType = "ladunek";  routingKey = "cargo";     break;
                case "3": serviceType = "satelita"; routingKey = "satellite"; break;
                default:
                    System.out.println("err: 1/2/3");
                    continue;
            }

            int orderNo = orderCounter.getAndIncrement();
            String orderId  = agencyName + "#" + orderNo;
            String msgBody  = "ZLECENIE|" + orderId + "|" + serviceType + "|" + agencyName;

            channel.basicPublish(EXCHANGE_ORDERS, routingKey, null, msgBody.getBytes("UTF-8"));
            channel.basicPublish(EXCHANGE_ADMIN, "", null,
                    ("[" + agencyName + " -> " + routingKey + "] " + msgBody).getBytes("UTF-8"));

            System.out.println("[send] " + orderId + " " + routingKey);
        }

        channel.close();
        connection.close();
    }
}