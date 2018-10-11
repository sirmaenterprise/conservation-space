package com.sirmaenterprise.sep.eai.spreadsheet.service.rest;

import com.sirma.itt.seip.domain.instance.Instance;
import com.sirma.itt.seip.eai.model.internal.BatchProcessedInstancesModel;
import com.sirmaenterprise.sep.eai.spreadsheet.model.internal.ParsedInstance;
import com.sirmaenterprise.sep.eai.spreadsheet.model.internal.SpreadsheetResultInstances;

/**
 * Wrapper class for the spreadsheet processing result. Contains a collection of successfully processed
 * {@link ParsedInstance}s and a reference to {@link Instance} containing a validation report for the processing
 */
public class SpreadsheetOperationReport {
	private BatchProcessedInstancesModel<ParsedInstance> result;
	private Instance report;

	/**
	 * {@link SpreadsheetOperationReport} initialize the {@link #getResult()} instance
	 */
	public SpreadsheetOperationReport() {
		result = new SpreadsheetResultInstances();
	}

	/**
	 * Gets the result instances. Might be null
	 *
	 * @return the result
	 */
	public BatchProcessedInstancesModel<ParsedInstance> getResult() {
		return result;
	}

	/**
	 * Sets the result model.
	 *
	 * @param result
	 *            the result to set
	 */
	public void setResult(BatchProcessedInstancesModel<ParsedInstance> result) {
		this.result = result;
	}

	/**
	 * Gets the report.
	 *
	 * @return the report
	 */
	public Instance getReport() {
		return report;
	}

	/**
	 * Sets the report.
	 *
	 * @param report
	 *            the new report
	 */
	public void setReport(Instance report) {
		this.report = report;
	}

}
