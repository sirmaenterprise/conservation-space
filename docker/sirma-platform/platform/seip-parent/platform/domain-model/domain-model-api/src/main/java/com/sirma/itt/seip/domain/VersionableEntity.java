package com.sirma.itt.seip.domain;

/**
 * Defines an entity that supports database versioning. It's used for optimistic locking of the entities.
 *
 * @author BBonev
 */
public interface VersionableEntity {

	/**
	 * Getter method for version.
	 *
	 * @return the version
	 */
	Long getVersion();

	/**
	 * Setter method for version.
	 *
	 * @param version
	 *            the version to set
	 */
	void setVersion(Long version);
}
