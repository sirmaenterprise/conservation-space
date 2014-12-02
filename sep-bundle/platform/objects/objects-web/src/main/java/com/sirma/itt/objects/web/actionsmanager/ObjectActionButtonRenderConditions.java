package com.sirma.itt.objects.web.actionsmanager;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Named;

import com.sirma.cmf.web.actionsmanager.ActionButtonRenderConditions;
import com.sirma.itt.cmf.constants.allowed_action.ActionTypeConstants;
import com.sirma.itt.emf.instance.model.Instance;
import com.sirma.itt.emf.security.model.Action;
import com.sirma.itt.objects.security.ObjectActionTypeConstants;

/**
 * Render condition methods. Operation buttons that perform more specific tasks or has different
 * implementation in its templates should be added to noDefaultButtons set.
 * 
 * @author svelikov
 */
@Named
@ApplicationScoped
public class ObjectActionButtonRenderConditions extends ActionButtonRenderConditions {

	/** The Constant serialVersionUID. */
	private static final long serialVersionUID = 8764235021793660642L;

	/**
	 * Should render move same case operation button.
	 * 
	 * @param action
	 *            current action
	 * @param instance
	 *            current instance
	 * @return true, if successful
	 */
	public boolean renderObjectMoveSameCase(Action action, Instance instance) {
		return ObjectActionTypeConstants.OBJECT_MOVE_SAME_CASE.equals(action.getActionId())
				&& isNonDefaultAction(action, instance);
	}

	/**
	 * Should render clone object operation button.
	 * 
	 * @param action
	 *            current action
	 * @param instance
	 *            current instance
	 * @return true, if successful
	 */
	public boolean renderCloneObjectButton(Action action, Instance instance) {
		return ActionTypeConstants.CLONE.equals(action.getActionId())
				&& isNonDefaultAction(action, instance);
	}

	/**
	 * Should render add thumbnail operation button.
	 * 
	 * @param action
	 *            current action
	 * @param instance
	 *            current instance
	 * @return true, if successful
	 */
	public boolean renderAddObjectThumbnail(Action action, Instance instance) {
		return ObjectActionTypeConstants.ADD_THUMBNAIL.equals(action.getActionId())
				&& isNonDefaultAction(action, instance);
	}

	/**
	 * Should render add primary image operation button.
	 * 
	 * @param action
	 *            current action
	 * @param instance
	 *            current instance
	 * @return true, if successful
	 */
	public boolean renderAddPrimaryImage(Action action, Instance instance) {
		return ObjectActionTypeConstants.ADD_PRIMARY_IMAGE.equals(action.getActionId())
				&& isNonDefaultAction(action, instance);
	}

	/**
	 * Render create object.
	 * 
	 * @param action
	 *            the action
	 * @param instance
	 *            the instance
	 * @return true, if successful
	 */
	public boolean renderCreateObject(Action action, Instance instance) {
		return ObjectActionTypeConstants.CREATE_OBJECT.equals(action.getActionId())
				&& isNonDefaultAction(action, instance);
	}

	/**
	 * Should render attach object operation button.
	 * 
	 * @param action
	 *            current action
	 * @param instance
	 *            current instance
	 * @return true, if successful
	 */
	public boolean renderAttachObject(Action action, Instance instance) {
		return ObjectActionTypeConstants.ATTACH_OBJECT.equals(action.getActionId())
				&& isNonDefaultAction(action, instance);
	}

	/**
	 * Should render document upload action button.
	 * 
	 * @param action
	 *            current action
	 * @param instance
	 *            current instance
	 * @return true, if successful
	 */
	public boolean renderObjectUploadButton(Action action, Instance instance) {
		return ActionTypeConstants.UPLOAD_IN_OBJECT.equals(action.getActionId())
				&& isNonDefaultAction(action, instance);
	}

	/**
	 * Should render save object as public template action button.
	 * 
	 * @param action
	 *            current action
	 * @param instance
	 *            current instance
	 * @return true, if successful
	 */
	public boolean renderSaveObjectAsPublicTemplateButton(Action action, Instance instance) {
		return ActionTypeConstants.SAVE_AS_TEMPLATE.equals(action.getActionId())
				&& isNonDefaultAction(action, instance);
	}
}
