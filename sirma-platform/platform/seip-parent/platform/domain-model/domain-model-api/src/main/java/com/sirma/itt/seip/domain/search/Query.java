package com.sirma.itt.seip.domain.search;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The Class Query provides facade for searching by encapsulates the search criteria. The api is intended to build tree.
 * Every operation for adding returns the currently added element so chain can be used. <br>
 * REVIEW: good idea is to change the behavior of the class to simplify the tree building in a way to be similar to
 * hibernate criteria API <br>
 * REVIEW implementation should allow adding multiple clauses on a single level (expr1 and expr2 and expr3..)
 *
 * @author borislav banchev
 */
public class Query implements Serializable {

	private static final long serialVersionUID = 2046137828877524473L;

	private static final Logger LOGGER = LoggerFactory.getLogger(Query.class);

	/** The boost. */
	private QueryBoost boost = QueryBoost.NEW;

	/** The value. */
	private final Serializable value;

	/** The key. */
	private final String key;

	/** The and. */
	private Query and;

	/** The or. */
	private Query or;

	/** The parent. */
	private Query parent;

	/** the current level. */
	private int level = 0;

	/** The starting. */
	private int starting = 0;

	/** The ending. */
	private int ending = 0;

	/**
	 * Instantiates a new query.
	 */
	private Query() {
		key = null;
		value = null;
	}

	/**
	 * The QueryPrinter iterates the tree and build string representing the structure.
	 */
	public static class QueryPrinter {

		/**
		 * Iterates the query and builds the model.
		 *
		 * @param query
		 *            the root of the query
		 * @return the string builder with the result representation
		 */
		public static StringBuilder iterateQuery(Query query) {
			if (query == null) {
				return null;
			}
			Query root = query.getRoot();
			int maxLevel = QueryPrinter.maxLevel(root);
			StringBuilder result = new StringBuilder();
			visitNodeInternal(result, Collections.singletonList(root), 1, maxLevel);
			return result;
		}

		/**
		 * Visit the node internal.
		 *
		 * @param result
		 *            the final result
		 * @param nodes
		 *            the nodes to process on this level
		 * @param level
		 *            the current level
		 * @param maxLevel
		 *            the max level of tree
		 */
		private static void visitNodeInternal(StringBuilder result, List<Query> nodes, int level, int maxLevel) {
			if (nodes.isEmpty() || QueryPrinter.isAllElementsNull(nodes)) {
				return;
			}

			int floor = maxLevel - level;
			int firstSpaces = (int) Math.pow(2, floor) - 1;
			int betweenSpaces = (int) Math.pow(2, floor + 1) - 1;

			QueryPrinter.printWhitespaces(result, firstSpaces);

			List<Query> newNodes = new ArrayList<>();
			for (Query node : nodes) {
				if (node != null) {
					if (node.getKey() != null) {
						result.append(node.getKey());
					}
					if (node.or != null && node.or.value instanceof Query) {
						newNodes.add((Query) node.or.value);
					} else {
						newNodes.add(node.or);
					}
					if (node.and != null && node.and.value instanceof Query) {
						newNodes.add((Query) node.and.value);
					} else {
						newNodes.add(node.and);
					}

				} else {
					newNodes.add(null);
					newNodes.add(null);
					result.append(" ");
				}

				QueryPrinter.printWhitespaces(result, betweenSpaces);
			}
			result.append("\n");
			visitNodeInternal(result, newNodes, level + 1, maxLevel);
		}

		/**
		 * Prints the whitespaces.
		 *
		 * @param result
		 *            the result
		 * @param count
		 *            the count
		 */
		private static void printWhitespaces(StringBuilder result, int count) {
			for (int i = 0; i < count; i++) {
				// char array
				result.append(" ");
			}
		}

		/**
		 * Calculates Max level.
		 *
		 * @param node
		 *            the node to go deeper from
		 * @return the levels number
		 */
		private static int maxLevel(Query node) {
			if (node == null) {
				return 0;
			}
			return Math.max(QueryPrinter.maxLevel(node.or), QueryPrinter.maxLevel(node.and)) + 1;
		}

		/**
		 * Checks if is all elements are null.
		 *
		 * @param <T>
		 *            the generic type
		 * @param list
		 *            the list
		 * @return true, if is all elements null
		 */
		private static <T> boolean isAllElementsNull(List<T> list) {
			for (Object object : list) {
				if (object != null) {
					return false;
				}
			}

			return true;
		}

	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + (parent == null ? 0 : parent.hashCode());
		result = prime * result + (boost == null ? 0 : boost.hashCode());
		result = prime * result + (key == null ? 0 : key.hashCode());
		result = prime * result + (value == null ? 0 : value.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		if (obj == null) {
			return false;
		}
		if (getClass() != obj.getClass()) {
			return false;
		}
		Query other = (Query) obj;
		if (parent == null) {
			if (other.parent != null) {
				return false;
			}
		} else if (!parent.equals(other.parent)) {
			return false;
		}
		if (boost != other.boost) {
			return false;
		}
		if (key == null) {
			if (other.key != null) {
				return false;
			}
		} else if (!key.equals(other.key)) {
			return false;
		}
		if (value == null) {
			if (other.value != null) {
				return false;
			}
		} else if (!value.equals(other.value)) {
			return false;
		}
		return true;
	}

