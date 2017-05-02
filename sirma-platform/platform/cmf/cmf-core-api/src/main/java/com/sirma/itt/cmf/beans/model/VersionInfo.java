package com.sirma.itt.cmf.beans.model;

import java.io.Serializable;
import java.util.Date;

import com.sirma.itt.seip.domain.instance.DMSInstance;

/**
 * Version information holder for document.
 *
 * @author bbanchev
 */
public class VersionInfo implements Serializable {

	/** The Constant serialVersionUID. */
	private static final long serialVersionUID = 8676726801433185307L;

	/** The version label. */
	private final String versionLabel;
	/** The version name. */
	private final String versionName;

	/** The version description. */
	private String versionDescription = "";

	/** The version date. */
	private final Date versionDate;

	/** The doc instance. */
	private final DMSInstance docInstance;
	/** user created version. */
	private final String versionCreator;

	/**
	 * Instantiates a new version info.
	 *
	 * @param versionLabel
	 *            the version label
	 * @param versionName
	 *            is the file name for the provided version
	 * @param versionDescription
	 *            the version description
	 * @param versionDate
	 *            the version date
	 * @param versionCreator
	 *            the version creator
	 */
	public VersionInfo(String versionLabel, String versionName, String versionDescription, Date versionDate,
			String versionCreator) {
		this(null, versionLabel, versionName, versionDate, versionCreator, versionDescription);
	}

	/**
	 * Instantiates a new version info.
	 *
	 * @param docInstance
	 *            the doc instance
	 * @param versionLabel
	 *            the version label
	 * @param versionName
	 *            is the file name for the provided version
	 * @param versionDate
	 *            the version date
	 * @param versionCreator
	 *            the version creator
	 * @param versionDescription
	 *            the version description
	 */
	public VersionInfo(DMSInstance docInstance, String versionLabel, String versionName, Date versionDate,
			String versionCreator, String versionDescription) {
		super();
		this.versionLabel = versionLabel;
		this.versionName = versionName;
		this.versionDescription = versionDescription;
		this.versionDate = versionDate;
		this.versionCreator = versionCreator;
		this.docInstance = docInstance;
	}

	/**
	 * Gets the version label.
	 *
	 * @return the versionLabel
	 */
	public String getVersionLabel() {
		return versionLabel;
	}

	/**
	 * Gets the version description.
	 *
	 * @return the versionDescription
	 */
	public String getVersionDescription() {
		return versionDescription;
	}

	/**
	 * Gets the doc instance.
	 *
	 * @return the docInstance
	 */
	public DMSInstance getDocInstance() {
		return docInstance;
	}

	/**
	 * @return the versionDate
	 */
	public Date getVersionDate() {
		return versionDate;
	}

	public String getVersionName() {
		return versionName;
	}

	/**
	 * @return the versionCreator
	 */
	public String getVersionCreator() {
		return versionCreator;
	}

}
