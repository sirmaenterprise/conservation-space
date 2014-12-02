package com.sirma.itt.emf.search;

/**
 * All named queries that can be executed as filters
 *
 * @author kirq4e
 */
public interface NamedQueries {

	/**
	 * Query all workflows for User, Project or Case. </br>For Project and Case pass the parameter
	 * ?object </br> For Users pass the parameter ?user
	 */
	String QUERY_ALL_WORKFLOWS_FOR_OBJECT = "QUERY_ALL_WORKFLOWS_FOR_OBJECT";
	/**
	 * Not Completed workflows - all workflows which are not in end state (Completed or Cancelled)
	 * For Project and Case pass the parameter ?object </br> For Users pass the parameter ?user
	 * </br> Returns all Business process that are in states IN_PROGRESS, ON_HOLD and APPROVED
	 */
	String QUERY_NOT_COMPLETED_WORKFLOWS_FOR_OBJECT = "QUERY_NOT_COMPLETED_WORKFLOWS_FOR_OBJECT";

	/**
	 * High Priority workflows - Not Completed workflows, which are only with High Priority </br>
	 * For Project and Case pass the parameter ?object </br> For Users pass the parameter ?user
	 * </br> Returns all Business process that are in states IN_PROGRESS, ON_HOLD and APPROVED
	 */
	String QUERY_NOT_COMPLETED_WORKFLOWS_FOR_OBJECT_WITH_HIGH_PRIORITY = "QUERY_NOT_COMPLETED_WORKFLOWS_FOR_OBJECT_WITH_HIGH_PRIORITY";

	/**
	 * *Overdue workflows - workflows which end date is before today and the workflows are still not
	 * completed.</br> For Project and Case pass the parameter ?object </br> For Users pass the
	 * parameter ?user </br> Returns all Business process that are in states IN_PROGRESS, ON_HOLD
	 * and APPROVED
	 */
	String QUERY_NOT_COMPLETED_OVERDUE_WORKFLOWS_FOR_OBJECT = "QUERY_NOT_COMPLETED_OVERDUE_WORKFLOWS_FOR_OBJECT";

	/**
	 * The check existing instance query - checks if the passed URIs as parameter exist in the
	 * repository and the objects aren`t deleted.
	 */
	String CHECK_EXISTING_INSTANCE = "CHECK_EXISTING_INSTANCE";

	/**
	 * Query all cases and projects to which the given event entity has any kind of relation. Need
	 * to pass ?objectId as parameter
	 */
	String QUERY_CONTEXT_FOR_AUDIT_ENTRY = "QUERY_CONTEXT_FOR_AUDIT_ENTRY";

	/**
	 * Query all classes that are part of the Object Library - has the predicate
	 * isPartOfObjectLibrary
	 */
	String QUERY_CLASSES_PART_OF_OBJECT_LIBRARY = "QUERY_CLASSES_PART_OF_OBJECT_LIBRARY";
	
	/**
	 * Executes solr query through sparql query
	 */
	String QUERY_SOLR_SEARCH = "QUERY_SOLR_SEARCH";
	
	/**
	 * Executes solr query through sparql query and return total number of results
	 */
	String QUERY_SOLR_SEARCH_TOTAL_RESULTS = "QUERY_SOLR_SEARCH_TOTAL_RESULTS";

}
