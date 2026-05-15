port com.rabbitmq.client.BuiltinExchangeType;
import com.rabbitmq.client.BuiltinExchangeType;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;
import java.io.BufferedReader;
import java.io.InputStreamReader;

public class Z2_Producer {


    // Zmień typ Exchang na DIRECT or TOPIC
    private static final BuiltinExchangeType EXCHANGE_TYPE = BuiltinExchangeType.TOPIC;
    private static final String EXCHANGE_NAME = "exchange_topic"; // zmiana nazwy przy zmianie typu

    public static void main(String[] argv) throws Exception {

        System.out.println("Z2 PRODUCER | type=" + EXCHANGE_TYPE);

        ConnectionFactory factory = new ConnectionFactory();
        factory.setHost("localhost");
        Connection connection = factory.newConnection();
        Channel channel = connection.createChannel();

        channel.exchangeDeclare(EXCHANGE_NAME, EXCHANGE_TYPE);

        BufferedReader br = new BufferedReader(new InputStreamReader(System.in));

        while (true) {
            System.out.print("Enter routing key: ");
            String key = br.readLine();
            if ("exit".equals(key)) break;

            System.out.print("Enter message: ");
            String message = br.readLine();
            if ("exit".equals(message)) break;

            channel.basicPublish(EXCHANGE_NAME, key, null, message.getBytes("UTF-8"));
            System.out.println("Sent [key=" + key + "]: " + message);
        }

        channel.close();
        connection.close();
    }
}

