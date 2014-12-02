package com.sirma.itt.cmf.security.provider;

import static com.sirma.itt.emf.security.SecurityModel.BaseRoles.COLLABORATOR;
import static com.sirma.itt.emf.security.SecurityModel.BaseRoles.CONSUMER;
import static com.sirma.itt.emf.security.SecurityModel.BaseRoles.CONTRIBUTOR;
import static com.sirma.itt.emf.security.SecurityModel.BaseRoles.CREATOR;
import static com.sirma.itt.emf.security.SecurityModel.BaseRoles.MANAGER;
import static com.sirma.itt.emf.security.SecurityModel.BaseRoles.VIEWER;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import com.sirma.itt.cmf.beans.model.CaseInstance;
import com.sirma.itt.cmf.beans.model.DocumentInstance;
import com.sirma.itt.cmf.beans.model.SectionInstance;
import com.sirma.itt.cmf.beans.model.StandaloneTaskInstance;
import com.sirma.itt.cmf.beans.model.TaskInstance;
import com.sirma.itt.cmf.beans.model.WorkflowInstanceContext;
import com.sirma.itt.cmf.constants.allowed_action.ActionTypeConstants;
import com.sirma.itt.emf.domain.Pair;
import com.sirma.itt.emf.forum.model.CommentInstance;
import com.sirma.itt.emf.forum.model.TopicInstance;
import com.sirma.itt.emf.link.LinkInstance;
import com.sirma.itt.emf.link.LinkReference;
import com.sirma.itt.emf.plugin.Extension;
import com.sirma.itt.emf.security.SecurityUtil;
import com.sirma.itt.emf.security.model.Action;
import com.sirma.itt.emf.security.model.ActionRegistry;
import com.sirma.itt.emf.security.model.Permission;
import com.sirma.itt.emf.security.model.Role;
import com.sirma.itt.emf.security.model.RoleIdentifier;
import com.sirma.itt.emf.security.model.RoleImpl;

/**
 * The CmfRoleProviderExtension provides the roles and permission for that roles related to cmf
 * module.
 */
@ApplicationScoped
@Extension(target = RoleProviderExtension.TARGET_NAME, order = 0)
public class CmfRoleProviderExtension extends AbstractRoleProviderExtension {

	/** The action registry. */
	@Inject
	private ActionRegistry actionRegistry;

	/**
	 * {@inheritDoc}.<br>
	 * Provides mapping for roles in {@link com.sirma.itt.emf.security.SecurityModel.BaseRoles}
	 * enum. This is the root provider that registers most of the base roles
	 */
	@Override
	public Map<RoleIdentifier, Role> getModel(Map<RoleIdentifier, Role> enriched) {

		Map<RoleIdentifier, Role> map = new LinkedHashMap<RoleIdentifier, Role>();

		map.put(VIEWER, viewer());
		map.put(CONSUMER, consumer());
		map.put(CONTRIBUTOR, contributor());
		map.put(COLLABORATOR, collaborator());
		map.put(CREATOR, creator(map));
		map.put(MANAGER, manager());
		enriched.putAll(map);
		return enriched;
	}

	/**
	 * Constructs the {@link com.sirma.itt.emf.security.SecurityModel.BaseRoles#VIEWER} role model.
	 * Currently no operations are allowed.
	 *
	 * @return the viewer role
	 */
	private Role viewer() {
		Map<Permission, List<Pair<Class<?>, Action>>> permissions = new HashMap<Permission, List<Pair<Class<?>, Action>>>();
		RoleIdentifier roleId = VIEWER;
		return new RoleImpl(roleId, permissions);

	}

