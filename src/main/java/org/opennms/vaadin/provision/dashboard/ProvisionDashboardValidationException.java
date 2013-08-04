package org.opennms.vaadin.provision.dashboard;

import com.vaadin.data.fieldgroup.FieldGroup.CommitException;

public class ProvisionDashboardValidationException extends CommitException {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1524056241176263403L;

	public ProvisionDashboardValidationException() {
		super();
	}

	public ProvisionDashboardValidationException(String arg0, Throwable arg1) {
		super(arg0, arg1);
	}

	public ProvisionDashboardValidationException(String arg0) {
		super(arg0);
	}

	public ProvisionDashboardValidationException(Throwable arg0) {
		super(arg0);
	}
	
}
