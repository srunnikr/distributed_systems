package unit.rmi;

import rmi.RMIException;
import rmi.RmiUtils;

/**
 * Created by Sreejith Unnikrishnan on 4/9/16.
 */
public class SimpleServerImplementation implements SimpleServer {

    @Override
    public String getHello(String name) throws RMIException {
        return "Hello " + name;
    }

    @Override
    public void helloWorld() throws RMIException {
        RmiUtils.safePrintln("Hello World!");
    }
}
