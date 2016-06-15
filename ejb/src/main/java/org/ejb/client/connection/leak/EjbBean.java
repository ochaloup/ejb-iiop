package org.ejb.client.connection.leak;

import javax.ejb.Remote;
import javax.ejb.Stateless;

import org.jboss.qe.EjbBeanInterface;

@Stateless
@Remote(EjbBeanInterface.class)
public class EjbBean implements EjbBeanInterface {

	public String sayHello() {
		return "hello";
	}

}
