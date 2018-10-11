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
 * Bean to convey <b>emf_properties</b> data.
 *
 * @author BBonev
 */
@PersistenceUnitBinding(PersistenceUnits.PRIMARY)
@Entity
@Table(name = "emf_properties", indexes = {
		@Index(name = "idx_prE_propertyId", columnList = "propertyId"),
		@Index(name = "idx_prE_propertyId_index", columnList = "propertyId,listIndex"),
		@Index(name = "idx_prE_entityId_index", columnList = "bean_id,bean_type") })
@NamedQueries({
		@NamedQuery(name = PropertyEntity.QUERY_PROPERTIES_BY_ENTITY_ID_KEY, query = PropertyEntity.QUERY_PROPERTIES_BY_ENTITY_ID),
		@NamedQuery(name = PropertyEntity.QUERY_PROPERTIES_KEY, query = PropertyEntity.QUERY_PROPERTIES),
		@NamedQuery(name = PropertyEntity.DELETE_PROPERTIES_KEY, query = PropertyEntity.DELETE_PROPERTIES),
		@NamedQuery(name = PropertyEntity.DELETE_ALL_PROPERTIES_FOR_BEAN_KEY, query = PropertyEntity.DELETE_ALL_PROPERTIES_FOR_BEAN) })
public class PropertyEntity extends BasePropertyEntity implements Serializable {

	private static final long serialVersionUID = 4516465742517264790L;

	/** Query instance properties ({@link PropertyEntity}) by <code>beanId</code> and <code>beanType</code> */
	public static final String QUERY_PROPERTIES_BY_ENTITY_ID_KEY = "QUERY_PROPERTIES_BY_ENTITY_ID";
	static final String QUERY_PROPERTIES_BY_ENTITY_ID = "Select p from PropertyEntity p inner join fetch p.value v where p.entityId.beanId=:beanId and p.entityId.beanType=:beanType order By p.id asc";

	/** Delete {@link PropertyEntity}s by <code>id</code>s, <code>beanType</code> and <code>beanId</code> */
	public static final String DELETE_PROPERTIES_KEY = "DELETE_PROPERTIES";
	static final String DELETE_PROPERTIES = "delete from PropertyEntity p where p.key.propertyId in (:id) and p.entityId.beanId=:beanId and p.entityId.beanType=:beanType";

	/** Delete instance properties by <code>beanId</code> and <code>beanType</code> */
	public static final String DELETE_ALL_PROPERTIES_FOR_BEAN_KEY = "DELETE_ALL_PROPERTIES_FOR_BEAN";
	static final String DELETE_ALL_PROPERTIES_FOR_BEAN = "delete from PropertyEntity p where p.entityId.beanId=:beanId and p.entityId.beanType=:beanType";

	/** Query multiple instance properties by by <code>beanId</code>s and <code>beanType</code>s */
	public static final String QUERY_PROPERTIES_KEY = "QUERY_PROPERTIES";
	static final String QUERY_PROPERTIES = "Select p from PropertyEntity p inner join fetch p.value v left outer join fetch v.serializableValue as sv where p.entityId.beanId in (:beanId) and p.entityId.beanType in (:beanType) order By p.id asc";

	// when the entity is deleted we remove also the value
	@OneToOne(cascade = {
			CascadeType.ALL }, fetch = FetchType.EAGER, orphanRemoval = true, targetEntity = PropertyValue.class)
	@OnDelete(action = OnDeleteAction.CASCADE)
	private PropertyModelValue value;

	/**
	 * Required default constructor.
	 */
	public PropertyEntity() {
		// nothing to do here
	}

	@Override
	public String toString() {
		return new StringBuilder(128)
				.append("PropertyEntity [id=")
				.append(getKey())
				.append(", entityId=")
				.append(getEntityId())
				.append(", value=")
				.append(value)
				.append("]")
				.toString();
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

	/**
	 * Gets the value.
	 *
	 * @return the value
	 */
	@Override
	public PropertyModelValue getValue() {
		return value;
	}

	@Override
	public int hashCode() {
		return super.hashCode() + 6373;
	}

	@Override
	public boolean equals(Object obj) {
		if (obj instanceof PropertyEntity) {
			return super.equals(obj);
		}
		return false;
	}

}
