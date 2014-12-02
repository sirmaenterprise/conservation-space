package com.sirma.itt.emf.cls.rest;

import java.io.IOException;
import java.util.Arrays;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.ejb.Stateless;
import javax.inject.Inject;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

import org.apache.log4j.Logger;
import org.codehaus.jackson.JsonGenerationException;
import org.codehaus.jackson.map.JsonMappingException;
import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.map.ser.FilterProvider;
import org.codehaus.jackson.map.ser.impl.SimpleBeanPropertyFilter;
import org.codehaus.jackson.map.ser.impl.SimpleFilterProvider;

import com.sirma.itt.emf.cls.entity.CodeList;
import com.sirma.itt.emf.cls.entity.CodeValue;
import com.sirma.itt.emf.cls.retriever.CodeListSearchCriteria;
import com.sirma.itt.emf.cls.retriever.CodeValueSearchCriteria;
import com.sirma.itt.emf.cls.retriever.SearchCriteria;
import com.sirma.itt.emf.cls.retriever.SearchResult;
import com.sirma.itt.emf.cls.service.CodeListService;
import com.sirma.itt.emf.rest.EmfRestService;
import com.sirma.itt.emf.rest.RestServiceConstants;
import com.sirma.itt.emf.web.util.DateUtil;

/**
 * Class providing REST services for working with code lists and values. In
 * version 1.0, the supported methods are only GET.
 * 
 * @author Mihail Radkov
 * @author Nikolay Velkov
 * @version 1.0
 */
@Path("/codelists")
@Stateless
// TODO: Place @Produces here?
public class RetrievalService extends EmfRestService {

	/** Logs information about this class's actions. */
	private static final Logger LOGGER = Logger
			.getLogger(RetrievalService.class);

	/** Date formatter used to convert strings to dates. */
	// REVIEW: don't use hardcoded date format

	/** Retrieves code lists and values from the data base. */
	@Inject
	private CodeListService retriever;

	@Inject
	private DateUtil dateUtil;

	@Inject
	private JacksonConfig jacksonConfig;

