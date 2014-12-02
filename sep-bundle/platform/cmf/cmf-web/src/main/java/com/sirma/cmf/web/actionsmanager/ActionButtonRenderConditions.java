package com.sirma.cmf.web.actionsmanager;

import java.io.Serializable;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.PostConstruct;
import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.inject.Named;

import com.sirma.itt.cmf.beans.model.CaseInstance;
import com.sirma.itt.cmf.beans.model.SectionInstance;
import com.sirma.itt.cmf.beans.model.WorkflowInstanceContext;
import com.sirma.itt.cmf.constants.allowed_action.ActionTypeConstants;
import com.sirma.itt.emf.instance.model.Instance;
import com.sirma.itt.emf.plugin.ExtensionPoint;
import com.sirma.itt.emf.security.model.Action;

/**
 * Render condition methods. Operation buttons that perform more specific tasks or has different
 * implementation in its templates should be added to nonDefAction set.
 * 
 * @author svelikov
 */
@Named
@ApplicationScoped
public class ActionButtonRenderConditions implements Serializable {

	private static final long serialVersionUID = 4653933549987160644L;

	@Inject
	@ExtensionPoint(value = NonDefaultActionButtonExtensionPoint.EXTENSION_POINT)
	private Iterable<NonDefaultActionButtonExtensionPoint> externalNonDefaultActionButtons;

	@SuppressWarnings("serial")
	protected Map<String, List<String>> nonDefAction = new HashMap<String, List<String>>() {
		{
			put(ActionTypeConstants.EDIT_OFFLINE, null);
			put(ActionTypeConstants.UPLOAD_NEW_VERSION, null);
			put(ActionTypeConstants.REASSIGN_TASK, null);
			put(ActionTypeConstants.COMPLETE, null);
			put(ActionTypeConstants.MOVE_SAME_CASE, null);
			put(ActionTypeConstants.SIGN, null);
			put(ActionTypeConstants.DOWNLOAD, null);
			put(ActionTypeConstants.UPLOAD, null);
			put(ActionTypeConstants.ATTACH_DOCUMENT, null);
			put(ActionTypeConstants.DETACH_DOCUMENT, null);
			put(ActionTypeConstants.CREATE_IDOC, null);
			// We don't add the project here nevertheless we can create object in projects, because
			// for the projects the action goes trough the default action button and is handled in
			// an observer in ProjectAction.
			put(ActionTypeConstants.CREATE_OBJECT,
					Arrays.asList(SectionInstance.class.getSimpleName(),
							CaseInstance.class.getSimpleName()));
			put(ActionTypeConstants.CREATE_OBJECTS_SECTION, null);
			put(ActionTypeConstants.CREATE_DOCUMENTS_SECTION, null);
			put(ActionTypeConstants.MOVE_OTHER_CASE, null);
			put(ActionTypeConstants.PRINT, null);
			put(ActionTypeConstants.EXPORT, null);
			put(ActionTypeConstants.CLONE, null);
			put(ActionTypeConstants.NO_PERMISSIONS, null);
			put(ActionTypeConstants.STOP,
					Arrays.asList(WorkflowInstanceContext.class.getSimpleName()));
		}
	};

	/**
	 * Initializes the bean by collecting non default action types that may be provided by other
	 * modules.
	 */
	@PostConstruct
	public void init() {
		for (NonDefaultActionButtonExtensionPoint extension : externalNonDefaultActionButtons) {
			nonDefAction.putAll(extension.getActions());
		}
	}

	/**
	 * Method that check action is non default or default button.
	 * 
	 * @param action
	 *            current action
	 * @param instance
	 *            current instance
	 * @return true, if successful
	 */
	public boolean isNonDefaultAction(Action action, Instance instance) {

		if (nonDefAction.containsKey(action.getActionId())) {
			String actionId = action.getActionId();

			if (nonDefAction.get(actionId) == null) {
				return true;
			}

			if (instance != null) {
				String instanceType = instance.getClass().getSimpleName();

				if (nonDefAction.get(actionId).contains(instanceType)) {
					return true;
				}
			}
		}
		return false;
	}

