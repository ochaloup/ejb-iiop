package org.jboss.qe;

import java.util.Properties;

import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;

public class Util {
	private Util() {
		// no instantiation
	}

    /**
     * Creates JNDI context string based on given parameters.
     * See details at https://docs.jboss.org/author/display/AS71/EJB+invocations+from+a+remote+client+using+JNDI
     *
     * @param appName - typically the ear name without the .ear
     *                - could be empty string when deploying just jar with EJBs
     * @param moduleName - jar file name without trailing .jar
     * @param distinctName - AS7 allows each deployment to have an (optional) distinct name
     *                     - could be empty string when not specified
     * @param beanName - The EJB name which by default is the simple class name of the bean implementation class
     * @param viewClassName - the remote view is fully qualified class name of @Remote EJB interface
     * @param isStateful - if the bean is stateful set to true
     *
     * @return - JNDI context string to use in your client JNDI lookup
     */
    public static String getEjbJndiName(
            String appName,
            String moduleName,
            String distinctName,
            String beanName,
            String viewClassName,
            boolean isStateful) {

        return "ejb:" + appName + "/" + moduleName + "/" + distinctName + "/" + beanName + "!" + viewClassName
                + (isStateful ? "?stateful" : "");
    }

    /**
     * Helper to create InitialContext with necessary properties.
     *
     * @return new InitialContext.
     * @throws NamingException
     */
    public static Context createNamingContext() throws NamingException {

        final Properties jndiProps = new Properties();
        jndiProps.setProperty(Context.URL_PKG_PREFIXES, "org.jboss.ejb.client.naming");

        return new InitialContext(jndiProps);

    }
}
