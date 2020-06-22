package com.sirma.sep.email.model.domain;

import java.util.List;

import com.sirma.sep.email.model.account.GenericAttribute;

/**
 * Represents generic information about mail server domain. Stores its name, id and a list of {@link DomainAttribute}s.
 *
 *
 * @author g.tsankov
 */
public class DomainInformation {
	private String domainName;
	private String domainId;
	private List<GenericAttribute> attributes;

	/**
	 * Default constructor, used primarily to initiate empty domain information, used to indicate that the requested
	 * domain is non existent.
	 */
	public DomainInformation() {
		// indicates that there is no domain informaiton.
	}

	/**
	 * Constructor initializing domain Id, domain name and its list of attributes.
	 *
	 * @param domainId
	 *            domain ID.
	 * @param domainName
	 *            domain name.
	 * @param attributes
	 *            list of domain attributes.
	 */
	public DomainInformation(String domainId, String domainName, List<GenericAttribute> attributes) {
		this.domainId = domainId;
		this.domainName = domainName;
		this.attributes = attributes;
	}

	/**
	 * Getter method for domainName.
	 *
	 * @return the domainName
	 */
	public String getDomainName() {
		return domainName;
	}

	/**
	 * Setter method for domainName.
	 *
	 * @param domainName
	 *            the domainName to set
	 */
	public void setDomainName(String domainName) {
		this.domainName = domainName;
	}

	/**
	 * Getter method for domainId.
	 *
	 * @return the domainId
	 */
	public String getDomainId() {
		return domainId;
	}

	/**
	 * Setter method for domainId.
	 *
	 * @param domainId
	 *            the domainId to set
	 */
	public void setDomainId(String domainId) {
		this.domainId = domainId;
	}

	/**
	 * Getter method for attributes.
	 *
	 * @return the attributes
	 */
	public List<GenericAttribute> getAttributes() {
		return attributes;
	}

	/**
	 * Setter method for attributes.
	 *
	 * @param attributes
	 *            the attributes to set
	 */
	public void setAttributes(List<GenericAttribute> attributes) {
		this.attributes = attributes;
	}
}
