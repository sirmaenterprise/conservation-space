package com.sirma.itt.objects.security;

import static com.sirma.itt.emf.security.SecurityModel.BaseRoles.CREATOR;
import static com.sirma.itt.emf.security.SecurityModel.BaseRoles.VIEWER;

import java.io.Serializable;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import com.sirma.itt.cmf.beans.model.SectionInstance;
import com.sirma.itt.cmf.constants.allowed_action.ActionTypeConstants;
import com.sirma.itt.emf.converter.TypeConverter;
import com.sirma.itt.emf.domain.Pair;
import com.sirma.itt.emf.instance.InstanceUtil;
import com.sirma.itt.emf.instance.model.Instance;
import com.sirma.itt.emf.plugin.Extension;
import com.sirma.itt.emf.properties.DefaultProperties;
import com.sirma.itt.emf.resources.model.Resource;
import com.sirma.itt.emf.rest.model.ViewInstance;
import com.sirma.itt.emf.security.RoleEvaluator;
import com.sirma.itt.emf.security.RoleEvaluatorRuntimeSettings;
import com.sirma.itt.emf.security.RoleEvaluatorType;
import com.sirma.itt.emf.security.SecurityModel.BaseRoles;
import com.sirma.itt.emf.security.evaluator.BaseRoleEvaluator;
import com.sirma.itt.emf.security.model.Action;
import com.sirma.itt.emf.security.model.EmfAction;
import com.sirma.itt.emf.security.model.Role;
import com.sirma.itt.objects.domain.ObjectTypesObject;
import com.sirma.itt.objects.domain.model.ObjectInstance;

/**
 * The ObjectRoleEvaluator provides role evaluation and actions filtering .
 */
@ApplicationScoped
@RoleEvaluatorType(ObjectTypesObject.OBJECT)
@Extension(target = RoleEvaluator.TARGET_NAME, order = 90)
public class ObjectRoleEvaluator extends BaseRoleEvaluator<ObjectInstance> {

	/** The Constant SUPPORTED. */
	private static final List<Class<?>> SUPPORTED = Arrays
			.asList(new Class<?>[] { ObjectInstance.class });

	/**
	 * Some predefined action constants to be used when filtering actions
	 */
	/** The Constant EDIT_DETAILS. */
	protected static final Action EDIT_DETAILS = new EmfAction(ActionTypeConstants.EDIT_DETAILS);
	/** The Constant DELETE. */
	protected static final Action DELETE = new EmfAction(ActionTypeConstants.DELETE);
	protected static final Action MOVE_TO_OTHER_SECTION = new EmfAction(
			ObjectActionTypeConstants.OBJECT_MOVE_SAME_CASE);
	protected static final Action LOCK = new EmfAction(ActionTypeConstants.LOCK);
	protected static final Action UNLOCK = new EmfAction(ActionTypeConstants.UNLOCK);
	protected static final Action CLONE = new EmfAction(ActionTypeConstants.CLONE);
	protected static final Action PRINT = new EmfAction(ActionTypeConstants.PRINT);
	protected static final Action EXPORT = new EmfAction(ActionTypeConstants.EXPORT);
	protected static final Action UPLOAD = new EmfAction(ActionTypeConstants.UPLOAD_IN_OBJECT);
	protected static final Action SAVE_AS_TEMPLATE = new EmfAction(
			ObjectActionTypeConstants.SAVE_AS_TEMPLATE);

	/** The Constant INACTIVE_TARGET_ALLOWED_ACTIONS. */
	private static final Set<Action> INACTIVE_TARGET_ALLOWED_ACTIONS = new HashSet<Action>(
			Arrays.asList(CLONE, PRINT, EXPORT, UPLOAD, SAVE_AS_TEMPLATE));

	@Inject
	private TypeConverter typeConverter;

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected String getContainer(ObjectInstance target) {
		return target.getContainer();
	}

	/**
	 * {@inheritDoc}
	 */
	protected Class<ObjectInstance> allowedClass() {
		return ObjectInstance.class;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public List<Class<?>> getSupportedObjects() {
		return SUPPORTED;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected Pair<Role, RoleEvaluator<ObjectInstance>> evaluateInternal(ObjectInstance target,
			Resource user, final RoleEvaluatorRuntimeSettings settings) {
		// if object is deleted we cannot do anything
		if (isDeleted(target)) {
			return constructRoleModel(VIEWER);
		}

		// if he owns the object
		Map<String, Serializable> properties = target.getProperties();
		if (resourceService.areEqual(user, properties.get(DefaultProperties.CREATED_BY))) {
			if (isRoleIrrelevant(settings, CREATOR)) {
				return constructRoleModel(CREATOR);
			}
		}
		Instance context = InstanceUtil.getContext(target, true);
		if (isDeleted(context)) {
			return constructRoleModel(VIEWER);
		}
		if (isInInactiveState(context)) {
			return constructRoleModel(BaseRoles.CONSUMER);
		}
		return constructRoleModel(context, user, BaseRoles.CONSUMER, chainRuntimeSettings);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected Boolean filterInternal(ObjectInstance target, Resource resource, Role role,
			Set<Action> actions) {
		// if the context is inactive state the object viewed via that context does not have any
		// actions
		Instance context = InstanceUtil.getContext(target, true);
		if (isInInactiveState(context)) {
			// keep only the actions that are applicable for the target state
			actions.retainAll(INACTIVE_TARGET_ALLOWED_ACTIONS);
		}
		Instance instance = InstanceUtil.getDirectParent(target, true);
		if (!(instance instanceof SectionInstance)) {
			actions.remove(MOVE_TO_OTHER_SECTION);
		}
		ViewInstance viewInstance = typeConverter.convert(ViewInstance.class, target);
		if ((viewInstance != null) && viewInstance.isLocked()) {
			// if locked by the current user we should allow them to edit the instance if left
			// locked from before
			if (!resourceService.areEqual(viewInstance.getLockedBy(), resource)) {
				actions.remove(EDIT_DETAILS);
			}
			actions.remove(DELETE);
			actions.remove(MOVE_TO_OTHER_SECTION);
			actions.remove(LOCK);
		} else {
			actions.remove(UNLOCK);
		}
		return Boolean.FALSE;
	}
}
