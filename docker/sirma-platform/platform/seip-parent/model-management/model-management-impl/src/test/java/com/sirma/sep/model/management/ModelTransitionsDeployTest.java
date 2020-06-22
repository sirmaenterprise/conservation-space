package com.sirma.sep.model.management;

import org.junit.Test;

/**
 * @author Boyan Tonchev.
 */
public class ModelTransitionsDeployTest extends BaseModelDeploymentTest {

	@Test
	public void shouldProperlyDeployTransitionGroups() {
		withDefinitions("entity.xml", "genericProject.xml", "genericTestProject.xml", "PR0001.xml");
		// GIVEN:
		// We have loaded project definition with id "PR0001". It has
		// A group with id "exportTab" it is not inherited from parent definition.
		// A group with id "tools" it is inherited from parent.

		// WHEN:
		// 1. Update attribute "order" of group "exportTab".
		// 2. Update an attribute of group "tools".
		deployChanges("deploy-transition-group-changes.json");

		// THEN:
		// 1. The attribute "order" of group "exportTab" is deployed.
		// 2. The group "tools" have to be present with attribute order.
		verifyImportedModel("PR0001.xml", "PR0001_transition_group_changed.xml");
	}

	@Test
	public void shouldRestoreGroupAttribute() {
		withDefinitions("entity.xml", "genericProject.xml", "genericTestProject.xml", "PR0001.xml");
		// GIVEN:
		// We have loaded project definition with id "PR0001". It has
		// A group with id "exportTab" it is not inherited from parent definition.
		// A group with id "tools" it is inherited from parent.

		// WHEN:
		// 1. Update attribute "order" of group "exportTab".
		// 2. Update an attribute of group "tools".
		// 3. Restore inherited "order" attribute of croup "tools".
		deployChanges("deploy-transition-group-changes.json", "restore-transition-group-changes.json");

		// THEN:
		// 1. The attribute "order" of group "exportTab" is deployed.
		// 2. The group "tools" have not be present.
		verifyImportedModel("PR0001.xml", "PR0001_transition_group_restored.xml");
	}

	@Test
	public void shouldProperlyDeployTransitions() {
		withDefinitions("entity.xml", "genericProject.xml", "genericTestProject.xml", "PR0001.xml");
		// GIVEN:
		// We have loaded project definition with id "PR0001". It has
		// A transition with id "createSurvey" it is not inherited from parent definition.
		// A transition with id "clone" it is inherited from parent.

		// WHEN:
		// 1. Update attribute "order" of transition "createSurvey".
		// 2. Update an attribute of transition "clone".
		deployChanges("deploy-transitions-changes.json");

		// THEN:
		// 1. The attribute "order" of transition "createSurvey" is deployed.
		// 2. The transition "clone" have to be present with attribute order.
		verifyImportedModel("PR0001.xml", "PR0001_transitions_changed.xml");
	}

	@Test
	public void shouldRestoreTransitionAttribute() {
		withDefinitions("entity.xml", "genericProject.xml", "genericTestProject.xml", "PR0001.xml");
		// GIVEN:
		// We have loaded project definition with id "PR0001". It has
		// A transition with id "createSurvey" it is not inherited from parent definition.
		// A transition with id "clone" it is inherited from parent.

		// WHEN:
		// 1. Update attribute "order" of transition "createSurvey".
		// 2. Update an attribute of transition "clone".
		// 3. Restore inherited "order" attribute of transition "clone".
		deployChanges("deploy-transitions-changes.json", "restore-transitions-changes.json");

		// THEN:
		// 1. The attribute "order" of transition "createSurvey" is deployed.
		// 2. The transition "clone" have not be present.
		verifyImportedModel("PR0001.xml", "PR0001_transitions_restored.xml");
	}

	@Test
	public void shouldProperlyDeployTransitionFields() {
		withDefinitions("entity.xml", "genericProject.xml", "genericTestProject.xml", "PR0001.xml");
		// GIVEN:
		// We have loaded project definition with id "PR0001". It has
		// A transition with id "create" and field with id "sendCreateMail" which is not inherited from parent definition.

		// WHEN:
		// 1. Update attribute "phase" of field "sendCreateMail".
		deployChanges("deploy-transition-fields-changes.json");

		// THEN:
		// 1. The attribute "phase" of field "sendCreateMail" have to be deployed.
		verifyImportedModel("PR0001.xml", "PR0001_transition_fields_changed.xml");
	}
}
