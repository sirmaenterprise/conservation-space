package com.sirma.itt.emf.cls.service;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.ejb.Stateless;
import javax.inject.Inject;
import javax.persistence.EntityManager;
import javax.persistence.Query;
import javax.persistence.TypedQuery;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.From;
import javax.persistence.criteria.Join;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;

import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;

import com.sirma.itt.emf.cls.db.ClsQueries;
import com.sirma.itt.emf.cls.entity.Code;
import com.sirma.itt.emf.cls.entity.CodeList;
import com.sirma.itt.emf.cls.entity.CodeListDescription;
import com.sirma.itt.emf.cls.entity.CodeValue;
import com.sirma.itt.emf.cls.entity.CodeValueDescription;
import com.sirma.itt.emf.cls.entity.Description;
import com.sirma.itt.emf.cls.entity.Tenant;
import com.sirma.itt.emf.cls.retriever.CodeListSearchCriteria;
import com.sirma.itt.emf.cls.retriever.CodeValueSearchCriteria;
import com.sirma.itt.emf.cls.retriever.PredicateUtils;
import com.sirma.itt.emf.cls.retriever.SearchCriteria;
import com.sirma.itt.emf.cls.retriever.SearchResult;
import com.sirma.itt.emf.instance.dao.InstanceDao;
import com.sirma.itt.emf.instance.dao.InstanceType;

/**
 * Retrieves code lists and values from the database based on a search criteria. The only retrieved
 * information is the data base IDs which are passed to {@link InstanceDao} which returns the code
 * lists and/or values.
 * 
 * @author Mihail Radkov
 * @author Nikolay Velkov
 * @author Vilizar Tsonev
 */
@Stateless
public class CodeListServiceImpl implements CodeListService {

	/** Entity manager for working with the data base. */
	@Inject
	private EntityManager em;

	/** Logs information about this class's actions. */
	private static final Logger LOGGER = Logger.getLogger(CodeListServiceImpl.class);

	/** Data access object for code lists. */
	@Inject
	@InstanceType(type = "CodeListInstance")
	private InstanceDao<CodeList> codeListInstanceDao;

	/** Data access object for code values. */
	@Inject
	@InstanceType(type = "CodeValueInstance")
	private InstanceDao<CodeValue> codeValueInstanceDao;

	/** Executed after injection. */
	@PostConstruct
	public void postConstruct() {
		if (LOGGER.isTraceEnabled()) {
			LOGGER.trace("Constructed.");
		}
	}

	/** Executed before destroying. */
	@PreDestroy
	public void preDestroy() {
		if (LOGGER.isTraceEnabled()) {
			LOGGER.trace("Destroyed.");
		}
	}

	@Override
	public SearchResult getCodeLists(CodeListSearchCriteria criteria) {
		TypedQuery<Long> codeListCountQuery = buildCodeListCountQuery(criteria);
		Long totalResults = codeListCountQuery.getSingleResult();

		List<CodeList> codeLists = codeListInstanceDao.loadInstancesByDbKey(
				getIdsWithOffset(buildCodeListQuery(criteria), criteria.getOffset(),
						criteria.getLimit()), !criteria.isExcludeValues());

		SearchResult result = new SearchResult();
		result.setTotal(totalResults.intValue());
		result.setResults(codeLists);
		result.setOffset(criteria.getOffset());
		result.setLimit(criteria.getLimit());

		return result;
	}

	@Override
	public SearchResult getCodeValues(CodeValueSearchCriteria criteria) {
		TypedQuery<Long> codeValueCountQuery = buildCodeValueCountQuery(criteria);
		Long totalResults = codeValueCountQuery.getSingleResult();

		List<CodeValue> codeValues = codeValueInstanceDao.loadInstancesByDbKey(getIdsWithOffset(
				buildCodeValueQuery(criteria), criteria.getOffset(), criteria.getLimit()));

		SearchResult result = new SearchResult();
		result.setTotal(totalResults.intValue());
		result.setResults(codeValues);
		result.setOffset(criteria.getOffset());
		result.setLimit(criteria.getLimit());

		return result;
	}

