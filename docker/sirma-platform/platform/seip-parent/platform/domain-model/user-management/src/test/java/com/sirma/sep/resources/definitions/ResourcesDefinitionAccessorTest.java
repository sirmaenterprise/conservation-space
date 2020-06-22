package com.sirma.sep.resources.definitions;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Set;

import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.Spy;

import com.sirma.itt.seip.cache.lookup.EntityLookupCacheContext;
import com.sirma.itt.seip.configuration.ConfigurationProperty;
import com.sirma.itt.seip.domain.ObjectTypes;
import com.sirma.itt.seip.domain.definition.GenericDefinition;
import com.sirma.itt.seip.domain.instance.EmfInstance;
import com.sirma.itt.seip.domain.instance.InstanceType;
import com.sirma.itt.seip.resources.EmfGroup;
import com.sirma.itt.seip.resources.EmfResource;
import com.sirma.itt.seip.resources.EmfUser;
import com.sirma.itt.seip.resources.ResourceType;
import com.sirma.itt.seip.testutil.mocks.ConfigurationPropertyMock;

/**
 * Test for {@link ResourcesDefinitionAccessor}.
 *
 * @author A. Kunchev
 */
public class ResourcesDefinitionAccessorTest {

	@InjectMocks
	private ResourcesDefinitionAccessor accessor;

	@Mock
	private EntityLookupCacheContext cacheContext;

	@Spy
	private ConfigurationProperty<String> defaultUserDefinition = new ConfigurationPropertyMock<>("userDefinition");

	@Spy
	private ConfigurationProperty<String> defaultGroupDefinition = new ConfigurationPropertyMock<>("groupDefinition");

	@Before
	public void setup() {
		accessor = new ResourcesDefinitionAccessor();
		MockitoAnnotations.initMocks(this);
	}

	@Test
	public void initinializeCache_internalServiceCalled() {
		accessor.initinializeCache();
		verify(cacheContext, times(2)).containsCache(anyString());
	}

	@Test
	public void getSupportedObjects() {
		Set<Class<?>> supportedObjects = accessor.getSupportedObjects();
		assertNotNull(supportedObjects);
		assertEquals(4, supportedObjects.size());
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

	@Test
	public void getTargetDefinition() {
		assertEquals(GenericDefinition.class, accessor.getTargetDefinition());
	}

	@Test
	public void getBaseCacheName() {
		assertEquals("GENERIC_DEFINITION_CACHE", accessor.getBaseCacheName());
	}

	@Test
	public void getMaxRevisionCacheName() {
		assertEquals("GENERIC_DEFINITION_MAX_REVISION_CACHE", accessor.getMaxRevisionCacheName());
	}
}