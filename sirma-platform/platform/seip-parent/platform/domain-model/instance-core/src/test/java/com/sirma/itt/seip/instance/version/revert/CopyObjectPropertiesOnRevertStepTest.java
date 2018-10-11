package com.sirma.itt.seip.instance.version.revert;

import static com.sirma.itt.seip.domain.instance.DefaultProperties.NAME;
import static com.sirma.itt.seip.domain.instance.DefaultProperties.TYPE;
import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.when;

import java.util.stream.Stream;

import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import com.sirma.itt.seip.definition.DefinitionService;
import com.sirma.itt.seip.domain.definition.DataTypeDefinition;
import com.sirma.itt.seip.domain.definition.PropertyDefinition;
import com.sirma.itt.seip.domain.instance.EmfInstance;
import com.sirma.itt.seip.domain.instance.Instance;
import com.sirma.itt.seip.testutil.mocks.DataTypeDefinitionMock;
import com.sirma.itt.seip.testutil.mocks.PropertyDefinitionMock;

/**
 * Test for {@link CopyObjectPropertiesOnRevertStep}.
 *
 * @author A. Kunchev
 */
public class CopyObjectPropertiesOnRevertStepTest {

	@InjectMocks
	private CopyObjectPropertiesOnRevertStep step;

	@Mock
	private DefinitionService definitionService;

	@Before
	public void setup() {
		step = new CopyObjectPropertiesOnRevertStep();
		MockitoAnnotations.initMocks(this);
	}

	@Test
	public void getName() {
		assertEquals("copyObjectProperties", step.getName());
	}

	@Test
	public void invoke_withDefininition_propertiesTransferred() {
		Instance current = new EmfInstance();
		current.setId("instance-id");
		current.add(TYPE, "current-instance-type");

		Instance result = new EmfInstance();
		result.setId("instance-id-v1.6");
		result.add(TYPE, "version-instnace-type");
		result.add(NAME, "Bruce Wayne");

		when(definitionService.getInstanceObjectProperties(current))
				.thenReturn(Stream.of(buildField("type"), buildField("name")));

		RevertContext context = RevertContext
				.create("instance-id-v1.6")
					.setCurrentInstance(current)
					.setRevertResultInstance(result);
		step.invoke(context);

		assertEquals(1, context.getRevertResultInstance().getProperties().size());
		assertEquals("current-instance-type", context.getRevertResultInstance().getString(TYPE));
	}

	private static PropertyDefinition buildField(String identifier) {
		PropertyDefinitionMock type = new PropertyDefinitionMock();
		type.setIdentifier(identifier);
		type.setName(identifier);
		type.setDataType(new DataTypeDefinitionMock(DataTypeDefinition.URI));
		return type;
	}
}
