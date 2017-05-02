/**
 *
 */
package com.sirma.itt.emf.web.rest;

import java.util.Date;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import com.sirma.itt.seip.time.schedule.DeadlineCalculator;
import com.sirma.itt.seip.time.schedule.OperationType;

/**
 * Implementation of the DateService.
 *
 * @author Stanislav Milev
 */
@ApplicationScoped
public class DateServiceImpl implements DateService {

	@Inject
	private DeadlineCalculator dateCalculator;

	@Override
	public Date calculateDateMindingWorkDays(Date startDate, int numberOfDays, OperationType operation) {
		return dateCalculator.calculateDate(startDate, numberOfDays, true, operation);
	}

}