	// TODO: split this method into 2 for update and save
	@Override
	public void saveOrUpdateCodeList(CodeList newCodeList, boolean update) throws CodeListException {
		CodeListSearchCriteria criteria = new CodeListSearchCriteria();
		criteria.setIds(Arrays.asList(newCodeList.getValue()));
		SearchResult result = getCodeLists(criteria);
		if (update) {
			if (result.getResults().isEmpty()) {
				throw new CodeListException("The code list with code " + newCodeList.getValue()
						+ " doesn't exist in the database, so there's nothning to update.");
			} else {
				CodeList oldCodeList = (CodeList) result.getResults().get(0);
				// don't allow editing of expired code lists
				if ((oldCodeList.getValidTo() != null)
						&& (oldCodeList.getValidTo().before(new Date()))) {
					throw new CodeListException("The code list with code " + newCodeList.getValue()
							+ " is expired and can't be edited.");
				}
				newCodeList.setId(oldCodeList.getId());
				newCodeList.setCodeValues(oldCodeList.getCodeValues());
				// delete from the DB the descriptions that don't exist
				// for this code list after the editing
				deleteRedundantDescriptions(Long.parseLong(oldCodeList.getId().toString()),
						newCodeList.getDescriptions(), oldCodeList.getDescriptions(), true);
				// update the attributes of the already existing CL
				// descriptions, preserving their old DB IDs
				for (CodeListDescription description : newCodeList.getDescriptions()) {
					if (isDescriptionPresent(oldCodeList.getDescriptions(),
							description.getLanguage())) {
						// set the id of the new entity to be the already
						// existing DB id (allowing proper update)
						description.setId(getDescriptionByLanguage(oldCodeList.getDescriptions(),
								description.getLanguage()).getId());
					}
				}
				// if the tenant name is changed, check the DB if it already
				// exists, and set its original DB id to update it
				updateTenantsDbIds(newCodeList, oldCodeList);
				formatCodeAttributes(newCodeList);
				codeListInstanceDao.saveEntity(newCodeList);
			}
		} else {
			if (result.getResults().isEmpty()) {
				formatCodeAttributes(newCodeList);
				codeListInstanceDao.saveEntity(newCodeList);
			} else {
				throw new CodeListException("The code list with code " + newCodeList.getValue()
						+ " already exists in the database so it can't be created");
			}
		}
	}

	@Override
	public void updateCodeValue(CodeValue codeValue) throws CodeListException {
		/*
		 * Filter the code values by code value and code list id.
		 */
		CodeValueSearchCriteria criteria = new CodeValueSearchCriteria();
		criteria.setIds(Arrays.asList(codeValue.getValue()));
		criteria.setCodeListId(codeValue.getCodeListId());

		/*
		 * Execute a native query to get the latest code values version. Using a native query
		 * because the getCodeValues method returns only whole codevalue objects and we need the db
		 * id only.
		 */
		TypedQuery<Long> query = em.createQuery(ClsQueries.QUERY_LAST_CODEVALUE, Long.class);
		query.setParameter("cvid", codeValue.getValue());
		query.setParameter("clid", codeValue.getCodeListId());
		List<Long> ids = query.getResultList();
		// Retrieve the code value(s) from the cache or the db.
		List<CodeValue> codeValues = codeValueInstanceDao.loadInstancesByDbKey(ids);
		// don't allow editing of expired code values
		if ((codeValues.get(0).getValidTo() != null)
				&& (codeValues.get(0).getValidTo().before(new Date()))) {
			throw new CodeListException("The code value with code " + codeValues.get(0).getValue()
					+ " is expired and can't be edited.");
		}
		// don't allow update of code values for expired code lists
		if (codeValue.getCodeListId() != null) {
			CodeListSearchCriteria codeListCriteria = new CodeListSearchCriteria();
			codeListCriteria.setIds(Arrays.asList(codeValue.getCodeListId()));
			SearchResult result = getCodeLists(codeListCriteria);
			CodeList codeList = (CodeList) result.getResults().get(0);
			if ((codeList.getValidTo() != null) && (codeList.getValidTo().before(new Date()))) {
				throw new CodeListException("Can't create a code value for expried code list");
			}
		}
		// if a 'future' code value already exists for this code list, its
		// attributes have to be overwritten with the new ones
		if (ids.size() == 2) {
			CodeValue futureCodeValue = codeValues.get(1);
			codeValue.setId(futureCodeValue.getId());
			// delete from the DB the descriptions that don't exist
			// for this code value after the editing
			deleteRedundantDescriptions(Long.parseLong(futureCodeValue.getId().toString()),
					codeValue.getDescriptions(), futureCodeValue.getDescriptions(), false);
			// update the attributes of the already existing CV
			// descriptions, preserving their old DB IDs
			for (CodeValueDescription description : codeValue.getDescriptions()) {
				if (isDescriptionPresent(futureCodeValue.getDescriptions(),
						description.getLanguage())) {
					// set the id of the new entity to be the already
					// existing DB id (allowing proper update)
					description.setId(getDescriptionByLanguage(futureCodeValue.getDescriptions(),
							description.getLanguage()).getId());
				}
			}
		}
		/*
		 * Update the last codevalues validTo date to match the new versions validTo and save the
		 * new one.
		 */
		CodeValue oldCodeValue = codeValues.get(0);
		oldCodeValue.setValidTo(codeValue.getValidFrom());
		codeValueInstanceDao.saveEntity(oldCodeValue);
		codeValue.setCreatedOn(new Date());
		formatCodeAttributes(codeValue);
		codeValueInstanceDao.saveEntity(codeValue);
	}

