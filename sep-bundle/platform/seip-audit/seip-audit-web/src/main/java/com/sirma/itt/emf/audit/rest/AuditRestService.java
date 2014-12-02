package com.sirma.itt.emf.audit.rest;

import java.io.IOException;

import javax.ejb.Stateless;
import javax.inject.Inject;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.ResponseBuilder;
import javax.ws.rs.core.Response.Status;

import org.json.JSONArray;
import org.json.JSONException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sirma.itt.emf.audit.db.AuditService;
import com.sirma.itt.emf.audit.export.AuditExportService;
import com.sirma.itt.emf.audit.solr.query.ServiceResult;
import com.sirma.itt.emf.audit.solr.query.SolrQueryParams;
import com.sirma.itt.emf.audit.solr.service.SolrService;
import com.sirma.itt.emf.audit.solr.service.SolrServiceException;
import com.sirma.itt.emf.rest.EmfRestService;

/**
 * The Rest service that enables access to records from the audit log database.
 * 
 * @author Vilizar Tsonev
 * @author Nikolay Velkov
 * @author Mihail Radkov
 */
@Path("/events")
@Stateless
public class AuditRestService extends EmfRestService {

	private static final Logger LOGGER = LoggerFactory.getLogger(AuditRestService.class);

	/** The audit service. Retrieves db ids from solr and records from the rdb. */
	@Inject
	private AuditService auditService;

	@Inject
	private SolrService solrService;

	@Inject
	private AuditExportService exportService;

	/**
	 * Retrieves the audit activities based on the provided solr parameters.
	 * 
	 * @param solrQuery
	 *            is the solr query (the 'q' parameter)
	 * @param filterQueries
	 *            are the solr filter queries
	 * @param start
	 *            is the starting offset from which the audit activities will be retrieved
	 * @param rows
	 *            is the number of audit activities to be retrieved
	 * @param sortField
	 *            is the field name by which the results have to be sorted
	 * @param sortOrder
	 *            is the sort direction - asc/desc
	 * @return the audit activities in JSON format, based on the given solr query parameters.
	 */
	@GET
	@Produces(MediaType.APPLICATION_JSON)
	public Response getEvents(@QueryParam("query") String solrQuery,
			@QueryParam("fq") String[] filterQueries, @QueryParam("start") int start,
			@QueryParam("limit") int rows, @QueryParam("sort") String sortField,
			@QueryParam("dir") String sortOrder) {
		try {
			SolrQueryParams solrParams = buildParams(solrQuery, filterQueries, start, rows,
					sortField, sortOrder);
			return buildResponse(Status.OK, auditService.getActivitiesBySolrQuery(solrParams));
		} catch (SolrServiceException e) {
			// TODO: Warn?
			LOGGER.warn(e.getMessage(), e);
			return buildResponse(Status.INTERNAL_SERVER_ERROR, e.getMessage());
		}
	}

	/**
	 * Exports audit activities in a file based on the provided solr parameters.
	 * 
	 * @param solrQuery
	 *            is the solr query (the 'q' parameter)
	 * @param filterQueries
	 *            are the solr filter queries
	 * @param sortField
	 *            is the field name by which the results have to be sorted
	 * @param sortOrder
	 *            is the sort direction - asc/desc
	 * @param exportFormat
	 *            is the file format to export - csv/pdf
	 * @param columns
	 *            columns to be exported
	 * @return the audit activities in JSON format, based on the given solr query parameters.
	 */
	@GET
	@Path("/export")
	// @Produces("text/csv")
	public Response export(@QueryParam("query") String solrQuery,
			@QueryParam("fq") String[] filterQueries, @QueryParam("sort") String sortField,
			@QueryParam("dir") String sortOrder, @QueryParam("format") String exportFormat,
			@QueryParam("columns") String columns) {
		SolrQueryParams solrParams = buildParams(solrQuery, filterQueries, 0, Integer.MAX_VALUE,
				sortField, sortOrder);

		StringBuilder queryBuilder = new StringBuilder();
		boolean first = true;
		for (int i = 0; i < filterQueries.length; i++) {
			if (first) {
				queryBuilder.append(filterQueries[i]);
				first = false;
			} else {
				queryBuilder.append("&");
				queryBuilder.append(filterQueries[i]);
			}
		}

		try {
			ServiceResult result = auditService.getActivitiesBySolrQuery(solrParams);
			Object exportedFile = null;
			ResponseBuilder response = null;
			if ("csv".equalsIgnoreCase(exportFormat)) {
				// new JsonParser().parse(columns);
				exportedFile = exportService.exportAsCsv(result.getRecords(),
						queryBuilder.toString(), new JSONArray(columns));
				response = Response.ok(exportedFile);
				response.type("text/" + exportFormat);
				response.header("Content-Disposition",
						"attachment; filename=\"auditLogExport.csv\"");
			} else if ("pdf".equalsIgnoreCase(exportFormat)) {
				exportedFile = exportService.exportAsPdf(result.getRecords(),
						queryBuilder.toString(), new JSONArray(columns));
				response = Response.ok(exportedFile);
				response.type("text/" + exportFormat);
				response.header("Content-Disposition",
						"attachment; filename=\"auditLogExport.pdf\"");
			} else {
				response = Response.status(Status.BAD_REQUEST).entity(
						"Invalid file format " + exportFormat);
			}
			return response.build();
		} catch (IOException ex) {
			LOGGER.warn(ex.getMessage(), ex);
			return buildResponse(Status.INTERNAL_SERVER_ERROR, ex.getMessage());
		} catch (SolrServiceException e) {
			LOGGER.warn(e.getMessage(), e);
			return buildResponse(Status.INTERNAL_SERVER_ERROR, e.getMessage());
		} catch (JSONException e) {
			LOGGER.warn(e.getMessage(), e);
			return buildResponse(Status.INTERNAL_SERVER_ERROR, e.getMessage());
		}
	}

	/**
	 * A service to force solr (delta) data import.
	 */
	@GET
	@Path("/dataImport")
	public void dataImport() {
		solrService.dataImport(false);
	}

	/**
	 * Builds a {@link SolrQueryParams} based on the provided parameters.
	 * 
	 * @param solrQuery
	 *            the Solr query
	 * @param filterQueries
	 *            the Solr filter queries
	 * @param start
	 *            start index used for pagination
	 * @param rows
	 *            end index used for pagination
	 * @param sortField
	 *            filed for sorting
	 * @param sortOrder
	 *            sorting direction
	 * @return the builded {@link SolrQueryParams}
	 */
	private SolrQueryParams buildParams(String solrQuery, String[] filterQueries, int start,
			int rows, String sortField, String sortOrder) {
		SolrQueryParams solrParams = new SolrQueryParams();
		solrParams.setQuery(solrQuery);
		solrParams.setFilters(filterQueries);
		solrParams.setStart(start);
		solrParams.setRows(rows);
		// fields must be lowercase to match the db column names
		if (sortField != null) {
			solrParams.setSortField(sortField.toLowerCase());
		}
		solrParams.setSortOrder(sortOrder);
		return solrParams;
	}
}