	/**
	 * Constructs the {@link com.sirma.itt.emf.security.SecurityModel.BaseRoles#CONSUMER} role
	 * model.
	 *
	 * @return the consumer role
	 */
	private Role consumer() {
		RoleIdentifier roleId = CONSUMER;
		List<Pair<Class<?>, Action>> actions = SecurityUtil.createActionsList(
				DocumentInstance.class, actionRegistry, ActionTypeConstants.DOWNLOAD,
				ActionTypeConstants.PREVIEW, ActionTypeConstants.HISTORY_DOWNLOAD,
				ActionTypeConstants.HISTORY_PREVIEW
		/* * , ActionTypeConstants .PRINT */);
		Map<Permission, List<Pair<Class<?>, Action>>> permissions = new HashMap<Permission, List<Pair<Class<?>, Action>>>();
		addPermission(permissions, PERMISSION_READ, actions);

		// TODO - ability for dynamic mapping
		// actions = SecurityUtil.createActionsList(Instance.class,
		// actionRegistry,
		// ActionTypeConstants.CREATE_CREATE, ActionTypeConstants.TOPIC_REPLY);
		// addPermission(permissions, PERMISSION_COMMENT, actions);

		// for all types add the actions
		actions = SecurityUtil.createActionsList(CaseInstance.class, actionRegistry,
				ActionTypeConstants.CREATE_TOPIC);
		addPermission(permissions, PERMISSION_COMMENT, actions);
		actions = SecurityUtil.createActionsList(DocumentInstance.class, actionRegistry,
				ActionTypeConstants.CREATE_TOPIC);
		addPermission(permissions, PERMISSION_COMMENT, actions);
		actions = SecurityUtil.createActionsList(TaskInstance.class, actionRegistry,
				ActionTypeConstants.CREATE_TOPIC);
		addPermission(permissions, PERMISSION_COMMENT, actions);
		actions = SecurityUtil.createActionsList(StandaloneTaskInstance.class, actionRegistry,
				ActionTypeConstants.CREATE_TOPIC);
		addPermission(permissions, PERMISSION_COMMENT, actions);
		actions = SecurityUtil.createActionsList(WorkflowInstanceContext.class, actionRegistry,
				ActionTypeConstants.CREATE_TOPIC);
		addPermission(permissions, PERMISSION_COMMENT, actions);

		// permission comment on topic
		actions = SecurityUtil.createActionsList(TopicInstance.class, actionRegistry,
				ActionTypeConstants.TOPIC_REPLY);
		addPermission(permissions, PERMISSION_COMMENT, actions);
		actions = SecurityUtil.createActionsList(CommentInstance.class, actionRegistry,
				ActionTypeConstants.TOPIC_REPLY);
		addPermission(permissions, PERMISSION_COMMENT, actions);

		return new RoleImpl(roleId, permissions);
	}

	/**
	 * Constructs the {@link com.sirma.itt.emf.security.SecurityModel.BaseRoles#CONTRIBUTOR}.
	 *
	 * @return the contributor role
	 */
	private Role contributor() {

		Map<Permission, List<Pair<Class<?>, Action>>> permissions;
		List<Pair<Class<?>, Action>> actions;
		// copy permissions for consumer to collaborator
		permissions = new LinkedHashMap<Permission, List<Pair<Class<?>, Action>>>();
		// ############################## CASE #########################/
		actions = SecurityUtil.createActionsList(CaseInstance.class, actionRegistry,
				ActionTypeConstants.CLONE, ActionTypeConstants.LINK,
				ActionTypeConstants.CREATE_LINK);
		addPermission(permissions, PERMISSION_READ, actions);

		actions = SecurityUtil.createActionsList(CaseInstance.class, actionRegistry,
				ActionTypeConstants.START_WORKFLOW, ActionTypeConstants.CREATE_TASK,
				ActionTypeConstants.CREATE_DOCUMENTS_SECTION, ActionTypeConstants.CREATE_CASE);
		addPermission(permissions, PERMISSION_CREATE, actions);

		// ############################## SECTION #########################/
		actions = SecurityUtil.createActionsList(SectionInstance.class, actionRegistry,
				ActionTypeConstants.UPLOAD, ActionTypeConstants.CREATE_IDOC,
				ActionTypeConstants.ATTACH_DOCUMENT);
		addPermission(permissions, PERMISSION_CREATE, actions);

		// ############################## DOCUMENT #########################/
		actions = SecurityUtil.createActionsList(DocumentInstance.class, actionRegistry,
				ActionTypeConstants.CREATE_LINK, ActionTypeConstants.START_WORKFLOW);
		addPermission(permissions, PERMISSION_CREATE, actions);
		actions = SecurityUtil.createActionsList(DocumentInstance.class, actionRegistry,
				ActionTypeConstants.DETACH_DOCUMENT);
		addPermission(permissions, PERMISSION_EDIT, actions);

		actions = SecurityUtil.createActionsList(CaseInstance.class, actionRegistry,
				ActionTypeConstants.CREATE_OBJECT);
		addPermission(permissions, PERMISSION_EDIT, actions);

		return new RoleImpl(CONTRIBUTOR, permissions);
	}

