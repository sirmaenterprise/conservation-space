package com.sirma.itt.seip.rest.resources.instances;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyBoolean;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.UriInfo;

import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.mockito.Spy;

import com.sirma.itt.seip.db.DatabaseIdManager;
import com.sirma.itt.seip.domain.instance.EmfInstance;
import com.sirma.itt.seip.domain.instance.Instance;
import com.sirma.itt.seip.domain.instance.InstanceReference;
import com.sirma.itt.seip.domain.util.InstanceUtil;
import com.sirma.itt.seip.exceptions.InstanceNotFoundException;
import com.sirma.itt.seip.instance.DomainInstanceService;
import com.sirma.itt.seip.instance.InstanceTypeResolver;
import com.sirma.itt.seip.instance.actions.Actions;
import com.sirma.itt.seip.instance.actions.delete.DeleteRequest;
import com.sirma.itt.seip.instance.actions.save.CreateOrUpdateRequest;
import com.sirma.itt.seip.rest.exceptions.BadRequestException;
import com.sirma.itt.seip.rest.exceptions.ResourceNotFoundException;
import com.sirma.itt.seip.rest.utils.request.RequestInfo;
import com.sirma.itt.seip.security.context.SecurityContextManager;
import com.sirma.itt.seip.testutil.fakes.SecurityContextManagerFake;
import com.sirma.itt.seip.testutil.mocks.InstanceReferenceMock;

/**
 * Tests for instance CRUD operations.
 *
 * @author yasko
 * @author A. Kunchev
 */

public class InstanceResourceTest {

	@InjectMocks
	private InstanceResource resource;

	@Spy
	private SecurityContextManager securityContextManager = new SecurityContextManagerFake();

	@Mock
	private DomainInstanceService domainInstanceService;

	@Mock
	private DatabaseIdManager idManager;

	@Mock
	private Instance instance;

	@Mock
	private MultivaluedMap<String, String> paramsMap;

	@Mock
	private UriInfo uriInfo;

	@Mock
	private RequestInfo request;

	@Mock
	private Actions actions;

	@Mock
	private InstanceTypeResolver typeResolver;

	@Before
	public void init() {
		MockitoAnnotations.initMocks(this);
		InstanceUtil.init(idManager);
		when(uriInfo.getQueryParameters()).thenReturn(paramsMap);
		when(request.getUriInfo()).thenReturn(uriInfo);
	}

	/**
	 * Test successful instance creation.
	 */
	@Test
	public void testCreate() {
		when(instance.getId()).thenReturn("emf:instance");
		when(typeResolver.resolveReference("emf:instance")).then(a -> Optional.empty());
		when(idManager.isPersisted(instance)).thenReturn(false);
		resource.create(instance);
		verify(idManager).isPersisted(any());
	}

	/**
	 * Test instance creation failure.
	 */
	@Test(expected = BadRequestException.class)
	public void testCreatePersistedInstance() {
		EmfInstance toCreate = new EmfInstance();
		toCreate.setId("emf:instance");
		when(typeResolver.resolveReference("emf:instance"))
				.then(a -> Optional.of(InstanceReferenceMock.createGeneric(a.getArgumentAt(0, String.class))));
		resource.create(toCreate);
	}

	/**
	 * Test successful instance update.
	 */
	@Test
	public void testUpdate() {
		when(instance.getId()).thenReturn("1");
		resource.update("1", instance);
	}

	@Test
	public void delete_loadAndDeleteMethodCalled() {
		resource.delete("instanceId");
		verify(actions).callAction(any(DeleteRequest.class));
	}

	@Test(expected = BadRequestException.class)
	public void deleteAll_nullIdentifiers() {
		resource.deleteAll(null);
	}

	@Test(expected = BadRequestException.class)
	public void deleteAll_emptyIdentifiers() {
		resource.deleteAll(new ArrayList<>());
	}

	@Test(expected = BadRequestException.class)
	public void findAll_emptyCollection() {
		when(paramsMap.get("id")).thenReturn(new ArrayList<>());
		resource.findAll(request);
	}

	@Test(expected = BadRequestException.class)
	public void findAll_nullCollection() {
		when(paramsMap.get("id")).thenReturn(null);
		resource.findAll(request);
	}

	@Test
	public void findAll_threeIds() {
		List<String> ids = Arrays.asList("instanceId1", "instanceId2", "instanceId3");
		when(paramsMap.get("id")).thenReturn(ids);
		resource.findAll(request);
		verify(domainInstanceService).loadInstances(ids);
	}

	@Test(expected = BadRequestException.class)
	public void batch_emptyCollection() {
		resource.batch(Collections.emptyList());
	}

	@Test(expected = BadRequestException.class)
	public void batch_nullCollection() {
		resource.batch(null);
	}

	@Test
	public void batch_threeIds() {
		List<String> ids = Arrays.asList("instanceId1", "instanceId2", "instanceId3");
		resource.batch(ids);
		verify(domainInstanceService).loadInstances(ids);
	}

	@Test
	public void testDefaults() {
		String parentId = "parentId";
		String definitionId = "definitionId";

		String instanceId = "instanceId";
		Instance testInstance = new EmfInstance();
		testInstance.setId(instanceId);

		when(domainInstanceService.createInstance(definitionId, parentId)).thenReturn(testInstance);
		resource.defaults(definitionId, parentId);

		verify(domainInstanceService).createInstance(definitionId, parentId);
		verify(idManager).unregister(testInstance);
	}

	@Test
	public void updateAll_actionsCalled() {
		EmfInstance instance1 = new EmfInstance();
		instance1.setId("instance-id-1");
		EmfInstance instance2 = new EmfInstance();
		instance2.setId("instance-id-2");
		InstanceReference reference = Mockito.mock(InstanceReference.class);
		instance1.setReference(reference);
		instance2.setReference(reference);
		resource.updateAll(Arrays.asList(instance1, instance2));
		verify(actions, Mockito.times(2)).callAction(any(CreateOrUpdateRequest.class));
	}

	@Test
	public void getInstanceContext() throws Exception {
		when(domainInstanceService.getInstanceContext("emf:instance"))
				.thenReturn(Arrays.asList(new EmfInstance(), new EmfInstance()));
		ContextPath path = resource.getInstanceContextPath("emf:instance");

		assertNotNull(path);
		assertEquals(2, path.getPath().size());
	}

	@Test
	public void find() throws Exception {
		when(domainInstanceService.loadInstance(anyString(), anyBoolean())).thenReturn(new EmfInstance());
		Instance result = resource.find("emf:instance", true);
		assertNotNull(result);
	}

	@Test(expected = ResourceNotFoundException.class)
	public void find_notFound() throws Exception {
		when(domainInstanceService.loadInstance(anyString(), anyBoolean())).thenThrow(InstanceNotFoundException.class);
		resource.find("emf:instance", true);
	}
}
