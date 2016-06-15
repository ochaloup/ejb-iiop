package org.jboss.qe;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

import javax.naming.Context;
import javax.naming.NamingException;

public class ClientMain {
	private static String APP = "ejb-client-connection-leak-ear-1.0.0-SNAPSHOT";
	private static String MODULE = "ejb-client-connection-leak-ejb-1.0.0-SNAPSHOT";

    public static void main( String[] args ) {
        System.out.println( "Getting info from remote bean..." );

        try{
    		BufferedReader br = new BufferedReader(new InputStreamReader(System.in));

    		String line = null;
    		while((line = br.readLine()) != null){
    			if(line.equals("exit")) break;

    			ClientMain client = new ClientMain();
    			Context context = client.getContext();
    			try {
    				client.printHelloMessage(context);
    			} finally {
					//  try {
					//    context.close();
					//  } catch (NamingException ioeContext) {
					//    System.err.println("cant close context");
					//  }
    			}
    		}
    			
    	}catch(IOException io){
    		io.printStackTrace();
    		throw new IllegalStateException("Can't read from stdin", io);
    	}	
    }

    private void printHelloMessage(Context context) {
        String jndi = Util.getEjbJndiName(APP, MODULE, "", "EjbBean", EjbBeanInterface.class.getName(), false);
        
        try {
			EjbBeanInterface ejb = (EjbBeanInterface) context.lookup(jndi);
			String returnString = ejb.sayHello();
			System.out.println(">>>>>>>>>>>>> Saying: " + returnString);
		} catch (Exception e) {
			e.printStackTrace();
		}
    }

    private Context getContext() {
        Context context = null;
        try {
			context = Util.createNamingContext();
		} catch (NamingException ne) {
			ne.printStackTrace();
			throw new IllegalStateException("Can't create naming context", ne);
		}
        return context;
    }
}
