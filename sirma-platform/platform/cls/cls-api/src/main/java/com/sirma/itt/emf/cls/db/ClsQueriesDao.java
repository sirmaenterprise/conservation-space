package com.sirma.itt.emf.cls.db;

import javax.persistence.Column;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.persistence.Table;

import com.sirma.itt.seip.Entity;

/**
 * Entity class holding named queries defined in {@link ClsQueries}.
 *
 * @author Mihail Radkov
 */
@javax.persistence.Entity
@Table(name = "cls_dataTypeDefinition")
@NamedQueries(value = { @NamedQuery(name = ClsQueries.QUERY_CODELISTS_KEY, query = ClsQueries.QUERY_CODELISTS),
		@NamedQuery(name = ClsQueries.QUERY_CODELIST_BY_ID_KEY, query = ClsQueries.QUERY_CODELIST_BY_ID),
		@NamedQuery(name = ClsQueries.QUERY_CODEVALUE_BY_ID_KEY, query = ClsQueries.QUERY_CODEVALUE_BY_ID),
		@NamedQuery(name = ClsQueries.QUERY_CODEVALUE_BY_CL_ID_KEY, query = ClsQueries.QUERY_CODEVALUE_BY_CL_ID),
		@NamedQuery(name = ClsQueries.QUERY_LAST_CODEVALUE, query = ClsQueries.QUERY_LAST_CODEVALUE),
		@NamedQuery(name = ClsQueries.DELETE_CODELIST_DESCRIPTION_BY_LANGUAGE, query = ClsQueries.DELETE_CODELIST_DESCRIPTION_BY_LANGUAGE) })
public class ClsQueriesDao implements Entity<Long> {

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
