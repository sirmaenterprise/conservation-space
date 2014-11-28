package com.sirma.itt.seip.rest;

/**
 * A set of all available instance types in the system.
 * 
 * @author Adrian Mitev
 */
public enum EntityType {

	PROJECT("projectinstance"), CASE("caseinstance"), SECTION("sectioninstance"), DOMAIN_OBJECT(
			"objectinstance"), DOCUMENT("documentinstance"), WORKFLOW("workflowinstancecontext"), STANDALONE_TASK(
			"standalonetaskinstance");

	private final String value;

	/**
	 * Instantiates an enum member by passing the instance type.
	 * 
	 * @param type
	 *            instance type in the system.
	 */
	private EntityType(String type) {
		this.value = type;
	}

	/**
	 * Provides the system entity type.
	 * 
	 * @return entity type used within the system.
	 */
	public String getSystemType() {
		return value;
	}
}
