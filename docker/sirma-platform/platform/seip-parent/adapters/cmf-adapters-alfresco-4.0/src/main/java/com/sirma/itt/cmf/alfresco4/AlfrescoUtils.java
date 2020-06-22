package com.sirma.itt.cmf.alfresco4;

import java.io.Serializable;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.Iterator;
import java.util.Map;

import org.apache.log4j.Logger;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.sirma.itt.emf.adapter.DMSException;
import com.sirma.itt.seip.collections.CollectionUtils;
import com.sirma.itt.seip.domain.DmsAware;
import com.sirma.itt.seip.domain.instance.DMSInstance;
import com.sirma.itt.seip.domain.search.SearchArguments;

/**
 * The Class AlfrescoUtils contains methods that helps with common logic to provide alfresco adapter.
 *
 * @author bbanchev
 */
public class AlfrescoUtils {

	/** example 2012-09-26T00:00:00. */
	private static final SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("yyyy-MM-dd");
	/** The Constant END. */
	public static final String SEARCH_END = " ) ";
	/** The Constant START. */
	public static final String SEARCH_START = " ( ";
	/** The Constant DATE_SUFFIX. */
	public static final String DATE_FROM_SUFFIX = "T00:00:00";
	/** The Constant OR. */
	public static final String SEARCH_OR = " OR ";
	/** The Constant OR. */
	public static final String SEARCH_AND = " AND ";

	/** The Constant END_VALUE. */
	public static final String SEARCH_END_VALUE = "\"";

	/** The Constant START_VALUE. */
	public static final String SEARCH_START_VALUE = ":\"";

	private AlfrescoUtils() {
		// utility class
	}

	/**
	 * Populate paging from json alfresco result to search args.
	 *
	 * @param <E>
	 *            the element type
	 * @param result
	 *            the result
	 * @param args
	 *            the args
	 * @throws JSONException
	 *             the jSON exception
	 */
	public static final <E> void populatePaging(JSONObject result, SearchArguments<E> args) throws JSONException {
		if (result.has(AlfrescoCommunicationConstants.KEY_PAGING)) {
			JSONObject paging = result.getJSONObject(AlfrescoCommunicationConstants.KEY_PAGING);
			args.setTotalItems(paging.getInt("totalItems"));
		}
	}

	/**
	 * Synchronized format of date to string using {@link #DATE_FORMAT}.
	 *
	 * @param date
	 *            is the date to format
	 * @return the formated date with {@link #DATE_FORMAT}
	 */
	public static String formatDate(Date date) {
		synchronized (DATE_FORMAT) {
			return DATE_FORMAT.format(date);
		}
	}

	/**
	 * Internal method to validate dmsinstance. Instance is checked if is null or the {@link DMSInstance#getDmsId()}
	 * returns null
	 *
	 * @param instance
	 *            is the instance to check
	 * @return true if this is valid dms instance
	 * @throws DMSException
	 *             if this is not a valid dms instance
	 */
	public static boolean validateExistingDMSInstance(final DmsAware instance) throws DMSException {
		if (instance == null || instance.getDmsId() == null) {
			StringBuilder exceptionMsg = new StringBuilder();
			exceptionMsg.append("Invalid '");
			exceptionMsg.append(instance != null ? instance : "instance");
			exceptionMsg.append("' is provided: ");
			exceptionMsg
					.append(instance == null ? "null" : instance.getDmsId() == null ? " missing DMS ID" : "unknown");
			throw new DMSException(exceptionMsg.toString());
		}
		return true;
	}

	/**
	 * Checks if is authority system admin.
	 *
	 * @param sysAdminName
	 *            the systemadmin name
	 * @param userName
	 *            the username to check
	 * @return true, if is authority system admin
	 */
	public static boolean isAuthoritySystemAdmin(String sysAdminName, String userName) {
		int sysAdminNameLength = 0;
		return userName.toLowerCase().startsWith(sysAdminName)
				&& (userName.length() == (sysAdminNameLength = sysAdminName.length())
						|| userName.charAt(sysAdminNameLength) == '@');
	}

}
