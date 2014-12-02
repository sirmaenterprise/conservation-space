package com.sirma.itt.pm.constants;

import com.sirma.itt.emf.properties.DefaultProperties;

/**
 * All known properties for a project instance.
 *
 * @author BBonev
 */
public interface ProjectProperties extends DefaultProperties {

	/** The owner. */
	String OWNER = "owner";
	/** The notes. */
	String NOTES = "notes";
	/** The visibility. */
	String VISIBILITY = "visibility";

	// probably QVI properties that will be moved to other file
	/** The approved by. */
	String APPROVED_BY = "approvedBy";
	/** The severity. */
	String SEVERITY = "severity";
	/** The account. */
	String ACCOUNT = "account";
	/** The contact. */
	String CONTACT = "contact";
	/** The dependency list. */
	String DEPENDENCY_LIST = "dependencyList";
	/** The reference list. */
	String REFERENCE_LIST = "referenceList";
	/** The billing code. */
	String BILLING_CODE = "billingCode";

	/** The project sequence. */
	String PROJECT_SEQUENCE = "projectSequence";
	/** The parent/child assoc to cases. */
	String OWNED_INSTANCES = "owningReference";

	/**
	 * The Visbility enum for project
	 */
	enum Visbility {

		/** The public. */
		PUBLIC("visibleToAll"),
		/** The moderated. */
		PRIVATE("visibleToMembers");

		/** The name. */
		private String name;

		/**
		 * Instantiates a new visbility.
		 *
		 * @param name
		 *            the name
		 */
		private Visbility(String name) {
			this.name = name;
		}

		/**
		 * Gets the name.
		 *
		 * @return the name
		 */
		public String getName() {
			return name;
		}
	}
}
