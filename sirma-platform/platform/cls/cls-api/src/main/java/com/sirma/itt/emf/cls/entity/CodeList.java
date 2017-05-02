package com.sirma.itt.emf.cls.entity;

import java.io.Serializable;
import java.util.List;
import java.util.Map;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.OneToMany;
import javax.persistence.Table;
import javax.persistence.Transient;

import org.codehaus.jackson.annotate.JsonIgnoreProperties;
import org.codehaus.jackson.map.annotate.JsonSerialize;
import org.codehaus.jackson.map.annotate.JsonSerialize.Inclusion;
import org.hibernate.annotations.Fetch;
import org.hibernate.annotations.FetchMode;

import com.sirma.itt.seip.domain.Node;
import com.sirma.itt.seip.domain.PathElement;
import com.sirma.itt.seip.domain.instance.Instance;
import com.sirma.itt.seip.domain.instance.InstanceReference;

/**
 * Entity POJO representing a code list. Extends the base class {@link Code} that contains the common attributes. The
 * descriptions are eagerly fetched from the database. <br>
 * <br>
 * <b>NOTE</b>: The DB ID is not serialized to JSON.<br>
 * <b>NOTE</b>: Attributes with null values are not serialized to JSON.<br>
 * <b>NOTE</b>: Non specific to CLS attributes are not serialized to JSON.
 *
 * @author Mihail Radkov
 * @author Nikolay Velkov
 */
@Entity
@Table(name = "CLS_CODELIST")
@JsonIgnoreProperties(value = { "id", "properties", "path", "identifier", "revision", "parentElement" })
@JsonSerialize(include = Inclusion.NON_NULL)
public class CodeList extends Code implements Instance {

	/** Auto generated serial version UID. */
	private static final long serialVersionUID = 3782224564730632920L;

	/** The code list's DB ID. */
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	/** The code list's descriptions. */
	@OneToMany(cascade = CascadeType.ALL, fetch = FetchType.EAGER)
	@JoinColumn(name = "codeId")
	@Fetch(value = FetchMode.SUBSELECT)
	private List<CodeListDescription> descriptions;

	/**
	 * The code list's code values. They list is transient and they are not persisted in the database, because we have a
	 * case where we need to retrieve only the codelists without their codevalues. If we use FetchType.LAZY, it still
	 * won't work because when jackson goes over the fields of this class with reflection, it will call this list and
	 * the list will load from the database anyways. Therefore the code values are loaded on request by seperately doing
	 * 1 query for each code list for which code values are requested.
	 */
	@Transient
	private transient List<CodeValue> codeValues;

	/** The code list's display type. */
	@Column(name = "DISPLAY_TYPE")
	private Short displayType;

	/** The code list's sort by filter. */
	@Column(length = 255, name = "SORT_BY")
	private String sortBy;

	/**
	 * Gets the code list's display type.
	 *
	 * @return the code list's display type
	 */
	public Short getDisplayType() {
		return displayType;
	}

	/**
	 * Sets the code list's display type.
	 *
	 * @param displayType
	 *            the new code list'sdisplay type
	 */
	public void setDisplayType(Short displayType) {
		this.displayType = displayType;
	}

	/**
	 * Gets the code list's sort by filter.
	 *
	 * @return the code list's sort by filter
	 */
	public String getSortBy() {
		return sortBy;
	}

	/**
	 * Sets the code list's sort by filter.
	 *
	 * @param sortBy
	 *            the new code list's sort by filter
	 */
	public void setSortBy(String sortBy) {
		this.sortBy = sortBy;
	}

	/**
	 * Gets the code list's code values.
	 *
	 * @return the code list's code values
	 */
	public List<CodeValue> getCodeValues() {
		return codeValues;
	}

	/**
	 * Sets the code list's code values.
	 *
	 * @param codeValues
	 *            the new code list's code values
	 */
	public void setCodeValues(List<CodeValue> codeValues) {
		this.codeValues = codeValues;
	}

	/**
	 * Gets the code list'sdescriptions.
	 *
	 * @return the code list's descriptions
	 */
	public List<CodeListDescription> getDescriptions() {
		return descriptions;
	}

	/**
	 * Sets the code list's descriptions.
	 *
	 * @param descriptions
	 *            the new code list'sdescriptions
	 */
	public void setDescriptions(List<CodeListDescription> descriptions) {
		this.descriptions = descriptions;
	}

	@Override
	public void setRevision(Long revision) {
		// doesn't need implementation at this point
	}

	@Override
	public InstanceReference toReference() {
		return null;
	}

	@Override
	public Map<String, Serializable> getProperties() {
		return null;
	}

	@Override
	public void setProperties(Map<String, Serializable> properties) {
		// doesn't need implementation at this point
	}

	@Override
	public Long getRevision() {
		return null;
	}

	@Override
	public PathElement getParentElement() {
		return null;
	}

	@Override
	public String getPath() {
		return null;
	}

	@Override
	public boolean hasChildren() {
		return false;
	}

	@Override
	public Node getChild(String name) {
		return null;
	}

	@Override
	public String getIdentifier() {
		return null;
	}

	@Override
	public void setIdentifier(String identifier) {
		// doesn't need implementation at this point
	}

	@Override
	public Serializable getId() {
		return id;
	}

	@Override
	public void setId(Serializable id) {
		this.id = Long.valueOf(id.toString());
	}

	@Override
	public boolean isDeleted() {
		return false;
	}

}
