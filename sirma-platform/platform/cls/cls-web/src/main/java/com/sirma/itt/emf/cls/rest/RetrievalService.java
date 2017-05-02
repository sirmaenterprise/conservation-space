package com.sirma.itt.emf.cls.rest;

import java.io.IOException;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;
import javax.ws.rs.core.UriInfo;

import org.apache.log4j.Logger;
import org.codehaus.jackson.JsonGenerationException;
import org.codehaus.jackson.map.JsonMappingException;
import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.map.ser.FilterProvider;
import org.codehaus.jackson.map.ser.impl.SimpleBeanPropertyFilter;
import org.codehaus.jackson.map.ser.impl.SimpleFilterProvider;
import org.joda.time.DateTime;

import com.sirma.itt.emf.cls.entity.CodeList;
import com.sirma.itt.emf.cls.entity.CodeValue;
import com.sirma.itt.emf.cls.retriever.CodeListSearchCriteria;
import com.sirma.itt.emf.cls.retriever.CodeValueSearchCriteria;
import com.sirma.itt.emf.cls.retriever.SearchCriteria;
import com.sirma.itt.emf.cls.retriever.SearchResult;
import com.sirma.itt.emf.cls.service.CodeListService;
import com.sirma.itt.emf.rest.EmfRestService;
import com.sirma.itt.seip.domain.search.SearchRequest;
import com.sirma.itt.seip.rest.annotations.security.AdminResource;

/**
 * Provides REST services for working with code lists and values. In version 1.0, the supported methods are only GET.
 *
 * @author Mihail Radkov
 * @author Nikolay Velkov
 * @author Vilizar Tsonev
 * @version 1.0
 */
@AdminResource
@Path("/codelists")
@ApplicationScoped
public class RetrievalService extends EmfRestService {

	private static final Logger LOGGER = Logger.getLogger(RetrievalService.class);

	/** Retrieves code lists and values from the data base. */
	@Inject
	private CodeListService retriever;

	@Inject
	private JacksonConfig jacksonConfig;

	/**
	 * Retrieves code lists based on the provided query parameters from which a search criteria is builded. Returns a
	 * result in JSON.
	 *
	 * @param uriInfo
	 *            contains the query search parameters
	 * @return a response with the code lists as content in JSON
	 */
	@GET
	@Produces(MediaType.APPLICATION_JSON)
	public Response getCodeLists(@Context UriInfo uriInfo) {
		MultivaluedMap<String, String> queryParams = uriInfo.getQueryParameters();
		SearchRequest request = new SearchRequest(queryParams);

		boolean excludeValues = request.getFirstBoolean("excludeValues");
		Set<String> responseFields = new HashSet<String>(request.get("responseFields"));

		CodeListSearchCriteria criteria = new CodeListSearchCriteria();
		setCommonCriterias(criteria, request);

		// Paging and exclusion.
		criteria.setExcludeValues(excludeValues);

		SearchResult codeLists = retriever.getCodeLists(criteria);

		return filterFields(codeLists, CodeList.class, responseFields);
	}

	/**
	 * Returns information about specific code list.
	 *
	 * @param value
	 *            the code list value
	 * @param excludeValues
	 *            indicates if the code values should be retrieved or not
	 * @return the code list in JSON
	 */
	@GET
	@Path("/{value}")
	@Produces(MediaType.APPLICATION_JSON)
	public SearchResult getCodeList(@PathParam(value = "value") String value,
			@QueryParam("excludeValues") boolean excludeValues) {
		CodeListSearchCriteria criteria = new CodeListSearchCriteria();
		criteria.setIds(Arrays.asList(value));
		criteria.setExcludeValues(excludeValues);

		return retriever.getCodeLists(criteria);
	}

	/**
	 * Retrieves code values based on the provided path parameter defining the code list and the query parameters from
	 * which a search criteria is builded. Returns a result in JSON.
	 *
	 * @param codeListID
	 *            the code list id
	 * @param uriInfo
	 *            contains the query search parameters
	 * @return the code values in JSON
	 */
	@GET
	@Path("/{codeListID}/codevalues/")
	@Produces(MediaType.APPLICATION_JSON)
	public Response getCodeValues(@PathParam("codeListID") String codeListID, @Context UriInfo uriInfo) {
		MultivaluedMap<String, String> queryParams = uriInfo.getQueryParameters();
		SearchRequest request = new SearchRequest(queryParams);

		Set<String> responseFields = new HashSet<String>(request.get("responseFields"));
		CodeValueSearchCriteria criteria = new CodeValueSearchCriteria();
		criteria.setCodeListId(codeListID);
		setCommonCriterias(criteria, request);
		SearchResult codeValues = retriever.getCodeValues(criteria);
		return filterFields(codeValues, CodeValue.class, responseFields);
	}

