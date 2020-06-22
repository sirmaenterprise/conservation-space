package com.sirma.itt.seip.permissions.script;

import static org.mockito.Mockito.when;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;

import com.sirma.itt.seip.domain.instance.Instance;
import com.sirma.itt.seip.domain.instance.InstanceReference;
import com.sirma.itt.seip.permissions.PermissionService;
import com.sirma.itt.seip.resources.ResourceService;
import com.sirma.itt.seip.script.ScriptInstance;

public class PermissionScriptProviderTest {

	private static final String USER_ID = "userID";
	@Mock
	private PermissionService service;
	@Mock
	private ResourceService resourceService;
	@InjectMocks
	private PermissionScriptProvider provider;
	@Mock
	private Instance instance;
	@Mock
	private InstanceReference reference;

	@Mock
	private ScriptInstance node;

	@Before
	public void setUp() throws Exception {
		MockitoAnnotations.initMocks(this);
	}

	@Test
	public void testNullRole() {
		Mockito.when(resourceService.loadByDbId(Mockito.anyString())).thenReturn(null);
		Assert.assertNull("Expected null value", provider.getUserRole("ID", USER_ID));
	}

	@Test
	public void testNullRoleFound() {
		Mockito.when(instance.toReference()).thenReturn(reference);
		Mockito.when(resourceService.loadByDbId(Mockito.anyString())).thenReturn(instance);
		Assert.assertNull("Expected null value", provider.getUserRole("ID", USER_ID));
	}

	@Test
	public void testNodeRoleSearch() {
		when(node.toReference()).thenReturn(reference);
		Mockito.when(instance.toReference()).thenReturn(reference);
		Mockito.when(resourceService.loadByDbId(Mockito.anyString())).thenReturn(instance);
		Assert.assertNull("Expected null value", provider.getUserRole(node, USER_ID));
	}

}
