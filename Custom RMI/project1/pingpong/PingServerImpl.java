package pingpong;

import rmi.RMIException;

/**
 * Created by Sreejith Unnikrishnan on 4/19/16.
 */
public class PingServerImpl implements PingServer {

    @Override
    public String ping(int idNumber) throws RMIException {
        return "Pong "+String.valueOf(idNumber);
    }
}
