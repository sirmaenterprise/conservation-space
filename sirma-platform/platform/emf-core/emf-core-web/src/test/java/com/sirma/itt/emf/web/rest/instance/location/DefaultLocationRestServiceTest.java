/**
 *
 */
package com.sirma.itt.emf.web.rest.instance.location;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyCollection;
import static org.mockito.Matchers.anyMap;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.mockito.MockitoAnnotations.initMocks;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;

import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

import org.json.JSONObject;
import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;

import com.sirma.itt.seip.convert.TypeConverter;
import com.sirma.itt.seip.db.DatabaseIdManager;
import com.sirma.itt.seip.domain.definition.label.LabelProvider;
import com.sirma.itt.seip.domain.instance.ClassInstance;
import com.sirma.itt.seip.domain.instance.DefaultProperties;
import com.sirma.itt.seip.domain.instance.EmfInstance;
import com.sirma.itt.seip.domain.instance.Instance;
import com.sirma.itt.seip.domain.instance.InstanceReference;
import com.sirma.itt.seip.domain.rest.RestServiceException;
import com.sirma.itt.seip.instance.location.InstanceDefaultLocationService;
import com.sirma.itt.seip.permissions.action.AuthorityService;

/**
 * @author A. Kunchev
 */
public class DefaultLocationRestServiceTest {

	private static final String data = "[{\"definitionId\":\"definitionId\", \"instanceId\":\"locationId\", \"instanceType\":\"locationType\"}]";

	@InjectMocks
	private DefaultLocationRestService service = new DefaultLocationRestService();

	@Mock
	private LabelProvider labelProvider;

	@Mock
	private InstanceDefaultLocationService instanceDefaultLocationService;

	@Mock
	private AuthorityService authorityService;

	@Mock
	private TypeConverter typeConverter;

	@Mock
	private DatabaseIdManager databaseIdManager;

	@Before
	public void setUp() {
		initMocks(this);
	}

	// ------------------------------- setDefaultLocations ---------------------------------

	@Test(expected = RestServiceException.class)
	public void setDefaultLocations_notAdminUser_exception() {
		stubAuthorityService(false);
		service.setDefaultLocations("");
		verify(instanceDefaultLocationService, never()).addDefaultLocations(anyMap());
	}

	@Test(expected = RestServiceException.class)
	public void setDefaultLocations_emptyData_exception() {
		stubAuthorityService(true);
		service.setDefaultLocations("");
		verify(instanceDefaultLocationService, never()).addDefaultLocations(anyMap());
	}

	@Test(expected = RestServiceException.class)
	public void setDefaultLocations_nullData_exception() {
		stubAuthorityService(true);
		service.setDefaultLocations(null);
		verify(instanceDefaultLocationService, never()).addDefaultLocations(anyMap());
	}

	@Test(expected = RestServiceException.class)
	public void setDefaultLocations_emptyJSONArray_ok() {
		stubAuthorityService(true);
		service.setDefaultLocations("[]");
		verify(instanceDefaultLocationService, never()).addDefaultLocations(anyMap());
	}

	@Test
	public void setDefaultLocations_oneLocation_ok() {
		stubAuthorityService(true);
		stubTypeConverter();
		Response response = service.setDefaultLocations(data);
		verify(instanceDefaultLocationService).addDefaultLocations(anyMap());
		assertEquals(Status.OK.getStatusCode(), response.getStatus());
	}

	// ------------------------------- updateDefaultLocations ------------------------------

	@Test(expected = RestServiceException.class)
	public void updateDefaultLocations_notAdminUser_exception() {
		stubAuthorityService(false);
		service.updateDefaultLocations(null);
		verify(instanceDefaultLocationService, never()).updateDefaultLocations(anyMap());
	}

	@Test(expected = RestServiceException.class)
	public void updateDefaultLocations_emptyData_exception() {
		stubAuthorityService(true);
		service.updateDefaultLocations("");
		verify(instanceDefaultLocationService, never()).updateDefaultLocations(anyMap());
	}

	@Test(expected = RestServiceException.class)
	public void updateDefaultLocations_nullData_exception() {
		stubAuthorityService(true);
		service.updateDefaultLocations(null);
		verify(instanceDefaultLocationService, never()).updateDefaultLocations(anyMap());
	}

	@Test(expected = RestServiceException.class)
	public void updateDefaultLocations_emptyJSONArray_exception() {
		stubAuthorityService(true);
		service.updateDefaultLocations("[]");
		verify(instanceDefaultLocationService, never()).updateDefaultLocations(anyMap());
	}

