package com.sirma.itt.seip.instance.properties.entity;

import java.io.Serializable;

import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.Index;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.persistence.OneToOne;
import javax.persistence.Table;

import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;

import com.sirma.itt.seip.db.PersistenceUnits;
import com.sirma.itt.seip.db.discovery.PersistenceUnitBinding;
import com.sirma.itt.seip.instance.properties.PropertyModelValue;

/**
 * Bean to convey <b>emf_archivedProperties</b> data.
 *
 * @author BBonev
 */
@PersistenceUnitBinding(PersistenceUnits.PRIMARY)
@Entity
@Table(name = "emf_archivedProperties", indexes = { @Index(name = "idx_aprE_propertyId", columnList = "propertyId"),
		@Index(name = "idx_aprE_propertyId_index", columnList = "propertyId,listIndex"),
		@Index(name = "idx_aprE_entityId_index", columnList = "bean_id,bean_type") })
@NamedQueries({
		@NamedQuery(name = ArchivedPropertyEntity.QUERY_ARCHIVED_PROPERTIES_BY_ENTITY_ID_KEY, query = ArchivedPropertyEntity.QUERY_ARCHIVED_PROPERTIES_BY_ENTITY_ID),
		@NamedQuery(name = ArchivedPropertyEntity.DELETE_ARCHIVED_PROPERTIES_KEY, query = ArchivedPropertyEntity.DELETE_ARCHIVED_PROPERTIES),
		@NamedQuery(name = ArchivedPropertyEntity.DELETE_ARCHIVED_PROPERTY_VALUES_KEY, query = ArchivedPropertyEntity.DELETE_ARCHIVED_PROPERTY_VALUES),
		@NamedQuery(name = ArchivedPropertyEntity.QUERY_ARCHIVED_PROPERTIES_KEY, query = ArchivedPropertyEntity.QUERY_ARCHIVED_PROPERTIES),
		@NamedQuery(name = ArchivedPropertyEntity.DELETE_ALL_PROPERTIES_FOR_ARCHIVED_BEAN_KEY, query = ArchivedPropertyEntity.DELETE_ALL_PROPERTIES_FOR_ARCHIVED_BEAN) })
public class ArchivedPropertyEntity extends BasePropertyEntity implements Serializable {

	private static final long serialVersionUID = 4815494725725712015L;

	/** Query ArchivedPropertyEntity by <code>beanType</code> and <code>beanId</code> */
	public static final String QUERY_ARCHIVED_PROPERTIES_BY_ENTITY_ID_KEY = "QUERY_ARCHIVED_PROPERTIES_BY_ENTITY_ID";
	static final String QUERY_ARCHIVED_PROPERTIES_BY_ENTITY_ID = "Select p from ArchivedPropertyEntity p inner join fetch p.value v where p.entityId.beanId=:beanId and p.entityId.beanType=:beanType order By p.id asc";

	/** Delete ArchivedPropertyEntitys by <code>id</code>s, <code>beanType</code> and <code>beanId</code>. */
	public static final String DELETE_ARCHIVED_PROPERTIES_KEY = "DELETE_ARCHIVED_PROPERTIES";
	static final String DELETE_ARCHIVED_PROPERTIES = "delete from ArchivedPropertyEntity p where p.key.propertyId in (:id) and p.entityId.beanId=:beanId and p.entityId.beanType=:beanType";

	/** Delete ArchivedPropertyValues by <code>id</code>s, <code>beanType</code> and <code>beanId</code>. */
	public static final String DELETE_ARCHIVED_PROPERTY_VALUES_KEY = "DELETE_ARCHIVED_PROPERTY_VALUES";
	static final String DELETE_ARCHIVED_PROPERTY_VALUES = "delete from ArchivedPropertyValue pv where pv.id in (select p.value.id from ArchivedPropertyEntity p where p.key.propertyId in (:id) and p.entityId.beanId=:beanId and p.entityId.beanType=:beanType)";

	/** Query ArchivedPropertyEntitys by <code>beanType</code>s and <code>beanId</code>s. */
	public static final String QUERY_ARCHIVED_PROPERTIES_KEY = "QUERY_ARCHIVED_PROPERTIES";
	static final String QUERY_ARCHIVED_PROPERTIES = "Select p from ArchivedPropertyEntity p inner join fetch p.value v where p.entityId.beanId in (:beanId) and p.entityId.beanType in (:beanType) order By p.id asc";

	/** Delete single ArchivedPropertyEntity by <code>beanType</code> and <code>beanId</code>. */
	public static final String DELETE_ALL_PROPERTIES_FOR_ARCHIVED_BEAN_KEY = "DELETE_ALL_PROPERTIES_FOR_ARCHIVED_BEAN";
	static final String DELETE_ALL_PROPERTIES_FOR_ARCHIVED_BEAN = "delete from ArchivedPropertyEntity p where p.entityId.beanId=:beanId and p.entityId.beanType=:beanType";

	// when the entity is deleted we remove also the value
	@OneToOne(cascade = {
			CascadeType.ALL }, fetch = FetchType.EAGER, orphanRemoval = true, targetEntity = ArchivedPropertyValue.class)
	@OnDelete(action = OnDeleteAction.CASCADE)
	private PropertyModelValue value;

	/**
	 * Required default constructor.
	 */
	public ArchivedPropertyEntity() {
		// nothing to do here but is required to be present
	}

	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder(128);
		builder
				.append("ArchivedPropertyEntity [id=")
					.append(getId())
					.append(", entityId=")
					.append(getEntityId())
					.append(", value=")
					.append(value)
					.append("]");
		return builder.toString();
	}

	/**
	 * Gets the value.
	 *
	 * @return the value
	 */
	@Override
	public PropertyModelValue getValue() {
		return value;
	}

	/**
	 * Sets the value.
	 *
	 * @param value
	 *            the new value
	 */
	@Override
	public void setValue(PropertyModelValue value) {
		this.value = value;
	}

	@Override
	public int hashCode() {
		return super.hashCode() + 7753;
	}

	@Override
	public boolean equals(Object obj) {
		if (obj instanceof ArchivedPropertyEntity) {
			return super.equals(obj);
		}
		return false;
	}

}