	/**
	 * Constructs the {@link com.sirma.itt.emf.security.SecurityModel.BaseRoles#COLLABORATOR}.
	 *
	 * @return the collaborator role
	 */
	private Role collaborator() {

		Map<Permission, List<Pair<Class<?>, Action>>> permissions;
		List<Pair<Class<?>, Action>> actions;
		// copy permissions for consumer to collaborator
		permissions = new LinkedHashMap<Permission, List<Pair<Class<?>, Action>>>();
		// ############################## CASE #########################/
		actions = SecurityUtil.createActionsList(CaseInstance.class, actionRegistry,
				ActionTypeConstants.CLONE, ActionTypeConstants.LINK);
		addPermission(permissions, PERMISSION_READ, actions);

		actions = SecurityUtil.createActionsList(CaseInstance.class, actionRegistry,
				ActionTypeConstants.START_WORKFLOW, ActionTypeConstants.CREATE_TASK,
				ActionTypeConstants.CREATE_DOCUMENTS_SECTION, ActionTypeConstants.CREATE_CASE);
		addPermission(permissions, PERMISSION_CREATE, actions);

		actions = SecurityUtil.createActionsList(CaseInstance.class, actionRegistry,
				ActionTypeConstants.EDIT_DETAILS, ActionTypeConstants.COMPLETE,
				ActionTypeConstants.START, ActionTypeConstants.SUSPEND,
				ActionTypeConstants.APPROVE, ActionTypeConstants.RESTART);
		addPermission(permissions, PERMISSION_EDIT, actions);
		actions = SecurityUtil.createActionsList(CaseInstance.class, actionRegistry,
				ActionTypeConstants.STOP);
		addPermission(permissions, PERMISSION_DELETE, actions);
		// ############################## DOCUMENT #########################/
		actions = SecurityUtil.createActionsList(DocumentInstance.class, actionRegistry,
				ActionTypeConstants.MOVE_SAME_CASE, ActionTypeConstants.MOVE_OTHER_CASE
		/*
		 * ,ActionTypeConstants.COPY_CONTENT, ActionTypeConstants.LINK_DOCUMENT
		 */);
		addPermission(permissions, PERMISSION_CREATE, actions);

		actions = SecurityUtil.createActionsList(DocumentInstance.class, actionRegistry,
				ActionTypeConstants.CREATE_SUB_DOCUMENT, ActionTypeConstants.UPLOAD,
				ActionTypeConstants.CLONE, ActionTypeConstants.SAVE_AS_PUBLIC_TEMPLATE,
				ActionTypeConstants.PRINT, ActionTypeConstants.EXPORT,
				ActionTypeConstants.START_WORKFLOW);
		addPermission(permissions, PERMISSION_CREATE, actions);

		actions = SecurityUtil.createActionsList(DocumentInstance.class, actionRegistry,
				ActionTypeConstants.EDIT_STRUCTURED_DOCUMENT, ActionTypeConstants.EDIT_DETAILS,
				ActionTypeConstants.SIGN);
		addPermission(permissions, PERMISSION_EDIT, actions);

		actions = SecurityUtil.createActionsList(DocumentInstance.class, actionRegistry,
				ActionTypeConstants.LOCK, ActionTypeConstants.UNLOCK,
				ActionTypeConstants.STOP_EDIT, ActionTypeConstants.EDIT_OFFLINE,
				ActionTypeConstants.EDIT_ONLINE, ActionTypeConstants.UPLOAD_NEW_VERSION,
				ActionTypeConstants.REVERT, ActionTypeConstants.HISTORY_PREVIEW);
		addPermission(permissions, PERMISSION_LOCK, actions);

		// revert history version of document
		addPermission(permissions, PERMISSION_REVERT, actions);

		// ############################## SECTION #########################/
		actions = SecurityUtil.createActionsList(SectionInstance.class, actionRegistry,
				ActionTypeConstants.UPLOAD, ActionTypeConstants.CREATE_IDOC);
		addPermission(permissions, PERMISSION_CREATE, actions);

		// ############################## LINK #########################/
		actions = SecurityUtil.createActionsList(LinkInstance.class, actionRegistry,
				ActionTypeConstants.EDIT_DETAILS);
		addPermission(permissions, PERMISSION_EDIT, actions);
		actions = SecurityUtil.createActionsList(LinkReference.class, actionRegistry,
				ActionTypeConstants.EDIT_DETAILS);
		addPermission(permissions, PERMISSION_EDIT, actions);

		// ############################## WORKFLOW #########################/
		actions = SecurityUtil.createActionsList(WorkflowInstanceContext.class, actionRegistry,
				ActionTypeConstants.STOP);
		addPermission(permissions, PERMISSION_DELETE, actions);

		actions = SecurityUtil.createActionsList(CaseInstance.class, actionRegistry,
				ActionTypeConstants.CREATE_OBJECT);
		addPermission(permissions, PERMISSION_EDIT, actions);

		return new RoleImpl(COLLABORATOR, permissions);
	}

