package com.sirma.itt.emf.audit.rest;

import static com.sirma.itt.seip.collections.CollectionUtils.toArray;

import java.io.IOException;
import java.util.List;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.transaction.Transactional;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.ResponseBuilder;
import javax.ws.rs.core.Response.Status;

import com.sirma.itt.seip.rest.exceptions.ResourceException;
import org.apache.commons.lang.StringUtils;
import org.apache.solr.client.solrj.SolrQuery;
import org.apache.solr.client.solrj.SolrQuery.ORDER;
import org.json.JSONArray;
import org.json.JSONException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sirma.itt.emf.audit.db.AuditService;
import com.sirma.itt.emf.audit.export.AuditExportService;
import com.sirma.itt.emf.audit.solr.query.ServiceResult;
import com.sirma.itt.emf.audit.solr.service.SolrService;
import com.sirma.itt.emf.audit.solr.service.SolrServiceException;
import com.sirma.itt.emf.rest.EmfRestService;
import com.sirma.itt.seip.rest.annotations.security.AdminResource;
import com.sirma.itt.seip.rest.utils.Versions;

/**
 * The Rest service that enables access to records from the audit log database.
 *
 * @author Vilizar Tsonev
 * @author Nikolay Velkov
 * @author Mihail Radkov
 */
@Transactional
@AdminResource
@Path("/events")
@ApplicationScoped
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
	 * @param query
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
	public Response getEvents(@QueryParam("query") String query, @QueryParam("fq") List<String> filterQueries,
			@QueryParam("start") int start, @QueryParam("limit") int rows, @QueryParam("sort") String sortField,
			@QueryParam("dir") String sortOrder) {
		try {
			SolrQuery solrQuery = buildParams(query, filterQueries, start, rows, sortField, sortOrder);
			return buildResponse(Status.OK, auditService.getActivitiesBySolrQuery(solrQuery));
		} catch (SolrServiceException e) {
			LOGGER.warn(e.getMessage(), e);
			return buildResponse(Status.INTERNAL_SERVER_ERROR, e.getMessage());
		}
	}

	/**
	 * Retrieves the audit activities based on the provided {@link AuditSearchRequest}.
	 *
	 * @param searchRequest the audit search request
	 * @return the audit activities in JSON format.
	 */
	@POST
	@Path("/search")
	@Produces(Versions.V2_JSON)
	@Consumes(Versions.V2_JSON)
	public ServiceResult searchEvents(AuditSearchRequest searchRequest) {
        try {
            return auditService.getActivities(searchRequest);
        } catch (SolrServiceException e) {
            LOGGER.error(e.getMessage(), e);
			throw new ResourceException(Status.INTERNAL_SERVER_ERROR, e.getMessage());
        }
	}

	/**
	 * Exports audit activities in a file based on the provided solr parameters.
	 *
	 * @param query
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
	public Response export(@QueryParam("query") String query, @QueryParam("fq") List<String> filterQueries,
			@QueryParam("sort") String sortField, @QueryParam("dir") String sortOrder,
			@QueryParam("format") String exportFormat, @QueryParam("columns") String columns) {
		SolrQuery solrQuery = buildParams(query, filterQueries, 0, Integer.MAX_VALUE, sortField, sortOrder);

		String joinedFilterQuery = StringUtils.join(filterQueries, "&");

		try {
			ServiceResult result = auditService.getActivitiesBySolrQuery(solrQuery);
			Object exportedFile;
			ResponseBuilder response;
			if ("csv".equalsIgnoreCase(exportFormat)) {
				exportedFile = exportService.exportAsCsv(result.getRecords(), joinedFilterQuery,
						new JSONArray(columns));
				response = Response.ok(exportedFile);
				response.type("text/" + exportFormat);
				response.header("Content-Disposition", "attachment; filename=\"auditLogExport.csv\"");
			} else if ("pdf".equalsIgnoreCase(exportFormat)) {
				exportedFile = exportService.exportAsPdf(result.getRecords(), joinedFilterQuery,
						new JSONArray(columns));
				response = Response.ok(exportedFile);
				response.type("text/" + exportFormat);
				response.header("Content-Disposition", "attachment; filename=\"auditLogExport.pdf\"");
			} else {
				response = Response.status(Status.BAD_REQUEST).entity("Invalid file format " + exportFormat);
			}
			return response.build();
		} catch (IOException | SolrServiceException | JSONException ex) {
			LOGGER.warn(ex.getMessage(), ex);
			return buildResponse(Status.INTERNAL_SERVER_ERROR, ex.getMessage());
		}
	}

	/**
	 * A service to force solr (delta) data import.
	 */
	@GET
	@Path("/dataImport")
	public Response dataImport() {
		solrService.dataImport(false);
		return Response.ok().build();
	}

	/**
	 * Builds a {@link SolrQuery} based on the provided parameters.
	 *
	 * @param query
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
	 * @return the builded {@link SolrQuery}
	 */
	private SolrQuery buildParams(String query, List<String> filterQueries, int start, int rows, String sortField,
			String sortOrder) {
		SolrQuery solrQuery = new SolrQuery();
		solrQuery.setQuery(query);
		solrQuery.setFilterQueries(toArray(filterQueries, String.class));
		solrQuery.setStart(start);
		solrQuery.setRows(rows);
		// fields must be lowercase to match the db column names
		if (sortField != null && sortOrder != null) {
			if ("asc".equalsIgnoreCase(sortOrder)) {
				solrQuery.setSort(sortField.toLowerCase(), ORDER.asc);
			} else if ("desc".equalsIgnoreCase(sortOrder)) {
				solrQuery.setSort(sortField.toLowerCase(), ORDER.desc);
			}
		}
		return solrQuery;
	}
}
