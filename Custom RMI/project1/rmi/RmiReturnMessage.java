package rmi;

import java.io.Serializable;

/**
 * Created by stanislavmushits on 28/04/16.
 */
public class RmiReturnMessage implements Serializable {
    public enum RETURN_TYPE {OBJECT, EXCEPTION}

    public Object result;
    public RETURN_TYPE type;

    public RmiReturnMessage(Object result){
        this.result = result;
        this.type = RETURN_TYPE.OBJECT;
    }

    public RmiReturnMessage(Object result, RETURN_TYPE type){
        this.result = result;
        this.type = type;
    }
}
