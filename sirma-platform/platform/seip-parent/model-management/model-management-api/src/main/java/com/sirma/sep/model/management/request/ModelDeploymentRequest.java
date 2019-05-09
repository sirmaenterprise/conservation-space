package com.sirma.sep.model.management.request;

import java.util.List;

import com.sirma.itt.seip.collections.CollectionUtils;

import com.fasterxml.jackson.annotation.JsonIgnore;

/**
 * A request specifying which changed models to deploy and to which version to select non deployed changes.
 *
 * @author Mihail Radkov
 */
public class ModelDeploymentRequest {

	private List<String> modelsToDeploy;

	private Long version;

	public List<String> getModelsToDeploy() {
		return modelsToDeploy;
	}

	public ModelDeploymentRequest setModelsToDeploy(List<String> modelsToDeploy) {
		this.modelsToDeploy = modelsToDeploy;
		return this;
	}

	public Long getVersion() {
		return version;
	}

	public ModelDeploymentRequest setVersion(Long version) {
		this.version = version;
		return this;
	}

	@JsonIgnore
	public boolean isEmpty() {
		return CollectionUtils.isEmpty(modelsToDeploy);
	}
}