	@Override
	public void saveCodeValue(CodeValue codeValue) throws CodeListException {
		// don't allow creation of code values for expired code lists
		if (codeValue.getCodeListId() != null) {
			CodeListSearchCriteria criteria = new CodeListSearchCriteria();
			criteria.setIds(Arrays.asList(codeValue.getCodeListId()));
			SearchResult result = getCodeLists(criteria);
			CodeList codeList = (CodeList) result.getResults().get(0);
			if ((codeList.getValidTo() != null) && (codeList.getValidTo().before(new Date()))) {
				throw new CodeListException("Can't create a code value for expried code list");
			}
		}
		// persist the new code value into the db
		codeValue.setCreatedOn(new Date());
		formatCodeAttributes(codeValue);
		codeValueInstanceDao.saveEntity(codeValue);
	}

	/**
	 * Trims all white spaces from the beginning/end of the code list/value's attributes and if any
	 * of them is an empty string, makes it null to avoid the single quotes in the DB.
	 * 
	 * @param code
	 *            is the {@link Code} to format
	 */
	private void formatCodeAttributes(Code code) {
		code.setValue(StringUtils.stripToNull(code.getValue()));
		code.setMasterValue(StringUtils.stripToNull(code.getMasterValue()));
		code.setExtra1(StringUtils.stripToNull(code.getExtra1()));
		code.setExtra2(StringUtils.stripToNull(code.getExtra2()));
		code.setExtra3(StringUtils.stripToNull(code.getExtra3()));
		code.setExtra4(StringUtils.stripToNull(code.getExtra4()));
		code.setExtra5(StringUtils.stripToNull(code.getExtra5()));
	}

	/**
	 * If the tenant names of the new code list are edited (not the same as the old code list),
	 * checks the database if these tenants exist there, and if yes, take their old (original) DB
	 * IDs and set them to the new ones.
	 * 
	 * @param newCodeList
	 *            is the new, updated code list
	 * @param oldCodeList
	 *            is the old code list taken from the DB
	 */
	private void updateTenantsDbIds(CodeList newCodeList, CodeList oldCodeList) {
		Tenant newTenant = newCodeList.getTenants().toArray(new Tenant[0])[0];
		String newTenantName = newTenant.getName().trim();
		if (newTenantName.equals(oldCodeList.getTenants().toArray(new Tenant[0])[0].getName())) {
			newCodeList.setTenants(oldCodeList.getTenants());
		} else {
			Query query = em.createQuery(ClsQueries.GET_TENANT_BY_NAME);
			query.setParameter("name", newTenantName);
			@SuppressWarnings("unchecked")
			List<Tenant> retrievedTenants = query.getResultList();
			// that tenant already exists in the DB?
			if (!retrievedTenants.isEmpty()) {
				newTenant.setName(newTenantName);
				newTenant.setContact(retrievedTenants.get(0).getContact());
				newTenant.setId(retrievedTenants.get(0).getId());
				newCodeList.getTenants().clear();
				newCodeList.getTenants().add(newTenant);
			}
		}
	}

