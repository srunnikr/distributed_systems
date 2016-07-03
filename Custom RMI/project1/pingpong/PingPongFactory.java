package pingpong;

import rmi.RMIException;

import java.net.UnknownHostException;

/**
 * Created by Sreejith Unnikrishnan on 4/19/16.
 */
public interface PingPongFactory {

    public PingServer makePingServer() throws RMIException, UnknownHostException;

}
