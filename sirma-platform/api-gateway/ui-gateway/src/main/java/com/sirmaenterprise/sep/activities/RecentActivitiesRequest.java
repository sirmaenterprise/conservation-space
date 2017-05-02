package com.sirmaenterprise.sep.activities;

import static com.sirma.itt.seip.collections.CollectionUtils.addNonNullValue;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

import org.apache.commons.lang3.StringUtils;

import com.sirma.itt.seip.rest.utils.request.RequestInfo;
import com.sirma.itt.seip.rest.utils.request.params.RequestParam;
import com.sirma.itt.seip.rest.utils.request.params.RequestParams;
import com.sirma.itt.seip.time.DateRange;
import com.sirma.itt.seip.time.ISO8601DateFormat;

/**
 * Storage for the recent activities request properties.
 *
 * @author A. Kunchev
 */
public class RecentActivitiesRequest {

	private Collection<Serializable> ids;

	private int limit;

	private int offset;

	private Optional<DateRange> dateRange = Optional.empty();

	/**
	 * Builds new request by retrieving the information from the {@link RequestInfo} object. The parameters are
	 * retrieved mostly as query parameters from the request.
	 *
	 * @param info
	 *            the {@link RequestInfo} from which the different parameters are retrieved
	 * @param idRequestParam
	 *            {@link RequestParam} used to retrieved the instance id from the request
	 * @return new {@link RecentActivitiesRequest}
	 */
	public static RecentActivitiesRequest buildRequestFromInfo(RequestInfo info, RequestParam<String> idRequestParam) {
		List<Serializable> ids = new ArrayList<>(1);
		// make sure the id is not empty string
		addNonNullValue(ids, StringUtils.trimToNull(idRequestParam.get(info)));
		return new RecentActivitiesRequest()
				.setLimit(RequestParams.QUERY_LIMIT.get(info))
					.setOffset(RequestParams.QUERY_OFFSET.get(info))
					.setDateRange(buildDateRangeFromInfo(info))
					.setIds(ids);
	}

	private static DateRange buildDateRangeFromInfo(RequestInfo info) {
		String startDate = RequestParams.QUERY_START.get(info);
		String endDate = RequestParams.QUERY_END.get(info);
		if (StringUtils.isBlank(startDate) && StringUtils.isBlank(endDate)) {
			return null;
		}

		return new DateRange(ISO8601DateFormat.parse(startDate), ISO8601DateFormat.parse(endDate));
	}

	public Collection<Serializable> getIds() {
		return ids;
	}

	public RecentActivitiesRequest setIds(Collection<Serializable> ids) {
		this.ids = ids;
		return this;
	}

	public int getLimit() {
		return limit;
	}

	public RecentActivitiesRequest setLimit(int limit) {
		this.limit = limit;
		return this;
	}

	public int getOffset() {
		return offset;
	}

	public RecentActivitiesRequest setOffset(int offset) {
		this.offset = offset;
		return this;
	}

	public Optional<DateRange> getDateRange() {
		return dateRange;
	}

	public RecentActivitiesRequest setDateRange(DateRange dateRange) {
		this.dateRange = Optional.ofNullable(dateRange);
		return this;
	}

}
