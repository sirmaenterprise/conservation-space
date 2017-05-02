/**
 *
 */
package com.sirma.itt.semantic.model.vocabulary;

import org.openrdf.model.Namespace;
import org.openrdf.model.URI;
import org.openrdf.model.ValueFactory;
import org.openrdf.model.impl.NamespaceImpl;
import org.openrdf.model.impl.ValueFactoryImpl;

/**
 * Permissions model constants
 *
 * @author kirq4e
 */
public final class Security {

	/** http://www.sirma.com/ontologies/2014/11/security# */
	public static final String NAMESPACE = "http://www.sirma.com/ontologies/2014/11/security#";

	/**
	 * Recommended prefix for the EnterpriseManagementFramework namespace: "sec"
	 */
	public static final String PREFIX = "sec";

	/**
	 * An immutable {@link Namespace} constant that represents the Permissions model
	 */
	public static final Namespace NS = new NamespaceImpl(PREFIX, NAMESPACE);
	// /////////////////////////////////////////
	// Context
	// /////////////////////////////////////////

	// /////////////////////////////////////////
	// CLASSES
	// /////////////////////////////////////////
	public static final URI ROLE;

	// /////////////////////////////////////////
	// PROPERTIES
	// /////////////////////////////////////////

	/**
	 * Default URI for the permissions predicates
	 */
	public static final URI HAS_PERMISSION;
	public static final URI IS_MANAGER_OF;
	public static final URI HAS_ROLE_TYPE;
	public static final URI ASSIGNED_TO;

	/*
	 * Relation properties for automatic permission assignment
	 */
	/**
	 * Relation property that should contain a role identifier that is the minimal role that an user should have on the
	 * resource that is the source of the relation.
	 */
	public static final URI AUTO_ASSIGN_PERMISSION_ROLE;
	/**
	 * Boolean relation property to indicate if the automatic role can override existing permissions if the user will
	 * have lower permissions after the automatic assign
	 */
	public static final URI ALLOW_PERMISSION_OVERRIDE;
	/**
	 * Relation property that should contain a role identifier that is the minimal role that an user should have on the
	 * parent instance on the resource that is the source of the relation. This is applicable only if the
	 * {@link #AUTO_ASSIGN_PERMISSION_ROLE} is present on the same relation.
	 */
	public static final URI AUTO_ASSIGN_PARENT_PERMISSION_ROLE;

	// /////////////////////////////////////////
	// Instances
	// /////////////////////////////////////////

	/**
	 * Default URI for the permissions set to all other users
	 */
	public static final URI SYSTEM_ALL_OTHER_USERS;

	// end of EmfInstance member variables

	static {
		ValueFactory factory = ValueFactoryImpl.getInstance();
		// init context URIs

		// init Class URIs
		ROLE = factory.createURI(NAMESPACE, "Role");
		// init property URIs
		HAS_PERMISSION = factory.createURI(NAMESPACE, "hasPermission");
		IS_MANAGER_OF = factory.createURI(NAMESPACE, "isManagerOf");
		HAS_ROLE_TYPE = factory.createURI(NAMESPACE, "hasRoleType");
		ASSIGNED_TO = factory.createURI(NAMESPACE, "assignedTo");

		AUTO_ASSIGN_PERMISSION_ROLE = factory.createURI(NAMESPACE, "autoAssignPermissionRole");
		ALLOW_PERMISSION_OVERRIDE = factory.createURI(NAMESPACE, "allowPermissionOverride");
		AUTO_ASSIGN_PARENT_PERMISSION_ROLE = factory.createURI(NAMESPACE, "autoAssignParentPermissionRole");

		// init instances
		SYSTEM_ALL_OTHER_USERS = factory.createURI(NAMESPACE, "SYSTEM_ALL_OTHER_USERS");
	}

	/**
	 * This class is only for constants and it should not be instantiated
	 */
	private Security() {
	}
}
