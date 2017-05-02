package com.sirma.itt.cmf.services.impl.dao;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.MockitoAnnotations;
import org.mockito.Spy;

import com.sirma.itt.seip.configuration.ConfigurationProperty;
import com.sirma.itt.seip.domain.ObjectTypes;
import com.sirma.itt.seip.domain.instance.ClassInstance;
import com.sirma.itt.seip.domain.instance.EmfInstance;
import com.sirma.itt.seip.domain.instance.InstanceType;
import com.sirma.itt.seip.instance.relation.LinkInstance;
import com.sirma.itt.seip.instance.relation.LinkReference;
import com.sirma.itt.seip.resources.EmfGroup;
import com.sirma.itt.seip.resources.EmfResource;
import com.sirma.itt.seip.resources.EmfUser;
import com.sirma.itt.seip.resources.ResourceType;
import com.sirma.itt.seip.testutil.mocks.ConfigurationPropertyMock;

/**
 * Tests for {@link GenericDefinitionAccessor}.
 *
 * @author A. Kunchev
 */
public class GenericDefinitionAccessorTest {

	@InjectMocks
	private GenericDefinitionAccessor accessor;

	@Spy
	private ConfigurationProperty<String> defaultUserDefinition = new ConfigurationPropertyMock<>("userDefinition");

	@Spy
	private ConfigurationProperty<String> defaultGroupDefinition = new ConfigurationPropertyMock<>("groupDefinition");

	@Spy
	private ConfigurationProperty<String> defaultRelationDefinition = new ConfigurationPropertyMock<>("linkDefinition");

	@Spy
	private ConfigurationProperty<String> defaultClassInstanceDefinition = new ConfigurationPropertyMock<>(
			"classDefinition");

	@Before
	public void setup() {
		accessor = new GenericDefinitionAccessor();
		MockitoAnnotations.initMocks(this);
	}

	@Test
	public void getDefaultDefinitionId_justObject() {
		String definitionId = accessor.getDefaultDefinitionId(new Object());
		assertNull(definitionId);
	}

	@Test
	public void getDefaultDefinitionId_userInstance() {
		EmfInstance instance = new EmfInstance();
		InstanceType type = stubType(ObjectTypes.USER, true);
		instance.setType(type);
		String definitionId = accessor.getDefaultDefinitionId(instance);
		assertEquals("userDefinition", definitionId);
	}

	@Test
	public void getDefaultDefinitionId_groupInstance() {
		EmfInstance instance = new EmfInstance();
		InstanceType type = stubType(ObjectTypes.GROUP, true);
		instance.setType(type);
		String definitionId = accessor.getDefaultDefinitionId(instance);
		assertEquals("groupDefinition", definitionId);
	}

	private static InstanceType stubType(String typeToSet, boolean isSameType) {
		InstanceType type = mock(InstanceType.class);
		when(type.is(typeToSet)).thenReturn(isSameType);
		return type;
	}

	@Test
	public void getDefaultDefinitionId_linkInstance() {
		LinkInstance instance = new LinkInstance();
		String definitionId = accessor.getDefaultDefinitionId(instance);
		assertEquals("linkDefinition", definitionId);
	}

	@Test
	public void getDefaultDefinitionId_linkReference() {
		LinkReference instance = new LinkReference();
		String definitionId = accessor.getDefaultDefinitionId(instance);
		assertEquals("linkDefinition", definitionId);
	}

	@Test
	public void getDefaultDefinitionId_classInstance() {
		ClassInstance instance = new ClassInstance();
		String definitionId = accessor.getDefaultDefinitionId(instance);
		assertEquals("classDefinition", definitionId);
	}

	@Test
	public void getDefaultDefinitionId_userResource() {
		EmfResource instance = new EmfResource();
		instance.setType(ResourceType.USER);
		String definitionId = accessor.getDefaultDefinitionId(instance);
		assertEquals("userDefinition", definitionId);
	}

	@Test
	public void getDefaultDefinitionId_groupResource() {
		EmfResource instance = new EmfResource();
		instance.setType(ResourceType.GROUP);
		String definitionId = accessor.getDefaultDefinitionId(instance);
		assertEquals("groupDefinition", definitionId);
	}

	@Test
	public void getDefaultDefinitionId_nullType_returnIdentifier() {
		EmfResource instance = new EmfResource();
		instance.setIdentifier("resource-identifier");
		String definitionId = accessor.getDefaultDefinitionId(instance);
		assertEquals("resource-identifier", definitionId);
	}

	@Test
	public void getDefaultDefinitionId_userWithUnknownType() {
		EmfResource instance = mock(EmfUser.class);
		when(instance.getType()).thenReturn(ResourceType.UNKNOWN);
		String definitionId = accessor.getDefaultDefinitionId(instance);
		assertEquals("userDefinition", definitionId);
	}

	@Test
	public void getDefaultDefinitionId_groupWithUnknownType() {
		EmfResource instance = mock(EmfGroup.class);
		when(instance.getType()).thenReturn(ResourceType.UNKNOWN);
		String definitionId = accessor.getDefaultDefinitionId(instance);
		assertEquals("groupDefinition", definitionId);
	}

	@Test
	public void getDefaultDefinitionId_resourceWithUnknownType() {
		EmfResource instance = mock(EmfResource.class);
		when(instance.getType()).thenReturn(ResourceType.UNKNOWN);
		String definitionId = accessor.getDefaultDefinitionId(instance);
		assertNull(definitionId);
	}

}
