import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.HashMap;

/**
 * Created by sreej on 5/11/2016.
 */
public class NamingServer {

    private InetSocketAddress namingServerSocketAddress;
    private ServerSocket namingServerService;
    // Maps the string id of the storage server to the IP
    private HashMap<String, InetSocketAddress> storageServers;
    private boolean isStopped;

    public NamingServer(String host, int port) {
        if (host == null || port > 65535 || port < 0) {
            throw new IllegalArgumentException("Wrong arguments supplied to Naming server");
        }
        this.isStopped = false;
        try {
            namingServerSocketAddress = new InetSocketAddress(host, port);
            namingServerService = new ServerSocket();
            namingServerService.bind(namingServerSocketAddress);
        } catch (Exception e) {
            System.out.println("Exception detected while starting Naming server socket "+e.toString());
        }
    }

    private void start() {
        // Start the naming server
        while (isStopped != false) {
            try {
                // wait for the client to connect
                Socket client = namingServerService.accept();


            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private void stop() {
        // Stop the naming server
    }

}
