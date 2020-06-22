package com.sirma.itt.emf.web.rest;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNotNull;
import static org.testng.Assert.assertNull;

import java.util.Optional;

import javax.ws.rs.core.Response;

import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import com.sirma.itt.emf.rest.EmfRestService;
import com.sirma.itt.seip.convert.TypeConverter;
import com.sirma.itt.seip.domain.instance.InstanceReference;
import com.sirma.itt.seip.domain.rest.BadRequestException;
import com.sirma.itt.seip.instance.InstanceTypeResolver;

/**
 * The Class EmfRestServiseTest.
 *
 * @author svelikov
 */
@Test
public class EmfRestServiseTest {

	@InjectMocks
	private EmfRestService service;
	@Mock
	private TypeConverter typeConverter;
	@Mock
	private InstanceTypeResolver instanceTypeResolver;
	/** The Constant CASEINSTANCEID. */
	private static final String CASEINSTANCEID = "caseinstanceid";

	@BeforeMethod
	public void beforeMethod() {
		MockitoAnnotations.initMocks(this);
	}

	/**
	 * Gets the instance referense test.
	 */
	public void getInstanceReferenseTest() {
		InstanceReference instanceReferense = service.getInstanceReference(null);
		assertNull(instanceReferense);

		when(instanceTypeResolver.resolveReference(CASEINSTANCEID)).thenReturn(Optional.empty());

		instanceReferense = service.getInstanceReference(CASEINSTANCEID);
		assertNull(instanceReferense);

		instanceReferense = service.getInstanceReference(null);
		assertNull(instanceReferense);

		InstanceReference caseInstanceReference = mock(InstanceReference.class);
		when(caseInstanceReference.getId()).thenReturn(CASEINSTANCEID);
		when(instanceTypeResolver.resolveReference(CASEINSTANCEID)).thenReturn(Optional.of(caseInstanceReference));
		instanceReferense = service.getInstanceReference(CASEINSTANCEID);
		assertNotNull(instanceReferense);
		assertEquals(instanceReferense.getId(), CASEINSTANCEID);
	}

	/**
	 * Fetch instance test.
	 */
	@Test(expectedExceptions = { BadRequestException.class }, dataProvider = "bad-request-provider")
	public void fetchInstanceTest(String id) {
		service.fetchInstance(id);
	}

	/**
	 * Data provider for {@link #fetchInstanceTest(String, String)} with invalid parameters.
	 *
	 * @return Test data.
	 */
	@DataProvider(name = "bad-request-provider")
	protected Object[][] badRequestProvider() {
		return new Object[][] { { null } };
	}

	/**
	 * Builds the response test.
	 */
	public void buildResponseTest() {
		Response response = service.buildResponse(null, null);
		assertNull(response);

		response = service.buildResponse(Response.Status.ACCEPTED, null);
		assertEquals(response.getStatus(), Response.Status.ACCEPTED.getStatusCode());
		assertEquals(response.getEntity(), null);

		response = service.buildResponse(Response.Status.ACCEPTED, "some message");
		assertEquals(response.getStatus(), Response.Status.ACCEPTED.getStatusCode());
		assertEquals(response.getEntity(), "some message");
	}

}
