package com.sirma.itt.emf.cls.db;

/**
 * Interface containing specific queries for the CL server.
 * 
 * @author Mihail Radkov
 * @author Nikolay Velkov
 * @author Vilizar Tsonev
 */
public interface ClsQueries {

	/* Truncating. */
	/** Key to {@link CLS_TRUNCATE}. */
	String CLS_TRUNCATE_KEY = "CLS_TRUNCATE";

	/** Query for Truncating the CL server's tables. */
	String CLS_TRUNCATE = "truncate table cls_codelist, cls_codevalue, cls_codevaluedescription, cls_codelistdescription, cls_codelist_cls_tenant, cls_tenant";

	/* Selecting all CL. */
	/** Key to QUERY_CODELISTS. */
	String QUERY_CODELISTS_KEY = "QUERY_CODELISTS";

	/** Query for selecting all code lists. */
	String QUERY_CODELISTS = "select cl from CodeList cl";

	/* Selecting CL by IDs */
	/** Key to QUERY_CODELIST_BY_ID. */
	String QUERY_CODELIST_BY_ID_KEY = "QUERY_CODELIST_BY_ID";

	/** Query for selecting code lists by their IDs. */
	String QUERY_CODELIST_BY_ID = "select cl from CodeList cl where cl.id in (:id)";

	/* Selecting CV by IDs */
	/** Key to QUERY_CODEVALUE_BY_ID. */
	String QUERY_CODEVALUE_BY_ID_KEY = "QUERY_CODEVALUE_BY_ID";

	/** Query for selecting code values by their IDs. */
	String QUERY_CODEVALUE_BY_ID = "select cv from CodeValue cv where cv.id in (:id)";

	/* Selecting CV by CL IDs */
	/** Key to QUERY_CODEVALUE_BY_CL_ID. */
	String QUERY_CODEVALUE_BY_CL_ID_KEY = "QUERY_CODEVALUE_BY_CL_ID";

	/** Query for selecting code values by their code list's IDs. */
	String QUERY_CODEVALUE_BY_CL_ID = "select cv.id from CodeValue cv where cv.codeListId in (:id)";

	/**
	 * Get the latest codevalue from the specified code list with the specified
	 * code value value
	 */
	String QUERY_LAST_CODEVALUE = "select cv.id from CodeValue cv where cv.codeListId in (:clid) and cv.value in (:cvid) order by cv.validTo";

	/**
	 * Deletes the descriptions for the given code list which are in the given
	 * language.
	 */
	String DELETE_CODELIST_DESCRIPTION_BY_LANGUAGE = "DELETE FROM CodeListDescription WHERE codeid = :id AND language = :language";

	/**
	 * Deletes the descriptions for the given code value which are in the given
	 * language.
	 */
	String DELETE_CODEVALUE_DESCRIPTION_BY_LANGUAGE = "DELETE FROM CodeValueDescription WHERE codeid = :id AND language = :language";

	/**
	 * Retrieves from the tenants table the tenant with the given name
	 */
	String GET_TENANT_BY_NAME = "SELECT tn FROM Tenant tn WHERE tn.name = :name";
}
