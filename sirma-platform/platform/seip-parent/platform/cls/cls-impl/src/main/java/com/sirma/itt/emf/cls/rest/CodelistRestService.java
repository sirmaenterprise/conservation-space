package com.sirma.itt.emf.cls.rest;

import java.lang.invoke.MethodHandles;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;

import com.sirma.itt.seip.security.UserPreferences;
import org.apache.commons.lang.StringUtils;

import com.sirma.itt.seip.domain.codelist.CodelistService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * REST Service for retrieving specific codelists.
 *
 * @author yasko
 */
@ApplicationScoped
@Path("/codelist")
@Produces(MediaType.APPLICATION_JSON)
public class CodelistRestService {

	private static final Logger LOGGER = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

	@Inject
	private UserPreferences userPreferences;

	@Inject
	private CodelistService codeListService;

	/**
	 * Retrieve codelist values by specified codelist number and optional language for the description. The codevalues
	 * result can be filtered by providing additional arguments:<br>
	 * <ul>
	 * <li>If filterBy and filterSource arguments are passed, then the result is filtered by property/ies found in
	 * filterSource column in codelists.xls</li>
	 * <li>If customFilters argument is provided, then the filters are executed on codevalues from the given codelist
	 * and the result is retained with the codevalues extracted and (optionally filtered on previous step).</li>
	 * </ul>
	 *
	 * @param codelist
	 *            Codelist number.
	 * @param filterBy
	 *            the keyword to be used for filtering
	 * @param filterSource
	 *            against which description property to apply filter
	 * @param inclusive
	 *            if the filter should be inclusive or not
	 * @param customFilters
	 *            custom filters list is taken from the property definition and if provided, then the filters are
	 *            executed and the values returned are retained with the once returned by other service calls
	 * @param language
	 *            optional language. Defaults to 'en' if not specified.
	 * @param query
	 *            string that represent codelist value prefix.
	 * @return JSON object as string containing an array of codelist value code and description.
	 */
	@GET
	@Path("/{codelist}")
	public List<CodeValueInfo> retrieveCodeValues(@PathParam("codelist") Integer codelist,
			@QueryParam("filterBy") String filterBy, @QueryParam("filterSource") String filterSource,
			@QueryParam("inclusive") boolean inclusive, @QueryParam("customFilters[]") String[] customFilters,
			@QueryParam("lang") String language, @QueryParam("q") String query) {

		if (codelist == null) {
			return Collections.emptyList();
		}

		LOGGER.debug("codelist[{}], filter[{}], filterSource[{}], inclusive[{}], customFilters:[{}]", codelist, filterBy,
				filterSource, inclusive, customFilters);

		Map<String, com.sirma.itt.seip.domain.codelist.model.CodeValue> codeValues = null;
		if (StringUtils.isNotEmpty(filterBy) && StringUtils.isNotEmpty(filterSource)) {
			codeValues = codeListService.filterCodeValues(codelist, inclusive, filterSource, filterBy);
		} else {
			codeValues = codeListService.getCodeValues(codelist);
		}

		// if custom filters are provided, then retain the result values from both service
		// invocations
		if (customFilters != null && customFilters.length > 0) {
			Map<String, com.sirma.itt.seip.domain.codelist.model.CodeValue> filteredCodeValues = codeListService
					.getFilteredCodeValues(codelist, customFilters);
			codeValues.entrySet().retainAll(filteredCodeValues.entrySet());
		}

		List<CodeValueInfo> resultValues = codeValues.values()
				.stream()
				.map(codeValue -> toCodeValueInfo(codeValue, language))
				.collect(Collectors.toList());

		if (StringUtils.isNotBlank(query)) {
			return resultValues
					.stream()
					.filter(valInfo -> StringUtils.containsIgnoreCase(valInfo.getLabel(), query))
					.collect(Collectors.toList());
		}
		return resultValues;
	}

	private CodeValueInfo toCodeValueInfo(com.sirma.itt.seip.domain.codelist.model.CodeValue inputValue,
			String language) {
		String userLanguage = getUserLanguage(language);
		String label = (String) inputValue.getProperties().get(userLanguage);
		return new CodeValueInfo(inputValue.getValue(), label, inputValue.getCodelist(), inputValue.getProperties());
	}

	private String getUserLanguage(String language) {
		String currentLanguage = language;
		if (StringUtils.isBlank(currentLanguage)) {
			currentLanguage = userPreferences.getLanguage();
		}
		return currentLanguage;
	}
}
