package org.opennms.vaadin.provision.dashboard;

import com.sun.jersey.api.client.ClientResponse;
import com.sun.jersey.api.client.UniformInterfaceException;

public class ProvisionDashboardException extends UniformInterfaceException {

	public ProvisionDashboardException(ClientResponse r) {
		super(r);
	}

	/**
	 * 
	 */
	private static final long serialVersionUID = 1524056241176263403L;

	
}
