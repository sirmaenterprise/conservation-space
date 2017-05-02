/**
 *
 */
package com.sirma.itt.emf.web.rest;

import java.util.Date;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response.Status;

import org.json.JSONObject;

import com.sirma.itt.emf.rest.EmfRestService;
import com.sirma.itt.emf.web.util.DateUtil;
import com.sirma.itt.seip.domain.rest.RestServiceException;
import com.sirma.itt.seip.json.JsonUtil;
import com.sirma.itt.seip.time.schedule.OperationType;

/**
 * Contains rest service for calculating dates.
 *
 * @author smilev
 */
@ApplicationScoped
@Path("/date")
@Produces(MediaType.APPLICATION_JSON)
public class DateRestService extends EmfRestService {

	@Inject
	private DateService dateService;

	@Inject
	private DateUtil dateUtil;

	/**
	 * Calculates date. Adds/subtracts the given number of days to/from the start date. The starting date, number of
	 * days are passed as parameters and the operation type - as part of the path. This service consumes GET request, if
	 * any of the parameters is missing, it will return null. <BR>
	 * The startDate format is EMF.date.format(startDate, SF.config.dateFormatPattern, false) (js) (or what
	 * dateUtil.parse() can parse) and the response date format is dateUtil.getFormattedDate(calculatedDate) (java).
	 *
	 * @param operationRaw
	 *            the operation in raw format (as a string)
	 * @param startDateRaw
	 *            the starting date in raw format (as a string)
	 * @param numberOfDays
	 *            the number of days
	 * @return The calculated date, or null if parameters are missing.
	 */
	@GET
	@Path("/{operation}")
	public String calculate(@PathParam("operation") String operationRaw, @QueryParam("startDate") String startDateRaw,
			@QueryParam("numberOfDays") int numberOfDays) {
		if ((operationRaw == null) || operationRaw.isEmpty()) {
			throw new RestServiceException("No operation!", Status.BAD_REQUEST);
		}
		if ((startDateRaw == null) || startDateRaw.isEmpty()) {
			throw new RestServiceException("No starting date!", Status.BAD_REQUEST);
		}
		Date startDate = dateUtil.parseDate(startDateRaw);
		if (startDate == null) {
			startDate = dateUtil.parse(startDateRaw);
			if (startDate == null) {
				throw new RestServiceException("Can't parse starting date!", Status.BAD_REQUEST);
			}
		}
		OperationType operation = OperationType.valueOf(operationRaw);
		Date calculatedDate = dateService.calculateDateMindingWorkDays(startDate, numberOfDays, operation);

		JSONObject result = new JSONObject();
		JsonUtil.addToJson(result, "calculatedDate", dateUtil.getFormattedDate(calculatedDate));
		return result.toString();
	}

}