	/**
	 * Returns information about certain code value of certain code list.
	 *
	 * @param clvalue
	 *            the code value's code list
	 * @param cvalue
	 *            the code value
	 * @return the code value in JSON
	 */
	@GET
	@Path("/{clvalue}/codevalues/{cvalue}")
	@Produces(MediaType.APPLICATION_JSON)
	public SearchResult getCodeValue(@PathParam(value = "clvalue") String clvalue, @PathParam("cvalue") String cvalue) {
		CodeValueSearchCriteria criteria = new CodeValueSearchCriteria();
		criteria.setCodeListId(clvalue);
		criteria.setIds(Arrays.asList(cvalue));

		return retriever.getCodeValues(criteria);
	}

	/**
	 * Sets common criteria parameters.
	 *
	 * @param criteria
	 *            the criteria
	 * @param request
	 *            is the search request containing all search parameters
	 */
	public void setCommonCriterias(SearchCriteria criteria, SearchRequest request) {
		List<String> ids = request.get("id");
		List<String> descriptions = request.get("description");
		List<String> comments = request.get("comment");
		List<String> masterValue = request.get("masterValue");
		String fromDate = request.getFirst("from");
		String toDate = request.getFirst("to");
		List<String> extra1 = request.get("extra1");
		List<String> extra2 = request.get("extra2");
		List<String> extra3 = request.get("extra3");
		List<String> extra4 = request.get("extra4");
		List<String> extra5 = request.get("extra5");
		criteria.setIds(ids);
		criteria.setDescriptions(descriptions);
		criteria.setComments(comments);
		criteria.setMasterValue(masterValue);
		criteria.setExtra1(extra1);
		criteria.setExtra2(extra2);
		criteria.setExtra3(extra3);
		criteria.setExtra4(extra4);
		criteria.setExtra5(extra5);
		if (fromDate != null) {
			criteria.setFromDate(new DateTime(fromDate).toDate());
		}
		if (toDate != null) {
			criteria.setToDate(new DateTime(toDate).toDate());
		}
		if (request.getFirstInteger("offset") != null) {
			criteria.setOffset(request.getFirstInteger("offset"));
		}
		if (request.getFirstInteger("limit") != null) {
			criteria.setLimit(request.getFirstInteger("limit"));
		}
	}

	/**
	 * Filter the fields of a specified {@link SearchResult} by returning only the specified ones.
	 *
	 * @param result
	 *            the result to be filtered
	 * @param clazz
	 *            the class of the objects to be constructed as a result
	 * @param fieldsToFilter
	 *            the names of the fields in the result to be filtered
	 * @return the built response from the filtered fields
	 */
	private Response filterFields(SearchResult result, Class<?> clazz, Set<String> fieldsToFilter) {
		String response = "";
		Status responseStatus = Response.Status.OK;
		ObjectMapper objectMapper = jacksonConfig.getContext(clazz);
		try {
			if (fieldsToFilter != null && !fieldsToFilter.isEmpty()) {
				FilterProvider filters = new SimpleFilterProvider().addFilter("codeFilter",
						SimpleBeanPropertyFilter.filterOutAllExcept(new HashSet<String>(fieldsToFilter)));
				response = objectMapper.writer(filters).writeValueAsString(result);
			} else {
				response = objectMapper.writer().writeValueAsString(result);
			}
		} catch (JsonGenerationException e) {
			responseStatus = Response.Status.INTERNAL_SERVER_ERROR;
			response = e.getMessage();
			LOGGER.error(e.getMessage(), e);
		} catch (JsonMappingException e) {
			responseStatus = Response.Status.INTERNAL_SERVER_ERROR;
			response = e.getMessage();
			LOGGER.error(e.getMessage(), e);
		} catch (IOException e) {
			responseStatus = Response.Status.INTERNAL_SERVER_ERROR;
			response = e.getMessage();
			LOGGER.error(e.getMessage(), e);
		}

		return buildResponse(responseStatus, response);
	}

}
