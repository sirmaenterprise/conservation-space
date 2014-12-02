package com.sirma.itt.pm.domain;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.persistence.Table;

import com.sirma.itt.pm.services.DbQueryTemplatesPm;

/**
 * Holder class for all prepared statements.
 */
@Entity
@Table(name = "emf_dataTypeDefinition")
@NamedQueries(value = {
		@NamedQuery(name = DbQueryTemplatesPm.UPDATE_ALL_PROJECT_DEFINITIONS_KEY, query = DbQueryTemplatesPm.UPDATE_ALL_PROJECT_DEFINITIONS),
		@NamedQuery(name = DbQueryTemplatesPm.UPDATE_PROJECT_DEFINITIONS_KEY, query = DbQueryTemplatesPm.UPDATE_PROJECT_DEFINITIONS),
		@NamedQuery(name = DbQueryTemplatesPm.QUERY_PROJECT_DEFINITIONS_FOR_MIGRATION_KEY, query = DbQueryTemplatesPm.QUERY_PROJECT_DEFINITIONS_FOR_MIGRATION),

		@NamedQuery(name = DbQueryTemplatesPm.QUERY_PROJECT_BY_DMS_ID_KEY, query = DbQueryTemplatesPm.QUERY_PROJECT_BY_DMS_ID),
		@NamedQuery(name = DbQueryTemplatesPm.QUERY_ALL_PROJECT_ENTITY_IDS_KEY, query = DbQueryTemplatesPm.QUERY_ALL_PROJECT_ENTITY_IDS),
		@NamedQuery(name = DbQueryTemplatesPm.QUERY_PROJECT_ENTITIES_BY_DMS_ID_KEY, query = DbQueryTemplatesPm.QUERY_PROJECT_ENTITIES_BY_DMS_ID),
		@NamedQuery(name = DbQueryTemplatesPm.QUERY_PROJECT_ENTITIES_BY_ID_KEY, query = DbQueryTemplatesPm.QUERY_PROJECT_ENTITIES_BY_ID)
})
public class PmQueryDao {

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