	/**
	 * The Interface Visitor is facade for iterating tree.
	 */
	public interface Visitor {

		/**
		 * Visit simple entry
		 *
		 * @param query
		 *            the query
		 * @throws Exception
		 *             the exception
		 */
		void visit(Query query) throws Exception;

		/**
		 * Visit starting element
		 *
		 * @param query
		 *            the query
		 * @param boost
		 *            the boost
		 * @throws Exception
		 *             the exception
		 */
		void visitStart(Query query, QueryBoost boost) throws Exception;

		/**
		 * Visit ending element
		 *
		 * @param query
		 *            the query
		 * @throws Exception
		 *             the exception
		 */
		void visitEnd(Query query) throws Exception;

		/**
		 * Gets the query.
		 *
		 * @return the query
		 */
		StringBuilder getQuery();

		/**
		 * Sets the query.
		 *
		 * @param query
		 *            the new query
		 */
		void setQuery(StringBuilder query);

		/**
		 * Invoked on query iteration start.
		 */
		void start();

		/**
		 * Invoked on query iteration end - i.e on exit
		 */
		void end();
	}

	/**
	 * Gets the empty.
	 *
	 * @return the empty
	 */
	public static Query getEmpty() {
		return new Query();
	}

	/**
	 * The Enum Boost representing the actual join phrase. <br>
	 * REVIEW: rename the class to more suitable name
	 */
	public enum QueryBoost {

		/** The new. */
		NEW(" "), /** The include and. */
		INCLUDE_AND(" AND "), /** The include or. */
		INCLUDE_OR(" OR "), /** The exclude or. */
		EXCLUDE_OR(" OR NOT "), /** The exclude and. */
		EXCLUDE_AND(" AND NOT "), /** The exclude. */
		EXCLUDE(" NOT ");
		/** The data. */
		private final String data;

		/**
		 * Instantiates a new boost.
		 *
		 * @param data
		 *            the data
		 */
		private QueryBoost(String data) {
			this.data = data;
		}

		@Override
		public String toString() {
			return data;
		}
	}

	/**
	 * Instantiates a new query.
	 *
	 * @param key
	 *            the key
	 * @param value
	 *            the value
	 */
	public Query(String key, Serializable value) {
		this.key = key;
		this.value = value;
	}

	/**
	 * Instantiates a new query with starting param
	 *
	 * @param key
	 *            the key
	 * @param value
	 *            the value
	 * @param start
	 *            is whether is this the first clause.
	 */
	public Query(String key, Serializable value, boolean start) {
		this.key = key;
		this.value = value;
		if (start) {
			starting++;
		}
	}

	/**
	 * Marks query as ending.
	 *
	 * @return the current query
	 */
	public Query end() {
		ending++;
		return this;
	}

	/**
	 * Instantiates a new query.
	 *
	 * @param key
	 *            the key
	 * @param value
	 *            the value
	 * @param boost
	 *            the boost to use
	 */
	public Query(String key, Serializable value, QueryBoost boost) {
		this.key = key;
		this.value = value;
		this.boost = boost;
	}

	/**
	 * And internal using {@link #and}
	 *
	 * @param query
	 *            the query to append
	 * @return the query
	 */
	private Query andInternal(Query query) {
		if (and != null) {
			throw new RuntimeException("this.AND and");
		}
		and = query;
		query.parent = this;
		query.level = level + 1;
		query.setBoost(QueryBoost.INCLUDE_AND);
		return query;
	}

	/**
	 * Or internal using {@link #or}
	 *
	 * @param query
	 *            the query
	 * @return the query
	 */
	private Query orInternal(Query query) {
		if (or != null) {
			throw new RuntimeException("this.OR or");
		}
		or = query;
		query.parent = this;
		query.level = level + 1;
		query.setBoost(QueryBoost.INCLUDE_OR);
		return query;
	}

	/**
	 * And not internal using {@link #and}
	 *
	 * @param query
	 *            the query
	 * @return the query
	 */
	private Query andNotInternal(Query query) {
		if (and != null) {
			throw new RuntimeException("this.AND and");
		}
		Query parent = this;
		if (value instanceof Query) {
			parent = (Query) value;
		} else {
			parent = this;
		}
		parent.and = query;
		query.parent = parent;
		query.level = level + 1;
		query.setBoost(QueryBoost.EXCLUDE_AND);
		return query;
	}

