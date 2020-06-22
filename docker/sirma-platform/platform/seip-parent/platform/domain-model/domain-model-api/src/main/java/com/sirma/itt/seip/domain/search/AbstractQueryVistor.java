package com.sirma.itt.seip.domain.search;

import com.sirma.itt.seip.domain.search.Query.QueryBoost;
import com.sirma.itt.seip.domain.search.Query.Visitor;

/**
 * Basic implementation of query visitor.
 */
public abstract class AbstractQueryVistor implements Visitor {

	/** The builder. */
	protected StringBuilder builder = new StringBuilder(1024);

	@Override
	public void visitStart(Query query, QueryBoost boost) {

	}

	@Override
	public void visitEnd(Query query) {

	}

	@Override
	public StringBuilder getQuery() {
		return builder;
	}

	@Override
	public void setQuery(StringBuilder builder) {
		this.builder = builder;
	}

	@Override
	public void start() {

	}

	@Override
	public void end() {

	}
}
