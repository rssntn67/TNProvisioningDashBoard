package org.opennms.rest.client;

import javax.ws.rs.core.MultivaluedMap;

import com.sun.jersey.core.util.MultivaluedMapImpl;

public class JerseyAbstractService {

    private JerseyClientImpl m_jerseyClient;
    
    public JerseyClientImpl getJerseyClient() {
        return m_jerseyClient;
    }

    public void setJerseyClient(JerseyClientImpl jerseyClient) {
        m_jerseyClient = jerseyClient;
    }

	public MultivaluedMap<String, String> setLimit(Integer limit) {
		MultivaluedMap< String, String> queryParams = new MultivaluedMapImpl();
		queryParams.add("limit", Integer.toString(limit));
		return queryParams;
	}
	
	public MultivaluedMap<String, String> setOffset(Integer offset){
		MultivaluedMap< String, String> queryParams = new MultivaluedMapImpl();
		queryParams.add("offset", Integer.toString(offset));
		return queryParams;
	}
	
	public MultivaluedMap<String, String> setOrderBy(String field) {
		MultivaluedMap< String, String> queryParams = new MultivaluedMapImpl();
		queryParams.add("orderBy", field);
		return queryParams;
	}

	public MultivaluedMap<String, String> set(MultivaluedMap<String, String> queryParams, String field,String value) {
		queryParams.add(field, value);
		return queryParams;
	}

	public MultivaluedMap<String, String> setOrderDesc(MultivaluedMap<String, String> queryParams) {
		queryParams.add("order", "desc");
		return queryParams;
	}

	public MultivaluedMap<String, String> setLimit(MultivaluedMap<String, String> queryParams, Integer limit) {
		queryParams.add("limit", Integer.toString(limit));
		return queryParams;
	}
	
	public MultivaluedMap<String, String> setOffset(MultivaluedMap<String, String> queryParams, Integer offset) {
		queryParams.add("offset", Integer.toString(offset));
		return queryParams;
	}
	
	public MultivaluedMap<String, String> setOrderBy(MultivaluedMap<String, String> queryParams, String field) {
		queryParams.add("orderBy", field);
		return queryParams;
	}
	
	public MultivaluedMap<String, String> setEqualComparator(MultivaluedMap<String, String> queryParams) {
		queryParams.add("comparator", "eq");
		return queryParams;
	}

	public MultivaluedMap<String, String> setNotEqualComparator(MultivaluedMap<String, String> queryParams) {
		queryParams.add("comparator", "ne");
		return queryParams;
	}
	
	public MultivaluedMap<String, String> setIlikeComparator(MultivaluedMap<String, String> queryParams) {
		queryParams.add("comparator", "ilike");
		return queryParams;
	}
	
	public MultivaluedMap<String, String> setLikeComparator(MultivaluedMap<String, String> queryParams) {
		queryParams.add("comparator", "like");
		return queryParams;
	}
	
	public MultivaluedMap<String, String> setGreaterThanComparator(MultivaluedMap<String, String> queryParams) {
		queryParams.add("comparator", "gt");
		return queryParams;
	}
	
	public MultivaluedMap<String, String> setGreaterEqualsComparator(MultivaluedMap<String, String> queryParams) {
		queryParams.add("comparator", "ge");
		return queryParams;
	}
	
	public MultivaluedMap<String, String> setLessThanComparator(MultivaluedMap<String, String> queryParams) {
		queryParams.add("comparator", "lt");
		return queryParams;
	}
	
	public MultivaluedMap<String, String> setLessEqualsComparator(MultivaluedMap<String, String> queryParams) {
		queryParams.add("comparator", "le");
		return queryParams;
	}
	
	public MultivaluedMap<String, String> setQuery(MultivaluedMap<String, String> queryParams, String sqlStatement) {
		queryParams.add("query", sqlStatement);
		return queryParams;
	}
	
	
}
