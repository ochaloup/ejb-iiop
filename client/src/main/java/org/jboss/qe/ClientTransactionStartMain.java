package org.jboss.qe;

import java.util.Properties;
import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.rmi.PortableRemoteObject;
import javax.transaction.SystemException;
import org.ejb.client.iiop.IIOPBeanHome;
import org.ejb.client.iiop.IIOPBeanMandatory;
import org.ejb.client.iiop.IIOPRemote;
import org.jboss.as.network.NetworkUtils;
import org.omg.CORBA.ORBPackage.InvalidName;
import com.arjuna.ats.arjuna.recovery.RecoveryManager;
import com.arjuna.ats.internal.jts.ORBManager;
import com.arjuna.ats.internal.jts.context.ContextPropagationManager;
import com.arjuna.ats.jts.OTSManager;
import com.arjuna.orbportability.OA;
import com.arjuna.orbportability.ORB;
import com.sun.corba.se.impl.orbutil.ORBConstants;

public class ClientTransactionStartMain {
	private static final String HOST = NetworkUtils.formatPossibleIpv6Address("localhost");
	private static final int PORT = 3528;
	private static ORB orb = null;

    public static void main(String[] args) {
    	System.out.println( "Preset for ORB..." );
    	try {
    		presetOrb();

    		// Recovery manager has to be started on client when we want recovery
        	// and we start the transaction on client
        	RecoveryManager.manager().startRecoveryManagerThread();

    		System.out.println( "Getting info from remote bean " + IIOPBeanMandatory.class.getSimpleName() + "..." );

        	Context context = getContext();

        	System.out.println("Context lookup for IIOP Bean...");
			final Object iiopObj = context.lookup(IIOPBeanMandatory.class.getSimpleName());
			final IIOPBeanHome beanHome = (IIOPBeanHome) PortableRemoteObject.narrow(iiopObj, IIOPBeanHome.class);
			final IIOPRemote bean = beanHome.create();

			startCorbaTx();

			System.out.println("Bean saying: " + bean.sayHello());

			commitCorbaTx();
		} catch (Exception e) {
			try {
				rollbackCorbaTx();
			} catch (Exception e1) {
				e1.printStackTrace();
			}
			throw new RuntimeException(e);
		} finally {
			orbShutdown();
		}
    }

    private static Context getContext() throws NamingException {
        System.setProperty("com.sun.CORBA.ORBUseDynamicStub", "true");
        final Properties prope = new Properties();
        prope.put(Context.PROVIDER_URL, "corbaloc::" + HOST + ":" + PORT + "/JBoss/Naming/root");
        prope.setProperty(Context.URL_PKG_PREFIXES, "org.jboss.iiop.naming:org.jboss.naming.client");
        prope.put(Context.INITIAL_CONTEXT_FACTORY, "com.sun.jndi.cosnaming.CNCtxFactory");
        // prope.put(Context.OBJECT_FACTORIES, "org.jboss.tm.iiop.client.IIOPClientUserTransactionObjectFactory");

        Context context = new InitialContext(prope);

        return context;
    }

    private static void presetOrb() throws InvalidName, SystemException {
        Properties properties = new Properties();
        properties.setProperty(ORBConstants.PERSISTENT_SERVER_PORT_PROPERTY, "15151");
        properties.setProperty(ORBConstants.ORB_SERVER_ID_PROPERTY, "1");

        new ContextPropagationManager();

        org.omg.CORBA.ORB sunOrb = org.omg.CORBA.ORB.init(new String[0], properties);

        orb = com.arjuna.orbportability.ORB.getInstance("ClientSide");
        orb.setOrb(sunOrb);

        OA oa = OA.getRootOA(orb);
        org.omg.PortableServer.POA rootPOA = org.omg.PortableServer.POAHelper.narrow(sunOrb
                .resolve_initial_references("RootPOA"));
        oa.setPOA(rootPOA);

        oa.initOA();

        ORBManager.setORB(orb);
        ORBManager.setPOA(oa);
    }

    private static void startCorbaTx() throws Exception {
    	System.out.println("Starting txn");
        OTSManager.get_current().begin();
    }

    private static void commitCorbaTx() throws Exception {
    	System.out.println("Committing txn");
        OTSManager.get_current().commit(true);
    }

    private static void rollbackCorbaTx() throws Exception {
    	System.out.println("Rolling back txn");
        OTSManager.get_current().rollback();
    }

    public static void orbShutdown() {
        if (orb != null) {
            orb.shutdown();
        }
        RecoveryManager.manager().terminate();
    }
}
