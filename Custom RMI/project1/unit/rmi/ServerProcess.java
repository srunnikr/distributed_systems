package unit.rmi;

import rmi.RMIException;
import rmi.Skeleton;

import java.net.InetSocketAddress;
import java.nio.channels.ServerSocketChannel;

/**
 * Created by Sreejith Unnikrishnan on 4/9/16.
 */
public class ServerProcess implements Runnable {

    private InetSocketAddress address;

    public ServerProcess(InetSocketAddress address) {
        this.address = address;
    }

    @Override
    public void run() {
        SimpleServerImplementation server = new SimpleServerImplementation();
        Skeleton<SimpleServer> skeleton = new Skeleton<SimpleServer>(SimpleServer.class, server, address);
        try {
            skeleton.start();
        } catch (RMIException e) {
            e.printStackTrace();
        }
    }
}
