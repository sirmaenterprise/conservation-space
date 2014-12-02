package com.sirma.cmf.web.caseinstance;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNotNull;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

import javax.ws.rs.core.Response;

import org.mockito.Mockito;
import org.testng.annotations.Test;

import com.sirma.cmf.CMFTest;
import com.sirma.itt.cmf.beans.model.CaseInstance;
import com.sirma.itt.cmf.beans.model.SectionInstance;
import com.sirma.itt.cmf.services.SectionService;
import com.sirma.itt.commons.utils.reflection.ReflectionUtils;
import com.sirma.itt.emf.web.header.InstanceHeaderBuilder;
import com.sirma.itt.emf.web.treeHeader.TreeHeaderBuilder;

/**
 * The Class CaseInstanceRestServiceTest.
 * 
 * @author svelikov
 */
@Test
public class CaseInstanceRestServiceTest extends CMFTest {

	/** The service. */
	private final CaseInstanceRestService service;
	private final SectionService sectionService;
	private final SectionInstance sectionInstance;
	private final CaseInstance caseInstance;
	private final InstanceHeaderBuilder treeHeaderBuilder;

	/**
	 * Instantiates a new case instance rest service test.
	 */
	public CaseInstanceRestServiceTest() {
		service = new CaseInstanceRestService() {

		};

		sectionInstance = createSectionInstance(Long.valueOf(1));
		caseInstance = createCaseInstance(Long.valueOf(1));
		Map<String, Serializable> properties = new HashMap<String, Serializable>(1);
		caseInstance.setProperties(properties);

		sectionService = Mockito.mock(SectionService.class);
		treeHeaderBuilder = Mockito.mock(TreeHeaderBuilder.class);

		ReflectionUtils.setField(service, "log", SLF4J_LOG);
		ReflectionUtils.setField(service, "sectionService", sectionService);
		ReflectionUtils.setField(service, "treeHeaderBuilder", treeHeaderBuilder);
	}

	/**
	 * Gets the object sections test.
	 */
	public void getObjectSectionsTest() {
		// if all required arguments are missing
		Response response = service.getObjectSections(null, null, null, null, null);
		assertEquals(response.getStatus(), Response.Status.BAD_REQUEST.getStatusCode());

		response = service.getObjectSections("caseid", null, null, null, null);
		assertEquals(response.getStatus(), Response.Status.BAD_REQUEST.getStatusCode());

		response = service.getObjectSections(null, "sectioninstance", null, null, null);
		assertEquals(response.getStatus(), Response.Status.BAD_REQUEST.getStatusCode());

		response = service.getObjectSections(null, "documentinstance", null, null, null);
		assertEquals(response.getStatus(), Response.Status.BAD_REQUEST.getStatusCode());

		// if section can't be found
		Mockito.when(sectionService.loadByDbId(Mockito.any(Serializable.class))).thenReturn(null);
		response = service.getObjectSections("sectionid", "sectioninstance", null, null, null);
		assertEquals(response.getStatus(), Response.Status.BAD_REQUEST.getStatusCode());

		// if section is found but no owning instance exists
		sectionInstance.setOwningInstance(caseInstance);
		Mockito.when(sectionService.loadByDbId(Mockito.any(Serializable.class))).thenReturn(
				sectionInstance);
		response = service.getObjectSections("sectionid", "sectioninstance", null, null, null);
		assertEquals(response.getStatus(), Response.Status.OK.getStatusCode());
		assertNotNull(response.getEntity());
	}
}
