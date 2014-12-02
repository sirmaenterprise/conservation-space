package com.sirma.cmf.web.caseinstance;

import java.io.Serializable;
import java.util.Map;

import javax.faces.component.UIComponent;
import javax.inject.Inject;
import javax.inject.Named;

import org.apache.myfaces.extensions.cdi.core.api.scope.conversation.ViewAccessScoped;

import com.sirma.cmf.web.constants.CMFConstants;
import com.sirma.cmf.web.constants.NavigationConstants;
import com.sirma.cmf.web.document.StandardDocumentEditor;
import com.sirma.cmf.web.document.editor.DocumentEditor;
import com.sirma.cmf.web.form.FormViewMode;
import com.sirma.cmf.web.instance.landingpage.InstanceLandingPage;
import com.sirma.itt.cmf.beans.definitions.CaseDefinition;
import com.sirma.itt.cmf.beans.definitions.DocumentDefinitionRef;
import com.sirma.itt.cmf.beans.model.CaseInstance;
import com.sirma.itt.cmf.beans.model.DocumentInstance;
import com.sirma.itt.cmf.beans.model.SectionInstance;
import com.sirma.itt.cmf.constants.DocumentProperties;
import com.sirma.itt.cmf.constants.allowed_action.ActionTypeConstants;
import com.sirma.itt.cmf.domain.ObjectTypesCmf;
import com.sirma.itt.cmf.event.cases.CaseOpenEvent;
import com.sirma.itt.commons.utils.string.StringUtils;
import com.sirma.itt.emf.configuration.RuntimeConfiguration;
import com.sirma.itt.emf.configuration.RuntimeConfigurationProperties;
import com.sirma.itt.emf.instance.InstanceUtil;
import com.sirma.itt.emf.instance.dao.InstanceService;
import com.sirma.itt.emf.instance.model.Instance;
import com.sirma.itt.emf.instance.model.InstanceReference;
import com.sirma.itt.emf.plugin.ExtensionPoint;
import com.sirma.itt.emf.state.operation.Operation;

/**
 * The Class CaseLandingPage is responsible for case landing page initialization.
 * 
 * @author svelikov
 */
