import com.rabbitmq.client.AMQP;
import com.rabbitmq.client.BuiltinExchangeType;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;
import com.rabbitmq.client.Consumer;
import com.rabbitmq.client.DefaultConsumer;
import com.rabbitmq.client.Envelope;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.IOException;

public class Z2_Consumer {

    // ============================================================
    // Zmień typ Exchange (musi być taki sam jak u producenta)
    private static final BuiltinExchangeType EXCHANGE_TYPE = BuiltinExchangeType.TOPIC;
    private static final String EXCHANGE_NAME = "exchange_topic"; // musi być taka sama nazwa!

    // Identyfikator konsumenta, dla drugiego zmien na B
    private static final String CONSUMER_ID = "A";

    public static void main(String[] argv) throws Exception {

        System.out.println("Z2 CONSUMER [" + CONSUMER_ID + "] | type=" + EXCHANGE_TYPE);

        ConnectionFactory factory = new ConnectionFactory();
        factory.setHost("localhost");
        Connection connection = factory.newConnection();
        Channel channel = connection.createChannel();

        channel.exchangeDeclare(EXCHANGE_NAME, EXCHANGE_TYPE);

        // tymczasowa, anonimowa kolejka przypisana do tego konsumenta
        String queueName = channel.queueDeclare().getQueue();

        // wczytaj klucz routingu z konsoli (dla Topic można użyć wzorca, np. "blue.*.sedan")
        BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
        System.out.print("Enter binding key (routing key or pattern): ");
        String bindingKey = br.readLine();

        channel.queueBind(queueName, EXCHANGE_NAME, bindingKey);
        System.out.println("Queue [" + queueName + "] bound with key: " + bindingKey);

        Consumer consumer = new DefaultConsumer(channel) {
            @Override
            public void handleDelivery(String consumerTag, Envelope envelope,
                                       AMQP.BasicProperties properties, byte[] body) throws IOException {
                String message = new String(body, "UTF-8");
                System.out.println("[" + CONSUMER_ID + "] Received [key=" + envelope.getRoutingKey() + "]: " + message);
            }
        };

        System.out.println("Waiting for messages...");
        channel.basicConsume(queueName, true, consumer);
    }
}