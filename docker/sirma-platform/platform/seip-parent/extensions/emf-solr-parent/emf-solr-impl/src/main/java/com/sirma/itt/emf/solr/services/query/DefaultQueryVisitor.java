package com.sirma.itt.emf.solr.services.query;

import com.sirma.itt.seip.domain.search.AbstractQueryVistor;
import com.sirma.itt.seip.domain.search.Query;
import com.sirma.itt.seip.domain.search.Query.QueryBoost;

/**
 * Default visitor implementation for queries.
 *
 * @author Borislav Banchev
 */
public abstract class DefaultQueryVisitor extends AbstractQueryVistor {

	/*
	 * (non-Javadoc)
	 * @see com.sirma.itt.cmf.search.Query.Visitor#visitStart(com.sirma.itt.cmf .search.Query,
	 * com.sirma.itt.cmf.search.Query.Boost)
	 */
	/**
	 * {@inheritDoc}
	 */
	@Override
	public void visitStart(Query query, QueryBoost boost) {

		int lastIndexOf = getQuery().lastIndexOf("(");
		if (lastIndexOf > 0) {
			String substring = getQuery().substring(lastIndexOf + 1);
			if (substring.trim().length() > 0) {
				getQuery().append(query.getBoost().toString());
			} else if (query.getBoost() == QueryBoost.EXCLUDE) {
				getQuery().append(query.getBoost().toString());
			}
		} else if (query.getBoost() == QueryBoost.EXCLUDE) {
			getQuery().append(query.getBoost().toString());
		}

		getQuery().append("(");
	}

	/*
	 * (non-Javadoc)
	 * @see com.sirma.itt.cmf.search.Query.Visitor#visitEnd(com.sirma.itt.cmf .search.Query)
	 */
	/**
	 * {@inheritDoc}
	 */
	@Override
	public void visitEnd(Query query) {
		getQuery().append(")");
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public abstract void visit(Query query) throws Exception;

}
