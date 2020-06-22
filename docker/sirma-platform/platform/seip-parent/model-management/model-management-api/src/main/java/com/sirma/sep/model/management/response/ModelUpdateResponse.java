package com.sirma.sep.model.management.response;

import java.util.List;

import com.sirma.sep.model.management.operation.ModelChangeSetInfo;

/**
 * Response returned after model update request. It carries the model changes unknown for the caller including model
 * hierarchy and what is the last known model version.
 *
 * @author <a href="mailto:borislav.bonev@sirma.bg">Borislav Bonev</a>
 * @since 31/07/2018
 */
public class ModelUpdateResponse {

	private long modelVersion;

	private List<ModelChangeSetInfo> changeSets;

	private boolean modelHierarchyChanged;

	public long getModelVersion() {
		return modelVersion;
	}

	public ModelUpdateResponse setModelVersion(long modelVersion) {
		this.modelVersion = modelVersion;
		return this;
	}

	public List<ModelChangeSetInfo> getChangeSets() {
		return changeSets;
	}

	public ModelUpdateResponse setChangeSets(List<ModelChangeSetInfo> changeSets) {
		this.changeSets = changeSets;
		return this;
	}

	public boolean isModelHierarchyChanged() {
		return modelHierarchyChanged;
	}

	public ModelUpdateResponse setModelHierarchyChanged(boolean modelHierarchyChanged) {
		this.modelHierarchyChanged = modelHierarchyChanged;
		return this;
	}
}
