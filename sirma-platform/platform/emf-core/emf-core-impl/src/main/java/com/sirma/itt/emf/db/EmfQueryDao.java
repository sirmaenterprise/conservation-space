package com.sirma.itt.emf.db;

import javax.persistence.Column;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.persistence.Table;

import com.sirma.itt.seip.Entity;

/**
 * Holder class for all prepared statements.
 *
 * @author BBonev
 */
@javax.persistence.Entity(name = "EmfQueryDao")
@Table(name = "emf_dataTypeDefinition")
@NamedQueries(value = {

		@NamedQuery(name = EmfQueries.QUERY_SEQUENCES_KEY, query = EmfQueries.QUERY_SEQUENCES),
		@NamedQuery(name = EmfQueries.QUERY_SEQUENCE_BY_NAME_KEY, query = EmfQueries.QUERY_SEQUENCE_BY_NAME),
		@NamedQuery(name = EmfQueries.UPDATE_SEQUENCES_ENTRY_KEY, query = EmfQueries.UPDATE_SEQUENCES_ENTRY),

		@NamedQuery(name = EmfQueries.QUERY_LINK_BY_SRC_AND_IDS_KEY, query = EmfQueries.QUERY_LINK_BY_SRC_AND_IDS),
		@NamedQuery(name = EmfQueries.QUERY_LINK_BY_SRC_KEY, query = EmfQueries.QUERY_LINK_BY_SRC),
		@NamedQuery(name = EmfQueries.QUERY_LINK_BY_TARGET_AND_IDS_KEY, query = EmfQueries.QUERY_LINK_BY_TARGET_AND_IDS),
		@NamedQuery(name = EmfQueries.QUERY_LINK_BY_TARGET_KEY, query = EmfQueries.QUERY_LINK_BY_TARGET),
		@NamedQuery(name = EmfQueries.QUERY_LINK_BY_TARGET_AND_SOURCE_AND_IDS_KEY, query = EmfQueries.QUERY_LINK_BY_TARGET_AND_SOURCE_AND_IDS),
		@NamedQuery(name = EmfQueries.QUERY_LINK_BY_TARGET_AND_SOURCE_KEY, query = EmfQueries.QUERY_LINK_BY_TARGET_AND_SOURCE),
		@NamedQuery(name = EmfQueries.DELETE_ALL_LINKS_FOR_INSTANCE_KEY, query = EmfQueries.DELETE_ALL_LINKS_FOR_INSTANCE),
		@NamedQuery(name = EmfQueries.DELETE_LINKS_FOR_INSTANCE_KEY, query = EmfQueries.DELETE_LINKS_FOR_INSTANCE),
		@NamedQuery(name = EmfQueries.DELETE_LINK_FOR_INSTANCE_KEY, query = EmfQueries.DELETE_LINK_FOR_INSTANCE),
		@NamedQuery(name = EmfQueries.DELETE_LINK_FOR_INSTANCE_AND_TYPE_KEY, query = EmfQueries.DELETE_LINK_FOR_INSTANCE_AND_TYPE),
		@NamedQuery(name = EmfQueries.QUERY_LABELS_BY_ID_KEY, query = EmfQueries.QUERY_LABELS_BY_ID),
		@NamedQuery(name = EmfQueries.QUERY_LABEL_BY_ID_KEY, query = EmfQueries.QUERY_LABEL_BY_ID) })
public class EmfQueryDao implements Entity<Long> {

	/**
	 * Gets the id.
	 *
	 * @return the id
	 */
	@Override
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "id")
	public Long getId() {
		return 0L;
	}

	@Override
	public void setId(Long id) {
		// nothing to do here
	}

}
