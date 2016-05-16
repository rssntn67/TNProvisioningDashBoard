create table vrf (
	name text primary key,
    notifylevel text not null,	
    networklevel text default 'Accesso' not null,	
    dnsdomain default 'wl.tnnet.it' text not null,	
    thresholdlevel default 'ThresholdWARNING' text not null,	
    backupprofile  default 'accesso_radius' text not null,	
    snmpprofile  default 'snmp_default_v2' text not null,	
    versionid integer not null default 0
);

create table backupprofiles (
	name text primary key,
	username text,
	password text,
	enable text,
	connection text,
	auto_enable text,
    versionid integer not null default 0
);

create table snmpprofiles (
	name text primary key,
	community text,
	version text,
	timeout text,
    versionid integer not null default 0
);

create table dnsdomains (
	dnsdomain varchar(253) primary key,
    versionid integer not null default 0	
);

create table dnssubdomains (
	dnssubdomain varchar(253) primary key,
    versionid integer not null default 0	
);

create table fastservicedevices (
	hostname text,
	ipaddr text,
	ipaddr_lan text,
	netmask_lan text,
	serial_number text,
	address_desc varchar(32),
	address_name varchar(64),
	address_number varchar(16),
	floor varchar(16),
	room varchar(16),
	city varchar(128),
	istat_code varchar(16),
	master_device boolean,
	snmpprofiles text,
	backupprofiles text,
	not_monitoring boolean,
	save_config boolean,
	notify_category text,
	order_code varchar(32),
	device_type varchar(64)
);

create table fastservicelink (
	order_code varchar(32),
	tariff varchar(32),
	link_type varchar(64),
	pcv_1_name text,
	pcv_2_name text,
	td text,
	delivery_device_network_side text,
	delivery_device_client_side text,
	delivery_interface text,
	interface_description text,
	vrf text,
	delivery_code text,
	site_code varchar(16)
);
	
create table fasttariff (
	tariff_description text,
	tariff varchar(32)
);

create sequence jobs_jobid_seq minvalue 1;	

create table jobs (
 jobid serial primary key,
 username  varchar(256) not null,
 jobdescr text,
 jobstatus varchar(10),
 jobstart timestamp with time zone,
 jobend   timestamp with time zone,
 versionid integer not null default 0
);

create table joblogs (
	 joblogid serial primary key,
	 jobid integer not null,
	 hostname text,
	 ipaddr text,
	 order_code varchar(32),
	 description text,
	 note text,
	 versionid integer not null default 0	 
);


 
