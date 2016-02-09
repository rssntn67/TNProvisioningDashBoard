package org.opennms.vaadin.provision.model;

public class FastService {

	 private final String m_orderCode;
	 private final String m_deliveryCode;
	 private final String m_vrf;
	 private final String m_tariff;
	 private final String m_linkType;
	 private final String m_pcv1Name;
	 private final String m_pcv2Name;
	 private final String m_td;
	 private final String m_deliveryDeviceNetworkSide;
	 private final String m_deliveryDeviceClientSide;
	 private final String m_deliveryInterface;
	 private final String m_interfaceDescription; 
	 private final boolean m_masterDevice;
	 private final boolean m_notMonitoring; 
	 private final boolean m_saveConfig; 
	 private final String m_notify_category;
	 private final String m_ipaddr;
	public FastService(String orderCode, String deliveryCode, String vrf,
			String tariff, String linkType, String pcv1Name, String pcv2Name,
			String td, String deliveryDeviceNetworkSide,
			String deliveryDeviceClientSide, String deliveryInterface,
			String interfaceDescription, boolean masterDevice,
			boolean notMonitoring, boolean saveConfig, String notify_category,
			String ipaddr) {
		super();
		m_orderCode = orderCode;
		m_deliveryCode = deliveryCode;
		m_vrf = vrf;
		m_tariff = tariff;
		m_linkType = linkType;
		m_pcv1Name = pcv1Name;
		m_pcv2Name = pcv2Name;
		m_td = td;
		m_deliveryDeviceNetworkSide = deliveryDeviceNetworkSide;
		m_deliveryDeviceClientSide = deliveryDeviceClientSide;
		m_deliveryInterface = deliveryInterface;
		m_interfaceDescription = interfaceDescription;
		m_masterDevice = masterDevice;
		m_notMonitoring = notMonitoring;
		m_saveConfig = saveConfig;
		m_notify_category = notify_category;
		m_ipaddr = ipaddr;
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
	public boolean isMasterDevice() {
		return m_masterDevice;
	}
	public boolean isNotMonitoring() {
		return m_notMonitoring;
	}
	public boolean isSaveConfig() {
		return m_saveConfig;
	}
	public String getNotify_category() {
		return m_notify_category;
	}
	public String getIpaddr() {
		return m_ipaddr;
	}
	 
}
