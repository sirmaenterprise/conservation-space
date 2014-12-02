/*
 *
 */
package com.sirma.itt.objects.security.provider;

import static com.sirma.itt.emf.security.SecurityModel.BaseRoles.COLLABORATOR;
import static com.sirma.itt.emf.security.SecurityModel.BaseRoles.CONSUMER;
import static com.sirma.itt.emf.security.SecurityModel.BaseRoles.CONTRIBUTOR;
import static com.sirma.itt.emf.security.SecurityModel.BaseRoles.CREATOR;
import static com.sirma.itt.emf.security.SecurityModel.BaseRoles.MANAGER;

import java.util.List;
import java.util.Map;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import com.sirma.itt.cmf.beans.model.CaseInstance;
import com.sirma.itt.cmf.beans.model.SectionInstance;
import com.sirma.itt.cmf.constants.allowed_action.ActionTypeConstants;
import com.sirma.itt.cmf.security.provider.AbstractRoleProviderExtension;
import com.sirma.itt.cmf.security.provider.RoleProviderExtension;
import com.sirma.itt.emf.domain.Pair;
import com.sirma.itt.emf.plugin.Extension;
import com.sirma.itt.emf.security.SecurityUtil;
import com.sirma.itt.emf.security.model.Action;
import com.sirma.itt.emf.security.model.ActionRegistry;
import com.sirma.itt.emf.security.model.Permission;
import com.sirma.itt.emf.security.model.Role;
import com.sirma.itt.emf.security.model.RoleIdentifier;
import com.sirma.itt.objects.domain.model.ObjectInstance;
import com.sirma.itt.objects.security.ObjectActionTypeConstants;

/**
 * The ObjectsRoleProviderExtension provides the roles and permission for that roles related to dom
 * module.
 */
@ApplicationScoped
@Extension(target = RoleProviderExtension.TARGET_NAME, order = 4)
public class ObjectsRoleProviderExtension extends AbstractRoleProviderExtension {

	/** The action registry. */
	@Inject
	private ActionRegistry actionRegistry;

	/**
	 * {@inheritDoc}.<br>
	 * Provides mapping for roles in {@link com.sirma.itt.emf.security.SecurityModel.BaseRoles} enum
	 * by enriching them with specific object module permissions
	 */
	@Override
	public Map<RoleIdentifier, Role> getModel(Map<RoleIdentifier, Role> enrichable) {

		consumer(enrichable);

		contributor(enrichable);

		collaborator(enrichable);

		creator(enrichable);

		manager(enrichable);

		return enrichable;
	}

	/**
	 * Constructs the {@link com.sirma.itt.emf.security.SecurityModel.BaseRoles#CONSUMER} role
	 * model.
	 *
	 * @param enrichable
	 *            the current permission model to be updated
	 */
	private void consumer(Map<RoleIdentifier, Role> enrichable) {
		Map<Permission, List<Pair<Class<?>, Action>>> permissions = getPermissions(enrichable,
				CONSUMER);
		addConsumerPermissions(permissions);
	}

	/**
	 * Adds the consumer permissions to the given permissions map.
	 *
	 * @param permissions
	 *            the permissions map to be enriched with consumer permissions
	 */
	private void addConsumerPermissions(Map<Permission, List<Pair<Class<?>, Action>>> permissions) {
		List<Pair<Class<?>, Action>> actions = SecurityUtil.createActionsList(ObjectInstance.class,
				actionRegistry, ObjectActionTypeConstants.CLONE);
		addPermission(permissions, PERMISSION_READ, actions);

		actions = SecurityUtil.createActionsList(ObjectInstance.class, actionRegistry,
				ObjectActionTypeConstants.PRINT);
		addPermission(permissions, PERMISSION_READ, actions);

		actions = SecurityUtil.createActionsList(ObjectInstance.class, actionRegistry,
				ObjectActionTypeConstants.EXPORT);
		addPermission(permissions, PERMISSION_READ, actions);

		actions = SecurityUtil.createActionsList(ObjectInstance.class, actionRegistry,
				ActionTypeConstants.CREATE_TOPIC);
		addPermission(permissions, PERMISSION_COMMENT, actions);
	}

