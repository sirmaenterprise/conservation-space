package com.sirma.cmf.web.instance;

import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertTrue;

import java.io.Serializable;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.testng.annotations.Test;

import com.sirma.cmf.CMFTest;
import com.sirma.cmf.web.DocumentContext;
import com.sirma.cmf.web.form.FormViewMode;
import com.sirma.itt.cmf.beans.model.CaseInstance;
import com.sirma.itt.emf.instance.model.Instance;
import com.sirma.itt.emf.security.model.EmfAction;

/**
 * Test for InstanceLandingPage class.
 * 
 * @author svelikov
 */
@Test
public class InstanceLandingPageTest extends CMFTest {

	private final InstanceLandingPageMock instanceLandingPage;
	protected Set<String> requiredFields;

	/**
	 * Instantiates a new instance landing page test.
	 */
	public InstanceLandingPageTest() {
		instanceLandingPage = new InstanceLandingPageMock() {

			private DocumentContext docContext = new DocumentContext();

			@Override
			public DocumentContext getDocumentContext() {
				return docContext;
			}

			@Override
			public void setDocumentContext(DocumentContext documentContext) {
				docContext = documentContext;
			}

			@Override
			public boolean isPersisted(CaseInstance instance) {
				return instance.getId() != null;
			}

			@Override
			public Set<String> getRequiredFieldsByDefinition(Instance instance, String operation) {
				return requiredFields;
			}

			@Override
			protected Class<CaseInstance> getInstanceClass() {
				return CaseInstance.class;
			}
		};

		requiredFields = new HashSet<String>();
	}

	/**
	 * Checks if is edit mode test.
	 */
	public void isEditModeTest() {
		// We start by setting the view mode to be EDIT and expect by default to be returned EDIT in
		// any case where there are not required fields or there is not completed required fields or
		// there is no selected action or the selected action is not immediate

		CaseInstance caseInstance = createCaseInstance(Long.valueOf(1l));
		Map<String, Serializable> properties = new HashMap<String, Serializable>();
		caseInstance.setProperties(properties);
		instanceLandingPage.getDocumentContext().setFormMode(FormViewMode.EDIT);

		// the instance is not persisted -> edit
		caseInstance.setId(null);
		boolean editMode = instanceLandingPage.isEditMode(caseInstance);
		assertTrue(editMode);

		// instance is persisted, there are no required fields -> edit
		caseInstance.setId("1");
		editMode = instanceLandingPage.isEditMode(caseInstance);
		assertTrue(editMode);

		// instance is persisted, there are required fields but all are completed -> edit
		requiredFields.add("status");
		properties.put("status", "IN_PROGRESS");
		editMode = instanceLandingPage.isEditMode(caseInstance);
		assertTrue(editMode);

		// instance is persisted, there are required fields and some are not completed and no action
		// is found in context -> edit
		requiredFields.add("createdBy");
		editMode = instanceLandingPage.isEditMode(caseInstance);
		assertTrue(editMode);

		// instance is persisted, there are required fields and some are not completed and action
		// is found in context but is not immediate -> edit
		EmfAction action = new EmfAction("suspend");
		instanceLandingPage.getDocumentContext().setSelectedAction(action);
		editMode = instanceLandingPage.isEditMode(caseInstance);
		assertTrue(editMode);

		// instance is persisted, there are required fields and some are not completed and an
		// immediate action is found in context -> edit
		action.setImmediate(true);
		instanceLandingPage.getDocumentContext().setSelectedAction(action);
		editMode = instanceLandingPage.isEditMode(caseInstance);
		assertTrue(editMode);

		// instance is persisted, there are required fields and some are not completed and action
		// is found in context but is not immediate -> preview
		properties.put("createdBy", "admin");
		instanceLandingPage.getDocumentContext().setSelectedAction(action);
		editMode = instanceLandingPage.isEditMode(caseInstance);
		assertFalse(editMode);

	}

}
