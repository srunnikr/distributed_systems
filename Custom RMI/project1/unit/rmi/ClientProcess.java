package unit.rmi;

import rmi.RMIException;
import rmi.Stub;

import java.net.InetSocketAddress;

/**
 * Created by Sreejith Unnikrishnan on 4/9/16.
 */
public class ClientProcess {

    public String run(InetSocketAddress address, String name) throws RMIException {
        SimpleServer server = Stub.create(SimpleServer.class, address);
        return server.getHello(name);
    }

    public void run2(InetSocketAddress address) throws RMIException{
        SimpleServer server = Stub.create(SimpleServer.class, address);
        server.helloWorld();
    }

}
