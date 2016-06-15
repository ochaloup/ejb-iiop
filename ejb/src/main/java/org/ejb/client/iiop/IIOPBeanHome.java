package org.ejb.client.iiop;

import java.rmi.RemoteException;

import javax.ejb.EJBHome;

public interface IIOPBeanHome extends EJBHome {
    public IIOPRemote create() throws RemoteException;
}
