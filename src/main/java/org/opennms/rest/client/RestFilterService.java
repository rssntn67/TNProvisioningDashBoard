package org.opennms.rest.client;

import javax.ws.rs.core.MultivaluedMap;

public interface RestFilterService {

	public MultivaluedMap<String, String> setLimit(Integer limit);
	
	public MultivaluedMap<String, String> setOffset(Integer offset);
	
	public MultivaluedMap<String, String> setOrderBy(String field);

	public MultivaluedMap<String, String> set(MultivaluedMap<String,String> queryParams, String field, String value);

	public MultivaluedMap<String, String> setOrderDesc(MultivaluedMap<String,String> queryParams);

	public MultivaluedMap<String, String> setLimit(MultivaluedMap<String,String> queryParams, Integer limit);
	
	public MultivaluedMap<String, String> setOffset(MultivaluedMap<String,String> queryParams, Integer offset);
	
	public MultivaluedMap<String, String> setOrderBy(MultivaluedMap<String,String> queryParams, String field);
	
	public MultivaluedMap<String, String> setEqualComparator(MultivaluedMap<String,String> queryParams);

	public MultivaluedMap<String, String> setNotEqualComparator(MultivaluedMap<String,String> queryParams);
	
	public MultivaluedMap<String, String> setIlikeComparator(MultivaluedMap<String,String> queryParams);
	
	public MultivaluedMap<String, String> setLikeComparator(MultivaluedMap<String,String> queryParams);
	
	public MultivaluedMap<String, String> setGreaterThanComparator(MultivaluedMap<String,String> queryParams);
	
	public MultivaluedMap<String, String> setGreaterEqualsComparator(MultivaluedMap<String,String> queryParams);
	
	public MultivaluedMap<String, String> setLessThanComparator(MultivaluedMap<String,String> queryParams);
	
	public MultivaluedMap<String, String> setLessEqualsComparator(MultivaluedMap<String,String> queryParams);
	
	public MultivaluedMap<String, String> setQuery(MultivaluedMap<String,String> queryParams, String sqlStatement);
	
}
