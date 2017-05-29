import com.rabbitmq.client.ConnectionFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class Test {

    private static final Logger log = LoggerFactory.getLogger(Test.class);

    private static ExecutorService executor = Executors.newFixedThreadPool(5);;

    public static void main(String[] args) {

        try {
            final DumbAmqpServer server = new DumbAmqpServer(30012);
            server.start();

            final ConnectionFactory factory = new ConnectionFactory();
            factory.setHost("localhost");
            factory.setPort(30012);

            factory.setUsername("irrelevant");
            factory.setPassword(null);

            factory.setAutomaticRecoveryEnabled(false);

            factory.newConnection(executor);

        } catch (Exception e) {
            log.error("Unexpected exception", e);

        }
    }

}
