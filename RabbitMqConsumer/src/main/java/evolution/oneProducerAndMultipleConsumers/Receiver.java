package evolution.oneProducerAndMultipleConsumers;

import java.io.IOException;

import com.rabbitmq.client.AMQP;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;
import com.rabbitmq.client.Consumer;
import com.rabbitmq.client.DefaultConsumer;
import com.rabbitmq.client.Envelope;

public class Receiver {
	// The exchange is responsible for sending the messages from the producer to the queues.
	private static final String EXCHANGE_NAME = "logs";// Define the exchange name as logs.

	public static void main(String[] args) throws Exception {
		ConnectionFactory factory = new ConnectionFactory();
		factory.setHost("localhost");
		Connection connection = factory.newConnection();
		Channel channel = connection.createChannel();
		channel.exchangeDeclare(EXCHANGE_NAME, "fanout");// A fanout exchange means that it sends a message to all the queues. 
		String queueName = channel.queueDeclare().getQueue();// The queue has a random name.
		channel.queueBind(queueName, EXCHANGE_NAME, "");// Bind the random queue to the fanout exchange called logs.
		System.out.println("One receiver is Waiting for messages. To exit press CTRL+C");
		Consumer consumer = new DefaultConsumer(channel) {
			@Override
			public void handleDelivery(String consumerTag, Envelope envelope,
					AMQP.BasicProperties properties, byte[] body) throws IOException {
				String message = new String(body, "UTF-8");
				System.out.println("One receiver received '" + message + "'");
			}
		};
		channel.basicConsume(queueName, true, consumer);
	}
}
