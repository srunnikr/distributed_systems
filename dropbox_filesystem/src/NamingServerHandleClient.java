import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.IOException;
import java.net.Socket;

/**
 * Created by sreej on 5/11/2016.
 */
public class NamingServerHandleClient implements Runnable {

    private Socket clientSocket;

    public NamingServerHandleClient(Socket clientSocket) {
        this.clientSocket = clientSocket;
    }

    @Override
    public void run() {
        // Handle client request here
        if (clientSocket == null) {
            throw new IllegalArgumentException("Cannot handle client without a client socket");
        }

        try {
            BufferedInputStream clientIn = new BufferedInputStream(clientSocket.getInputStream());
            BufferedOutputStream clientOut = new BufferedOutputStream(clientSocket.getOutputStream());
            // Parse the client request and process the same
        } catch (IOException e) {
            System.out.println("IOException while handling client "+e.toString());
        }
    }
}
