package org.opennms.rest.client.model;

public class FastAsset {

    public enum Meta {
        Router,
        Switch,
        Radio,
        MediaGW,
        Firewall,
        ADSL,
        OpticFiber,
        Wireless,
        AL,
        DarkFiber,
        Phones,
        ISDN,
        RTG,
        CallSrv,
        Internet,
        HDSL,
        IPMux
    }

    private Long id;
    private Meta meta;
    private String t_date;
    private String t_time;
    private String username;
    private String nome;
    private String cognome;
    private Integer order_id;
    private FastAssetAttributes attributes;
    public FastAssetAttributes getAttributes() {
        return attributes;
    }
    public void setAttributes(FastAssetAttributes attributes) {
        this.attributes = attributes;
    }
    public Long getId() {
        return id;
    }
    public void setId(Long id) {
        this.id = id;
    }
    public Meta getMeta() {
        return meta;
    }
    public void setMeta(Meta meta) {
        this.meta = meta;
    }
    public String getT_date() {
        return t_date;
    }
    public void setT_date(String t_date) {
        this.t_date = t_date;
    }
    public String getT_time() {
        return t_time;
    }
    public void setT_time(String t_time) {
        this.t_time = t_time;
    }
    public String getUsername() {
        return username;
    }
    public void setUsername(String username) {
        this.username = username;
    }
    public String getNome() {
        return nome;
    }
    public void setNome(String nome) {
        this.nome = nome;
    }
    public String getCognome() {
        return cognome;
    }
    public void setCognome(String cognome) {
        this.cognome = cognome;
    }
    public Integer getOrder_id() {
        return order_id;
    }
    public void setOrder_id(Integer order_id) {
        this.order_id = order_id;
    }

    @Override
    public String toString() {
        return "FastAsset [id=" + id + ", meta=" + meta + ", t_date=" + t_date
                + ", t_time=" + t_time + ", username=" + username + ", nome="
                + nome + ", cognome=" + cognome + ", order_id=" + order_id
                + ", attributes=" + attributes + "]";
    }
    
    
    
}