	/**
	 * Check if default button template should be rendered.
	 * 
	 * @param action
	 *            current action
	 * @param instance
	 *            current instance
	 * @return true, if successful
	 */
	public boolean shouldRenderDefaultButton(Action action, Instance instance) {
		return !isNonDefaultAction(action, instance);
	}

	/**
	 * Should render no permission message.
	 * 
	 * @param action
	 *            current action
	 * @return true, if successful
	 */
	public boolean renderNoPermissionMessage(Action action) {
		return ActionTypeConstants.NO_PERMISSIONS.equals(action.getActionId());
	}

	/**
	 * Should render cancel operation button.
	 * 
	 * @param action
	 *            current action
	 * @param instance
	 *            current instance
	 * @return true, if successful
	 */
	public boolean shouldRenderStopButton(Action action, Instance instance) {
		return ActionTypeConstants.STOP.equals(action.getActionId())
				&& isNonDefaultAction(action, instance);
	}

	/**
	 * Should render download operation button.
	 * 
	 * @param action
	 *            current action
	 * @param instance
	 *            current instance
	 * @return true, if successful
	 */
	public boolean shouldRenderDownloadButton(Action action, Instance instance) {
		return ActionTypeConstants.DOWNLOAD.equals(action.getActionId())
				&& isNonDefaultAction(action, instance);
	}

	/**
	 * Should render upload new version operation button.
	 * 
	 * @param action
	 *            current action
	 * @param instance
	 *            current instance
	 * @return true, if successful
	 */
	public boolean shouldUploadNewVersionButton(Action action, Instance instance) {
		return ActionTypeConstants.UPLOAD_NEW_VERSION.equals(action.getActionId())
				&& isNonDefaultAction(action, instance);
	}

	/**
	 * Should render edit off-line operation button.
	 * 
	 * @param action
	 *            current action
	 * @return true, if successful
	 */
	public boolean renderEditOfflineButton(Action action) {
		return ActionTypeConstants.EDIT_OFFLINE.equals(action.getActionId());
	}

	/**
	 * Should render download operation button.
	 * 
	 * @param action
	 *            current action
	 * @param instance
	 *            current instance
	 * @return true, if successful
	 */
	public boolean renderDownloadButton(Action action, Instance instance) {
		return ActionTypeConstants.DOWNLOAD.equals(action.getActionId())
				&& isNonDefaultAction(action, instance);
	}

	/**
	 * Should render create document section or create object section operations buttons.
	 * 
	 * @param action
	 *            current action
	 * @param instance
	 *            current instance
	 * @return true, if successful
	 */
	public boolean renderCreateSectionButton(Action action, Instance instance) {
		// FIXME: move method to objects module
		return ActionTypeConstants.CREATE_DOCUMENTS_SECTION.equals(action.getActionId())
				|| (ActionTypeConstants.CREATE_OBJECTS_SECTION.equals(action.getActionId()) && isNonDefaultAction(
						action, instance));
	}

	/**
	 * Should render sign operation button.
	 * 
	 * @param action
	 *            current action
	 * @param instance
	 *            current instance
	 * @return true, if successful
	 */
	public boolean shouldRenderSignButton(Action action, Instance instance) {
		return ActionTypeConstants.SIGN.equals(action.getActionId())
				&& isNonDefaultAction(action, instance);
	}

	/**
	 * Should render re-assign task operation button.
	 * 
	 * @param action
	 *            current action
	 * @param instance
	 *            current instance
	 * @return true, if successful
	 */
	public boolean shouldRenderTaskReassignButton(Action action, Instance instance) {
		return ActionTypeConstants.REASSIGN_TASK.equals(action.getActionId())
				&& isNonDefaultAction(action, instance);
	}

	/**
	 * Should render case complete operation button.
	 * 
	 * @param action
	 *            current action
	 * @param instance
	 *            current instance
	 * @return true, if successful
	 */
	public boolean shouldRenderCaseCloseButton(Action action, Instance instance) {
		return ActionTypeConstants.COMPLETE.equals(action.getActionId())
				&& isNonDefaultAction(action, instance);
	}

