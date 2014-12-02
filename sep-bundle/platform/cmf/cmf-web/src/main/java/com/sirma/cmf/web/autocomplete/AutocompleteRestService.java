package com.sirma.cmf.web.autocomplete;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;

import org.json.JSONException;

import com.sirma.itt.emf.label.retrieve.FieldValueRetrieverService;
import com.sirma.itt.emf.label.retrieve.RetrieveResponse;


/**
 * A service for autocomplete or load combobox values from different resources.
 */
@Path("/autocomplete")
@ApplicationScoped
@Produces(MediaType.APPLICATION_JSON)
public class AutocompleteRestService {
	
	@Inject
	private FieldValueRetrieverService fieldValueRetrieverService;
	
	/**
	 * Load.
	 *
	 * @param fieldId the id of the field
	 * @param queryTerm a filtering string to filter the results by label
	 * @param offset the offset of the results
	 * @param limit the maximum number of returned results
	 * @return the {@link RetrieveResponse} to jSON string
	 * @throws JSONException the jSON exception
	 */
	@GET
	@Path("/{field}")
	public String load(@PathParam("field") String fieldId, @QueryParam("q") String queryTerm, 
			@QueryParam("offset") Integer offset, @QueryParam("limit") Integer limit) throws JSONException {

		RetrieveResponse result = fieldValueRetrieverService.getValues(fieldId, queryTerm, offset, limit);
		return result.toJSONString();
	}

}
