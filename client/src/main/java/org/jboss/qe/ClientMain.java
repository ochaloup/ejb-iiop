package org.jboss.qe;

import java.util.Properties;
import javax.naming.Context;
import javax.naming.InitialContext;
import javax.rmi.PortableRemoteObject;
import org.ejb.client.iiop.IIOPBean;
import org.ejb.client.iiop.IIOPBeanHome;
import org.ejb.client.iiop.IIOPRemote;

public class ClientMain {
    public static void main( String[] args ) {
        System.out.println( "Getting info from remote bean..." );

        System.setProperty("com.sun.CORBA.ORBUseDynamicStub", "true");
        final Properties prope = new Properties();
        prope.put(Context.PROVIDER_URL, "corbaloc::localhost:3528/JBoss/Naming/root");
        prope.setProperty(Context.URL_PKG_PREFIXES, "org.jboss.iiop.naming:org.jboss.naming.client");
        prope.put(Context.INITIAL_CONTEXT_FACTORY, "com.sun.jndi.cosnaming.CNCtxFactory");
        prope.put(Context.OBJECT_FACTORIES, "org.jboss.tm.iiop.client.IIOPClientUserTransactionObjectFactory");
        
        try {
            Context context = new InitialContext(prope);

            final Object iiopObj = context.lookup(IIOPBean.class.getSimpleName());
            final IIOPBeanHome beanHome = (IIOPBeanHome) PortableRemoteObject.narrow(iiopObj, IIOPBeanHome.class);
            final IIOPRemote bean = beanHome.create();

            System.out.println("Bean saying: " + bean.sayHello());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
