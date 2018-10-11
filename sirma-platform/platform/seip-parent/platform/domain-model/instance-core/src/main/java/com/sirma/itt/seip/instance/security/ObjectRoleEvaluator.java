package com.sirma.itt.seip.instance.security;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import com.sirma.itt.seip.domain.ObjectTypes;
import com.sirma.itt.seip.domain.security.Action;
import com.sirma.itt.seip.domain.security.ActionTypeConstants;
import com.sirma.itt.seip.instance.ObjectInstance;
import com.sirma.itt.seip.instance.content.MimetypeConstants;
import com.sirma.itt.seip.instance.context.InstanceContextService;
import com.sirma.itt.seip.instance.revision.RevisionService;
import com.sirma.itt.seip.instance.version.InstanceVersionService;
import com.sirma.itt.seip.permissions.action.EmfAction;
import com.sirma.itt.seip.permissions.role.Role;
import com.sirma.itt.seip.permissions.role.RoleActionEvaluatorContext;
import com.sirma.itt.seip.permissions.role.RoleEvaluator;
import com.sirma.itt.seip.permissions.role.RoleEvaluatorType;
import com.sirma.itt.seip.plugin.Extension;
import com.sirma.itt.seip.resources.Resource;
import com.sirma.sep.content.Content;
import com.sirma.sep.content.InstanceContentService;

/**
 * The ObjectRoleEvaluator provides role evaluation and actions filtering .
 */
@ApplicationScoped
@RoleEvaluatorType(ObjectTypes.OBJECT)
@Extension(target = RoleEvaluator.TARGET_NAME, order = 90)
public class ObjectRoleEvaluator extends DomainObjectsBaseRoleEvaluator<ObjectInstance> {

	private static final List<Class> SUPPORTED = Collections.singletonList(ObjectInstance.class);

	/**
	 * Some predefined action constants to be used when filtering actions
	 */
	protected static final Action EDIT_DETAILS = new EmfAction(ActionTypeConstants.EDIT_DETAILS);
	protected static final Action DELETE = new EmfAction(ActionTypeConstants.DELETE);
	private static final Action UNLOCK = new EmfAction(ActionTypeConstants.UNLOCK);
	static final Action CLONE = new EmfAction(ActionTypeConstants.CLONE);
	static final Action PRINT = new EmfAction(ActionTypeConstants.PRINT);
	static final Action EXPORT = new EmfAction(ActionTypeConstants.EXPORT);
	private static final Action PUBLISH = new EmfAction(ActionTypeConstants.PUBLISH);
	private static final Action PUBLISH_AS_PDF = new EmfAction(ActionTypeConstants.PUBLISH_AS_PDF);
	private static final Action UPLOAD_REVISION = new EmfAction(ActionTypeConstants.UPLOAD_REVISION);
	private static final Action DETACH = new EmfAction(ActionTypeConstants.DETACH);
	private static final Action DOWNLOAD = new EmfAction(ActionTypeConstants.DOWNLOAD);
	private static final Action UPLOAD_NEW_VERSION = new EmfAction(ActionTypeConstants.UPLOAD_NEW_VERSION);
	private static final Action REVERT_VERSION = new EmfAction(InstanceVersionService.REVERT_VERSION_SERVER_OPERATION);
	private static final Action HISTORY_PREVIEW = new EmfAction(ActionTypeConstants.HISTORY_PREVIEW);
	private static final Action MANAGE_PERMISSIONS = new EmfAction(ActionTypeConstants.MANAGE_PERMISSIONS);
	private static final Action VIEW_DETAILS = new EmfAction(ActionTypeConstants.VIEW_DETAILS);
	private static final Action PRINT_TAB = new EmfAction(ActionTypeConstants.PRINT_TAB);
	private static final Action EXPORT_PDF = new EmfAction(ActionTypeConstants.EXPORT_PDF);
	private static final Action EXPORT_TAB_PDF = new EmfAction(ActionTypeConstants.EXPORT_TAB_PDF);

	private static final Action EXPORT_WORD = new EmfAction(ActionTypeConstants.EXPORT_WORD);
	private static final Action EXPORT_TAB_WORD = new EmfAction(ActionTypeConstants.EXPORT_TAB_WORD);
	private static final Action CREATE_IN_CONTEXT = new EmfAction(ActionTypeConstants.CREATE_IN_CONTEXT);

	private static final Action EDIT_OFFLINE = new EmfAction(ActionTypeConstants.EDIT_OFFLINE);

	private static final Set<Action> ALLOWED_ACTIONS_WHEN_LOCKED = new HashSet<>(
			Arrays.asList(EXPORT, MANAGE_PERMISSIONS, PRINT, UNLOCK, DOWNLOAD, EDIT_DETAILS, CLONE, VIEW_DETAILS,
					PRINT_TAB, EXPORT_PDF, EXPORT_TAB_PDF, EXPORT_WORD, EXPORT_TAB_WORD, CREATE_IN_CONTEXT));

