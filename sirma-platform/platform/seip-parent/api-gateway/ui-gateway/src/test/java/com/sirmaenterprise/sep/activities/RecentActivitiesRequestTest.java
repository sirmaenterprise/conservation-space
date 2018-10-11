package com.sirmaenterprise.sep.activities;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.when;

import java.util.Arrays;
import java.util.Date;

import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.UriInfo;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import com.sirma.itt.seip.rest.utils.request.RequestInfo;
import com.sirma.itt.seip.rest.utils.request.params.RequestParams;
import com.sirma.itt.seip.time.ISO8601DateFormat;

/**
 * Test for {@link RecentActivitiesRequest}.
 * @author A. Kunchev
 *
 */
public class RecentActivitiesRequestTest {

	@Mock
	private MultivaluedMap<String, String> quertyParamsMap;

	@Mock
	private MultivaluedMap<String, String> pathParamsMap;

	@Mock
	private UriInfo uriInfo;

	@Mock
	private RequestInfo request;

	@Before
	public void setup() {
		MockitoAnnotations.initMocks(this);

		when(pathParamsMap.get("id")).thenReturn(Arrays.asList("emf:instance-id"));
		when(uriInfo.getPathParameters()).thenReturn(pathParamsMap);

		when(quertyParamsMap.get("limit")).thenReturn(Arrays.asList("20"));
		when(quertyParamsMap.get("offset")).thenReturn(Arrays.asList("10"));
		when(quertyParamsMap.get("start")).thenReturn(Arrays.asList(ISO8601DateFormat.format(new Date())));
		when(uriInfo.getQueryParameters()).thenReturn(quertyParamsMap);

		when(request.getUriInfo()).thenReturn(uriInfo);
	}

	@Test
	public void buildRequestFromInfo() {
		RecentActivitiesRequest activitiesRequest = RecentActivitiesRequest.buildRequestFromInfo(request,
				RequestParams.PATH_ID);
		assertNotNull(activitiesRequest);
		assertEquals("emf:instance-id", activitiesRequest.getIds().iterator().next());
		assertEquals(20, activitiesRequest.getLimit());
		assertEquals(10, activitiesRequest.getOffset());
		assertTrue(activitiesRequest.getDateRange().isPresent());
	}

}
