package com.sirma.itt.seip.rest.utils.request.params;

import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

import org.junit.Assert;
import org.junit.Test;

/**
 * Test for some common request param converters.
 * @author yasko
 */
public class RequestParamConvertersTest {

	/**
	 * test integer converter.
	 */
	@Test
	public void testParamToInteger() {
		Assert.assertNull(RequestParamConverters.TO_INTEGER.apply(null, null));
		Assert.assertNull(RequestParamConverters.TO_INTEGER.apply(null, new LinkedList<>()));
		Assert.assertEquals(Integer.valueOf(1), RequestParamConverters.TO_INTEGER.apply(null, Arrays.asList("1")));
	}
	
	/**
	 * test return first item converter.
	 */
	@Test
	public void testFirstItem() {
		Assert.assertNull(RequestParamConverters.FIRST_ITEM.apply(null, null));
		Assert.assertNull(RequestParamConverters.FIRST_ITEM.apply(null, new LinkedList<>()));
		Assert.assertEquals("1", RequestParamConverters.FIRST_ITEM.apply(null, Arrays.asList("1")));
	}
	
	/**
	 * test return what is passed in converter.
	 */
	@Test
	public void testToList() {
		List<String> list = new LinkedList<>();
		Assert.assertNull(RequestParamConverters.TO_LIST.apply(null, null));
		Assert.assertTrue(list == RequestParamConverters.TO_LIST.apply(null, list));
	}
	
	@Test
	public void testQueryLimitParamConverter() {
		Assert.assertNull(RequestParamConverters.QUERY_LIMIT_CONVERTER.apply(null, null));
		Assert.assertNull(RequestParamConverters.QUERY_LIMIT_CONVERTER.apply(null, Collections.emptyList()));
		
		Assert.assertEquals(Integer.valueOf(-1), RequestParamConverters.QUERY_LIMIT_CONVERTER.apply(null, Arrays.asList("all")));
		Assert.assertEquals(Integer.valueOf(10), RequestParamConverters.QUERY_LIMIT_CONVERTER.apply(null, Arrays.asList("10")));
	}
}
