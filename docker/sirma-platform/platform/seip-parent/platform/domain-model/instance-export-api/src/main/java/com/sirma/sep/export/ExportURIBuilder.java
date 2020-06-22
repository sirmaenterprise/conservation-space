package com.sirma.sep.export;

import static com.sirma.itt.seip.collections.CollectionUtils.emptyList;

import java.net.URI;
import java.util.Collection;

/**
 * Provides logic that generates {@link URI} for instances which should be exported. These {@link URI}s are used as
 * argument for the third party libraries for generating specific file from HTML source.
 *
 * @author A. Kunchev
 */
public interface ExportURIBuilder {

	/**
	 * Generates {@link URI} for specific instance id. Generated {@link URI} represents link through which the instance
	 * could be opened. Uses current authentication token to generate the security part of the link.
	 *
	 * @param instanceId
	 *            the id of the instance for which should be generated link
	 * @return {@link URI} that could be used to open the specified instance
	 */
	default URI generateURI(String instanceId) {
		return generateURI(instanceId, null);
	}

	/**
	 * Generates {@link URI} for specific instance id. Generated {@link URI} represents link through which the instance
	 * could be opened.
	 *
	 * @param instanceId
	 *            the id of the instance for which should be generated link
	 * @param token
	 *            security token that is used to authenticate in the system. If blank then the current token will be
	 *            used instead
	 * @return {@link URI} that could be used to open the specified instance
	 */
	default URI generateURI(String instanceId, String token) {
		return generateURIForTabs(emptyList(), instanceId, token);
	}

	/**
	 * Generates {@link URI} for specific instance id and tabs. Generated {@link URI} represents link through which the
	 * instance could be opened. The tabs are used in export functionalities to specify the exact tabs that should be
	 * exported.
	 *
	 * @param tabs
	 *            specifies which tabs should be included in the link
	 * @param instanceId
	 *            the id of the instance for which should be generated link
	 * @param token
	 *            security token that is used to authenticate in the system. If blank then the current token will be
	 *            used instead
	 * @return {@link URI} that could be used to open the specified instance
	 */
	URI generateURIForTabs(Collection<String> tabs, String instanceId, String token);

	/**
	 * Provides the current JWT token or throws exception, if there is no authenticated user at the moment.
	 *
	 * @return JWT token for the currently authenticated user
	 */
	String getCurrentJwtToken();

}