	/**
	 * Constructs the {@link com.sirma.itt.emf.security.SecurityModel.BaseRoles#CONTRIBUTOR} role
	 * model.
	 *
	 * @param enrichable
	 *            the current permission model to be updated
	 */
	private void contributor(Map<RoleIdentifier, Role> enrichable) {
		Map<Permission, List<Pair<Class<?>, Action>>> permissions = getPermissions(enrichable,
				CONTRIBUTOR);
		addContributorPermissions(permissions);
	}

	/**
	 * Adds the contributor permissions to the given permissions map.
	 *
	 * @param permissions
	 *            the permissions map to be enriched with contributor permissions
	 */
	private void addContributorPermissions(Map<Permission, List<Pair<Class<?>, Action>>> permissions) {
		List<Pair<Class<?>, Action>> actions = SecurityUtil.createActionsList(ObjectInstance.class,
				actionRegistry, ObjectActionTypeConstants.CREATE_LINK);
		addPermission(permissions, PERMISSION_CREATE, actions);

		actions = SecurityUtil.createActionsList(CaseInstance.class, actionRegistry,
				ObjectActionTypeConstants.CREATE_OBJECTS_SECTION);
		addPermission(permissions, PERMISSION_CREATE, actions);

		actions = SecurityUtil.createActionsList(CaseInstance.class, actionRegistry,
				ActionTypeConstants.CREATE_OBJECT);
		addPermission(permissions, PERMISSION_EDIT, actions);

		actions = SecurityUtil.createActionsList(SectionInstance.class, actionRegistry,
				ObjectActionTypeConstants.ATTACH_OBJECT, ObjectActionTypeConstants.CREATE_OBJECT);
		addPermission(permissions, PERMISSION_CREATE, actions);

		actions = SecurityUtil.createActionsList(ObjectInstance.class, actionRegistry,
				ObjectActionTypeConstants.ADD_THUMBNAIL, ObjectActionTypeConstants.ADD_PRIMARY_IMAGE,
				ObjectActionTypeConstants.SAVE_AS_TEMPLATE);
		addPermission(permissions, PERMISSION_EDIT, actions);

		actions = SecurityUtil.createActionsList(ObjectInstance.class, actionRegistry,
				ObjectActionTypeConstants.LOCK);
		addPermission(permissions, PERMISSION_LOCK, actions);

		try {
			// this is this way because the objects module does not know for the project module
			// if you know better way fix it!
			Class<?> projectClass = Class.forName("com.sirma.itt.pm.domain.model.ProjectInstance");
			actions = SecurityUtil.createActionsList(projectClass, actionRegistry,
					ActionTypeConstants.CREATE_OBJECT);
			addPermission(permissions, PERMISSION_EDIT, actions);
		} catch (ClassNotFoundException e) {
			// not interested
		}
	}

	/**
	 * Collaborator.
	 *
	 * @param enrichable
	 *            the enrichable
	 */
	private void collaborator(Map<RoleIdentifier, Role> enrichable) {
		Map<Permission, List<Pair<Class<?>, Action>>> permissions = getPermissions(enrichable,
				COLLABORATOR);
		addCollaboratorPermissions(permissions);
	}

