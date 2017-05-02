package com.sirma.itt.seip.export.rest;

import java.util.List;
import java.util.Set;

import com.sirma.itt.seip.instance.actions.ActionRequest;

/**
 * Request model for exporting list data (e.g. widget) to specified file format. Contains title of widget, selected
 * instances, properties, header type and exporting format.
 *
 * @author gshefkedov
 */
public class ExportListDataXlsxRequest extends ActionRequest {
	public static final String EXPORT_XLSX = "exportXlsx";
	private static final long serialVersionUID = -7162319605784246665L;
	private String filename;
	private List<String> selectedInstances;
	private Set<String> selectedProperties;
	private String headerType;

	@Override
	public String getOperation() {
		return EXPORT_XLSX;
	}

	/**
	 * Getter method for filename.
	 *
	 * @return title
	 */
	public String getFileName() {
		return filename;
	}

	/**
	 * Setter method for filename.
	 *
	 * @param filename
	 *            the filename to set
	 */
	public void setFileName(String filename) {
		this.filename = filename;
	}

	/**
	 * Getter method for headerType - e.g. selected header type in widget.
	 *
	 * @return the headerType
	 */
	public String getHeaderType() {
		return headerType;
	}

	/**
	 * Setter method for headerTyper - e.g. selected header type in widget.
	 *
	 * @param headerType
	 *            the headerType to set
	 */
	public void setHeaderType(String headerType) {
		this.headerType = headerType;
	}

	/**
	 * Getter method for selectedInstances - e.g. selected selected instances in widget.
	 *
	 * @return the selectedInstances
	 */
	public List<String> getSelectedInstances() {
		return selectedInstances;
	}

	/**
	 * Setter method for selectedInstances - e.g. selected selected instances in widget.
	 *
	 * @param selectedInstances
	 *            the selectedInstances to set
	 */
	public void setSelectedInstances(List<String> selectedInstances) {
		this.selectedInstances = selectedInstances;
	}

	/**
	 * Getter method for selectedProperties - e.g. selected selected properties in widget.
	 *
	 * @return the selectedProperties
	 */
	public Set<String> getSelectedProperties() {
		return selectedProperties;
	}

	/**
	 * Setter method for selectedProperties - e.g. selected selected properties in widget.
	 *
	 * @param selectedProperties
	 *            the selectedProperties to set
	 */
	public void setSelectedProperties(Set<String> selectedProperties) {
		this.selectedProperties = selectedProperties;
	}
}
