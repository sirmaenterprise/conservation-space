package com.sirma.itt.emf.cls.entity;

import java.util.Date;

import javax.persistence.Column;
import javax.persistence.MappedSuperclass;

import org.codehaus.jackson.map.annotate.JsonFilter;
import org.hibernate.annotations.Index;

/**
 * POJO representing a code in CLS. A code is an object containing all the common fields contained both in code lists
 * and code values.
 *
 * @author Mihail Radkov
 * @author Nikolay Velkov
 */
@MappedSuperclass
@JsonFilter("codeFilter")
public abstract class Code {

	/** The code's value. */
	@Column(length = 255, name = "VALUE")
	@Index(name = "codeValue")
	private String value;

	/** The master code's id. */
	@Column(length = 255, name = "MASTER_VALUE")
	private String masterValue;

	/** The code's first extra. */
	@Column(length = 1000, name = "EXTRA1")
	private String extra1;

	/** The code's second extra. */
	@Column(length = 1000, name = "EXTRA2")
	private String extra2;

	/** The code's third extra. */
	@Column(length = 1000, name = "EXTRA3")
	private String extra3;

	/** The code's fourth extra. */
	@Column(length = 1000, name = "EXTRA4")
	private String extra4;

	/** The code's fifth extra. */
	@Column(length = 1000, name = "EXTRA5")
	private String extra5;

	/** The code list's validity start. */
	@Column(name = "VALID_FROM")
	private Date validFrom;

	/** The code list's validity end. */
	@Column(name = "VALID_TO")
	private Date validTo;

	/** Date when the code is created. */
	@Column(name = "CREATED_ON")
	private Date createdOn;

	/**
	 * Gets the validity start date.
	 *
	 * @return the validity start date
	 */
	public Date getValidFrom() {
		return validFrom;
	}

	/**
	 * Sets the validity start date.
	 *
	 * @param validFrom
	 *            the validity start date
	 */
	public void setValidFrom(Date validFrom) {
		this.validFrom = validFrom;
	}

	/**
	 * Gets the validity end date.
	 *
	 * @return the validity end date
	 */
	public Date getValidTo() {
		return validTo;
	}

	/**
	 * Sets the validity end date.
	 *
	 * @param validTo
	 *            the validity end date
	 */
	public void setValidTo(Date validTo) {
		this.validTo = validTo;
	}

	/**
	 * Gets the code creation date.
	 *
	 * @return the code creation date
	 */
	public Date getCreatedOn() {
		return createdOn;
	}

	/**
	 * Sets the code creation date.
	 *
	 * @param createdOn
	 *            the code creation date
	 */
	public void setCreatedOn(Date createdOn) {
		this.createdOn = createdOn;
	}

	/**
	 * Gets the code's value.
	 *
	 * @return the code's value
	 */
	public String getValue() {
		return value;
	}

	/**
	 * Sets the code's value.
	 *
	 * @param value
	 *            the new code's value
	 */
	public void setValue(String value) {
		this.value = value;
	}

	/**
	 * Gets the code's extra1.
	 *
	 * @return the code's extra1
	 */
	public String getExtra1() {
		return extra1;
	}

	/**
	 * Sets the code's extra1.
	 *
	 * @param extra1
	 *            the code's new extra1
	 */
	public void setExtra1(String extra1) {
		this.extra1 = extra1;
	}

	/**
	 * Gets the code's extra2.
	 *
	 * @return the code's extra2
	 */
	public String getExtra2() {
		return extra2;
	}

	/**
	 * Sets the code's extra2.
	 *
	 * @param extra2
	 *            the new code's extra2
	 */
	public void setExtra2(String extra2) {
		this.extra2 = extra2;
	}

	/**
	 * Gets the code's extra3.
	 *
	 * @return the code's extra3
	 */
	public String getExtra3() {
		return extra3;
	}

	/**
	 * Sets the code's extra3.
	 *
	 * @param extra3
	 *            the new code's extra3
	 */
	public void setExtra3(String extra3) {
		this.extra3 = extra3;
	}

	/**
	 * Gets the code's extra4.
	 *
	 * @return the code's extra4
	 */
	public String getExtra4() {
		return extra4;
	}

	/**
	 * Sets the code's extra4.
	 *
	 * @param extra4
	 *            the new code's extra4
	 */
	public void setExtra4(String extra4) {
		this.extra4 = extra4;
	}

	/**
	 * Gets the code's extra5.
	 *
	 * @return the code's extra5
	 */
	public String getExtra5() {
		return extra5;
	}

	/**
	 * Sets the code's extra5.
	 *
	 * @param extra5
	 *            the new code's extra5
	 */
	public void setExtra5(String extra5) {
		this.extra5 = extra5;
	}

	/**
	 * Gets the code's master value.
	 *
	 * @return the code's master value
	 */
	public String getMasterValue() {
		return masterValue;
	}

	/**
	 * Sets the code's master value.
	 *
	 * @param masterValue
	 *            the new code's master value
	 */
	public void setMasterValue(String masterValue) {
		this.masterValue = masterValue;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((value == null) ? 0 : value.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		Code other = (Code) obj;
		if (value == null) {
			if (other.value != null)
				return false;
		} else if (!value.equals(other.value))
			return false;
		return true;
	}
}
