package com.sirma.itt.seip.mail;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyInt;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import com.sirma.itt.seip.definition.DefinitionService;
import com.sirma.itt.seip.domain.codelist.CodelistService;
import com.sirma.itt.seip.domain.codelist.model.CodeValue;
import com.sirma.itt.seip.domain.definition.label.LabelProvider;
import com.sirma.itt.seip.domain.instance.DefaultProperties;
import com.sirma.itt.seip.domain.instance.EmfInstance;
import com.sirma.itt.seip.domain.instance.Instance;
import com.sirma.itt.seip.domain.instance.InstanceReference;
import com.sirma.itt.seip.permissions.PermissionService;
import com.sirma.itt.seip.permissions.role.ResourceRole;
import com.sirma.itt.seip.permissions.role.RoleIdentifier;
import com.sirma.itt.seip.resources.EmfUser;
import com.sirma.itt.seip.resources.ResourceService;
import com.sirma.itt.seip.resources.ResourceType;
import com.sirma.itt.seip.testutil.mocks.InstanceReferenceMock;
import com.sirma.itt.seip.testutil.mocks.PropertyDefinitionMock;

/**
 * Test for {@link MailResourceProvider}.
 *
 * @author A. Kunchev
 */
public class MailResourceProviderTest {

	@InjectMocks
	private MailResourceProvider provider;

	@Mock
	private LabelProvider labelProvider;

	@Mock
	private ResourceService resourceService;

	@Mock
	private PermissionService permissionService;

	@Mock
	private DefinitionService definitionService;

	@Mock
	private CodelistService codelistService;

	@Mock
	private MailNotificationHelperService mailNotificationHelperService;

	@Before
	public void setup() {
		provider = new MailResourceProviderImpl();
		MockitoAnnotations.initMocks(this);
	}

	@Test
	public void getDisplayableProperty_nullInstance_emptyString() {
		assertEquals("", provider.getDisplayableProperty(null, new EmfUser(), DefaultProperties.TITLE));
	}

	@Test
	public void getDisplayableProperty_nullResource_emptyString() {
		assertEquals("", provider.getDisplayableProperty(new EmfInstance(), null, DefaultProperties.TITLE));
	}

	@Test
	public void getDisplayableProperty_nullProperty_emptyString() {
		assertEquals("", provider.getDisplayableProperty(new EmfInstance(), new EmfUser(), null));
	}

	@Test
	public void getDisplayableProperty_emptyProperty_emptyString() {
		assertEquals("", provider.getDisplayableProperty(new EmfInstance(), new EmfUser(), ""));
	}

	@Test
	public void getDisplayableProperty_nullArguments_emptyString() {
		assertEquals("", provider.getDisplayableProperty(null, null, null));
	}

	@Test
	public void getDisplayableProperty_nullDefinitionProperty_identifier() {
		Instance instance = new EmfInstance();
		instance.setIdentifier("identifier");
		when(definitionService.getProperty(anyString(), any(Instance.class))).thenReturn(null);
		assertEquals("identifier", provider.getDisplayableProperty(instance, new EmfUser(), DefaultProperties.TYPE));
	}

	@Test
	public void getDisplayableProperty_nullPropertyValue_identifier() {
		Instance instance = new EmfInstance();
		instance.setIdentifier("identifier");
		when(definitionService.getProperty(anyString(), any(Instance.class))).thenReturn(new PropertyDefinitionMock());
		assertEquals("identifier", provider.getDisplayableProperty(instance, new EmfUser(), DefaultProperties.TYPE));
	}

	@Test
	public void getDisplayableProperty_emptyPropertyValue_identifier() {
		Instance instance = new EmfInstance();
		instance.setIdentifier("identifier");
		instance.add(DefaultProperties.TYPE, "");
		when(definitionService.getProperty(anyString(), any(Instance.class))).thenReturn(new PropertyDefinitionMock());
		assertEquals("identifier", provider.getDisplayableProperty(instance, new EmfUser(), DefaultProperties.TYPE));
	}

	@Test
	public void getDisplayableProperty_nullCodelist_identifier() {
		Instance instance = new EmfInstance();
		instance.setIdentifier("identifier");
		instance.add(DefaultProperties.TYPE, "type");
		when(definitionService.getProperty(anyString(), any(Instance.class))).thenReturn(new PropertyDefinitionMock());
		assertEquals("identifier", provider.getDisplayableProperty(instance, new EmfUser(), DefaultProperties.TYPE));
	}

	@Test
	public void getDisplayableProperty_successful_type() {
		Instance instance = new EmfInstance();
		instance.setIdentifier("identifier");
		instance.setRevision(1L);
		instance.add(DefaultProperties.TYPE, "type");
		PropertyDefinitionMock definition = new PropertyDefinitionMock();
		definition.setCodelist(1);
		when(definitionService.getProperty("type", instance)).thenReturn(definition);
		CodeValue codeValue = new CodeValue();
		codeValue.add("en", "instanceType");
		when(codelistService.getCodeValue(anyInt(), anyString())).thenReturn(codeValue);
		when(mailNotificationHelperService.getUserLanguage(any())).thenReturn("en");
		assertEquals("instanceType", provider.getDisplayableProperty(instance, new EmfUser(), DefaultProperties.TYPE));
	}

	@Test
	public void getUserRole_nullUser() {
		assertNull(provider.getUserRole(new EmfInstance(), null));
	}

	@Test
	public void getUserRole_nullInstance() {
		assertNull(provider.getUserRole(null, new EmfUser()));
	}

	@Test
	public void getUserRole_nullInstanceAndUser() {
		assertNull(provider.getUserRole(null, null));
	}

	@Test
	public void getUserRole_nullRole() {
		EmfInstance instance = new EmfInstance("instanceid");
		InstanceReference reference = InstanceReferenceMock.createGeneric(instance);
		EmfUser user = new EmfUser();
		user.setId("user1");
		when(permissionService.getPermissionAssignment(reference, user.getId())).thenReturn(new ResourceRole());
		assertNull(provider.getUserRole(instance, user));
	}

	@Test
	public void getUserRole_notNullInstanceAndUser() {
		ResourceRole resourceRole = new ResourceRole();
		RoleIdentifier role = mock(RoleIdentifier.class);
		when(role.getIdentifier()).thenReturn("roleIndentifier");
		resourceRole.setRole(role);
		when(permissionService.getPermissionAssignment(any(InstanceReference.class), anyString()))
				.thenReturn(resourceRole);
		EmfInstance instance = new EmfInstance("instanceid");
		InstanceReference reference = InstanceReferenceMock.createGeneric(instance);
		EmfUser user = new EmfUser();
		user.setId("user1");
		assertEquals("roleIndentifier", provider.getUserRole(reference.toInstance(), user));
	}

	@Test
	public void getLabel_nullInput_null() {
		assertNull(provider.getLabel(null));
	}

	@Test
	public void getLabel_emptyInput_null() {
		assertNull(provider.getLabel(""));
	}

	@Test
	public void getLabel_successful() {
		when(labelProvider.getValue(anyString())).thenReturn("result");
		assertEquals("result", provider.getLabel("input"));
	}

	@Test
	public void getResource_withType() {
		when(resourceService.getResource(anyString(), any(ResourceType.class))).thenReturn(new EmfUser());
		assertNotNull(provider.getResource("id", ResourceType.USER));
	}

	@Test
	public void getResource_onlyId() {
		when(resourceService.getResource(anyString())).thenReturn(new EmfUser());
		assertNotNull(provider.getResource("id"));
	}
}
