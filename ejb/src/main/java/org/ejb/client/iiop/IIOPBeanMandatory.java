package org.ejb.client.iiop;

import java.rmi.RemoteException;
import java.util.logging.Logger;
import javax.ejb.Remote;
import javax.ejb.RemoteHome;
import javax.ejb.Stateless;
import javax.ejb.TransactionAttribute;
import javax.ejb.TransactionAttributeType;

@Remote(IIOPRemote.class)
@RemoteHome(IIOPBeanHome.class)
@Stateless
public class IIOPBeanMandatory {
	private static final Logger log = Logger.getLogger(IIOPBeanMandatory.class.getName());

    @TransactionAttribute(TransactionAttributeType.MANDATORY)
    public String sayHello() throws RemoteException {
        String msg = "hello mandatory";
        log.info("Returning to client: " + msg);
        return msg;
    }
}