	private static final Set<Action> ALLOWED_ACTIONS_FOR_VERSION = new HashSet<>(Arrays.asList(EXPORT, PRINT, DOWNLOAD,
			VIEW_DETAILS, PRINT_TAB, EXPORT_PDF, EXPORT_TAB_PDF, EXPORT_WORD, EXPORT_TAB_WORD, REVERT_VERSION));

	@Inject
	private RevisionService revisionService;

	@Inject
	private InstanceVersionService instanceVersionService;

	@Inject
	private InstanceContentService instanceContentService;

	@Inject
	private InstanceContextService contextService;

	@Override
	public List<Class> getSupportedObjects() {
		return SUPPORTED;
	}

	@Override
	protected Boolean filterInternal(ObjectInstance target, Resource resource, Role role, Set<Action> actions) {
		if (InstanceVersionService.isVersion(target.getId())) {
			return filterForVersion(target, resource, role, actions);
		}

		if (target.isLocked()) {
			return filterForLocked(target, resource, actions);
		}

		actions.remove(UNLOCK);
		actions.remove(REVERT_VERSION);

		if (!contextService.getContext(target).isPresent()) {
			actions.remove(DETACH);
		}

		String contentInfoMimeType = instanceContentService
				.getContent(target.getId(), Content.PRIMARY_CONTENT)
					.getMimeType();

		if (!target.isUploaded()) {
			actions.remove(UPLOAD_NEW_VERSION);
			actions.remove(DOWNLOAD);
			actions.remove(EDIT_OFFLINE);
		} else if (!MimetypeConstants.isMimeTypeSupported(contentInfoMimeType)) {
			actions.remove(EDIT_OFFLINE);
		}

		boolean revision = revisionService.isRevision(target);
		if (revision) {
			actions.remove(PUBLISH);
			actions.remove(PUBLISH_AS_PDF);
			actions.remove(HISTORY_PREVIEW);
			actions.remove(DELETE);
			actions.remove(UPLOAD_REVISION);
			actions.remove(CLONE);
		}
		filterRestorePermissions(target, resource, role, actions);
		return Boolean.FALSE;
	}

	private Boolean filterForVersion(ObjectInstance target, Resource resource, Role role, Set<Action> actions) {
		actions.retainAll(ALLOWED_ACTIONS_FOR_VERSION);

		if (!target.isUploaded()) {
			actions.remove(DOWNLOAD);
		}

		handleRevertVersionAction(target, resource, role, actions);
		return Boolean.FALSE;
	}

	/**
	 * Removes revert version action, if it is not allowed or adds it, if it is allowed, but missing from the
	 * collection. The allowed state is determined by the date, when the version was created and the date, when the
	 * functionality for version revert is enabled for the application. There are few issues with older versions, thats
	 * why filter them this way.
	 * <p>
	 * Revert version action could be missing, when the status of the version does not allow transition for that action,
	 * because initial actions set is evaluated based on the version status, however for revert version operation this
	 * evaluation should be based on the state of the current instance status. So when the action is allowed and it is
	 * missing from the initial set of actions we put it back.<br>
	 * <b>This is workaround for CMF-24105, changes to actions evaluation/filtering are planed in the future, there this
	 * kind of logic could be isolated and handled more properly.</b>
	 */
	private void handleRevertVersionAction(ObjectInstance version, Resource resource, Role role, Set<Action> actions) {
		String revertActionId = REVERT_VERSION.getActionId();
		if (!instanceVersionService.isRevertOperationAllowed(version)) {
			actions.remove(REVERT_VERSION);
		} else if (!actions.contains(REVERT_VERSION)) {
			// apply action filters and then if action still exists add it to the result
			RoleActionEvaluatorContext evaluatorContext = buildContext(version, role, resource);
			boolean revertActionAllowed = role.getAllowedActions(evaluatorContext).stream().anyMatch(
					action -> action.getActionId().equals(revertActionId));

			if (revertActionAllowed) {
				Set<Action> revertAction = transitionManager.getActions(
						InstanceVersionService.getIdFromVersionId(version.getId()),
						Collections.singleton(revertActionId));
				actions.addAll(revertAction);
			}
		}
	}

	private Boolean filterForLocked(ObjectInstance target, Resource resource, Set<Action> actions) {
		actions.retainAll(ALLOWED_ACTIONS_WHEN_LOCKED);
		if (!target.isUploaded()) {
			actions.remove(DOWNLOAD);
		}

		if (!resourceService.areEqual(target.getLockedBy(), resource)) {
			actions.remove(EDIT_DETAILS);
		}
		return Boolean.FALSE;
	}

}
