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
 * Entity POJO representing a code value. The code value descriptions are eagerly fetched from the database. <br>
 * <br>
 * <b>NOTE</b>: The DB ID is not serialized to JSON.<br>
 * <b>NOTE</b>: Attributes with null values are not serialized to JSON.<br>
 * <b>NOTE</b>: Non specific to CLS attributes are not serialized to JSON.
 *
 * @author Mihail Radkov
 * @author Nikolay Velkov
 */
@Entity
@Table(name = "CLS_CODEVALUE")
@JsonIgnoreProperties(value = { "id", "properties", "path", "identifier", "revision", "parentElement" })
@JsonSerialize(include = Inclusion.NON_NULL)
public class CodeValue extends Code implements Instance {

	/** Comment for serialVersionUID. */
	private static final long serialVersionUID = -7678706140149829487L;

	/** The id. */
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	/** The descriptions. */
	@OneToMany(cascade = CascadeType.ALL, fetch = FetchType.EAGER)
	@JoinColumn(name = "codeId")
	@Fetch(value = FetchMode.SUBSELECT)
	private List<CodeValueDescription> descriptions;

	/** The code's order. */
	// TODO: RENAME !
	@Column(length = 10, name = "ORDYR")
	private int order;

	private String codeListId;

	/**
	 * Gets the order.
	 *
	 * @return the order
	 */
	public int getOrder() {
		return order;
	}

	/**
	 * Sets the order.
	 *
	 * @param order
	 *            the new order
	 */
	public void setOrder(int order) {
		this.order = order;
	}

	/**
	 * Gets the descriptions.
	 *
	 * @return the descriptions
	 */
	public List<CodeValueDescription> getDescriptions() {
		return descriptions;
	}

	/**
	 * Sets the descriptions.
	 *
	 * @param descriptions
	 *            the new descriptions
	 */
	public void setDescriptions(List<CodeValueDescription> descriptions) {
		this.descriptions = descriptions;
	}

	public String getCodeListId() {
		return codeListId;
	}

	public void setCodeListId(String codeListId) {
		this.codeListId = codeListId;
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
		this.id = Long.parseLong(id.toString());
	}

	@Override
	public boolean isDeleted() {
		return false;
	}

}
