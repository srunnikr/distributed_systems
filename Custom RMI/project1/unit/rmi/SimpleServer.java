package unit.rmi;

import rmi.RMIException;

/**
 * Created by Sreejith Unnikrishnan on 4/9/16.
 */
public interface SimpleServer {
    public String getHello(String name) throws RMIException;

    public void helloWorld() throws RMIException;
}