	/**
	 * Gets the root.
	 *
	 * @return the root
	 */
	public Query getRoot() {
		return getRoot(this);
	}

	/**
	 * Gets the root.
	 *
	 * @param query
	 *            the query
	 * @return the root
	 */
	public Query getRoot(Query query) {
		if (query.parent == null) {
			return query;
		}
		return getRoot(query.parent);
	}

	/**
	 * Gets the entry.
	 *
	 * @param key
	 *            the key
	 * @return the entry
	 */
	public Query getEntry(final String key) {
		List<Query> entries = getEntries(key, true);
		if (entries.size() == 1) {
			return entries.get(0);
		}
		return null;
	}

	/**
	 * Gets the entries.
	 *
	 * @param key
	 *            the key
	 * @return the entries
	 */
	public List<Query> getEntries(final String key) {
		return getEntries(key, false);
	}

	/**
	 * Gets the entries.
	 *
	 * @param key
	 *            the key
	 * @param breakOnFirst
	 *            the break on first
	 * @return the entries
	 */
	public List<Query> getEntries(final String key, final boolean breakOnFirst) {
		Query root = getRoot(this);
		final List<Query> queries = new ArrayList<>();
		try {
			visitInternal(root, new AbstractQueryVistor() {

				@Override
				public void visit(Query query) throws Exception {
					if (key.equals(query.key)) {
						queries.add(query);
						if (breakOnFirst) {
							throw new IllegalArgumentException("END");
						}
					}
				}

				@Override
				public StringBuilder getQuery() {
					return null;
				}

			});
		} catch (Exception e) {
			LOGGER.error("Error during iteration of query", e);
		}
		return queries;

	}

	/**
	 * Or not internal using {@link #or}
	 *
	 * @param query
	 *            the query
	 * @return the query
	 */
	private Query orNotInternal(Query query) {
		if (or != null) {
			throw new IllegalArgumentException("this.OR and");
		}
		Query parent = this;
		if (key == null || value instanceof Query) {
			parent = (Query) value;
		} else {
			parent = this;
		}
		parent.or = query;
		query.parent = parent;
		query.level = parent.level + 1;
		query.setBoost(QueryBoost.EXCLUDE_OR);
		return query;
	}

	/**
	 * And.
	 *
	 * @param key
	 *            the key
	 * @param value
	 *            the value
	 * @return the query
	 */
	public Query and(String key, Serializable value) {
		return andInternal(new Query(key, value));
	}

	/**
	 * Or.
	 *
	 * @param key
	 *            the key
	 * @param value
	 *            the value
	 * @return the query
	 */
	public Query or(String key, Serializable value) {
		return orInternal(new Query(key, value));
	}

	/**
	 * And not.
	 *
	 * @param key
	 *            the key
	 * @param value
	 *            the value
	 * @return the query
	 */
	public Query andNot(String key, Serializable value) {
		return andNotInternal(new Query(key, value));
	}

	/**
	 * Or not.
	 *
	 * @param key
	 *            the key
	 * @param value
	 *            the value
	 * @return the query
	 */
	public Query orNot(String key, Serializable value) {
		return orNotInternal(new Query(key, value));
	}

	/**
	 * And to query using {@link #and}
	 *
	 * @param query
	 *            the query
	 * @return the query added
	 */
	public Query and(Query query) {
		Query root = getRoot(query);
		root.starting++;
		andInternal(root);
		query.ending++;
		return query;

	}

	/**
	 * Or to query using {@link #or}
	 *
	 * @param query
	 *            the query
	 * @return the query added
	 */
	public Query or(Query query) {
		Query root = getRoot(query);
		root.starting++;
		orInternal(root);
		query.ending++;
		return query;
	}

	/**
	 * And not to query using {@link #and}
	 *
	 * @param query
	 *            the query
	 * @return the query added
	 */
	public Query andNot(Query query) {
		Query root = getRoot(query);
		root.starting++;
		andNotInternal(root);
		query.ending++;
		return query;
	}

	/**
	 * OR not to query using {@link #or}
	 *
	 * @param query
	 *            the query
	 * @return the query
	 */
	public Query orNot(Query query) {
		Query root = getRoot(query);
		root.starting++;
		orNotInternal(root);
		query.ending++;
		return query;
	}

	/**
	 * Gets the query by invoking visitor method during iteration.
	 *
	 * @param visitor
	 *            the visitor
	 * @throws Exception
	 *             the exception
	 */
	public void visit(Visitor visitor) throws Exception {
		if (visitor == null) {
			throw new RuntimeException("Visitor should be a valid object!");
		}
		// lazy init
		if (visitor.getQuery() == null) {
			visitor.setQuery(new StringBuilder(1024));
		}
		// get root
		Query parent = this;
		while (parent.parent != null) {
			parent = parent.parent;
		}
		visitor.start();
		visitInternal(parent, visitor);
		visitor.end();
	}

