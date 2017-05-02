package com.sirma.itt.seip.instance.version.revert;

import static com.sirma.itt.seip.domain.instance.DefaultProperties.NAME;
import static com.sirma.itt.seip.domain.instance.DefaultProperties.TYPE;
import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.when;

import java.util.Arrays;

import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import com.sirma.itt.seip.definition.DictionaryService;
import com.sirma.itt.seip.domain.definition.DataTypeDefinition;
import com.sirma.itt.seip.domain.definition.PropertyDefinition;
import com.sirma.itt.seip.domain.instance.EmfInstance;
import com.sirma.itt.seip.domain.instance.Instance;
import com.sirma.itt.seip.exception.EmfRuntimeException;
import com.sirma.itt.seip.testutil.mocks.DataTypeDefinitionMock;
import com.sirma.itt.seip.testutil.mocks.DefinitionMock;
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
	private DictionaryService dictionaryService;

	@Before
	public void setup() {
		step = new CopyObjectPropertiesOnRevertStep();
		MockitoAnnotations.initMocks(this);
	}

	@Test
	public void getName() {
		assertEquals("copyObjectProperties", step.getName());
	}

	@Test(expected = EmfRuntimeException.class)
	public void invoke_noDefinition_exceptionExpected() {
		Instance current = new EmfInstance();
		current.setId("instance-id");
		when(dictionaryService.getInstanceDefinition(current)).thenReturn(null);
		step.invoke(RevertContext.create("instance-id-v1.6").setCurrentInstance(current));
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

		DefinitionMock definition = new DefinitionMock();
		definition.setFields(Arrays.asList(buildField("type"), buildField("name")));
		when(dictionaryService.getInstanceDefinition(current)).thenReturn(definition);

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
		type.setDataType(new DataTypeDefinitionMock(DataTypeDefinition.URI));
		return type;
	}

}