	/**
	 * Deletes from the database all descriptions for the given {@link Code} that exist in the
	 * oldList, but not in the newList (have been deleted from the user). <br>
	 * <b>NOTE</b> The cache is not updated after execution of this method
	 * 
	 * @param id
	 *            is the DB id of the {@link Code}
	 * @param oldList
	 *            is the old list of descriptions
	 * @param newList
	 *            is the new (edited) list of descriptions
	 * @param isCodeList
	 *            if set to true, the query will be executed for {@link CodeListDescription},
	 *            otherwise - for {@link CodeValueDescription}
	 * @param <T>
	 *            is {@link CodeListDescription} or {@link CodeValueDescription}
	 */
	private <T extends Description> void deleteRedundantDescriptions(Long id, List<T> newList,
			List<T> oldList, boolean isCodeList) {
		for (T description : oldList) {
			if (!isDescriptionPresent(newList, description.getLanguage())) {
				String queryString;
				if (isCodeList) {
					queryString = ClsQueries.DELETE_CODELIST_DESCRIPTION_BY_LANGUAGE;
				} else {
					queryString = ClsQueries.DELETE_CODEVALUE_DESCRIPTION_BY_LANGUAGE;
				}
				Query query = em.createQuery(queryString);
				query.setParameter("id", id);
				query.setParameter("language", description.getLanguage());
				query.executeUpdate();
			}
		}
	}

	/**
	 * Checks if a {@link Description} with the given language is present in the target list.
	 * 
	 * @param target
	 *            is the target list
	 * @param language
	 *            is the language string to match
	 * @return true if a description with the given language is present in the target list
	 * @param <T>
	 *            is {@link CodeListDescription} or {@link CodeValueDescription}
	 */
	private <T extends Description> boolean isDescriptionPresent(List<T> target, String language) {
		if (target != null) {
			for (T desc : target) {
				if (desc.getLanguage().equals(language)) {
					return true;
				}
			}
		}
		return false;
	}

	/**
	 * Gets the {@link Description} with the given language from the descriptions list.
	 * 
	 * @param descriptions
	 *            is the list of descriptions
	 * @param language
	 *            is the language string to match
	 * @return a {@link Description} with the given language
	 * @param <T>
	 *            is {@link CodeListDescription} or {@link CodeValueDescription}
	 */
	private <T extends Description> Description getDescriptionByLanguage(List<T> descriptions,
			String language) {
		for (T desc : descriptions) {
			if (desc.getLanguage().equals(language)) {
				return desc;
			}
		}
		return null;
	}

	/**
	 * Builds a query to retrieve total number of codelists which satisfy the given
	 * {@link CodeListSearchCriteria}
	 * 
	 * @param criteria
	 *            the criteria by which the search is performed
	 * @return the typed query
	 */
	private TypedQuery<Long> buildCodeListCountQuery(CodeListSearchCriteria criteria) {
		CriteriaBuilder criteriaBuilder = em.getCriteriaBuilder();
		CriteriaQuery<Long> criteriaQuery = criteriaBuilder.createQuery(Long.class);
		Root<CodeList> codeLists = criteriaQuery.from(CodeList.class);

		criteriaQuery.select(criteriaBuilder.countDistinct(codeLists));
		criteriaQuery = appendCriteriaToCodeListQuery(criteriaBuilder, criteriaQuery, codeLists,
				criteria);

		return em.createQuery(criteriaQuery);
	}

	/**
	 * Builds a query to retrieve code lists from the given {@link CodeListSearchCriteria}.
	 * 
	 * @param criteria
	 *            the search criteria used to build the list of predicates
	 * @return the typed query
	 */
	private TypedQuery<Long> buildCodeListQuery(CodeListSearchCriteria criteria) {
		CriteriaBuilder criteriaBuilder = em.getCriteriaBuilder();
		CriteriaQuery<Long> criteriaQuery = criteriaBuilder.createQuery(Long.class);
		Root<CodeList> codeLists = criteriaQuery.from(CodeList.class);

		criteriaQuery = appendCriteriaToCodeListQuery(criteriaBuilder, criteriaQuery, codeLists,
				criteria);
		criteriaQuery.select(codeLists.get("id").as(Long.class));
		criteriaQuery.distinct(true);
		criteriaQuery.orderBy(criteriaBuilder.asc(codeLists.get("id")));

		return em.createQuery(criteriaQuery);
	}