	/**
	 * Retrieves code lists based on the provided query parameters from which a
	 * search criteria is builded. Returns a result in JSON.
	 * 
	 * @param codeListIDs
	 *            the code list IDs
	 * @param descriptions
	 *            the code list descriptions
	 * @param comments
	 *            the code list comments.
	 * @param masterValue
	 *            the master value
	 * @param fromDate
	 *            the code list from date
	 * @param toDate
	 *            the code list to date
	 * @param extra1
	 *            the code list extra1
	 * @param extra2
	 *            the code list extra2
	 * @param extra3
	 *            the code list extra3
	 * @param extra4
	 *            the code list extra4
	 * @param extra5
	 *            the code list extra5
	 * @param tenantNames
	 *            the tenant names
	 * @param excludeValues
	 *            if the values should be excluded
	 * @param offset
	 *            offset of the returned results
	 * @param limit
	 *            max number of returned results
	 * @param responseFields
	 *            a set with names of the fields to be returned with the
	 *            response. All fields will be returned if null or empty.
	 * @return a response with the code lists as content in JSON
	 */
	@GET
	@Produces(RestServiceConstants.APPLICATION_JSON_UTF_ENCODED)
	// TODO: Use @Context UriInfo uriInfo ?
	public Response getCodeLists(@QueryParam("id") List<String> codeListIDs,
			@QueryParam("description") List<String> descriptions,
			@QueryParam("comment") List<String> comments,
			@QueryParam("masterValue") List<String> masterValue,
			@QueryParam("from") String fromDate,
			@QueryParam("to") String toDate,
			@QueryParam("extra1") List<String> extra1,
			@QueryParam("extra2") List<String> extra2,
			@QueryParam("extra3") List<String> extra3,
			@QueryParam("extra4") List<String> extra4,
			@QueryParam("extra5") List<String> extra5,
			@QueryParam("tenantName") List<String> tenantNames,
			@QueryParam("excludeValues") boolean excludeValues,
			@QueryParam("offset") int offset, @QueryParam("limit") int limit,
			@QueryParam("responseFields") Set<String> responseFields) {

		CodeListSearchCriteria criteria = new CodeListSearchCriteria();
		setCommonCriterias(criteria, codeListIDs, descriptions, comments,
				masterValue, dateUtil.getISODateTime(fromDate),
				dateUtil.getISODateTime(toDate), extra1, extra2, extra3,
				extra4, extra5, offset, limit);

		// CodeList exclusive values
		criteria.setTenantNames(tenantNames);

		// Paging and exclusion.
		criteria.setExcludeValues(excludeValues);
		criteria.setOffset(offset);
		criteria.setLimit(limit);

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
	@Produces(RestServiceConstants.APPLICATION_JSON_UTF_ENCODED)
	public SearchResult getCodeList(@PathParam(value = "value") String value,
			@QueryParam("excludeValues") boolean excludeValues) {
		CodeListSearchCriteria criteria = new CodeListSearchCriteria();
		criteria.setIds(Arrays.asList(value));
		criteria.setExcludeValues(excludeValues);

		return retriever.getCodeLists(criteria);
	}

	/**
	 * Retrieves code values based on the provided path parameter defining the
	 * code list and the query parameters from which a search criteria is
	 * builded. Returns a result in JSON.
	 * 
	 * @param codeListID
	 *            the code list id
	 * @param codeValueIDs
	 *            the code value IDs
	 * @param descriptions
	 *            the code value descriptions
	 * @param comments
	 *            the code value comments
	 * @param master
	 *            the code value master
	 * @param fromDate
	 *            the code value from date
	 * @param toDate
	 *            the code value to date
	 * @param extra1
	 *            the code value extra1
	 * @param extra2
	 *            the code value extra2
	 * @param extra3
	 *            the code value extra3
	 * @param extra4
	 *            the code value extra4
	 * @param extra5
	 *            the code value extra5
	 * @param offset
	 *            offset of the returned results
	 * @param limit
	 *            max number of returned results
	 * @param responseFields
	 *            a set with names of the fields to be returned with the
	 *            response. All fields will be returned if null or empty
	 * @return the code values in JSON
	 */
	@GET
	@Path("/{codeListID}/codevalues/")
	@Produces(RestServiceConstants.APPLICATION_JSON_UTF_ENCODED)
	public Response getCodeValues(@PathParam("codeListID") String codeListID,
			@QueryParam("id") List<String> codeValueIDs,
			@QueryParam("description") List<String> descriptions,
			@QueryParam("comment") List<String> comments,
			@QueryParam("masterValue") List<String> master,
			@QueryParam("from") String fromDate,
			@QueryParam("to") String toDate,
			@QueryParam("extra1") List<String> extra1,
			@QueryParam("extra2") List<String> extra2,
			@QueryParam("extra3") List<String> extra3,
			@QueryParam("extra4") List<String> extra4,
			@QueryParam("extra5") List<String> extra5,
			@QueryParam("offset") int offset, @QueryParam("limit") int limit,
			@QueryParam("responseFields") Set<String> responseFields) {

		CodeValueSearchCriteria criteria = new CodeValueSearchCriteria();

		criteria.setCodeListId(codeListID);

		setCommonCriterias(criteria, codeValueIDs, descriptions, comments,
				master, dateUtil.getISODateTime(fromDate),
				dateUtil.getISODateTime(toDate), extra1, extra2, extra3,
				extra4, extra5, offset, limit);

		criteria.setOffset(offset);
		criteria.setLimit(limit);
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
	@Produces(RestServiceConstants.APPLICATION_JSON_UTF_ENCODED)
	public SearchResult getCodeValue(
			@PathParam(value = "clvalue") String clvalue,
			@PathParam("cvalue") String cvalue) {
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
	 * @param ids
	 *            the IDs
	 * @param descriptions
	 *            the descriptions
	 * @param comments
	 *            the comments
	 * @param masterValue
	 *            the master value
	 * @param fromDate
	 *            the from date
	 * @param toDate
	 *            the to date
	 * @param extra1
	 *            the extra1
	 * @param extra2
	 *            the extra2
	 * @param extra3
	 *            the extra3
	 * @param extra4
	 *            the extra4
	 * @param extra5
	 *            the extra5
	 * @param offset
	 *            the offset
	 * @param limit
	 *            the limit
	 */
	public void setCommonCriterias(SearchCriteria criteria, List<String> ids,
			List<String> descriptions, List<String> comments,
			List<String> masterValue, Date fromDate, Date toDate,
			List<String> extra1, List<String> extra2, List<String> extra3,
			List<String> extra4, List<String> extra5, int offset, int limit) {
		criteria.setIds(ids);
		criteria.setDescriptions(descriptions);
		criteria.setComments(comments);
		criteria.setMasterValue(masterValue);
		criteria.setFromDate(fromDate);
		criteria.setToDate(toDate);
		criteria.setExtra1(extra1);
		criteria.setExtra2(extra2);
		criteria.setExtra3(extra3);
		criteria.setExtra4(extra4);
		criteria.setExtra5(extra5);
		criteria.setOffset(offset);
		criteria.setLimit(limit);
	}

	/**
	 * Filter the fields of a specified {@link SearchResult} by returning only
	 * the specified ones.
	 * 
	 * @param result
	 *            the result to be filtered
	 * @param clazz
	 *            the class of the objects to be constructed as a result
	 * @param fieldsToFilter
	 *            the names of the fields in the result to be filtered
	 * @return the built response from the filtered fields
	 */
	private Response filterFields(SearchResult result, Class clazz,
			Set<String> fieldsToFilter) {
		String response = "";
		Status responseStatus = Response.Status.OK;
		ObjectMapper objectMapper = jacksonConfig.getContext(clazz);
		try {
			if ((fieldsToFilter != null) && !fieldsToFilter.isEmpty()) {
				FilterProvider filters = new SimpleFilterProvider().addFilter(
						"codeFilter", SimpleBeanPropertyFilter
								.filterOutAllExcept(new HashSet<String>(
										fieldsToFilter)));
				response = objectMapper.writer(filters).writeValueAsString(
						result);
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
