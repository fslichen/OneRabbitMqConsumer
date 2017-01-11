package evolution.distributeTasks2Workers;

import java.io.IOException;

import com.rabbitmq.client.AMQP;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;
import com.rabbitmq.client.Consumer;
import com.rabbitmq.client.DefaultConsumer;
import com.rabbitmq.client.Envelope;

public class Receiver {
	private static final String TASK_QUEUE_NAME = "anyQueueName1";// The producer and the consumer are communicating via a queue. Make sure that the producer and the consumer shares the same queue.

	public static void main(String[] argv) throws Exception {
		ConnectionFactory factory = new ConnectionFactory();
		factory.setHost("localhost");// Make sure you turn on the local RabbitMq server and don't make a mistake to close the terminal.
		final Connection connection = factory.newConnection();
		final Channel channel = connection.createChannel();// A channel corresponds to a queue.
		channel.queueDeclare(TASK_QUEUE_NAME, true, false, false, null);
		System.out.println("Waiting for messages. To exit press CTRL+C");
		channel.basicQos(1);// A worker accepts only one unacknowledged message at a time.
		final Consumer consumer = new DefaultConsumer(channel) {
			@Override
			public void handleDelivery(String consumerTag, Envelope envelope, AMQP.BasicProperties properties, byte[] body) throws IOException {
				String message = new String(body, "UTF-8");
				System.out.println("Received '" + message + "'");
				try {
					doWork(message);// A Time-Consuming Task
				} finally {
					System.out.println("Done");
					channel.basicAck(envelope.getDeliveryTag(), false);// Don't forget to send an acknowledgement to RabbitMq, otherwise RabbitMq will keep sending the same message until the machine runs out of memory.
				}
			}
		};
		boolean autoAck = false;
		channel.basicConsume(TASK_QUEUE_NAME, autoAck, consumer);
	}

	private static void doWork(String task) {
		for (char ch : task.toCharArray()) {
			if (ch == '.') {
				try {
					Thread.sleep(1000);
				} catch (InterruptedException _ignored) {
					Thread.currentThread().interrupt();
				}
			}
		}
	}
}