	/**
	 * Appends criteria to code list query.
	 * 
	 * @param criteriaBuilder
	 *            the criteria builder
	 * @param criteriaQuery
	 *            the criteria query
	 * @param root
	 *            query root
	 * @param criteria
	 *            the criteria
	 * @return the criteria query after appending all the predicates
	 */
	private CriteriaQuery<Long> appendCriteriaToCodeListQuery(CriteriaBuilder criteriaBuilder,
			CriteriaQuery<Long> criteriaQuery, Root<CodeList> root, CodeListSearchCriteria criteria) {

		Join<CodeList, Tenant> tenants;
		Join<CodeList, CodeListDescription> codeListDescrs = null;

		List<Predicate> predicates = new ArrayList<Predicate>();

		// If no tenant criteria was provided, the query does not need to create
		// additional joins for the tenants. They are loaded by default by
		// hibernate, and the join is used only if we want to filter them.
		if (criteria.getTenantNames() != null && !criteria.getTenantNames().isEmpty()) {
			tenants = root.join("tenants");
			PredicateUtils.addPredicatesUnsafe(predicates, criteriaBuilder, tenants, "name",
					criteria.getTenantNames());
		}

		buildCommonCriteria(predicates, criteriaBuilder, root, criteria);

		// If no description criteria was provided, the query does not need to
		// create additional
		// joins for the descriptions. They are loaded by default by
		// hibernate, and the join is used only if we want to filter them.
		if (criteria.getDescriptions() != null && !criteria.getDescriptions().isEmpty()) {
			codeListDescrs = root.join("descriptions");
			PredicateUtils.addPredicatesUnsafe(predicates, criteriaBuilder, codeListDescrs,
					"description", criteria.getDescriptions());
		}

		if (criteria.getComments() != null && !criteria.getComments().isEmpty()) {
			if (codeListDescrs == null) {
				codeListDescrs = root.join("descriptions");
			}
			PredicateUtils.addPredicatesUnsafe(predicates, criteriaBuilder, codeListDescrs,
					"comment", criteria.getComments());
		}
		if (!predicates.isEmpty()) {
			criteriaQuery.where(PredicateUtils.getArrayFromList(predicates));
		}

		return criteriaQuery;
	}

	/**
	 * Builds a query to retrieve total number of codevalues which satisfy the given
	 * {@link CodeValueSearchCriteria}
	 * 
	 * @param criteria
	 *            the criteria
	 * @return the typed query
	 */
	private TypedQuery<Long> buildCodeValueCountQuery(CodeValueSearchCriteria criteria) {
		CriteriaBuilder criteriaBuilder = em.getCriteriaBuilder();
		CriteriaQuery<Long> criteriaQuery = criteriaBuilder.createQuery(Long.class);

		Root<CodeValue> codeValues = criteriaQuery.from(CodeValue.class);

		criteriaQuery.select(criteriaBuilder.countDistinct(codeValues));
		criteriaQuery = appendCriteriaToCodeValueQuery(criteriaBuilder, criteriaQuery, codeValues,
				criteria);

		return em.createQuery(criteriaQuery);
	}

	/**
	 * Builds a query to retrieve code values from the given {@link CodeValueSearchCriteria}.
	 * 
	 * @param criteria
	 *            the search criteria used to build the list of predicates
	 * @return the typed query
	 */
	private TypedQuery<Long> buildCodeValueQuery(CodeValueSearchCriteria criteria) {
		CriteriaBuilder criteriaBuilder = em.getCriteriaBuilder();
		CriteriaQuery<Long> criteriaQuery = criteriaBuilder.createQuery(Long.class);
		Root<CodeValue> codeValues = criteriaQuery.from(CodeValue.class);
		criteriaQuery = appendCriteriaToCodeValueQuery(criteriaBuilder, criteriaQuery, codeValues,
				criteria);
		criteriaQuery.select(codeValues.get("id").as(Long.class));
		criteriaQuery.distinct(true);
		criteriaQuery.orderBy(criteriaBuilder.asc(codeValues.get("id")));
		return em.createQuery(criteriaQuery);
	}

