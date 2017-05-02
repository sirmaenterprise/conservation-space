package com.sirma.itt.seip.instance.revision.steps;

import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.when;

import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import com.sirma.itt.seip.definition.DictionaryService;
import com.sirma.itt.seip.domain.definition.DataTypeDefinition;
import com.sirma.itt.seip.domain.instance.EmfInstance;
import com.sirma.itt.seip.expressions.conditions.ConditionType;
import com.sirma.itt.seip.expressions.conditions.ConditionsManager;
import com.sirma.itt.seip.instance.revision.PublishInstanceRequest;
import com.sirma.itt.seip.instance.state.Operation;
import com.sirma.itt.seip.testutil.mocks.ConditionMock;
import com.sirma.itt.seip.testutil.mocks.DataTypeDefinitionMock;
import com.sirma.itt.seip.testutil.mocks.DefinitionMock;
import com.sirma.itt.seip.testutil.mocks.PropertyDefinitionMock;

/**
 * Test for {@link CopyRelationsPublishStep}
 *
 * @author BBonev
 */
public class CopyRelationsPublishStepTest {

	@InjectMocks
	private CopyRelationsPublishStep step;

	@Mock
	private DictionaryService dictionaryService;
	@Mock
	private ConditionsManager conditionsEvaluatorManager;

	@Before
	public void beforeMethod() {
		MockitoAnnotations.initMocks(this);
	}

	@Test
	public void processCopyRelations() throws Exception {
		EmfInstance instance = new EmfInstance();
		instance.add("notMandatory", "notMandatory");
		instance.add("mandatory", "mandatory");
		instance.add("dynamicMandatory1", "dynamicMandatory");
		instance.add("dynamicMandatory2", "dynamicMandatory");

		DefinitionMock definition = buildDefinition();
		when(dictionaryService.getInstanceDefinition(instance)).thenReturn(definition);

		EmfInstance revision = new EmfInstance();
		revision.getOrCreateProperties();

		step.execute(new PublishContext(new PublishInstanceRequest(instance, new Operation(), null, null), revision));

		assertTrue(revision.isValueNull("notMandatory"));
		assertTrue(revision.isValueNotNull("mandatory"));
		assertTrue(revision.isValueNotNull("dynamicMandatory1"));
		assertTrue(revision.isValueNotNull("dynamicMandatory2"));
	}

	private DefinitionMock buildDefinition() {
		DefinitionMock definition = new DefinitionMock();

		PropertyDefinitionMock notMandatory = new PropertyDefinitionMock();
		notMandatory.setDataType(buildUriType());
		notMandatory.setMandatory(Boolean.FALSE);
		notMandatory.setName("notMandatory");
		definition.getFields().add(notMandatory);

		PropertyDefinitionMock mandatory = new PropertyDefinitionMock();
		mandatory.setDataType(buildUriType());
		mandatory.setMandatory(Boolean.TRUE);
		mandatory.setName("mandatory");
		definition.getFields().add(mandatory);

		PropertyDefinitionMock dynamicMandatory1 = new PropertyDefinitionMock();
		dynamicMandatory1.setDataType(buildUriType());
		dynamicMandatory1.setMandatory(Boolean.FALSE);
		ConditionMock mandatoryCondition = new ConditionMock();
		mandatoryCondition.setRenderAs(ConditionType.MANDATORY.getRenderAs());
		mandatoryCondition.setExpression("some expression");
		dynamicMandatory1.getConditions().add(mandatoryCondition);
		dynamicMandatory1.setName("dynamicMandatory1");
		definition.getFields().add(dynamicMandatory1);
		when(conditionsEvaluatorManager.evalPropertyConditions(eq(dynamicMandatory1), eq(ConditionType.MANDATORY),
				any())).thenReturn(Boolean.TRUE);

		PropertyDefinitionMock dynamicMandatory2 = new PropertyDefinitionMock();
		dynamicMandatory2.setDataType(buildUriType());
		dynamicMandatory2.setMandatory(Boolean.FALSE);
		ConditionMock requiredCondition = new ConditionMock();
		requiredCondition.setRenderAs(ConditionType.REQUIRED.getRenderAs());
		requiredCondition.setExpression("some expression");
		dynamicMandatory2.getConditions().add(requiredCondition);
		dynamicMandatory2.setName("dynamicMandatory2");
		definition.getFields().add(dynamicMandatory2);
		when(conditionsEvaluatorManager.evalPropertyConditions(eq(dynamicMandatory2), eq(ConditionType.REQUIRED),
				any())).thenReturn(Boolean.TRUE);
		return definition;
	}

	private static DataTypeDefinition buildUriType() {
		return new DataTypeDefinitionMock(DataTypeDefinition.URI);
	}
}
