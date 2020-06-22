package com.sirma.itt.seip.definition.validator;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.List;

import org.junit.Before;
import org.junit.Test;

import com.sirma.itt.seip.definition.TransitionDefinition;
import com.sirma.itt.seip.definition.TransitionGroupDefinition;
import com.sirma.itt.seip.definition.model.TransitionDefinitionImpl;
import com.sirma.itt.seip.definition.model.TransitionGroupDefinitionImpl;
import com.sirma.itt.seip.domain.definition.GenericDefinition;

public class TransitionGroupValidatorTest {

	private TransitionGroupValidator validator = new TransitionGroupValidator();

	@Test
	public void should_ReportValid_WhenDataIsCorrect() {
		withGroup("gr1", "gr2", "label1", "menu");
		withGroup("gr2", "gr3", "label2", "menu");
		withGroup("gr3", null, "label3", "menu");

		withTransition("t1", null);
		withTransition("t2", "gr2");

		assertTrue(validator.validate(model).isEmpty());
	}

	@Test
	public void should_NotAllowGroupsWithoutType() {
		withGroup("gr1", null, "label1", null);

		assertFalse(validator.validate(model).isEmpty());
	}

	@Test
	public void should_NotAllowGroupsWithoutLabel() {
		withGroup("gr1", null, null, "menu");

		assertFalse(validator.validate(model).isEmpty());
	}

	@Test
	public void should_NotAllowTransitions_WithNonExistingGroup() {
		withTransition("tr1", "gr1");

		assertFalse(validator.validate(model).isEmpty());
	}

	@Test
	public void should_NotAllow_NonExistingGroup_AsGroupParent() {
		withGroup("gr1", "gr2", "label1", "menu");

		assertFalse(validator.validate(model).isEmpty());
	}

	@Test
	public void should_NotAllowCyclesInGroupHierarchy() {
		withGroup("gr1", "gr2", "label1", "menu");
		withGroup("gr2", "gr3", "label1", "menu");
		withGroup("gr3", "gr1", "label1", "menu");

		assertFalse(validator.validate(model).isEmpty());
	}

	@Before
	public void init() {
		when(model.getTransitions()).thenReturn(transitions);
		when(model.getTransitionGroups()).thenReturn(groups);
	}

	private void withGroup(String id, String parent, String label, String type) {
		TransitionGroupDefinitionImpl group = new TransitionGroupDefinitionImpl();
		group.setIdentifier(id);
		group.setParent(parent);
		group.setLabelId(label);
		group.setType(type);

		groups.add(group);
	}

	private void withTransition(String id, String group) {
		TransitionDefinitionImpl transition = new TransitionDefinitionImpl();
		transition.setIdentifier(id);
		transition.setGroup(group);

		transitions.add(transition);
	}

	private GenericDefinition model = mock(GenericDefinition.class);
	private List<TransitionDefinition> transitions = new ArrayList<>();
	private List<TransitionGroupDefinition> groups = new ArrayList<>();

}
