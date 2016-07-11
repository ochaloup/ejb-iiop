package org.ejb.client.iiop;

import java.rmi.RemoteException;
import java.util.logging.Logger;

import javax.ejb.Remote;
import javax.ejb.RemoteHome;
import javax.ejb.Stateless;

@Remote(IIOPRemote.class)
@RemoteHome(IIOPBeanHome.class)
@Stateless
public class IIOPBean {
    private static final Logger log = Logger.getLogger(IIOPBean.class.getName());

    public String sayHello() throws RemoteException {
        String msg = "hello";
        log.info("Returning to client: " + msg);
        return msg;
    }
}