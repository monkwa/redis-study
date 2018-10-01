package rabbitmq;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import com.rabbitmq.client.*;

public class RabbitProducer {

    private static final String EXCHANGE_NAME = "exchange_demo";
    private static final String ROUTING_KEY = "routingkey_demo";
    private static final String QUEUE_NAME = "queue_demo";
    private static final String IP_ADDRESS = "127.0.0.1";
    private static final int PORT = 5672;
    
    public static void main(String[] args) throws IOException, TimeoutException, InterruptedException {
        ConnectionFactory factory = new ConnectionFactory();
        factory.setHost(IP_ADDRESS);
        factory.setPort(PORT);
        factory.setUsername("root");
        factory.setPassword("root");
        Connection connection = factory.newConnection();
        connection.addShutdownListener(new ShutdownListener() {
            public void shutdownCompleted(ShutdownSignalException cause) {
                System.out.println(cause.isHardError());
            }
        });
        Channel channel = connection.createChannel();
//        channel.exchangeDeclare(EXCHANGE_NAME, "direct", true, false, null);
//        channel.queueDeclare(QUEUE_NAME, true, false, false, null);
//        channel.queueBind(QUEUE_NAME, EXCHANGE_NAME, ROUTING_KEY);
        String message = "Hello World!";
        channel.addReturnListener(new ReturnListener() {
            public void handleReturn(int replyCode, String replyText, String exchange, String routingKey,
                                     AMQP.BasicProperties properties, byte[] body) throws IOException {
                System.out.println(new String(body));
            }
        });
        channel.basicPublish("", QUEUE_NAME, MessageProperties.PERSISTENT_TEXT_PLAIN, message.getBytes());
//        channel.basicPublish(EXCHANGE_NAME, ROUTING_KEY, true, MessageProperties.PERSISTENT_TEXT_PLAIN, message.getBytes());
//        TimeUnit.SECONDS.sleep(5);

        Map<String, Object> argss = new HashMap<String, Object>();
        argss.put("x-expires", 10000);
        channel.queueDeclare("myQueue", false, false, false, argss);
        channel.queueBind("myQueue", EXCHANGE_NAME, "myroute");
        channel.basicPublish(EXCHANGE_NAME, "myroute", MessageProperties.PERSISTENT_TEXT_PLAIN, "myqueue".getBytes());

        //备份交换器
        Map<String, Object> map = new HashMap<String, Object>();
        map.put("alternate-exchange", "myAe");
        channel.exchangeDeclare("normalExchange", "direct", true, false, map);
        channel.queueDeclare("normalQueue", true, false, false, null);
        channel.queueBind("normalQueue", "normalExchange", "normalKey");
        channel.exchangeDeclare("myAe", "fanout", true, false, null);
        channel.queueDeclare("unroutedQueue", true, false, false, null);
        channel.queueBind("unroutedQueue", "myAe", "");
        channel.basicPublish("normalExchange", "errorKey", true, MessageProperties.PERSISTENT_TEXT_PLAIN, "alternate".getBytes());
        TimeUnit.SECONDS.sleep(5);
        channel.close();
        connection.close();
    }
}