	/**
	 * Appends criteria to code value query.
	 * 
	 * @param criteriaBuilder
	 *            the criteria builder
	 * @param criteriaQuery
	 *            the criteria query
	 * @param root
	 *            the root
	 * @param criteria
	 *            the criteria
	 * @return the criteria query
	 */
	private CriteriaQuery<Long> appendCriteriaToCodeValueQuery(CriteriaBuilder criteriaBuilder,
			CriteriaQuery<Long> criteriaQuery, Root<CodeValue> root,
			CodeValueSearchCriteria criteria) {

		Join<CodeValue, CodeValueDescription> codeValueDescrs = null;

		List<Predicate> predicates = new ArrayList<Predicate>();

		PredicateUtils.addSinglePredicate(predicates, criteriaBuilder, root, "codeListId",
				criteria.getCodeListId());
		buildCommonCriteria(predicates, criteriaBuilder, root, criteria);

		// If no description criteria was provided, the query does not need to
		// create additional
		// joins for the descriptions. They are loaded by default by
		// hibernate, and the join is used only if we want to filter them.
		if (criteria.getDescriptions() != null && !criteria.getDescriptions().isEmpty()) {
			codeValueDescrs = root.join("descriptions");
			PredicateUtils.addPredicatesUnsafe(predicates, criteriaBuilder, codeValueDescrs,
					"description", criteria.getDescriptions());
		}
		if (criteria.getComments() != null && !criteria.getComments().isEmpty()) {
			if (codeValueDescrs == null) {
				codeValueDescrs = root.join("descriptions");
			}
			PredicateUtils.addPredicatesUnsafe(predicates, criteriaBuilder, codeValueDescrs,
					"comment", criteria.getComments());
		}
		if (!predicates.isEmpty()) {
			criteriaQuery.where(PredicateUtils.getArrayFromList(predicates));
		}

		return criteriaQuery;
	}

	/**
	 * Builds predicates based on common search criteria using a criteria builder and adds them to a
	 * list of predicates. <br>
	 * <b>NOTE:</b> The common criteria are: code value, extra 1 to 5, master code value and the
	 * dates for validity start and end.
	 * 
	 * @param predicates
	 *            list of predicate
	 * @param cb
	 *            criteria builder
	 * @param from
	 *            the specified table/join/root
	 * @param criteria
	 *            the common criteria object
	 */
	private void buildCommonCriteria(List<Predicate> predicates, CriteriaBuilder cb,
			From<?, ?> from, SearchCriteria criteria) {
		PredicateUtils.addPredicates(predicates, cb, from, "value", criteria.getIds());
		PredicateUtils
				.addPredicates(predicates, cb, from, "masterValue", criteria.getMasterValue());
		PredicateUtils.addPredicates(predicates, cb, from, "extra1", criteria.getExtra1());
		PredicateUtils.addPredicates(predicates, cb, from, "extra2", criteria.getExtra2());
		PredicateUtils.addPredicates(predicates, cb, from, "extra3", criteria.getExtra3());
		PredicateUtils.addPredicates(predicates, cb, from, "extra4", criteria.getExtra4());
		PredicateUtils.addPredicates(predicates, cb, from, "extra5", criteria.getExtra5());
		PredicateUtils.addDateRange(predicates, cb, from, criteria.getFromDate(),
				criteria.getToDate());
	}

	/**
	 * Executes the given {@link TypedQuery} and retrieves IDs from the database based on the query
	 * and the given index and offset for pagination.
	 * 
	 * @param typedQuery
	 *            the typed query to be executed on the database
	 * @param offset
	 *            the offset from which the results will start
	 * @param resultLength
	 *            the count of the results to be retrieved from the offset
	 * @return the IDs
	 */
	private List<Long> getIdsWithOffset(TypedQuery<Long> typedQuery, int offset, int resultLength) {
		typedQuery.setFirstResult(offset);
		typedQuery.setMaxResults(resultLength);
		return typedQuery.getResultList();
	}

}
