import com.rabbitmq.client.*;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

public class Carrier {

    static final String EXCHANGE_ORDERS    = "orders";
    static final String EXCHANGE_CONFIRM   = "confirmations";
    static final String EXCHANGE_ADMIN     = "admin_copy";
    static final String EXCHANGE_BROADCAST = "broadcast";

    static final String[][] SERVICE_DEFS = {
            {"1", "osoby",    "people"},
            {"2", "ladunek",  "cargo"},
            {"3", "satelita", "satellite"}
    };

    public static void main(String[] args) throws Exception {

        BufferedReader br = new BufferedReader(new InputStreamReader(System.in));

        System.out.print("nazwa: ");
        String carrierName = br.readLine().trim();

        System.out.println("uslugi: 1=osoby 2=ladunek 3=satelita");
        System.out.print("wybierz 2 (np. 1 2): ");
        String[] chosen = br.readLine().trim().split("\\s+");
        if (chosen.length != 2) {
            System.out.println("err: wymagane 2");
            System.exit(1);
        }

        List<String[]> myServices = new ArrayList<>();
        for (String c : chosen)
            for (String[] s : SERVICE_DEFS)
                if (s[0].equals(c)) myServices.add(s);

        if (myServices.size() != 2) {
            System.out.println("err: bad input");
            System.exit(1);
        }

        ConnectionFactory factory = new ConnectionFactory();
        factory.setHost("localhost");
        Connection connection = factory.newConnection();
        Channel channel = connection.createChannel();
        channel.basicQos(1);

        channel.exchangeDeclare(EXCHANGE_ORDERS,    BuiltinExchangeType.DIRECT, true);
        channel.exchangeDeclare(EXCHANGE_CONFIRM,   BuiltinExchangeType.DIRECT, true);
        channel.exchangeDeclare(EXCHANGE_ADMIN,     BuiltinExchangeType.FANOUT, true);
        channel.exchangeDeclare(EXCHANGE_BROADCAST, BuiltinExchangeType.TOPIC,  true);

        for (String[] svc : myServices) {
            String queueName = "orders." + svc[2];
            channel.queueDeclare(queueName, true, false, false, null);
            channel.queueBind(queueName, EXCHANGE_ORDERS, svc[2]);
        }

        String broadcastQueue = channel.queueDeclare().getQueue();
        channel.queueBind(broadcastQueue, EXCHANGE_BROADCAST, "carriers");
        channel.queueBind(broadcastQueue, EXCHANGE_BROADCAST, "all");

        System.out.println("[" + carrierName + "] up: "
                + myServices.get(0)[1] + "+" + myServices.get(1)[1]);

        Consumer broadcastConsumer = new DefaultConsumer(channel) {
            @Override
            public void handleDelivery(String tag, Envelope env,
                                       AMQP.BasicProperties props, byte[] body) throws IOException {
                System.out.println("[admin] " + new String(body, "UTF-8"));
            }
        };
        channel.basicConsume(broadcastQueue, true, broadcastConsumer);

        String finalCarrierName = carrierName;
        Consumer orderConsumer = new DefaultConsumer(channel) {
            @Override
            public void handleDelivery(String tag, Envelope envelope,
                                       AMQP.BasicProperties props, byte[] body) throws IOException {

                String msg = new String(body, "UTF-8");
                String[] parts = msg.split("\\|");
                if (parts.length < 4) {
                    channel.basicNack(envelope.getDeliveryTag(), false, false);
                    return;
                }
                String orderId    = parts[1];
                String serviceType= parts[2];
                String agencyName = parts[3];

                System.out.println("[recv] " + orderId + " " + serviceType + " <- " + agencyName);

                String confirmation = "POTWIERDZENIE|" + orderId + "|" + serviceType
                        + "|wykonane przez: " + finalCarrierName;

                channel.basicPublish(EXCHANGE_CONFIRM, agencyName,
                        null, confirmation.getBytes("UTF-8"));
                channel.basicPublish(EXCHANGE_ADMIN, "",
                        null, ("[" + finalCarrierName + " -> " + agencyName + "] "
                                + confirmation).getBytes("UTF-8"));

                System.out.println("[done] " + orderId);
                channel.basicAck(envelope.getDeliveryTag(), false);
            }
        };

        for (String[] svc : myServices) {
            String queueName = "orders." + svc[2];
            channel.basicConsume(queueName, false, orderConsumer);
        }
    }
}