package org.ejb.client.iiop;

import java.rmi.RemoteException;

import javax.ejb.EJBObject;

public interface IIOPRemote extends EJBObject {
    String sayHello() throws RemoteException;
}
