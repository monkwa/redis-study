package rabbitmq.rpc;

import com.rabbitmq.client.*;

import java.io.IOException;
import java.util.Date;
import java.util.Map;
import java.util.Queue;
import java.util.UUID;
import java.util.concurrent.TimeoutException;

public class RPCClient {
    private Connection connection;
    private Channel channel;
    private String requestQueueName = "rpc_queue";
    private String replyQueueName;
    private QueueingConsumer consumer;

    private static final String EXCHANGE_NAME = "exchange_demo";
    private static final String ROUTING_KEY = "routingkey_demo";
    private static final String IP_ADDRESS = "127.0.0.1";
    private static final int PORT = 5672;

    public static void main(String[] args) throws Exception {
        RPCClient fibRpc = new RPCClient();
        String response = fibRpc.call("30");
        System.out.println(response);
        fibRpc.close();
    }

    public RPCClient() throws IOException, TimeoutException {
        ConnectionFactory factory = new ConnectionFactory();
        factory.setHost(IP_ADDRESS);
        factory.setPort(PORT);
        factory.setUsername("root");
        factory.setPassword("root");
        connection = factory.newConnection();
        channel = connection.createChannel();
//        channel.exchangeDeclare(EXCHANGE_NAME, "direct", true, false, null);
//        channel.queueDeclare(requestQueueName, true, false, false, null);
//        channel.queueBind(requestQueueName, EXCHANGE_NAME, ROUTING_KEY);

        replyQueueName = channel.queueDeclare().getQueue();
        consumer = new QueueingConsumer(channel);
        channel.basicConsume(replyQueueName, true, consumer);
    }

    public String call(String message) throws IOException, InterruptedException {
        String response = null;
        String corrId = UUID.randomUUID().toString();
        System.out.println(corrId);
        AMQP.BasicProperties properties =
                new AMQP.BasicProperties.Builder().correlationId(corrId).replyTo(replyQueueName).build();
        channel.basicPublish("", requestQueueName, properties, message.getBytes());

        while(true) {
            QueueingConsumer.Delivery delivery = consumer.nextDelivery();
            if(delivery.getProperties().getCorrelationId().equals(corrId)){
                response = new String(delivery.getBody());
                break;
            }
        }
        return response;
    }

    public void close() throws IOException {
        connection.close();
    }
}
