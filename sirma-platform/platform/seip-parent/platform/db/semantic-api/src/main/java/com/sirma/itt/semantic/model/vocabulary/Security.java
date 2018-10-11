/**
 *
 */
package com.sirma.itt.semantic.model.vocabulary;

import org.eclipse.rdf4j.model.IRI;
import org.eclipse.rdf4j.model.Namespace;
import org.eclipse.rdf4j.model.ValueFactory;
import org.eclipse.rdf4j.model.impl.SimpleNamespace;
import org.eclipse.rdf4j.model.impl.SimpleValueFactory;

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
	public static final Namespace NS = new SimpleNamespace(PREFIX, NAMESPACE);
	// /////////////////////////////////////////
	// Context
	// /////////////////////////////////////////

	// /////////////////////////////////////////
	// CLASSES
	// /////////////////////////////////////////
	public static final IRI ROLE;

	// /////////////////////////////////////////
	// PROPERTIES
	// /////////////////////////////////////////

	/**
	 * Default IRI for the permissions predicates
	 */
	public static final IRI HAS_PERMISSION;
	public static final IRI IS_MANAGER_OF;
	public static final IRI HAS_ROLE_TYPE;
	public static final IRI ASSIGNED_TO;

	/*
	 * Relation properties for automatic permission assignment
	 */
	/**
	 * Relation property that should contain a role identifier that is the minimal role that an user should have on the
	 * resource that is the source of the relation.
	 */
	public static final IRI AUTO_ASSIGN_PERMISSION_ROLE;
	/**
	 * Boolean relation property to indicate if the automatic role can override existing permissions if the user will
	 * have lower permissions after the automatic assign
	 */
	public static final IRI ALLOW_PERMISSION_OVERRIDE;
	/**
	 * Relation property that should contain a role identifier that is the minimal role that an user should have on the
	 * parent instance on the resource that is the source of the relation. This is applicable only if the
	 * {@link #AUTO_ASSIGN_PERMISSION_ROLE} is present on the same relation.
	 */
	public static final IRI AUTO_ASSIGN_PARENT_PERMISSION_ROLE;

	// /////////////////////////////////////////
	// Instances
	// /////////////////////////////////////////

	/**
	 * Default IRI for the permissions set to all other users
	 */
	public static final IRI SYSTEM_ALL_OTHER_USERS;

	// end of EmfInstance member variables

	static {
		ValueFactory factory = SimpleValueFactory.getInstance();
		// init context IRIs

		// init Class IRIs
		ROLE = factory.createIRI(NAMESPACE, "Role");
		// init property IRIs
		HAS_PERMISSION = factory.createIRI(NAMESPACE, "hasPermission");
		IS_MANAGER_OF = factory.createIRI(NAMESPACE, "isManagerOf");
		HAS_ROLE_TYPE = factory.createIRI(NAMESPACE, "hasRoleType");
		ASSIGNED_TO = factory.createIRI(NAMESPACE, "assignedTo");

		AUTO_ASSIGN_PERMISSION_ROLE = factory.createIRI(NAMESPACE, "autoAssignPermissionRole");
		ALLOW_PERMISSION_OVERRIDE = factory.createIRI(NAMESPACE, "allowPermissionOverride");
		AUTO_ASSIGN_PARENT_PERMISSION_ROLE = factory.createIRI(NAMESPACE, "autoAssignParentPermissionRole");

		// init instances
		SYSTEM_ALL_OTHER_USERS = factory.createIRI(NAMESPACE, "SYSTEM_ALL_OTHER_USERS");
	}

	/**
	 * This class is only for constants and it should not be instantiated
	 */
	private Security() {
	}
}
