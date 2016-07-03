package pingpong;

import rmi.RMIException;
/**
 * Created by Sreejith Unnikrishnan on 4/19/16.
 */
public interface PingServer {

    public String ping(int idNumber) throws rmi.RMIException;

}
