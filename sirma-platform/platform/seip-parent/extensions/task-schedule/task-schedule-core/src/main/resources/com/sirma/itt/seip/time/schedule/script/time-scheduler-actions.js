/**
 * Builds a DateRange object with given start and end date
 * and modified hour, minute, second and millisecond fields.
 * The start date time is set to beginning of day (00:00:00.000) 
 * and  the end date time is set to end of day (23:59:59.999).
 * 
 * @param startDate - the start date
 * @param endDate - the end date
 */
function buildDateRange(startDate, endDate) {
	var Calendar = Java.type('java.util.Calendar');
	var DateRange = Java.type('com.sirma.itt.seip.time.DateRange');
	
	var calendar = Calendar.getInstance();
	var fromDate = null;
	var toDate = null;
	
	if (startDate) {
		calendar.setTime(startDate);
		calendar.set(Calendar.HOUR_OF_DAY, 0);
		calendar.set(Calendar.MINUTE, 0);
		calendar.set(Calendar.SECOND, 0);
		calendar.set(Calendar.MILLISECOND, 0);

		fromDate = calendar.getTime();
	}
	
	if (endDate) {
		calendar.setTime(endDate);
		calendar.set(Calendar.HOUR_OF_DAY, 23);
		calendar.set(Calendar.MINUTE, 59);
		calendar.set(Calendar.SECOND, 59);
		calendar.set(Calendar.MILLISECOND, 999);

		toDate = calendar.getTime();
	}

	return new DateRange(fromDate, toDate);
}