@Named
@ViewAccessScoped
public class CaseLandingPage extends InstanceLandingPage<CaseInstance, CaseDefinition> implements
		Serializable {

	private static final long serialVersionUID = -2151826034436664297L;

	/** All registered document editor plugins. */
	@Inject
	@ExtensionPoint("documentEditor")
	private Iterable<DocumentEditor> documentEditors;

	/** The structured document editor. */
	private DocumentEditor structuredDocumentEditor;

	@Override
	public void itemSelectedAction() {
		log.debug("CMFWeb: CaseLandingPage.itemSelectedAction selected definition: ["
				+ getSelectedType() + "]");

		if (StringUtils.isNullOrEmpty(getSelectedType())) {
			return;
		}

		// clear the structuredDocumentPath on case selection

		// - get selected case type and create the case
		CaseDefinition selectedCaseDefinition = dictionaryService.getDefinition(
				getInstanceDefinitionClass(), getSelectedType());

		Instance contextInstance = getDocumentContext().getContextInstance();

		CaseInstance newCaseInstance = getNewInstance(selectedCaseDefinition, contextInstance);

		// - get documents from the definition
		// - find structured documents and check if one has attribute purpose =
		// INIT
		DocumentInstance initiatingDocumentInstance = getInitiatingDocument(newCaseInstance);

		// - if there is one, then fire an event that client application should
		// init a structured document for editing
		if (initiatingDocumentInstance != null) {
			log.debug("CMFWeb: Initiating structured document is found.");

			fireInstanceOpenEvent(initiatingDocumentInstance);

			clearFormPanel(getPanel(CMFConstants.INSTANCE_DATA_PANEL));

			Map<String, Serializable> properties = initiatingDocumentInstance.getProperties();

			// - retrieve the document type
			final String documentType = (String) properties.get(DocumentProperties.TYPE);

			// don't handle document if document type is not set
			if (!StringUtils.isNullOrEmpty(documentType)) {

				// find out the editor that can handle the initiating document instance
				for (DocumentEditor current : documentEditors) {
					if (current.canHandle(initiatingDocumentInstance)) {
						structuredDocumentEditor = current;
						structuredDocumentEditor.handle(initiatingDocumentInstance, false);

						// set the document instance and definition in context
						DocumentDefinitionRef documentDefinition = (DocumentDefinitionRef) dictionaryService
								.getInstanceDefinition(initiatingDocumentInstance);
						getDocumentContext().populateContext(initiatingDocumentInstance,
								DocumentDefinitionRef.class, documentDefinition);

						// NOTE: this is workaround that allows a case with initiating structured
						// document to be created in CMF
						if (structuredDocumentEditor instanceof StandardDocumentEditor) {
							structuredDocumentEditor = null;
							renderFields(selectedCaseDefinition, newCaseInstance);
						}
						break;
					}
				}
			}
		} else {
			// - otherwise read case instance properties and render the form
			// with the form builder
			fireInstanceOpenEvent(newCaseInstance);
			renderFields(selectedCaseDefinition, newCaseInstance);
		}

		// set the case instance and definition in context
		getDocumentContext().populateContext(newCaseInstance, getInstanceDefinitionClass(),
				selectedCaseDefinition);
	}

	/**
	 * Render the case fields.
	 * 
	 * @param selectedCaseDefinition
	 *            the selected case definition
	 * @param newCaseInstance
	 *            the new case instance
	 */
	protected void renderFields(CaseDefinition selectedCaseDefinition, CaseInstance newCaseInstance) {
		UIComponent panel = getPanel(CMFConstants.INSTANCE_DATA_PANEL);
		if ((panel != null) && (panel.getChildCount() > 0)) {
			panel.getChildren().clear();
		}

		invokeReader(selectedCaseDefinition, newCaseInstance, panel, FormViewMode.EDIT, null);
	}

	@Override
	protected Class<CaseDefinition> getInstanceDefinitionClass() {
		return CaseDefinition.class;
	}

	@Override
	protected CaseInstance getNewInstance(CaseDefinition selectedDefinition, Instance context) {
		return caseInstanceService.createInstance(selectedDefinition, context, new Operation(
				ActionTypeConstants.CREATE_CASE));
	}

	@Override
	protected Class<CaseInstance> getInstanceClass() {
		return CaseInstance.class;
	}

	@Override
	protected InstanceReference getParentReference() {
		return null;
	}

	@Override
	protected String saveInstance(CaseInstance instance) {
		boolean isPersisted = InstanceUtil.isPersisted(instance);
		try {
			// REVIEW: the code logic should be change so these hacks to be obsolete
			if (isPersisted) {
				RuntimeConfiguration.setConfiguration(
						RuntimeConfigurationProperties.DO_NOT_SAVE_CHILDREN, Boolean.TRUE);
			}
			caseInstanceService.save(instance, createOperation());
		} finally {
			if (isPersisted) {
				RuntimeConfiguration
						.clearConfiguration(RuntimeConfigurationProperties.DO_NOT_SAVE_CHILDREN);
			}
		}
		getDocumentContext().addInstance(instance);
		getDocumentContext().setCurrentInstance(instance);
		initializeRoot(instance);

		eventService.fire(new CaseOpenEvent(instance));

		return NavigationConstants.NAVIGATE_TAB_CASE_DETAILS;
	}

	@Override
	protected String cancelEditInstance(CaseInstance instance) {
		reloadCaseInstance();
		return NavigationConstants.BACKWARD;
	}

	@Override
	protected void onExistingInstanceInitPage(CaseInstance instance) {
		// Auto-generated method stub

	}

	@Override
	protected void onNewInstanceInitPage(CaseInstance instance) {
		// Auto-generated method stub

	}

	@Override
	protected FormViewMode getFormViewModeExternal(CaseInstance instance) {
		// Auto-generated method stub
		return null;
	}

	@Override
	protected String getNavigationString() {
		return NavigationConstants.NAVIGATE_TAB_CASE_DETAILS;
	}

	/**
	 * Returns the initiating document if any. Otherwise returns null.
	 * 
	 * @param caseInstance
	 *            The case instance.
	 * @return Initiating document if any or null.
	 */
	private DocumentInstance getInitiatingDocument(CaseInstance caseInstance) {
		DocumentInstance initiatingDocument = null;

		for (SectionInstance sectionInstance : caseInstance.getSections()) {
			for (Instance instance : sectionInstance.getContent()) {
				if (!(instance instanceof DocumentInstance)) {
					continue;
				}
				DocumentInstance documentInstance = (DocumentInstance) instance;
				boolean isInit = CMFConstants.DOCUMENT_PURPOSE_INIT.equals(documentInstance
						.getPurpose());
				Boolean isStructured = Boolean.FALSE;
				if (documentInstance.getStructured() != null) {
					isStructured = documentInstance.getStructured();
				}
				if ((isStructured.compareTo(Boolean.TRUE) == 0) && isInit) {
					initiatingDocument = documentInstance;
					break;
				}
			}
		}
		return initiatingDocument;
	}

	@Override
	protected String getDefinitionFilterType() {
		return ObjectTypesCmf.CASE;
	}

	@Override
	protected InstanceService<CaseInstance, CaseDefinition> getInstanceService() {
		return caseInstanceService;
	}

	/**
	 * Getter method for structuredDocumentEditor.
	 * 
	 * @return the structuredDocumentEditor
	 */
	public DocumentEditor getStructuredDocumentEditor() {
		return structuredDocumentEditor;
	}

	/**
	 * Setter method for structuredDocumentEditor.
	 * 
	 * @param documentEditor
	 *            the structuredDocumentEditor to set
	 */
	public void setStructuredDocumentEditor(DocumentEditor documentEditor) {
		this.structuredDocumentEditor = documentEditor;
	}

}
