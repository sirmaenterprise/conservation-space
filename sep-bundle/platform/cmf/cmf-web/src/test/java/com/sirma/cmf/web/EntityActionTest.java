package com.sirma.cmf.web;

import static org.testng.Assert.assertEquals;

import java.io.Serializable;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;

import org.junit.Assert;
import org.mockito.Mockito;
import org.testng.annotations.Test;

import com.sirma.cmf.CMFTest;
import com.sirma.itt.cmf.beans.definitions.impl.CaseDefinitionImpl;
import com.sirma.itt.cmf.beans.model.CaseInstance;
import com.sirma.itt.cmf.constants.CaseProperties;
import com.sirma.itt.commons.utils.reflection.ReflectionUtils;
import com.sirma.itt.emf.definition.model.FieldDefinitionImpl;
import com.sirma.itt.emf.definition.model.PropertyDefinition;
import com.sirma.itt.emf.domain.DisplayType;
import com.sirma.itt.emf.instance.dao.InstanceService;

/**
 * Tests for EntityAction class.
 * 
 * @author svelikov
 */
@Test
public class EntityActionTest extends CMFTest {

	private final EntityAction action;

	private final DocumentContext documentContext;

	private final InstanceService instanceService;

	/**
	 * Constructor initializes the class under test.
	 */
	public EntityActionTest() {
		documentContext = new DocumentContext();
		action = new EntityAction() {

		};

		instanceService = Mockito.mock(InstanceService.class);

		ReflectionUtils.setField(action, "log", LOG);
		ReflectionUtils.setField(action, "instanceService", instanceService);
	}

	/**
	 * Test for method that retrieves the CaseInstance status property.
	 */
	public void getCaseStatusTest() {
		CaseInstance caseInstance = new CaseInstance();
		caseInstance.setProperties(new LinkedHashMap<String, Serializable>());

		caseInstance.getProperties().put(CaseProperties.STATUS, "Отворена");
		documentContext.addInstance(caseInstance);
		action.setDocumentContext(documentContext);

		String status = action.getCaseStatus();
		assertEquals("Отворена", status);
	}

	/**
	 * Checks for editable fields test.
	 */
	public void hasEditableFieldsTest() {

		CaseDefinitionImpl definitionModel = new CaseDefinitionImpl();
		List<PropertyDefinition> fields = new LinkedList<PropertyDefinition>();
		definitionModel.setFields(fields);

		fields.add(createField(DisplayType.READ_ONLY));
		fields.add(createField(DisplayType.HIDDEN));
		fields.add(createField(DisplayType.SYSTEM));

		// when no editable fields exists in definition, then method should
		// return false
		Assert.assertFalse(action.hasEditableFields(definitionModel));

		// when there is at least one editable field in definition, then method
		// should return true
		fields.add(createField(DisplayType.EDITABLE));
		Assert.assertTrue(action.hasEditableFields(definitionModel));
	}

	/**
	 * Refresh instance test.
	 */
	@SuppressWarnings("unchecked")
	public void refreshInstanceTest() {
		action.refreshInstance(null);
		Mockito.verify(instanceService, Mockito.never()).refresh(null);

		//
		CaseInstance caseInstance = createCaseInstance(null);
		caseInstance.setId(null);
		action.refreshInstance(caseInstance);
		Mockito.verify(instanceService, Mockito.never()).refresh(caseInstance);

		//
		caseInstance.setId(Long.valueOf(1L));
		action.refreshInstance(caseInstance);
		Mockito.verify(instanceService, Mockito.atLeastOnce()).refresh(caseInstance);
	}

	/**
	 * Creates the field.
	 * 
	 * @param displayType
	 *            the display type
	 * @return the field definition impl
	 */
	private FieldDefinitionImpl createField(DisplayType displayType) {
		FieldDefinitionImpl field = new FieldDefinitionImpl();
		field.setDisplayType(displayType);
		return field;
	}

}
