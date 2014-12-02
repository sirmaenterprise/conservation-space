package com.sirma.cmf.web.rest;

import static org.testng.Assert.assertEquals;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

import net.javacrumbs.jsonunit.JsonAssert;

import org.mockito.Mockito;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import com.sirma.cmf.CMFTest;
import com.sirma.itt.cmf.beans.model.CaseInstance;
import com.sirma.itt.commons.utils.reflection.ReflectionUtils;
import com.sirma.itt.emf.instance.HeadersService;
import com.sirma.itt.emf.instance.model.Instance;
import com.sirma.itt.emf.properties.DefaultProperties;

/**
 * Test for TooltipsRestService class.
 * 
 * @author svelikov
 */
@Test
public class TooltipsRestServiceTest extends CMFTest {

	private static final String DEFAULT_HEADER_FOR_INSTANCE = "default header for instance";
	private TooltipsRestService service;
	private CaseInstance caseInstance;
	private HeadersService headersService;

	/**
	 * Instantiates a new tooltips rest service test.
	 */
	@BeforeMethod
	public void init() {
		service = new TooltipsRestService() {
			@Override
			public Instance fetchInstance(String instanceId, String instanceType) {
				if ((instanceId != null) && (instanceType != null)) {
					return caseInstance;
				}
				return null;
			}
		};
		headersService = Mockito.mock(HeadersService.class);
		ReflectionUtils.setField(service, "headersService", headersService);
		caseInstance = createCaseInstance(Long.valueOf(1L));
		Map<String, Serializable> properties = new HashMap<>();
		caseInstance.setProperties(properties);
		properties.put(DefaultProperties.HEADER_DEFAULT, DEFAULT_HEADER_FOR_INSTANCE);
	}

	/**
	 * test for getTooltip method.
	 */
	public void getTooltipTest() {
		Response response = service.getTooltip(null, null, null);
		assertEquals(response.getStatus(), Status.BAD_REQUEST.getStatusCode());

		response = service.getTooltip("instance1", "caseinstance", null);
		assertEquals(response.getStatus(), Status.OK.getStatusCode());
		JsonAssert.assertJsonEquals("{\"tooltip\":\"default header for instance\"}", response
				.getEntity().toString());
	}
}
