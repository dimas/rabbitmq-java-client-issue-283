import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.net.ServerSocketFactory;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.atomic.AtomicInteger;

class DumbAmqpServer implements Runnable {

    private static final Logger log = LoggerFactory.getLogger(DumbAmqpServer.class);

    public static final int CLOSE_SOCKET_TIMEOUT = 10000;
    public static final int SLEEP_TIMEOUT = 100;

    private static final byte[] PROTOCOL_HEADER = {'A', 'M', 'Q', 'P', 0, 0, 9, 1};

    private final int port;

    private Thread serverThread;

    private volatile ServerSocket serverSocket;
    private volatile Socket clientSocket;
    private volatile boolean shutdown;

    private final AtomicInteger connectionCount = new AtomicInteger();

    DumbAmqpServer(final int port) {
        this.port = port;
    }

    public void start() throws IOException {

        final ServerSocket socket = ServerSocketFactory.getDefault().createServerSocket();
        socket.setReuseAddress(true);
        socket.bind(new InetSocketAddress(port));

        serverSocket = socket;

        serverThread = new Thread(this, this.getClass().getSimpleName());
        serverThread.start();

        log.info("Started server on port {}", port);
    }

    private void service(final Socket socket) throws IOException {

        clientSocket = socket;

        // Our server does nothing but just registers connection attempt

        connectionCount.incrementAndGet();

        InputStream inputStream = null;
        OutputStream outputStream = null;
        try {
            // Read what client sends us (supposed to be the same stuff as in PROTOCOL_HEADER but we do not care)
            inputStream = clientSocket.getInputStream();
            final byte[] buf = new byte[1024];
            if (inputStream.read(buf) > 0) {
                // AMQP 0.9.1 specification, section "4.2.2 Protocol Header":
                //     The server either accepts or rejects the protocol header.
                //     If it rejects the protocol header writes a valid
                //     protocol header to the socket and then closes the socket.
                outputStream = clientSocket.getOutputStream();
                outputStream.write(PROTOCOL_HEADER);
            }

        } finally {
            IOUtils.closeQuietly(outputStream);
            IOUtils.closeQuietly(inputStream);
        }

        close(clientSocket);
        clientSocket = null;
    }

    @Override
    public void run() {
        while (!Thread.interrupted() && !shutdown) {
            Socket socket = null;
            try {
                socket = serverSocket.accept();
                log.debug("Accepted connection {} => {}",
                        socket.getRemoteSocketAddress(), socket.getLocalSocketAddress());
                service(socket);
            } catch (IOException e) {
                log.warn("Error servicing connection", e);
            } finally {
                close(socket);
            }
        }
        if (!shutdown) {
            closeSocket();
        }
    }

    public void stop() {
        shutdown = true;
        closeSocket();
    }

    private void closeSocket() {
        close(serverSocket);
        serverSocket = null;
        shutdown(clientSocket);
        close(clientSocket);
        clientSocket = null;
    }

    private void close(final ServerSocket socket) {
        if (socket == null || socket.isClosed()) {
            return;
        }

        try {
            socket.close();
        } catch (IOException e) {
            log.warn("Error closing socket", e);
        }
    }

    private void close(final Socket socket) {
        if (socket == null || socket.isClosed()) {
            return;
        }

        try {
            socket.close();
        } catch (IOException e) {
            log.warn("Error closing socket", e);
        }
    }

    private void shutdown(final Socket socket) {
        if (socket == null || socket.isClosed()) {
            return;
        }

        try {
            socket.shutdownInput();
        } catch (final IOException e) {
            log.debug("Error shutdown input", e);
        }

        try {
            final OutputStream out = socket.getOutputStream();
            out.flush();
            socket.shutdownOutput();
        } catch (IOException e) {
            log.debug("Error shutdown output", e);
        }

    }

    public int getConnectionCount() {
        return connectionCount.get();
    }

    public void reset() {
        connectionCount.set(0);
    }
}
