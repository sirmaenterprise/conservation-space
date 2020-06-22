package com.sirma.itt.seip.eai.model.internal;

import java.util.List;

import com.sirma.itt.seip.eai.exception.EAIReportableException;

/**
 * Basic class for multiple processed instances. Optionally contains an aggregated error that is generated during
 * parsing.
 * 
 * @param <E>
 *            the instance model type
 * @author bbanchev
 */
public abstract class BatchProcessedInstancesModel<E> implements ProcessedInstanceModel {
	private List<E> instances;
	private EAIReportableException error;

	/**
	 * Gets the processed instances - possibly empty.
	 *
	 * @return the instances
	 */
	public List<E> getInstances() {
		return instances;
	}

	/**
	 * Sets the instances that are processed during service request.
	 *
	 * @param instances
	 *            the new instances
	 */
	public void setInstances(List<E> instances) {
		this.instances = instances;
	}

	/**
	 * Gets the error that might occur during partial instance processing.
	 *
	 * @return the error. Might be null
	 */
	public EAIReportableException getError() {
		return error;
	}

	/**
	 * Sets the error that might occur during partial instance retrieval.
	 *
	 * @param error
	 *            the error to set
	 */
	public void setError(EAIReportableException error) {
		this.error = error;
	}
}
