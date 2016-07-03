package rmi;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Proxy;

/**
 * Created by stanislavmushits on 11/04/16.
 */
public class RmiProxy extends Proxy {
    /**
     * Constructs a new {@code Proxy} instance from a subclass
     * (typically, a dynamic proxy class) with the specified value
     * for its invocation handler.
     *
     * @param h the invocation handler for this proxy instance
     * @throws NullPointerException if the given invocation handler, {@code h},
     *                              is {@code null}.
     */
    protected RmiProxy(InvocationHandler h) {
        super(h);
    }

    @Override
    public boolean equals(Object o) {
        return o.getClass().equals(RmiProxy.class) && h.equals(RmiProxy.class.cast(o).h);
    }

}
