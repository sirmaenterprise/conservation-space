package com.sirmaenterprise.sep.models;

import java.util.Date;

/**
 * Carries the data for a single template imported in the system.
 * 
 * @author Vilizar Tsonev
 */
public class ImportedTemplate {

	private String id;

	private String title;

	private Date modifiedOn;

	private String modifiedBy;

	private String purpose;

	private boolean primary;

	private String forObjectType;

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public String getModifiedBy() {
		return modifiedBy;
	}

	public void setModifiedBy(String modifiedBy) {
		this.modifiedBy = modifiedBy;
	}

	public String getPurpose() {
		return purpose;
	}

	public void setPurpose(String purpose) {
		this.purpose = purpose;
	}

	public boolean isPrimary() {
		return primary;
	}

	public void setPrimary(boolean primary) {
		this.primary = primary;
	}

	public Date getModifiedOn() {
		return modifiedOn;
	}

	public void setModifiedOn(Date modifiedOn) {
		this.modifiedOn = modifiedOn;
	}

	public String getForObjectType() {
		return forObjectType;
	}

	public void setForObjectType(String forObjectType) {
		this.forObjectType = forObjectType;
	}

	public String getTitle() {
		return title;
	}

	public void setTitle(String title) {
		this.title = title;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((forObjectType == null) ? 0 : forObjectType.hashCode());
		result = prime * result + ((id == null) ? 0 : id.hashCode());
		result = prime * result + ((modifiedBy == null) ? 0 : modifiedBy.hashCode());
		result = prime * result + ((modifiedOn == null) ? 0 : modifiedOn.hashCode());
		result = prime * result + (primary ? 1231 : 1237);
		result = prime * result + ((purpose == null) ? 0 : purpose.hashCode());
		result = prime * result + ((title == null) ? 0 : title.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj1) { // NOSONAR
		if (this == obj1)
			return true;
		if (obj1 == null)
			return false;
		if (getClass() != obj1.getClass())
			return false;
		ImportedTemplate otherTmpl = (ImportedTemplate) obj1;
		if (forObjectType == null) {
			if (otherTmpl.forObjectType != null)
				return false;
		} else if (!forObjectType.equals(otherTmpl.forObjectType))
			return false;
		if (id == null) {
			if (otherTmpl.id != null)
				return false;
		} else if (!id.equals(otherTmpl.id))
			return false;
		if (modifiedBy == null) {
			if (otherTmpl.modifiedBy != null)
				return false;
		} else if (!modifiedBy.equals(otherTmpl.modifiedBy))
			return false;
		if (modifiedOn == null) {
			if (otherTmpl.modifiedOn != null)
				return false;
		} else if (!modifiedOn.equals(otherTmpl.modifiedOn))
			return false;
		if (primary != otherTmpl.primary)
			return false;
		if (purpose == null) {
			if (otherTmpl.purpose != null)
				return false;
		} else if (!purpose.equals(otherTmpl.purpose))
			return false;
		if (title == null) {
			if (otherTmpl.title != null)
				return false;
		} else if (!title.equals(otherTmpl.title))
			return false;
		return true;
	}

}
