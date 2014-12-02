package com.sirma.itt.emf.cls.retriever;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.From;
import javax.persistence.criteria.Predicate;

import org.apache.commons.lang3.StringUtils;

/**
 * Utility class containing functionality for building predicates.
 * 
 * @author Mihail Radkov
 * @author Nikolay Velkov
 */
public final class PredicateUtils {

	/**
	 * Private constructor for utility class.
	 */
	private PredicateUtils() {
	}

	/**
	 * Builds case insensitive predicates of multiple parameters. The predicates are based on
	 * certain {@link From}.
	 * 
	 * @param predicates
	 *            the predicates list
	 * @param criteriaBuilder
	 *            criteria builder used for creating the predicates
	 * @param from
	 *            the specified table/join/root
	 * @param fieldName
	 *            the name of the attribute of the root for which the predicates are build
	 * @param filters
	 *            the acceptable filters for the attribute
	 */
	public static void addPredicates(List<Predicate> predicates, CriteriaBuilder criteriaBuilder,
			From<?, ?> from, String fieldName, List<String> filters) {
		if (filters != null && !filters.isEmpty()) {
			// TODO: try-catch NPE?
			addPredicatesUnsafe(predicates, criteriaBuilder, from, fieldName, filters);
		}
	}

	/**
	 * Builds case insensitive predicates of multiple parameters. The predicates are based on
	 * certain {@link From}. <br>
	 * <b>NOTE</b>: Does not perform null and empty checks!
	 * 
	 * @param predicates
	 *            the predicates list
	 * @param criteriaBuilder
	 *            criteria builder used for creating the predicates
	 * @param from
	 *            the specified table/join/root
	 * @param fieldName
	 *            the name of the attribute of the root for which the predicates are build
	 * @param filters
	 *            the acceptable filters for the attribute
	 */
	public static void addPredicatesUnsafe(List<Predicate> predicates,
			CriteriaBuilder criteriaBuilder, From<?, ?> from, String fieldName, List<String> filters) {
		List<Predicate> or = new ArrayList<Predicate>();
		for (String str : filters) {
			str = StringUtils.lowerCase(str).replace('*', '%');
			or.add(criteriaBuilder.like(
					criteriaBuilder.lower(from.get(fieldName).as(String.class)), str));
		}
		predicates.add(criteriaBuilder.or(getArrayFromList(or)));
	}

	/**
	 * Builds a case insensitive predicate of provided parameter. The predicate is based on certain
	 * {@link From}.
	 * 
	 * @param predicates
	 *            the predicates list
	 * @param criteriaBuilder
	 *            criteria builder used for creating the predicate
	 * @param from
	 *            the specified table/join/root
	 * @param fieldName
	 *            the name of the attribute of the root for which the predicate is build
	 * @param filters
	 *            the parameter
	 */
	public static void addSinglePredicate(List<Predicate> predicates,
			CriteriaBuilder criteriaBuilder, From<?, ?> from, String fieldName, String filters) {
		if (filters != null && !filters.isEmpty()) {
			filters = filters.toLowerCase().replace('*', '%');
			predicates.add(criteriaBuilder.like(
					criteriaBuilder.lower(from.get(fieldName).as(String.class)), filters));
		}
	}

	/**
	 * Converts a list of {@link Predicate} to an array of {@link Predicate}.
	 * 
	 * @param predicates
	 *            the list
	 * @return an array
	 */
	public static Predicate[] getArrayFromList(List<Predicate> predicates) {
		if (predicates != null) {
			return predicates.toArray(new Predicate[predicates.size()]);
		}
		return null;
	}

	/**
	 * Builds predicates for narrowing down the results by date range. The input date range is
	 * compared to the one in the database. If the range in the database has any common point in
	 * time with the input range, it is considered valid. For example from=25.05.1995,
	 * to=28.05.19915, database range 26.05.1995-29.05.1995 is valid. If the range in the database
	 * is missing a start/end point it is considered to be valid from/to -infinity/+infity. If the
	 * input parameter 'from' is not specified, then it is considered to be -infinity, so all
	 * codelists that were valid in the past are retrieved too. If the input parameter 'to' is not
	 * specified, then it is considered to be +infinity, so all codelists that will be valid in the
	 * future are retrieved too. If non of the input parameters are specified, all codelists from
	 * the past,present & future are retrieved.
	 * 
	 * @param predicates
	 *            the list with predicates
	 * @param criteriaBuilder
	 *            criteria builder
	 * @param root
	 *            the specified table/join/root
	 * @param from
	 *            specifies the start date criteria
	 * @param to
	 *            specifies the end date criteria
	 */
	public static void addDateRange(List<Predicate> predicates, CriteriaBuilder criteriaBuilder,
			From<?, ?> root, Date from, Date to) {
		if (from != null) {
			predicates.add(criteriaBuilder.or(criteriaBuilder.greaterThan(
					root.get("validFrom").as(Date.class), from), criteriaBuilder.greaterThan(root
					.get("validTo").as(Date.class), from), root.get("validTo").isNull()));
		}
		if (to != null) {
			predicates.add(criteriaBuilder.or(
					criteriaBuilder.lessThan(root.get("validFrom").as(Date.class), to),
					criteriaBuilder.lessThan(root.get("validTo").as(Date.class), to),
					root.get("validFrom").isNull()));
		}
	}
}
