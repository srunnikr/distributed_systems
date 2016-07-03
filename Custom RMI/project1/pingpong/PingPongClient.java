package pingpong;

import rmi.RMIException;
import rmi.RmiUtils;
import rmi.Stub;

import java.net.InetSocketAddress;
import java.net.UnknownHostException;

/**
 * Created by Sreejith Unnikrishnan on 4/19/16.
 */
public class PingPongClient {

    public static void main(String args[]) throws UnknownHostException, RMIException {

        if (args.length < 2) {
            System.out.println("ERROR : Wrong arguments, Usage PingPongClient <server ip> <port number>");
            System.exit(1);
        }

        int testsFailed = 0;

        String hostname = args[0];
        int port = Integer.valueOf(args[1]);
        RmiUtils.safePrintln("CLIENT : Host : "+hostname+" and port "+port);

        RmiUtils.safePrintln("Creating factory");
        PingPongFactory factory = Stub.create(PingPongFactory.class, new InetSocketAddress(hostname, port));
        RmiUtils.safePrintln("Creating server");
        PingServer server = factory.makePingServer();

        RmiUtils.safePrintln("Sending pings 4 times");
        for (int i=0; i<4; ++i) {
            try {
                String response = server.ping(i);
                System.out.println(response);
            } catch (Exception e) {
                System.out.println("Test "+i+" failed");
                testsFailed += 1;
            }
        }

        System.out.println("4 Tests Completed, "+testsFailed+" Tests Failed");

    }

}
