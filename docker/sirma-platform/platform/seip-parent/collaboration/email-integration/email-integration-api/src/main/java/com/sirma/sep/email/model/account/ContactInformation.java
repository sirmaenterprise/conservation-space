package com.sirma.sep.email.model.account;

import java.util.List;

/**
 * Generic contact information class.
 *
 * @author g.tsankov
 */
public class ContactInformation {
	private String id;
	private List<GenericAttribute> attributes;

	public ContactInformation() {
		// indicates no contacts found
	}

	public ContactInformation(String id, List<GenericAttribute> attributes) {
		this.id = id;
		this.attributes = attributes;
	}

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public List<GenericAttribute> getAttributes() {
		return attributes;
	}

	public void setAttributes(List<GenericAttribute> attributes) {
		this.attributes = attributes;
	}
}
