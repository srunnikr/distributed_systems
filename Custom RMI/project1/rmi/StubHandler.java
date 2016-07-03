package rmi;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.net.InetSocketAddress;
import java.net.Socket;

/**
 * Created by Sreejith Unnikrishnan on 4/9/16.
 */
public class StubHandler implements InvocationHandler, Serializable{
    InetSocketAddress address;
    Class<?> remoteInterface;

    public StubHandler(InetSocketAddress address, Class<?> c){
        this.address = address;
        this.remoteInterface = c;
    }

    @Override
    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable, RMIException {
        RmiUtils.safePrintln("invoke(" + method.toString() + ")");
        RmiUtils.safePrintln("invoke args==null: " + String.valueOf(args==null));

        if (isSpecialFunction(method)) {
            return doSpecialFunction(method, args);
        }
        RmiReturnMessage result;
        try {
            RmiMessage message = new RmiMessage(method, args, method.getParameterTypes());
            result = (RmiReturnMessage) exchangeMessage(message);

        } catch (IOException e){
            throw new RMIException(e);
        }
        checkException(result);
        return result.result;
    }

    private void checkException(RmiReturnMessage message) throws Throwable {
        if (message.type == RmiReturnMessage.RETURN_TYPE.EXCEPTION) {
            throw (Throwable) message.result;
        }
    }

    private Object doSpecialFunction(Method method, Object[] args) throws NoSuchMethodException {
        if (method.equals(Object.class.getDeclaredMethod("equals", Object.class))){
            return equals(args[0]);
        }
        if (method.equals(Object.class.getDeclaredMethod("toString"))){
            return toString();
        }
        if (method.equals(Object.class.getDeclaredMethod("hashCode"))){
            return hashCode();
        }
        return null;
    }

    private boolean isSpecialFunction(Method method) throws NoSuchMethodException {
        return (method.equals(Object.class.getDeclaredMethod("equals", Object.class)) ||
                method.equals(Object.class.getDeclaredMethod("toString")) ||
                method.equals(Object.class.getDeclaredMethod("hashCode")));
    }

    private Object exchangeMessage(RmiMessage message) throws Throwable {
        Socket socket = new Socket();
        socket.connect(address);
        ObjectOutputStream oos = new ObjectOutputStream(socket.getOutputStream());
        oos.writeObject(message);
        oos.flush();

        ObjectInputStream ois = new ObjectInputStream(socket.getInputStream());
        Object result = ois.readObject();

        oos.close();
        ois.close();
        socket.close();
        return result;
    }

    public boolean equals(Object other) {
        RmiUtils.safePrintln("Checking equals()");
        try{
            StubHandler otherHandler = StubHandler.class.cast(Proxy.getInvocationHandler(other));
            return this.remoteInterface == otherHandler.remoteInterface
                    && this.address.toString().equals(otherHandler.address.toString());
        } catch (Exception e){
            return false;
        }

    }

    public String toString() {
        return remoteInterface.toString()+" "+address.toString();
    }

    public int hashCode() {
        return toString().hashCode();
    }

}