	/**
	 * Adds the collaborator permissions.
	 *
	 * @param permissions
	 *            the permissions
	 */
	private void addCollaboratorPermissions(
			Map<Permission, List<Pair<Class<?>, Action>>> permissions) {
		List<Pair<Class<?>, Action>> actions = SecurityUtil.createActionsList(ObjectInstance.class,
				actionRegistry, ObjectActionTypeConstants.UPLOAD_IN_OBJECT);
		addPermission(permissions, PERMISSION_CREATE, actions);

		actions = SecurityUtil.createActionsList(ObjectInstance.class,
				actionRegistry, ObjectActionTypeConstants.EDIT_DETAILS,
				ObjectActionTypeConstants.ADD_THUMBNAIL, ObjectActionTypeConstants.ADD_PRIMARY_IMAGE);
		addPermission(permissions, PERMISSION_EDIT, actions);

		actions = SecurityUtil.createActionsList(ObjectInstance.class, actionRegistry,
				ObjectActionTypeConstants.DETACH_OBJECT,
				ObjectActionTypeConstants.OBJECT_MOVE_SAME_CASE);
		addPermission(permissions, PERMISSION_DELETE, actions);
	}

	/**
	 * Constructs the {@link com.sirma.itt.emf.security.SecurityModel.BaseRoles#CREATOR} role model.
	 *
	 * @param enrichable
	 *            the current permission model to be updated
	 */
	private void creator(Map<RoleIdentifier, Role> enrichable) {
		Map<Permission, List<Pair<Class<?>, Action>>> permissions = getPermissions(enrichable,
				CREATOR);
		addConsumerPermissions(permissions);
		addContributorPermissions(permissions);

		addCreatorPermissions(permissions);

	}

	/**
	 * Adds the creator permissions.
	 * 
	 * @param permissions
	 *            the permissions
	 */
	private void addCreatorPermissions(Map<Permission, List<Pair<Class<?>, Action>>> permissions) {
		List<Pair<Class<?>, Action>> actions = SecurityUtil.createActionsList(ObjectInstance.class,
				actionRegistry, ObjectActionTypeConstants.DETACH_OBJECT,
				ObjectActionTypeConstants.OBJECT_MOVE_SAME_CASE);
		addPermission(permissions, PERMISSION_DELETE, actions);

		actions = SecurityUtil.createActionsList(CaseInstance.class, actionRegistry,
				ActionTypeConstants.CREATE_OBJECT);
		addPermission(permissions, PERMISSION_EDIT, actions);

		actions = SecurityUtil.createActionsList(ObjectInstance.class, actionRegistry,
				ObjectActionTypeConstants.ADD_THUMBNAIL, ObjectActionTypeConstants.ADD_PRIMARY_IMAGE, ObjectActionTypeConstants.EDIT_DETAILS);
		addPermission(permissions, PERMISSION_EDIT, actions);

		actions = SecurityUtil.createActionsList(ObjectInstance.class, actionRegistry,
				ObjectActionTypeConstants.PRINT);
		addPermission(permissions, PERMISSION_READ, actions);

		actions = SecurityUtil.createActionsList(ObjectInstance.class, actionRegistry,
				ObjectActionTypeConstants.EXPORT);
		addPermission(permissions, PERMISSION_READ, actions);

		actions = SecurityUtil.createActionsList(ObjectInstance.class, actionRegistry,
				ObjectActionTypeConstants.UPLOAD_IN_OBJECT);
		addPermission(permissions, PERMISSION_CREATE, actions);

		actions = SecurityUtil.createActionsList(ObjectInstance.class, actionRegistry,
				ObjectActionTypeConstants.SAVE_AS_TEMPLATE);
		addPermission(permissions, PERMISSION_EDIT, actions);
	}

	/**
	 * Manager.
	 * 
	 * @param enrichable
	 *            the enrichable
	 */
	private void manager(Map<RoleIdentifier, Role> enrichable) {
		Map<Permission, List<Pair<Class<?>, Action>>> permissions = getPermissions(enrichable,
				MANAGER);
		addConsumerPermissions(permissions);
		addContributorPermissions(permissions);
		addCreatorPermissions(permissions);

		List<Pair<Class<?>, Action>> actions = SecurityUtil.createActionsList(ObjectInstance.class,
				actionRegistry, ObjectActionTypeConstants.DELETE);
		addPermission(permissions, PERMISSION_DELETE, actions);
	}
}
