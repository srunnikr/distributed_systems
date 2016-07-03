package rmi;

import java.io.Serializable;
import java.lang.reflect.Method;
import java.util.ArrayList;

/**
 * Created by Sreejith Unnikrishnan on 4/9/16.
 */
public class RmiMessage implements Serializable {
    public String methodName;
    public Object[] args;
    public Class<?>[] types;

    public RmiMessage(Method method, Object[] args, Class<?>[] types) {
        this.methodName = method.getName();
        this.args = args;
        this.types = types;
    }
}
