package pingpong;

import rmi.RMIException;
import rmi.RmiUtils;
import rmi.Skeleton;
import rmi.Stub;

import java.net.UnknownHostException;

/**
 * Created by Sreejith Unnikrishnan on 4/19/16.
 */
public class PingPongFactoryImpl implements PingPongFactory {

    public PingServer makePingServer() throws UnknownHostException, RMIException {

        RmiUtils.safePrintln("Creating PingServerImpl()");
        PingServerImpl pongServerImpl = new PingServerImpl();
        RmiUtils.safePrintln("Creating server skeleton");
        Skeleton<PingServer> serverSkeleton = new Skeleton<PingServer>(PingServer.class, pongServerImpl);
        RmiUtils.safePrintln("Starting the server skeleton");
        serverSkeleton.start();

        RmiUtils.safePrintln("Creating stub");
        PingServer p = Stub.create(PingServer.class, serverSkeleton);
        return p;

    }

}
