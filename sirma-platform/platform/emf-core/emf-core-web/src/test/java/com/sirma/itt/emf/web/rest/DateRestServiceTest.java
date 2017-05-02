/**
 *
 */
package com.sirma.itt.emf.web.rest;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.when;

import java.text.ParseException;
import java.text.SimpleDateFormat;

import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import com.sirma.itt.emf.web.util.DateUtil;
import com.sirma.itt.seip.domain.rest.RestServiceException;
import com.sirma.itt.seip.time.schedule.OperationType;

/**
 * Tests for calculate date rest service.
 *
 * @author Stanislav Milev
 */
@Test
public class DateRestServiceTest {

	private static final String TEST_DATE = "02/24/2016";

	private static final String RESULT_DATE = "02/26/2016";

	@InjectMocks
	private DateRestService service = new DateRestService();

	@Mock
	private DateService dateService;

	@Mock
	private DateUtil dateUtil;

	/**
	 * Initialise test mocks.
	 *
	 * @throws ParseException
	 */
	@BeforeClass
	public void setup() throws ParseException {
		MockitoAnnotations.initMocks(this);
		SimpleDateFormat format = new SimpleDateFormat("MM/dd/yyyy");

		when(dateUtil.parse(TEST_DATE)).thenReturn(format.parse(TEST_DATE));
		when(dateService.calculateDateMindingWorkDays(format.parse(TEST_DATE), 2, OperationType.ADD))
				.thenReturn(format.parse(RESULT_DATE));
		when(dateUtil.getFormattedDate(format.parse(RESULT_DATE))).thenReturn(RESULT_DATE);
	}

	/**
	 * <pre>
	 * Method: calculateDate
	 *
	 * params: null for operation
	 *
	 * Expected result: RestServiceException
	 * </pre>
	 */
	@Test(expectedExceptions = RestServiceException.class)
	public void calculateDate_nullOperation_bedResponse() {
		service.calculate(null, null, 0);
	}

	/**
	 * <pre>
	 * Method: calculateDate
	 *
	 * params: empty operation
	 *
	 * Expected result: RestServiceException
	 * </pre>
	 */
	@Test(expectedExceptions = RestServiceException.class)
	public void calculateDate_emptyOperation_bedResponse() {
		service.calculate("", null, 0);
	}

	/**
	 * <pre>
	 * Method: calculateDate
	 *
	 * params: wrong operation
	 *
	 * Expected result: RestServiceException
	 * </pre>
	 */
	@Test(expectedExceptions = RestServiceException.class)
	public void calculateDate_wrongOperation_bedResponse() {
		service.calculate("c", null, 0);
	}

	/**
	 * <pre>
	 * Method: calculateDate
	 *
	 * params: null starting date
	 *
	 * Expected result: RestServiceException
	 * </pre>
	 */
	@Test(expectedExceptions = RestServiceException.class)
	public void calculateDate_nullStartDate_bedResponse() {
		service.calculate("ADD", null, 0);
	}

	/**
	 * <pre>
	 * Method: calculateDate
	 *
	 * params: empty starting date
	 *
	 * Expected result: RestServiceException
	 * </pre>
	 */
	@Test(expectedExceptions = RestServiceException.class)
	public void calculateDate_emptyStartDate_bedResponse() {
		service.calculate("ADD", "", 0);
	}

	/**
	 * <pre>
	 * Method: calculateDate
	 *
	 * params: invalid starting date
	 *
	 * Expected result: RestServiceException
	 * </pre>
	 */
	@Test(expectedExceptions = RestServiceException.class)
	public void calculateDate_wrongStartDate_bedResponse() {
		service.calculate("ADD", "c", 0);
	}

	/**
	 * <pre>
	 * Method: calculateDate
	 *
	 * Request data: correct data
	 *
	 * Expected result: RestServiceException
	 * </pre>
	 */
	public void calculateDate_correctData_goodResponse() {
		String response = service.calculate("ADD", TEST_DATE, 2);
		assertEquals("{\"calculatedDate\":\"" + RESULT_DATE + "\"}", response);
	}

}
