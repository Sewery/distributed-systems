import com.rabbitmq.client.*;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

public class Administrator {

    static final String EXCHANGE_ADMIN     = "admin_copy";
    static final String EXCHANGE_BROADCAST = "broadcast";

    public static void main(String[] args) throws Exception {

        ConnectionFactory factory = new ConnectionFactory();
        factory.setHost("localhost");
        Connection connection = factory.newConnection();
        Channel channel = connection.createChannel();

        channel.exchangeDeclare(EXCHANGE_ADMIN,     BuiltinExchangeType.FANOUT, true);
        channel.exchangeDeclare(EXCHANGE_BROADCAST, BuiltinExchangeType.TOPIC,  true);

        String monitorQueue = channel.queueDeclare().getQueue();
        channel.queueBind(monitorQueue, EXCHANGE_ADMIN, "");

        System.out.println("[admin] up");

        Consumer monitorConsumer = new DefaultConsumer(channel) {
            @Override
            public void handleDelivery(String tag, Envelope env,
                                       AMQP.BasicProperties props, byte[] body) throws IOException {
                System.out.println("[mon] " + new String(body, "UTF-8"));
            }
        };
        channel.basicConsume(monitorQueue, true, monitorConsumer);

        BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
        System.out.println("1=agencje 2=przewoznicy 3=wszyscy exit");

        while (true) {
            System.out.print("> ");
            String mode = br.readLine().trim();
            if ("exit".equalsIgnoreCase(mode)) break;

            String routingKey;
            switch (mode) {
                case "1": routingKey = "agencies"; break;
                case "2": routingKey = "carriers"; break;
                case "3": routingKey = "all";      break;
                default:
                    System.out.println("err: 1/2/3");
                    continue;
            }

            System.out.print("msg: ");
            String text = br.readLine().trim();
            if (text.isEmpty()) continue;

            String msg = "[admin->" + routingKey + "] " + text;
            channel.basicPublish(EXCHANGE_BROADCAST, routingKey, null, msg.getBytes("UTF-8"));
            System.out.println("[send] " + routingKey);
        }

        channel.close();
        connection.close();
    }
}