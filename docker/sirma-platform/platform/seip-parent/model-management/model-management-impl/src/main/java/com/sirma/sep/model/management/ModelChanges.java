package com.sirma.sep.model.management;

import java.util.Collections;
import java.util.List;

import com.sirma.itt.seip.collections.CollectionUtils;
import com.sirma.sep.model.management.operation.ModelChangeSetInfo;

/**
 * Internal representation of model change request.
 *
 * @author <a href="mailto:borislav.bonev@sirma.bg">Borislav Bonev</a>
 * @since 09/08/2018
 */
public class ModelChanges {
	private Long modelVersion;
	private List<ModelChangeSetInfo> changes;

	public Long getModelVersion() {
		return modelVersion;
	}

	public void setModelVersion(Long modelVersion) {
		this.modelVersion = modelVersion;
	}

	public List<ModelChangeSetInfo> getChanges() {
		if (changes == null) {
			return Collections.emptyList();
		}
		return changes;
	}

	public void setChanges(List<ModelChangeSetInfo> changes) {
		this.changes = changes;
	}

	public boolean isEmpty() {
		return CollectionUtils.isEmpty(changes);
	}
}
