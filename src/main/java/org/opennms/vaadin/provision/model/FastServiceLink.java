package org.opennms.vaadin.provision.model;

import com.vaadin.data.Property;

public class FastServiceLink {

	private final String m_orderCode;
	private final String m_tariff;
	private final String m_linkType;
	private final String m_pcv1Name;
	private final String m_pcv2Name;
	private final String m_td;
	private final String m_deliveryDeviceNetworkSide;
	private final String m_deliveryDeviceClientSide;
	private final String m_deliveryInterface;
	private final String m_interfaceDescription; 
	private final String m_vrf;
	private final String m_deliveryCode;

	
	public FastServiceLink(
			Property<String> orderCode, 
			Property<String> tariff, 
			Property<String> linkType,
			Property<String> pcv1Name, 
			Property<String> pcv2Name, 
			Property<String> td,
			Property<String> deliveryDeviceNetworkSide, 
			Property<String> deliveryDeviceClientSide,
			Property<String> deliveryInterface, 
			Property<String> interfaceDescription, 
			Property<String> vrf,
			Property<String> deliveryCode) {
		super();
		if (orderCode!= null)
			m_orderCode = orderCode.getValue();
		else
			m_orderCode = null;
		
		if (tariff != null)
			m_tariff = tariff.getValue();
		else
			m_tariff = null;
		
		if (linkType != null)
			m_linkType = linkType.getValue();
		else
			m_linkType = null;
		
		if (pcv1Name != null)
			m_pcv1Name = pcv1Name.getValue();
		else
			m_pcv1Name = null;

		if (pcv2Name != null)
			m_pcv2Name = pcv2Name.getValue();
		else
			m_pcv2Name = null;

		if (td != null)
			m_td = td.getValue();
		else
			m_td = null;
		
		if (deliveryDeviceNetworkSide != null)
			m_deliveryDeviceNetworkSide = deliveryDeviceNetworkSide.getValue();
		else
			m_deliveryDeviceNetworkSide = null;
		
		if (deliveryDeviceClientSide != null)
			m_deliveryDeviceClientSide = deliveryDeviceClientSide.getValue();
		else
			m_deliveryDeviceClientSide = null;
		
		if (deliveryInterface != null)
			m_deliveryInterface = deliveryInterface.getValue();
		else
			m_deliveryInterface = null;
		
		if (interfaceDescription != null)
			m_interfaceDescription = interfaceDescription.getValue();
		else
			m_interfaceDescription = null;
		
		if (vrf != null)
			m_vrf = vrf.getValue();
		else
			m_vrf = null;
		
		if (deliveryCode != null)
			m_deliveryCode = deliveryCode.getValue();
		else
			m_deliveryCode = null;
	}

	public String getOrderCode() {
		return m_orderCode;
	}
	public String getDeliveryCode() {
		return m_deliveryCode;
	}
	public String getVrf() {
		return m_vrf;
	}
	public String getTariff() {
		return m_tariff;
	}
	public String getLinkType() {
		return m_linkType;
	}
	public String getPcv1Name() {
		return m_pcv1Name;
	}
	public String getPcv2Name() {
		return m_pcv2Name;
	}
	public String getTd() {
		return m_td;
	}
	public String getDeliveryDeviceNetworkSide() {
		return m_deliveryDeviceNetworkSide;
	}
	public String getDeliveryDeviceClientSide() {
		return m_deliveryDeviceClientSide;
	}
	public String getDeliveryInterface() {
		return m_deliveryInterface;
	}
	public String getInterfaceDescription() {
		return m_interfaceDescription;
	}
}
