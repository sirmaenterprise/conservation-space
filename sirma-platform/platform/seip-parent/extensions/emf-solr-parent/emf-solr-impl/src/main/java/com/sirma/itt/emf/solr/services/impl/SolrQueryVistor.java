package com.sirma.itt.emf.solr.services.impl;

import java.io.Serializable;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Set;

import com.sirma.itt.cmf.constants.CommonProperties;
import com.sirma.itt.emf.solr.services.query.DefaultQueryVisitor;
import com.sirma.itt.seip.convert.TypeConverterUtil;
import com.sirma.itt.seip.domain.instance.DefaultProperties;
import com.sirma.itt.seip.domain.search.Query;
import com.sirma.itt.seip.domain.search.Query.QueryBoost;
import com.sirma.itt.seip.time.DateRange;

/**
 * Visitor implementation for queries.
 *
 * @author Borislav Banchev
 */
public class SolrQueryVistor extends DefaultQueryVisitor {

	/**
	 * Instantiates a new query vistor.
	 */
	public SolrQueryVistor() {
		super();
	}

	@Override
	public void visit(Query query) throws Exception {
		appendByValueType(builder, query);
	}

	/**
	 * Append by value type.
	 *
	 * @param finalQuery
	 *            Final query.
	 * @param query
	 *            Query
	 */
	void appendByValueType(StringBuilder finalQuery, Query query) {

		StringBuilder queryBuilder = prepareStart(finalQuery, query);
		boolean appended = false;

		// TODO when dynamic model is ready make mapping emfprop/semanticprop/solrprop
		Serializable value = query.getValue();
		String key = convertKey(query.getKey(), value);

		if (value instanceof DateRange) {
			// daterange is converted to string
			queryBuilder.append(key).append(":").append(TypeConverterUtil.getConverter().convert(String.class, value));
			appended = true;
		} else if (checkNullity(query)) {
			// does not process null
		} else if (value instanceof String) {
			appended = onStringValue(query, queryBuilder, value, key);
		} else if (value instanceof Date) {
			queryBuilder.append(key).append(":").append(TypeConverterUtil.getConverter().convert(String.class, value));
			appended = true;
		} else if (value instanceof Number) {
			queryBuilder.append(key).append(":").append(value);
			appended = true;
		} else if (query.getValue() instanceof List) {
			appended = iterateCollection(query, key, query.getValue(), queryBuilder);
		} else if (query.getValue() instanceof Set) {
			appended = iterateCollection(query, key, query.getValue(), queryBuilder);
		} else if (value instanceof Collection) {
			appended = iterateCollection(query, key, value, queryBuilder);
		}
		if (appended) {
			finalQuery.append(queryBuilder).append(")");
		}
	}

	private static boolean checkNullity(Query query) {
		return query.getKey().endsWith(CommonProperties.PROPERTY_NOTNULL)
				|| query.getKey().endsWith(CommonProperties.PROPERTY_ISNULL);
	}

	private static boolean onStringValue(Query query, StringBuilder queryBuilder, Serializable value, String key) {
		boolean appended;
		// if during dms convert Collections are converted to string.
		if (query.getValue() instanceof Collection) {
			appended = iterateCollection(query, key, query.getValue(), queryBuilder);
		} else {
			queryBuilder.append(key).append(":(").append(value).append(")");
			appended = true;
		}
		return appended;
	}

	private static StringBuilder prepareStart(StringBuilder finalQuery, Query query) {
		StringBuilder queryBuilder = new StringBuilder();
		// prepare query
		if (query.getBoost() == QueryBoost.EXCLUDE) {
			queryBuilder.append(" -");
		}
		if (finalQuery.toString().trim().length() == 0) {
			queryBuilder.append(" ( ");
		} else {
			int lastIndexOf = finalQuery.lastIndexOf("(");
			if (lastIndexOf > 0) {
				String substring = finalQuery.substring(lastIndexOf + 1);
				if (substring.trim().length() > 0) {
					queryBuilder.append(query.getBoost());
				}
			}
			queryBuilder.append(" ( ");
		}
		return queryBuilder;
	}

	/**
	 * Convert key.
	 *
	 * @param key
	 *            Key to convert.
	 * @param value
	 *            the value for the key
	 * @return converted key (might be the same)
	 */
	private static String convertKey(String key, Serializable value) {
		if (DefaultProperties.TYPE.equals(key)) {
			return "instanceType";
		} else if (DefaultProperties.URI.equals(key)) {
			if (value instanceof Collection) {
				return key;
			}
			return DefaultProperties.URI;
		}
		return key;
	}

	/**
	 * Iterate collection
	 *
	 * @param query
	 *            Query
	 * @param key
	 *            Key
	 * @param value
	 *            Balue
	 * @param queryBuilder
	 *            Query builder
	 * @return if value was append
	 */
	@SuppressWarnings("rawtypes")
	private static boolean iterateCollection(Query query, String key, Serializable value, StringBuilder queryBuilder) {
		Collection collection = null;
		if (value instanceof Collection) {
			collection = (Collection) value;
		} else {
			collection = Collections.singletonList(value);
		}
		int index = collection.size();
		boolean appended = false;
		if (index > 0) {
			queryBuilder.append(key).append(":(");
			for (Object val : collection) {
				queryBuilder.append("\"").append(val).append("\"");
				index--;
				if (index > 0) {
					appendBoost(query, queryBuilder);
				} else {
					queryBuilder.append(")");
				}
			}
			appended = true;
		}
		return appended;
	}

	private static void appendBoost(Query query, StringBuilder queryBuilder) {
		if (query.getBoost() == QueryBoost.EXCLUDE) {
			queryBuilder.append(" AND ");
		} else {
			queryBuilder.append(" OR ");
		}
	}
}