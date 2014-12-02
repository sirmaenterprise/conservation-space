package com.sirma.itt.objects.domain;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;

/**
 * Holder class for all prepared statements.
 */
@Entity
@Table(name = "emf_dataTypeDefinition")
// @NamedQueries(value = {
//		@NamedQuery(name = DbQueryTemplatesObjects.QUERY_OBJECT_BY_DMS_ID_KEY, query = DbQueryTemplatesObjects.QUERY_OBJECT_BY_DMS_ID),
//		@NamedQuery(name = DbQueryTemplatesObjects.QUERY_ALL_OBJECT_ENTITY_IDS_KEY, query = DbQueryTemplatesObjects.QUERY_ALL_OBJECT_ENTITY_IDS),
//		@NamedQuery(name = DbQueryTemplatesObjects.QUERY_OBJECT_ENTITIES_BY_DMS_ID_KEY, query = DbQueryTemplatesObjects.QUERY_OBJECT_ENTITIES_BY_DMS_ID),
//		@NamedQuery(name = DbQueryTemplatesObjects.QUERY_OBJECT_ENTITIES_BY_ID_KEY, query = DbQueryTemplatesObjects.QUERY_OBJECT_ENTITIES_BY_ID)
// })
public class ObjectsQueryDao {

	/**
	 * Sets the id.
	 *
	 * @param id
	 *            the id to set
	 */
	public void setId(Long id) {
		// empty method. Nothing to set
	}

	/**
	 * Gets the id.
	 *
	 * @return the id
	 */
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "id")
	public Long getId() {
		return 0L;
	}
}