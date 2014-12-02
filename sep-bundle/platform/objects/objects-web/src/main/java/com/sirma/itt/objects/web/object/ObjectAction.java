package com.sirma.itt.objects.web.object;

import java.io.Serializable;

import javax.enterprise.event.Observes;
import javax.inject.Named;

import org.apache.myfaces.extensions.cdi.core.api.scope.conversation.ViewAccessScoped;
import org.json.JSONObject;

import com.sirma.cmf.web.EntityAction;
import com.sirma.itt.cmf.beans.model.CaseInstance;
import com.sirma.itt.cmf.beans.model.SectionInstance;
import com.sirma.itt.commons.utils.string.StringUtils;
import com.sirma.itt.emf.instance.InstanceUtil;
import com.sirma.itt.emf.instance.model.Instance;
import com.sirma.itt.emf.security.action.EMFAction;
import com.sirma.itt.emf.state.operation.Operation;
import com.sirma.itt.emf.util.JsonUtil;
import com.sirma.itt.emf.web.action.event.EMFActionEvent;
import com.sirma.itt.objects.domain.model.ObjectInstance;
import com.sirma.itt.objects.security.ObjectActionTypeConstants;
import com.sirma.itt.objects.web.constants.ObjectNavigationConstants;

/**
 * The Class ObjectAction handles object actions and observers.
 * 
 * @author svelikov
 */
@Named
@ViewAccessScoped
public class ObjectAction extends EntityAction implements Serializable {

	private static final long serialVersionUID = 4107319526384382508L;

	/**
	 * Data in JSON format passed when createObject action is executed. This contains the selected
	 * section id and context case instance type and id.
	 */
	private String createObjectData;

	/**
	 * Creates the object action called by an action link when we create an object for a case.
	 * 
	 * @return navigation string
	 */
	public String createObjectAction() {
		log.debug("Executing ObjectAction.attachObject: " + createObjectData);
		if (StringUtils.isNullOrEmpty(createObjectData)) {
			log.error("Can't create object because of missing required arguments!");
			return null;
		}
		JSONObject data = JsonUtil.createObjectFromString(createObjectData);
		String selectedSectionId = JsonUtil.getStringValue(data, "selectedSectionId");
		if (StringUtils.isNullOrEmpty(selectedSectionId)) {
			log.error("Can't create object because of missing required case section id!");
			return null;
		}
		// add selected section in context
		Instance sectionInstance = fetchInstance(selectedSectionId, SectionInstance.class
				.getSimpleName().toLowerCase());
		getDocumentContext().addInstance(sectionInstance);
		// if context is a section, then add it as context
		getDocumentContext().addContextInstance(sectionInstance);

		// add root instance for the root instance header
		Instance rootInstance = InstanceUtil.getRootInstance(sectionInstance, true);
		getDocumentContext().setRootInstance(rootInstance);
		getDocumentContext().addInstance(rootInstance);

		return ObjectNavigationConstants.OBJECT;
	}

	/**
	 * Attach object observer.
	 * 
	 * @param event
	 *            the event
	 */
	public void attachObject(
			@Observes @EMFAction(value = ObjectActionTypeConstants.ATTACH_OBJECT, target = SectionInstance.class) final EMFActionEvent event) {
		log.debug("Executing observer ObjectAction.attachObject");

		getDocumentContext().addInstance(event.getInstance());
	}

	/**
	 * Clone object observer.
	 * 
	 * @param event
	 *            the event
	 */
	public void cloneObject(
			@Observes @EMFAction(value = ObjectActionTypeConstants.CLONE, target = ObjectInstance.class) final EMFActionEvent event) {
		log.debug("Executing observer ObjectAction.clone");

		getDocumentContext().addInstance(event.getInstance());
	}

	/**
	 * Save as public template observer.
	 * 
	 * @param event
	 *            current event
	 */
	public void objectSaveAsPublicTemplate(
			@Observes @EMFAction(value = ObjectActionTypeConstants.SAVE_AS_TEMPLATE, target = ObjectInstance.class) final EMFActionEvent event) {
		log.debug("Executing observer ObjectAction.objectSaveAsPublicTemplate");
		Instance objectInstance = event.getInstance();
		if (objectInstance != null) {
			getDocumentContext().addInstance(objectInstance);
			getDocumentContext().addContextInstance(objectInstance);
			event.setNavigation(ObjectNavigationConstants.OBJECT);
		}
	}

	/**
	 * Edit object observer.
	 * 
	 * @param event
	 *            the event
	 */
	public void editObject(
			@Observes @EMFAction(value = ObjectActionTypeConstants.EDIT_DETAILS, target = ObjectInstance.class) final EMFActionEvent event) {
		log.debug("Executing observer ObjectAction.editObject");

		Instance objectInstance = event.getInstance();
		if (objectInstance != null) {
			getDocumentContext().addInstance(objectInstance);
			getDocumentContext().addContextInstance(objectInstance);
			event.setNavigation(ObjectNavigationConstants.OBJECT);
		}
	}

	/**
	 * Creates the case objects section.
	 * 
	 * @param event
	 *            the event
	 */
	public void createCaseObjectsSection(
			@Observes @EMFAction(value = ObjectActionTypeConstants.CREATE_OBJECTS_SECTION, target = CaseInstance.class) final EMFActionEvent event) {
		log.debug("Executing observer ObjectAction.createCaseDocumentsSection");
	}

	/**
	 * Delete object observer.
	 * 
	 * @param event
	 *            the event
	 */
	public void deleteObject(
			@Observes @EMFAction(value = ObjectActionTypeConstants.DELETE, target = ObjectInstance.class) final EMFActionEvent event) {
		log.debug("Executing observer ObjectAction.delete");

		ObjectInstance objectInstance = (ObjectInstance) event.getInstance();
		String currentOperation = getDocumentContext().getCurrentOperation(
				ObjectInstance.class.getSimpleName());
		if (objectInstance != null) {
			Operation operation = new Operation(currentOperation);
			instanceService.delete(objectInstance, operation, false);
		}
	}

	/**
	 * Getter method for createObjectData.
	 * 
	 * @return the createObjectData
	 */
	public String getCreateObjectData() {
		return createObjectData;
	}

	/**
	 * Setter method for createObjectData.
	 * 
	 * @param createObjectData
	 *            the createObjectData to set
	 */
	public void setCreateObjectData(String createObjectData) {
		this.createObjectData = createObjectData;
	}

}
