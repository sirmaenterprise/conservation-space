package com.sirma.itt.emf.web.rest;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNotNull;
import static org.testng.Assert.assertNull;
import static org.testng.Assert.assertTrue;

import javax.ws.rs.core.Response;

import org.mockito.Mockito;
import org.testng.annotations.Test;

import com.sirma.itt.commons.utils.reflection.ReflectionUtils;
import com.sirma.itt.emf.converter.TypeConverter;
import com.sirma.itt.emf.entity.LinkSourceId;
import com.sirma.itt.emf.instance.model.Instance;
import com.sirma.itt.emf.instance.model.InstanceReference;
import com.sirma.itt.emf.rest.EmfRestService;

/**
 * The Class EmfRestServiseTest.
 * 
 * @author svelikov
 */
@Test
public class EmfRestServiseTest {

	/** The service. */
	private final EmfRestService service;

	/** The type converter. */
	private final TypeConverter typeConverter;

	/** The Constant CASEINSTANCEID. */
	private static final String CASEINSTANCEID = "caseinstanceid";

	/**
	 * Instantiates a new emf rest servise test.
	 */
	public EmfRestServiseTest() {
		service = new EmfRestService();

		typeConverter = Mockito.mock(TypeConverter.class);

		ReflectionUtils.setField(service, "typeConverter", typeConverter);
	}

	/**
	 * Gets the instance class test.
	 */
	public void getInstanceClassTest() {

	}

	/**
	 * Gets the instance referense test.
	 */
	public void getInstanceReferenseTest() {
		InstanceReference instanceReferense = service.getInstanceReferense(null, null);
		assertNull(instanceReferense);

		instanceReferense = service.getInstanceReferense(CASEINSTANCEID, null);
		assertNull(instanceReferense);

		instanceReferense = service.getInstanceReferense(null, "caseinstance");
		assertNull(instanceReferense);

		InstanceReference caseInstanceReference = new LinkSourceId();
		Mockito.when(typeConverter.convert(InstanceReference.class, "caseinstance")).thenReturn(
				caseInstanceReference);
		instanceReferense = service.getInstanceReferense(CASEINSTANCEID, "caseinstance");
		assertNotNull(instanceReferense);
		assertEquals(instanceReferense.getIdentifier(), CASEINSTANCEID);
	}

	/**
	 * Fetch instance test.
	 */
	public void fetchInstanceTest() {
		Instance instance = service.fetchInstance(null, null);
		assertTrue(instance == null);
		service.fetchInstance("1", null);
		assertTrue(instance == null);
		service.fetchInstance(null, "caseinstance");
		assertTrue(instance == null);
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