	@Test
	public void updateDefaultLocations_oneLocation_ok() {
		stubAuthorityService(true);
		stubTypeConverter();
		Response response = service.updateDefaultLocations(data);
		verify(instanceDefaultLocationService).updateDefaultLocations(anyMap());
		assertEquals(Status.OK.getStatusCode(), response.getStatus());
	}

	// ------------------------------- retrieveLocations -----------------------------------

	@Test(expected = RestServiceException.class)
	public void retrieveLocations_emptyDefinitionId_exception() {
		service.retrieveLocations("");
	}

	@Test(expected = RestServiceException.class)
	public void retrieveLocations_nullDefinitionId_exception() {
		service.retrieveLocations(null);
	}

	@Test(expected = RestServiceException.class)
	public void retrieveLocations_noLocations_exception() {
		when(instanceDefaultLocationService.retrieveLocations(anyString())).thenReturn(Collections.emptyList());
		stubTypeConverter();
		service.retrieveLocations("definitionId");
	}

	@Test
	@SuppressWarnings({ "rawtypes", "unchecked" })
	public void retrieveLocations_twoLocations_ok() {
		Instance instance = prepareInstance("id1");
		instance.add("defaultLocation", "definitionId");
		Collection<Instance> instances = Arrays.asList(instance, prepareInstance("id2"));
		when(databaseIdManager.getValidId(anyString())).thenReturn("definitionId");
		when(instanceDefaultLocationService.retrieveLocations(anyString())).thenReturn((Collection) instances);
		stubTypeConverter();
		Response response = service.retrieveLocations("definitionId");
		verify(instanceDefaultLocationService).retrieveLocations(anyString());
		assertEquals(Status.OK.getStatusCode(), response.getStatus());
		assertNotNull(response.getEntity());
	}

	// ------------------------------- removeDefaultLocations ------------------------------

	@Test(expected = RestServiceException.class)
	public void removeDefaultLocations_notAdminUser_exception() {
		stubAuthorityService(false);
		service.removeDefaultLocations(null);
		verify(instanceDefaultLocationService, never()).removeDefaultLocations(anyCollection());
	}

	@Test(expected = RestServiceException.class)
	public void removeDefaultLocations_emptyData_exception() {
		stubAuthorityService(true);
		service.removeDefaultLocations("");
		verify(instanceDefaultLocationService, never()).removeDefaultLocations(anyCollection());
	}

	@Test(expected = RestServiceException.class)
	public void removeDefaultLocations_nullData_exception() {
		stubAuthorityService(true);
		service.removeDefaultLocations(null);
		verify(instanceDefaultLocationService, never()).removeDefaultLocations(anyCollection());
	}

	@Test(expected = RestServiceException.class)
	public void removeDefaultLocations_emptyJSONArray_exception() {
		stubAuthorityService(true);
		service.removeDefaultLocations("[]");
		verify(instanceDefaultLocationService, never()).removeDefaultLocations(anyCollection());
	}

	@Test(expected = RestServiceException.class)
	public void removeDefaultLocations_badJSONArray_exception() {
		stubAuthorityService(true);
		service.removeDefaultLocations("[{}}]");
		verify(instanceDefaultLocationService, never()).removeDefaultLocations(anyCollection());
	}

	@Test
	public void removeDefaultLocations_oneLocation_ok() {
		stubAuthorityService(true);
		stubTypeConverter();
		Response response = service.removeDefaultLocations("[\"type\"]");
		verify(instanceDefaultLocationService).removeDefaultLocations(anyCollection());
		assertEquals(Status.OK.getStatusCode(), response.getStatus());
	}

	// --------------------------- common test methods -------------------------------------

	private void stubAuthorityService(boolean toReturn) {
		when(authorityService.isAdminOrSystemUser()).thenReturn(toReturn);
	}

	private void stubTypeConverter() {
		when(typeConverter.convert(any(), anyString())).thenReturn(mock(InstanceReference.class));
		when(typeConverter.convert(any(), any(JSONObject.class))).thenReturn(mock(InstanceReference.class));
	}

	private static Instance prepareInstance(String id) {
		EmfInstance instance = new EmfInstance();
		instance.setId(id);
		instance.add(DefaultProperties.HEADER_COMPACT, "header");
		ClassInstance classInstance = new ClassInstance();
		classInstance.setCategory("test");
		instance.setType(classInstance.type());
		return instance;
	}

}
