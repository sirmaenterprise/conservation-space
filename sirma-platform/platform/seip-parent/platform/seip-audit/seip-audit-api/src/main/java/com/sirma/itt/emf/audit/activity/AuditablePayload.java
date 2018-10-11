package com.sirma.itt.emf.audit.activity;

import com.sirma.itt.seip.domain.instance.Instance;
import com.sirma.itt.seip.event.EmfEvent;

/**
 * Data transfer object that carries payload eligible for auditing.
 *
 * @author Mihail Radkov
 */
public class AuditablePayload {

	private Instance instance;

	private String operationId;

	private EmfEvent triggeredBy;

	private boolean showParentPath;

	private String relationStatus;

	private String targetProperties;

	private Instance extraContext;

	/**
	 * Creates new DTO with the provided parameters.
	 *
	 * @param instance
	 *            - the instance for auditing
	 * @param operationId
	 *            - the operation id
	 * @param triggeredBy
	 *            - the event that triggered the auditing
	 * @param showParentPath
	 *            - show instance parent path if true. Hide it otherwise
	 */
	public AuditablePayload(Instance instance, String operationId, EmfEvent triggeredBy, boolean showParentPath) {
		this.instance = instance;
		this.operationId = operationId;
		this.triggeredBy = triggeredBy;
		this.showParentPath = showParentPath;
	}

	/**
	 * Default constructor.
	 */
	public AuditablePayload() {
		// default constructor
	}

	/**
	 * Getter method for instance.
	 *
	 * @return the instance
	 */
	public Instance getInstance() {
		return instance;
	}

	/**
	 * Getter method for operationId.
	 *
	 * @return the operationId
	 */
	public String getOperationId() {
		return operationId;
	}

	/**
	 * Getter method for triggeredBy.
	 *
	 * @return the triggeredBy
	 */
	public EmfEvent getTriggeredBy() {
		return triggeredBy;
	}

	/**
	 * Getter method for showParentPath
	 *
	 * @return showParentPath
	 */
	public boolean showParentPath() {
		return showParentPath;
	}

	/**
	 * Getter method for extraContext.
	 *
	 * @return the extraContext
	 */
	public Instance getExtraContext() {
		return extraContext;
	}

	/**
	 * Setter method for extraContext.
	 *
	 * @param extraContextinstance
	 *            for auditing the extraContext to set
	 */
	public AuditablePayload setExtraContext(Instance extraContext) {
		this.extraContext = extraContext;
		return this;
	}

	/**
	 * Getter method for status.
	 *
	 * @return the status
	 */
	public String getStatus() {
		return relationStatus;
	}

	/**
	 * Getter method for showParentPath.
	 *
	 * @return the showParentPath
	 */
	public boolean isShowParentPath() {
		return showParentPath;
	}

	/**
	 * Setter method for showParentPath.
	 *
	 * @param showParentPath
	 *            the showParentPath to set
	 */
	public AuditablePayload setShowParentPath(boolean showParentPath) {
		this.showParentPath = showParentPath;
		return this;
	}

	/**
	 * Getter method for targetProperties.
	 *
	 * @return the targetProperties
	 */
	public String getTargetProperties() {
		return targetProperties;
	}

	/**
	 * Setter method for targetProperties.
	 *
	 * @param targetProperties
	 *            the targetProperties to set
	 */
	public AuditablePayload setTargetProperties(String targetProperties) {
		this.targetProperties = targetProperties;
		return this;
	}

	/**
	 * Setter method for instance.
	 *
	 * @param instance
	 *            the instance to set
	 */
	public AuditablePayload setInstance(Instance instance) {
		this.instance = instance;
		return this;
	}

	/**
	 * Setter method for operationId.
	 *
	 * @param operationId
	 *            the operationId to set
	 */
	public AuditablePayload setOperationId(String operationId) {
		this.operationId = operationId;
		return this;
	}

	/**
	 * Setter method for triggeredBy.
	 *
	 * @param triggeredBy
	 *            the triggeredBy to set
	 */
	public AuditablePayload setTriggeredBy(EmfEvent triggeredBy) {
		this.triggeredBy = triggeredBy;
		return this;
	}

	/**
	 * Setter method for status.
	 *
	 * @param status
	 *            the status to set
	 */
	public AuditablePayload setRelationStatus(String status) {
		this.relationStatus = status;
		return this;
	}

}
