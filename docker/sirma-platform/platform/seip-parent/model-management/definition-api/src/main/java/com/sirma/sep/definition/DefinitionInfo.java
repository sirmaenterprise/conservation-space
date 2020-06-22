package com.sirma.sep.definition;

import java.util.Date;

/**
 * Provides system information about a single definition.
 *
 * @author Adrian Mitev
 */
public class DefinitionInfo {

	private final String id;

	private final String fileName;

	private final boolean isAbstract;

	private final String modifiedBy;

	private final Date modifiedOn;

	/**
	 * Instantiates the class.
	 *
	 * @param id definition id
	 * @param fileName name of the definition file
	 * @param isAbstract true if definition is imported/false otherwise
	 * @param modifiedBy id of the user that updated the definition
	 * @param modifiedOn date where the definition was updated
	 */
	public DefinitionInfo(String id, String fileName, boolean isAbstract, String modifiedBy,
			Date modifiedOn) {
		this.id = id;
		this.fileName = fileName;
		this.isAbstract = isAbstract;
		this.modifiedBy = modifiedBy;
		this.modifiedOn = modifiedOn;
	}

	public String getId() {
		return id;
	}

	public String getFileName() {
		return fileName;
	}

	public boolean isAbstract() {
		return isAbstract;
	}

	public String getModifiedBy() {
		return modifiedBy;
	}

	public Date getModifiedOn() {
		return modifiedOn;
	}

}
