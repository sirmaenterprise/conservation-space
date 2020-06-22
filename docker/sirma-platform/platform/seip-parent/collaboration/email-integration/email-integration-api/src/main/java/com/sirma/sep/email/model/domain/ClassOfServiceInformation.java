package com.sirma.sep.email.model.domain;

import java.util.List;

import com.sirma.sep.email.model.account.GenericAttribute;

/**
 * Represents generic information about a mail server class of service. Contains its Id,name and list of attributes.
 *
 * @author georgi
 */
public class ClassOfServiceInformation {
	private String cosName;
	private String cosId;
	private List<GenericAttribute> attributes;

	/**
	 * Default constructor to initiate empty Class of Service information.Indicates that CoS is non existent.
	 */
	public ClassOfServiceInformation() {
		// indicates there is no class of service information.
	}

	/**
	 * Constructor initializing CoS id,name and list of attributes.
	 *
	 * @param cosId
	 *            class of service id
	 * @param cosName
	 *            class of service name
	 * @param attributes
	 *            class of service attributes
	 */
	public ClassOfServiceInformation(String cosId, String cosName, List<GenericAttribute> attributes) {
		this.cosId = cosId;
		this.cosName = cosName;

		this.attributes = attributes;
	}

	/**
	 * @return the cosName
	 */
	public String getCosName() {
		return cosName;
	}

	/**
	 * @param cosName
	 *            the cosName to set
	 */
	public void setCosName(String cosName) {
		this.cosName = cosName;
	}

	/**
	 * @return the cosId
	 */
	public String getCosId() {
		return cosId;
	}

	/**
	 * @param cosId
	 *            the cosId to set
	 */
	public void setCosId(String cosId) {
		this.cosId = cosId;
	}

	/**
	 * @return the attributes
	 */
	public List<GenericAttribute> getAttributes() {
		return attributes;
	}

	/**
	 * @param attributes
	 *            the attributes to set
	 */
	public void setAttributes(List<GenericAttribute> attributes) {
		this.attributes = attributes;
	}
}
