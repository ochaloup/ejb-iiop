/*
 * JBoss, Home of Professional Open Source.
 * Copyright 2016, Red Hat Middleware LLC, and individual contributors
 * as indicated by the @author tags. See the copyright.txt file in the
 * distribution for a full listing of individual contributors.
 *
 * This is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation; either version 2.1 of
 * the License, or (at your option) any later version.
 *
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this software; if not, write to the Free
 * Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
 * 02110-1301 USA, or see the FSF site: http://www.fsf.org.
 */

package org.jboss.qe;

import java.util.Properties;

import javax.naming.Context;
import javax.naming.InitialContext;
import javax.rmi.PortableRemoteObject;

import org.ejb.client.iiop.IIOPBeanHome;
import org.ejb.client.iiop.IIOPBeanMandatory;
import org.ejb.client.iiop.IIOPRemote;
import org.jboss.as.network.NetworkUtils;

import com.arjuna.ats.arjuna.recovery.RecoveryManager;
import com.arjuna.ats.internal.jts.ORBManager;
import com.arjuna.ats.internal.jts.context.ContextPropagationManager;
import com.arjuna.ats.jts.OTSManager;
import com.sun.corba.se.impl.orbutil.ORBConstants;
import com.arjuna.orbportability.ORB;
import com.arjuna.orbportability.OA;

public class DocTest {
    public static void main(String[] args) throws Throwable {
        final String host = NetworkUtils.formatPossibleIpv6Address("localhost");
        final int port = 3528;
        
        // For client we define how the Narayana will behave
        System.setProperty("com.arjuna.ats.jts.alwaysPropagateContext", "true");

        // Set orb to be initialized on client and being able to start ORB txn
        Properties properties = new Properties();
        properties.setProperty(ORBConstants.PERSISTENT_SERVER_PORT_PROPERTY, "15151");
        properties.setProperty(ORBConstants.ORB_SERVER_ID_PROPERTY, "1");

        // Registers the appropriate filter with the ORB
        new ContextPropagationManager();

        org.omg.CORBA.ORB sunOrb = org.omg.CORBA.ORB.init(new String[0], properties);
        ORB orb = null;
        try {
            orb = com.arjuna.orbportability.ORB.getInstance("ClientSide");
            orb.setOrb(sunOrb);
    
            OA oa = OA.getRootOA(orb);
            org.omg.PortableServer.POA rootPOA = org.omg.PortableServer.POAHelper.narrow(sunOrb.resolve_initial_references("RootPOA"));
            oa.setPOA(rootPOA);
    
            oa.initOA();
    
            ORBManager.setORB(orb);
            ORBManager.setPOA(oa);
    
            // Recovery manager has to be started on client when we want recovery to work at client
            RecoveryManager.manager().startRecoveryManagerThread();
    
            // Getting context to lookup
            System.setProperty("com.sun.CORBA.ORBUseDynamicStub", "true");
            final Properties prope = new Properties();
            prope.put(Context.PROVIDER_URL, "corbaloc::" + host + ":" + port + "/JBoss/Naming/root");
            prope.setProperty(Context.URL_PKG_PREFIXES, "org.jboss.iiop.naming:org.jboss.naming.client");
            prope.put(Context.INITIAL_CONTEXT_FACTORY, "com.sun.jndi.cosnaming.CNCtxFactory");
            Context context = new InitialContext(prope);
    
            // Bean lookup
            final Object iiopObj = context.lookup(IIOPBeanMandatory.class.getSimpleName());
            final IIOPBeanHome beanHome = (IIOPBeanHome) PortableRemoteObject.narrow(iiopObj, IIOPBeanHome.class);
            final IIOPRemote bean = beanHome.create();
    
            // Starting orb transaction
            OTSManager.get_current().begin();
    
            // Call bean - business logic
            bean.sayHello();
    
            // Manage the commit of the work
            OTSManager.get_current().commit(true);
            // or rollback
            // OTSManager.get_current().rollback();
        } finally {
            // It's good to release resources - do it only once at the end
            if (orb != null) {
                orb.shutdown();
            }
            RecoveryManager.manager().terminate();
        }
    }
}