	/**
	 * Should render move same case operation button.
	 * 
	 * @param action
	 *            current action
	 * @param instance
	 *            current instance
	 * @return true, if successful
	 */
	public boolean shouldRenderDocumentMoveInOtherSectionButton(Action action, Instance instance) {
		return ActionTypeConstants.MOVE_SAME_CASE.equals(action.getActionId())
				&& isNonDefaultAction(action, instance);
	}

	/**
	 * Should render document move in other case operation button.
	 * 
	 * @param action
	 *            current action
	 * @param instance
	 *            current instance
	 * @return true, if successful
	 */
	public boolean shouldRenderDocumentMoveInOtherCaseButton(Action action, Instance instance) {
		return ActionTypeConstants.MOVE_OTHER_CASE.equals(action.getActionId())
				&& isNonDefaultAction(action, instance);
	}

	/**
	 * Should render create sub document operation button.
	 * 
	 * @param action
	 *            current action
	 * @param instance
	 *            current instance
	 * @return true, if successful
	 */
	public boolean renderCreateSubDocumentButton(Action action, Instance instance) {
		return ActionTypeConstants.CREATE_SUB_DOCUMENT.equals(action.getActionId())
				&& isNonDefaultAction(action, instance);
	}

	/**
	 * Should render save as public template operation button.
	 * 
	 * @param action
	 *            current action
	 * @param instance
	 *            current instance
	 * @return true, if successful
	 */
	public boolean renderSaveAsTemplateButton(Action action, Instance instance) {
		return ActionTypeConstants.SAVE_AS_PUBLIC_TEMPLATE.equals(action.getActionId())
				&& isNonDefaultAction(action, instance);
	}

	/**
	 * Should render print document operation button.
	 * 
	 * @param action
	 *            current action
	 * @param instance
	 *            current instance
	 * @return true, if successful
	 */
	public boolean renderPrintButton(Action action, Instance instance) {
		return ActionTypeConstants.PRINT.equals(action.getActionId())
				&& isNonDefaultAction(action, instance);
	}

	/**
	 * Should render export document operation button.
	 * 
	 * @param action
	 *            current action
	 * @param instance
	 *            current instance
	 * @return true, if successful
	 */
	public boolean renderExportDocumentButton(Action action, Instance instance) {
		return ActionTypeConstants.EXPORT.equals(action.getActionId())
				&& isNonDefaultAction(action, instance);
	}

	/**
	 * Should render upload document operation action.
	 * 
	 * @param action
	 *            current action
	 * @param instance
	 *            current instance
	 * @return true, if successful
	 */
	public boolean renderUploadButton(Action action, Instance instance) {
		return ActionTypeConstants.UPLOAD.equals(action.getActionId())
				&& isNonDefaultAction(action, instance);
	}

	/**
	 * Should render attach document operation button.
	 * 
	 * @param action
	 *            current action
	 * @param instance
	 *            current instance
	 * @return true, if successful
	 */
	public boolean renderAttachDocumentButton(Action action, Instance instance) {
		return ActionTypeConstants.ATTACH_DOCUMENT.equals(action.getActionId())
				&& isNonDefaultAction(action, instance);
	}

	/**
	 * Should render detach operation button.
	 * 
	 * @param action
	 *            current action
	 * @param instance
	 *            current instance
	 * @return true, if successful
	 */
	public boolean renderDetachDocumentButton(Action action, Instance instance) {
		return ActionTypeConstants.DETACH_DOCUMENT.equals(action.getActionId())
				&& isNonDefaultAction(action, instance);
	}

	/**
	 * Should render create idoc operation button.
	 * 
	 * @param action
	 *            current action
	 * @param instance
	 *            current instance
	 * @return true, if successful
	 */
	public boolean renderCreateDocumentButton(Action action, Instance instance) {
		return ActionTypeConstants.CREATE_IDOC.equals(action.getActionId())
				&& isNonDefaultAction(action, instance);
	}

	/**
	 * Getter for all non default actions.
	 * 
	 * @return map with actions
	 */
	public Map<String, List<String>> getNonDefAction() {
		return nonDefAction;
	}

	/**
	 * Setter for all non default actions.
	 * 
	 * @param nonDefAction
	 *            map with non default actions
	 */
	public void setNonDefAction(Map<String, List<String>> nonDefAction) {
		this.nonDefAction = nonDefAction;
	}

}
