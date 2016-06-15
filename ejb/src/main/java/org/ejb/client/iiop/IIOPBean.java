package org.ejb.client.iiop;

import java.rmi.RemoteException;

import javax.ejb.Remote;
import javax.ejb.RemoteHome;
import javax.ejb.Stateless;

@Remote(IIOPRemote.class)
@RemoteHome(IIOPBeanHome.class)
@Stateless
public class IIOPBean {
   public String sayHello() throws RemoteException {
        return "hello";
   }
}