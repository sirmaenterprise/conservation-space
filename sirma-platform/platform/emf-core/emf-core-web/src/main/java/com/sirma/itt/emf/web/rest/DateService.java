/**
 *
 */
package com.sirma.itt.emf.web.rest;

import java.util.Date;

import com.sirma.itt.seip.time.schedule.OperationType;

/**
 * Contains logic related to calculating dates. There is a method for adding/subtracting a given number of days from a
 * given date minding work days.
 *
 * @author Stanislav Milev
 */
public interface DateService {

	/**
	 * Adds/Subtracts (depending on the operation parameter) the given number of days to/from the start date (minding
	 * work days) and returns the result.
	 *
	 * @param startDate
	 *            starting date for the calculation
	 * @param numberOfDays
	 *            number of days to add/subtract
	 * @param operation
	 *            operation type (addition or subtraction)
	 * @return the calculated date
	 */
	Date calculateDateMindingWorkDays(Date startDate, int numberOfDays, OperationType operation);

}
