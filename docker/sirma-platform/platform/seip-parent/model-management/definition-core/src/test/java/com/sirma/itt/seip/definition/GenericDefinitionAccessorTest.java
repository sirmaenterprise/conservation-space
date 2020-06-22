package com.sirma.itt.seip.definition;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.MockitoAnnotations;
import org.mockito.Spy;

import com.sirma.itt.seip.configuration.ConfigurationProperty;
import com.sirma.itt.seip.domain.instance.ClassInstance;
import com.sirma.itt.seip.instance.relation.LinkInstance;
import com.sirma.itt.seip.instance.relation.LinkReference;
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
}
