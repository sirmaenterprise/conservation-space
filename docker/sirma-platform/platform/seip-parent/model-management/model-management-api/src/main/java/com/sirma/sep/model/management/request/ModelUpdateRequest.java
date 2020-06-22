package com.sirma.sep.model.management.request;

import java.util.List;

import com.sirma.itt.seip.collections.CollectionUtils;
import com.sirma.sep.model.management.operation.ModelChangeSet;

import com.fasterxml.jackson.annotation.JsonIgnore;

/**
 * Request object used to update the model with a set of changes. The changes are expected to be executed over model
 * with version same as the one specified.
 *
 * @author <a href="mailto:borislav.bonev@sirma.bg">Borislav Bonev</a>
 * @since 31/07/2018
 */
public class ModelUpdateRequest {
	private long modelVersion;
	private List<ModelChangeSet> changes;

	public long getModelVersion() {
		return modelVersion;
	}

	public void setModelVersion(long modelVersion) {
		this.modelVersion = modelVersion;
	}

	public List<ModelChangeSet> getChanges() {
		return changes;
	}

	public void setChanges(List<ModelChangeSet> changes) {
		this.changes = changes;
	}

	@JsonIgnore
	public boolean isEmpty() {
		return CollectionUtils.isEmpty(changes);
	}
}
