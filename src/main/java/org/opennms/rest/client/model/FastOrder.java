package org.opennms.rest.client.model;

public class FastOrder {
    
    enum OrderState {
        Dismesso("Dismesso"),
        InProduzione("In Produzione"),
        Annullato("Annullato"),
        InLavorazione("In Lavorazione"),
        EspansioneInfrastruttura("Espansione Infrastruttura"), 
        RichiestaDismissione("Richiesta Dismissione");
        private String display;
        
        OrderState(String display) {
            this.display=display;
        }
        
        public String display() { return display; }

        // Optionally and/or additionally, toString.
        @Override public String toString() { return display; }
    }

    private Long row_order_id;
    private String order_code;
    private Long order_id;
    private String order_tariff;
    private String order_customer;
    private String order_billing;
    private String office;
    private String address_type;
    private String address_street;
    private String address_no;
    private String address;
    private String city;
    private String order_state;
    public Long getRow_order_id() {
        return row_order_id;
    }
    public void setRow_order_id(Long row_order_id) {
        this.row_order_id = row_order_id;
    }
    public String getOrder_code() {
        return order_code;
    }
    public void setOrder_code(String order_code) {
        this.order_code = order_code;
    }
    public Long getOrder_id() {
        return order_id;
    }
    public void setOrder_id(Long order_id) {
        this.order_id = order_id;
    }
    public String getOrder_tariff() {
        return order_tariff;
    }
    public void setOrder_tariff(String order_tariff) {
        this.order_tariff = order_tariff;
    }
    public String getOrder_customer() {
        return order_customer;
    }
    public void setOrder_customer(String order_customer) {
        this.order_customer = order_customer;
    }
    public String getOrder_billing() {
        return order_billing;
    }
    public void setOrder_billing(String order_billing) {
        this.order_billing = order_billing;
    }
    public String getOffice() {
        return office;
    }
    public void setOffice(String office) {
        this.office = office;
    }
    public String getAddress_type() {
        return address_type;
    }
    public void setAddress_type(String address_type) {
        this.address_type = address_type;
    }
    public String getAddress_street() {
        return address_street;
    }
    public void setAddress_street(String address_street) {
        this.address_street = address_street;
    }
    public String getAddress_no() {
        return address_no;
    }
    public void setAddress_no(String address_no) {
        this.address_no = address_no;
    }
    public String getAddress() {
        return address;
    }
    public void setAddress(String address) {
        this.address = address;
    }
    public String getCity() {
        return city;
    }
    public void setCity(String city) {
        this.city = city;
    }
    public String getOrder_state() {
        return order_state;
    }
    public void setOrder_state(String order_state) {
        this.order_state = order_state;
    }
    @Override
    public String toString() {
        return "FastOrder [row_order_id=" + row_order_id + ", order_code="
                + order_code + ", order_id=" + order_id + ", order_tariff="
                + order_tariff + ", order_customer=" + order_customer
                + ", order_billing=" + order_billing + ", office=" + office
                + ", address_type=" + address_type + ", address_street="
                + address_street + ", address_no=" + address_no + ", address="
                + address + ", city=" + city + ", order_state=" + order_state
                + "]";
    }         
    
    public boolean produzione() {
        return order_state.equals("In Produzione");
    }
}
