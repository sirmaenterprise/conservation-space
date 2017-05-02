package com.sirma.itt.emf.util;

/**
 * Generic data provider.
 *
 * @author yasko
 */
public interface DataProvider {

	String DATA_PROVIDER_EXTENSION = "data.provider";

	/**
	 * Initiates data load.
	 *
	 * @param context
	 *            Provider context data.
	 */
	void provide(DataProviderContext context);
}