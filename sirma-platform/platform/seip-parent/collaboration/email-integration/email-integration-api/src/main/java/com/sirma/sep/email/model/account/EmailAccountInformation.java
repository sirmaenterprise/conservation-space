package com.sirma.sep.email.model.account;

import java.util.List;

/**
 * Generic account information class.
 * 
 * @author g.tsankov
 *
 */
public class EmailAccountInformation {
	private String accountId;
	private String accountName;
	private List<GenericAttribute> attributes;

	/**
	 * @return the accountId
	 */
	public String getAccountId() {
		return accountId;
	}

	/**
	 * @param accountId
	 *            the accountId to set
	 */
	public void setAccountId(String accountId) {
		this.accountId = accountId;
	}

	/**
	 * @return the accountName
	 */
	public String getAccountName() {
		return accountName;
	}

	/**
	 * @param accountName
	 *            the accountName to set
	 */
	public void setAccountName(String accountName) {
		this.accountName = accountName;
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
