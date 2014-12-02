package com.sirma.itt.emf.search;

import com.sirma.itt.emf.search.Query.QueryBoost;
import com.sirma.itt.emf.search.Query.Visitor;

/**
 * Basic implementation of query visitor.
 */
public abstract class AbstractQueryVistor implements Visitor {

	/** The builder. */
	protected StringBuffer builder = new StringBuffer();

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.sirma.itt.cmf.search.Query.Visitor#visitStart(com.sirma.itt.cmf
	 * .search.Query, com.sirma.itt.cmf.search.Query.Boost)
	 */
	@Override
	public void visitStart(Query query, QueryBoost boost) {

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.sirma.itt.cmf.search.Query.Visitor#visitEnd(com.sirma.itt.cmf
	 * .search.Query)
	 */
	@Override
	public void visitEnd(Query query) {

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * com.sirma.itt.cmf.search.Query.Visitor#visit(com.sirma.itt.cmf.search
	 * .Query)
	 */
	@Override
	public abstract void visit(Query query) throws Exception;

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.sirma.itt.cmf.search.Query.Visitor#getQuery()
	 */
	@Override
	public StringBuffer getQuery() {
		return builder;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * com.sirma.itt.cmf.search.Query.Visitor#setQuery(java.lang.CharSequence )
	 */
	@Override
	public void setQuery(StringBuffer builder) {
		this.builder = builder;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.sirma.itt.cmf.search.Query.Visitor#start()
	 */
	@Override
	public void start() {

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.sirma.itt.cmf.search.Query.Visitor#end()
	 */
	@Override
	public void end() {

	}
}