	/**
	 * Constructs the {@link com.sirma.itt.emf.security.SecurityModel.BaseRoles#CREATOR} role model.
	 *
	 * @param chain
	 *            is the current model
	 * @return the creator role
	 */
	private Role creator(Map<RoleIdentifier, Role> chain) {
		RoleIdentifier roleId = CREATOR;
		List<Pair<Class<?>, Action>> actions = null;
		Map<Permission, List<Pair<Class<?>, Action>>> permissions = null;

		// copy from collaborator and add the delete operations
		permissions = initBasedOn(chain, COLLABORATOR);
		// remove some not needed
		List<Pair<Class<?>, Action>> removeable = SecurityUtil.createActionsList(
				CaseInstance.class, actionRegistry,
				// ActionTypeConstants.START_WORKFLOW,
				// ActionTypeConstants.CREATE_TASK, ActionTypeConstants.CREATE_DOCUMENTS_SECTION,
				ActionTypeConstants.CREATE_CASE);
		permissions.get(PERMISSION_CREATE).removeAll(removeable);

		// ############################## DELETE #########################/
		// // delete your cases and documents
		actions = SecurityUtil.createActionsList(CaseInstance.class, actionRegistry,
				ActionTypeConstants.DELETE);
		addPermission(permissions, PERMISSION_DELETE, actions);
		actions = SecurityUtil.createActionsList(DocumentInstance.class, actionRegistry,
				ActionTypeConstants.DELETE);
		addPermission(permissions, PERMISSION_DELETE, actions);

		// ############################## COMMENT #########################/
		actions = SecurityUtil.createActionsList(CommentInstance.class, actionRegistry,
				ActionTypeConstants.EDIT_DETAILS);
		addPermission(permissions, PERMISSION_EDIT, actions);
		actions = SecurityUtil.createActionsList(CommentInstance.class, actionRegistry,
				ActionTypeConstants.DELETE);
		addPermission(permissions, PERMISSION_DELETE, actions);

		// ############################## TOPIC #########################/
		actions = SecurityUtil.createActionsList(TopicInstance.class, actionRegistry,
				ActionTypeConstants.EDIT_DETAILS, ActionTypeConstants.SUSPEND,
				ActionTypeConstants.RESTART);
		addPermission(permissions, PERMISSION_EDIT, actions);
		actions = SecurityUtil.createActionsList(TopicInstance.class, actionRegistry,
				ActionTypeConstants.DELETE);
		addPermission(permissions, PERMISSION_DELETE, actions);

		// ############################## LINK #########################/
		actions = SecurityUtil.createActionsList(LinkInstance.class, actionRegistry,
				ActionTypeConstants.DELETE);
		addPermission(permissions, PERMISSION_DELETE, actions);
		actions = SecurityUtil.createActionsList(LinkReference.class, actionRegistry,
				ActionTypeConstants.DELETE);
		addPermission(permissions, PERMISSION_DELETE, actions);

		// ############################## WORKFLOW #########################/
		actions = SecurityUtil.createActionsList(WorkflowInstanceContext.class, actionRegistry,
				ActionTypeConstants.STOP);
		addPermission(permissions, PERMISSION_DELETE, actions);

		return new RoleImpl(roleId, permissions);
	}

	/**
	 * Constructs the {@link com.sirma.itt.emf.security.SecurityModel.BaseRoles#MANAGER} role model.
	 *
	 * @return the manager role
	 */
	private Role manager() {
		RoleIdentifier roleId = MANAGER;
		Map<Permission, List<Pair<Class<?>, Action>>> permissions = new HashMap<Permission, List<Pair<Class<?>, Action>>>();
		List<Pair<Class<?>, Action>> actions = SecurityUtil.createActionsList(
				CommentInstance.class, actionRegistry, ActionTypeConstants.EDIT_DETAILS);
		addPermission(permissions, PERMISSION_EDIT, actions);
		actions = SecurityUtil.createActionsList(CommentInstance.class, actionRegistry,
				ActionTypeConstants.DELETE);
		addPermission(permissions, PERMISSION_DELETE, actions);

		// topic
		actions = SecurityUtil.createActionsList(TopicInstance.class, actionRegistry,
				ActionTypeConstants.EDIT_DETAILS);
		addPermission(permissions, PERMISSION_EDIT, actions);
		actions = SecurityUtil.createActionsList(TopicInstance.class, actionRegistry,
				ActionTypeConstants.DELETE);
		addPermission(permissions, PERMISSION_DELETE, actions);
		return new RoleImpl(roleId, permissions);
	}

}