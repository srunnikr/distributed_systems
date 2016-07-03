package pingpong;

import rmi.RMIException;
import rmi.Skeleton;

import java.net.InetSocketAddress;

/**
 * Created by Sreejith Unnikrishnan on 4/19/16.
 */
public class PingPongServer {

    public static void main(String args[]) {

        int port;

        if (args.length < 1) {
            System.out.println("ERROR : Wrong arguments, Usage PingPongServer <port number>");
            System.exit(1);
        }

        port = Integer.valueOf(args[0]);

        PingPongFactoryImpl pingPongFactory = new PingPongFactoryImpl();
        Skeleton<PingPongFactory> pingPongFactorySkeleton = new Skeleton<PingPongFactory>(PingPongFactory.class,
                pingPongFactory, new InetSocketAddress(port));

        try {
            pingPongFactorySkeleton.start();
        } catch (RMIException e) {
            e.printStackTrace();
        }

        while(true) {
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

    }

}