	/*
	 * (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return key == null ? value == null ? "NULL" : value.toString()
				: key + ":" + (value == null ? "NULL" : value.toString());
	}

	/**
	 * Gets the query by using the provided visitor
	 *
	 * @param query
	 *            the query
	 * @param visitor
	 *            the visitor to use
	 * @throws Exception
	 *             on error
	 */
	private void visitInternal(Query query, Visitor visitor) throws Exception {
		if (query == null) {
			return;
		}

		for (int i = 0; i < query.starting; i++) {
			visitor.visitStart(query, query.getBoost());
		}
		if (query.key != null) {
			visitor.visit(query);
		}
		for (int i = 0; i < query.ending; i++) {
			visitor.visitEnd(query);
		}
		if (query.and != null) {
			visitInternal(query.and, visitor);
		}
		if (query.or != null) {
			visitInternal(query.or, visitor);
		}

	}

	/**
	 * Gets the key.
	 *
	 * @return the key
	 */
	public String getKey() {
		return key;
	}

	/**
	 * Gets the value.
	 *
	 * @return the value
	 */
	public Serializable getValue() {
		return value;
	}

	/**
	 * Gets the parent.
	 *
	 * @return the pARENT
	 */
	public Query getParent() {
		return parent;
	}

	/**
	 * Gets the boost.
	 *
	 * @return the boost
	 */
	public QueryBoost getBoost() {
		return boost;
	}

	/**
	 * Sets the boost.
	 *
	 * @param boost
	 *            the boost to set
	 */
	public void setBoost(QueryBoost boost) {
		this.boost = boost;
	}

	/**
	 * Helper method to build query using map.
	 *
	 * @param arguments
	 *            the arguments
	 * @param boost
	 *            the boost
	 * @return the query
	 */
	public static Query fromMap(Map<String, Serializable> arguments, QueryBoost boost) {
		if (arguments == null || arguments.isEmpty()) {
			return Query.getEmpty();
		}
		Set<Entry<String, Serializable>> entrySet = arguments.entrySet();
		Query current = null;
		for (Entry<String, Serializable> entry : entrySet) {
			if (current == null) {
				current = new Query(entry.getKey(), entry.getValue());
			} else {
				current = current.add(entry.getKey(), entry.getValue(), boost);
			}
		}
		return current;
	}

	/**
	 * Helper method to build query using map.
	 *
	 * @param arguments
	 *            the arguments
	 * @param boost
	 *            the boost
	 * @param startable
	 *            should add start marker for query. used form complex arguments
	 * @param endable
	 *            if should add end marker. Used when work on query continues.
	 * @return thq query
	 */
	public static Query fromMap(Map<String, Serializable> arguments, QueryBoost boost, boolean startable,
			boolean endable) {
		if (arguments == null || arguments.isEmpty()) {
			return Query.getEmpty();
		}
		Set<Entry<String, Serializable>> entrySet = arguments.entrySet();
		Query current = null;
		for (Entry<String, Serializable> entry : entrySet) {
			if (current == null) {
				current = new Query(entry.getKey(), entry.getValue(), startable);
			} else {
				current = current.add(entry.getKey(), entry.getValue(), boost);
			}
		}
		return endable && current != null ? current.end() : current;
	}

	/**
	 * Adds the key:value pair as query to current element/
	 *
	 * @param key
	 *            the key
	 * @param value
	 *            the value
	 * @param boost
	 *            the boost
	 * @return the query
	 */
	public Query add(String key, Serializable value, QueryBoost boost) {
		if (QueryBoost.INCLUDE_OR == boost) {
			return or(key, value);
		}
		if (QueryBoost.INCLUDE_AND == boost) {
			return and(key, value);
		}
		if (QueryBoost.EXCLUDE_OR == boost) {
			return orNot(key, value);
		}
		if (QueryBoost.EXCLUDE_AND == boost) {
			return andNot(key, value);
		}
		throw new RuntimeException("Boost: " + boost);
	}

	/**
	 * Gets the level.
	 *
	 * @return the level
	 */
	public int getLevel() {
		return level;
	}

	/**
	 * Iterate and make a list of all query values. The list is started with the root and iterates in DFS manner.
	 *
	 * @return list of query entries
	 */
	public List<Query> asList() {
		final List<Query> queryList = new ArrayList<>();
		try {
			getRoot().visit(new AbstractQueryVistor() {

				@Override
				public void visit(Query query) throws Exception {
					queryList.add(query);
				}
			});
		} catch (Exception e) {
			LOGGER.error("Error during iteration of query", e);
		}
		return queryList;
	}

	public boolean isStarting() {
		return starting > 0;
	}

}
