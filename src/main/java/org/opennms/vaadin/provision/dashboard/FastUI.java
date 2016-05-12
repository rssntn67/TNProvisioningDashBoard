package org.opennms.vaadin.provision.dashboard;

import com.vaadin.annotations.Theme;
import com.vaadin.server.VaadinRequest;
import com.vaadin.ui.Label;
import com.vaadin.ui.UI;
import com.vaadin.ui.VerticalLayout;

@Theme("runo")
public class FastUI extends UI {

	/**
	 * 
	 */
	private static final long serialVersionUID = -7128455812303263101L;

	@Override
	protected void init(VaadinRequest request) {

		 VerticalLayout content = new VerticalLayout();
	        setContent(content);

	        // Display the greeting
	        content.addComponent(new Label("Hello World!"));
	}

}
