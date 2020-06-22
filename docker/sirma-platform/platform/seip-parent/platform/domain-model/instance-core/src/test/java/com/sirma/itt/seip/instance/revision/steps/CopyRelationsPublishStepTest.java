package com.sirma.itt.seip.instance.revision.steps;

import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.when;

import java.util.HashSet;
import java.util.Set;

import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import com.sirma.itt.seip.definition.DefinitionService;
import com.sirma.itt.seip.domain.definition.DataTypeDefinition;
import com.sirma.itt.seip.domain.instance.EmfInstance;
import com.sirma.itt.seip.expressions.conditions.ConditionType;
import com.sirma.itt.seip.expressions.conditions.ConditionsManager;
import com.sirma.itt.seip.instance.revision.PublishInstanceRequest;
import com.sirma.itt.seip.instance.state.Operation;
import com.sirma.itt.seip.instance.state.StateService;
import com.sirma.itt.seip.instance.state.StateTransitionManager;
import com.sirma.itt.seip.testutil.mocks.ConditionMock;
import com.sirma.itt.seip.testutil.mocks.DataTypeDefinitionMock;
import com.sirma.itt.seip.testutil.mocks.DefinitionMock;
import com.sirma.itt.seip.testutil.mocks.PropertyDefinitionMock;

/**
 * Test for {@link CopyRelationsPublishStep}.
 *
 * @author BBonev
 */
public class CopyRelationsPublishStepTest {

	@InjectMocks
	private CopyRelationsPublishStep step;

	@Mock
	private DefinitionService definitionService;
	@Mock
	private ConditionsManager conditionsEvaluatorManager;
	@Mock
	private StateTransitionManager stateTransitionManager;
	@Mock
	private StateService stateService;

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
		instance.add("transitionMandatory1", "transitionMandatory");
		instance.add("transitionMandatory2", "transitionMandatory");

		DefinitionMock definition = buildDefinition();
		when(definitionService.getInstanceDefinition(instance)).thenReturn(definition);

		Set<String> transitionMandatory = new HashSet<>();
		transitionMandatory.add("transitionMandatory1");
		transitionMandatory.add("transitionMandatory2");

		when(stateService.getPrimaryState(instance)).thenReturn("DRAFT");
		when(stateTransitionManager.getRequiredFields(instance, "DRAFT", "publishAsPdf"))
		.thenReturn(transitionMandatory);

		EmfInstance revision = new EmfInstance();
		revision.getOrCreateProperties();

		step.execute(new PublishContext(new PublishInstanceRequest(instance, new Operation(), null, null), revision));

		assertTrue(revision.isValueNull("notMandatory"));
		assertTrue(revision.isValueNotNull("mandatory"));
		assertTrue(revision.isValueNotNull("dynamicMandatory1"));
		assertTrue(revision.isValueNotNull("dynamicMandatory2"));
		assertTrue(revision.isValueNotNull("transitionMandatory1"));
		assertTrue(revision.isValueNotNull("transitionMandatory2"));
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

		PropertyDefinitionMock transitionMandatory1 = new PropertyDefinitionMock();
		transitionMandatory1.setDataType(buildUriType());
		transitionMandatory1.setMandatory(Boolean.TRUE);
		transitionMandatory1.setName("transitionMandatory1");
		definition.getFields().add(transitionMandatory1);

		PropertyDefinitionMock transitionMandatory2 = new PropertyDefinitionMock();
		transitionMandatory2.setDataType(buildUriType());
		transitionMandatory2.setMandatory(Boolean.TRUE);
		transitionMandatory2.setName("transitionMandatory2");
		definition.getFields().add(transitionMandatory2);
		return definition;
	}

	private static DataTypeDefinition buildUriType() {
		return new DataTypeDefinitionMock(DataTypeDefinition.URI);
	}
}
