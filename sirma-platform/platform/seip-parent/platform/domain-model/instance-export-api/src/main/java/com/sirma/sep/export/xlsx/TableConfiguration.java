package com.sirma.sep.export.xlsx;

import java.util.Map;

/**
 * Holds the information about the configuration of the table and how the information should be exported.
 *
 * @author A. Kunchev
 */
public class TableConfiguration {

	private final boolean manuallySelected;
	private final Map<String, String> headersInfo;
	private final boolean showInstanceId;

	/**
	 * Instantiates new table configuration.
	 *
	 * @param manuallySelected shows if the objects are selected manually
	 * @param headersInfo      maps with the labels for the first row in the generated excel. It will represent the headers for the
	 *                         displayed information about the objects
	 * @param showInstanceId   - true if instance id have to be populated in generated excel.
	 */
	TableConfiguration(final boolean manuallySelected, final Map<String, String> headersInfo, boolean showInstanceId) {
		this.manuallySelected = manuallySelected;
		this.headersInfo = headersInfo;
		this.showInstanceId = showInstanceId;
	}

	/**
	 * Shows if the objects displayed in the table are manually selected.
	 *
	 * @return <code>true</code> if the object are manually selected, <code>false</code> otherwise
	 */
	public boolean isManuallySelected() {
		return manuallySelected;
	}

	/**
	 * Map with key property name, which are used for filling cell and value label of column.
	 *
	 * @return map with key property name of instance name and value its label
	 */
	public Map<String, String> getHeadersInfo() {
		return headersInfo;
	}

	/**
	 * Shows if instance id have to be populated in generated excel.
	 *
	 * @return <code>true</code> if the instance id have to be populated in generated excel.
	 */
	public boolean showInstanceId() {
		return showInstanceId;
	}
}
