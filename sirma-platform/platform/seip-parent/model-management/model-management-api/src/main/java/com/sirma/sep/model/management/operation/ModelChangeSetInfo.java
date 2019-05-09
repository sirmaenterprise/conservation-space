package com.sirma.sep.model.management.operation;

import java.util.Date;
import java.util.Objects;

import com.sirma.sep.model.management.Path;

/**
 * Represents an accepted model change request. It carries additional information about when by and who the change is
 * created and applied.
 *
 * @author <a href="mailto:borislav.bonev@sirma.bg">Borislav Bonev</a>
 * @since 07/08/2018
 */
public class ModelChangeSetInfo {
	private Long index;
	private ModelChangeSet changeSet;
	private long initialVersion;
	private Long appliedVersion;
	private String createdBy;
	private Date createdOn;
	private Date appliedOn;
	private Date deployedOn;
	private Date failedOn;
	private ModelChangeSetStatus status = ModelChangeSetStatus.NEW;
	private String statusMessage;

	/**
	 * Creates a {@link ModelChangeSetInfo} that wraps the given {@link ModelChangeSet} elements and mark it as
	 * {@link ModelChangeSetStatus#INTERMEDIATE} and fills it's basic properties.
	 *
	 * @param changeSet
	 *            the {@link ModelChangeSet} from which to generate intermediate {@link ModelChangeSetInfo}
	 * @param operation
	 *            the change operation name, required
	 * @return the created change set info
	 */
	public static ModelChangeSetInfo createIntermediate(ModelChangeSet changeSet, String operation) {
		return createIntermediate(changeSet.getPath(), operation, changeSet.getNewValue(), changeSet.getOldValue());
	}

	/**
	 * Creates a {@link ModelChangeSetInfo} that wraps the given {@link ModelChangeSet} elements and mark it as
	 * {@link ModelChangeSetStatus#INTERMEDIATE} and fills it's basic properties.
	 *
	 * @param selector the change selector path to use, required
	 * @param operation the change operation name, required
	 * @param newValue the change new value
	 * @param oldValue the change old value
	 * @return the created change set info
	 */
	public static ModelChangeSetInfo createIntermediate(Path selector, String operation, Object newValue,
			Object oldValue) {
		return createIntermediate(new ModelChangeSet()
				.setSelector(Objects.requireNonNull(selector, "Node selector is required").toString())
				.setOperation(Objects.requireNonNull(operation, "Change operation is required"))
				.setNewValue(newValue)
				.setOldValue(oldValue));
	}

	/**
	 * Creates a {@link ModelChangeSetInfo} that wraps the given {@link ModelChangeSet} and mark it as
	 * {@link ModelChangeSetStatus#INTERMEDIATE} and fills it's basic properties.
	 *
	 * @param changeSet the change set to wrap in change set info
	 * @return the created change set info
	 */
	public static ModelChangeSetInfo createIntermediate(ModelChangeSet changeSet) {
		ModelChangeSetInfo info = new ModelChangeSetInfo();
		info.setStatus(ModelChangeSetStatus.INTERMEDIATE);
		info.setCreatedOn(new Date());
		info.setCreatedBy("System");
		info.setChangeSet(changeSet);
		return info;
	}

	public ModelChangeSet getChangeSet() {
		return changeSet;
	}

	public ModelChangeSetInfo setChangeSet(ModelChangeSet changeSet) {
		this.changeSet = changeSet;
		return this;
	}

	public String getCreatedBy() {
		return createdBy;
	}

	public void setCreatedBy(String createdBy) {
		this.createdBy = createdBy;
	}

	public Date getCreatedOn() {
		return createdOn;
	}

	public void setCreatedOn(Date createdOn) {
		this.createdOn = createdOn;
	}

	public Date getAppliedOn() {
		return appliedOn;
	}

	public void setAppliedOn(Date appliedOn) {
		this.appliedOn = appliedOn;
	}

	public boolean hasIndex() {
		return index != null;
	}

	public Long getIndex() {
		return index;
	}

	public void setIndex(Long index) {
		this.index = index;
	}

	public Date getDeployedOn() {
		return deployedOn;
	}

	public void setDeployedOn(Date deployedOn) {
		this.deployedOn = deployedOn;
	}

	public long getInitialVersion() {
		return initialVersion;
	}

	public void setInitialVersion(long initialVersion) {
		this.initialVersion = initialVersion;
	}

	public Long getAppliedVersion() {
		return appliedVersion;
	}

	public void setAppliedVersion(Long appliedVersion) {
		this.appliedVersion = appliedVersion;
	}

	public Date getFailedOn() {
		return failedOn;
	}

	public void setFailedOn(Date failedOn) {
		this.failedOn = failedOn;
	}

	public ModelChangeSetStatus getStatus() {
		return status;
	}

	public void setStatus(ModelChangeSetStatus status) {
		this.status = status;
	}

	public String getStatusMessage() {
		return statusMessage;
	}

	public void setStatusMessage(String statusMessage) {
		this.statusMessage = statusMessage;
	}

	/**
	 * Checks if the current change set is intermediate and should be skipped for persistent operations.
	 *
	 * @return true if the current change is intermediate.
	 */
	public boolean isIntermediate() {
		return getStatus() == ModelChangeSetStatus.INTERMEDIATE;
	}

	/**
	 * Mark the current change as applied. The operation sets the applied version, the moment this is called and changes
	 * the status to {@link ModelChangeSetStatus#APPLIED}
	 *
	 * @param version the model version to set
	 */
	public void markAsApplied(long version) {
		setAppliedVersion(version);
		setStatus(ModelChangeSetStatus.APPLIED);
		setAppliedOn(new Date());
	}

	/**
	 * Marks the current change as failed to apply due to given message. This includes recording the moment this is called,
	 * setting the given failing message and changing the status to {@link ModelChangeSetStatus#FAIL_TO_APPLY}
	 *
	 * @param message the error message to set
	 */
	public void markAsFailedToApply(String message) {
		setFailedOn(new Date());
		setStatus(ModelChangeSetStatus.FAIL_TO_APPLY);
		setStatusMessage(message);
	}
}
