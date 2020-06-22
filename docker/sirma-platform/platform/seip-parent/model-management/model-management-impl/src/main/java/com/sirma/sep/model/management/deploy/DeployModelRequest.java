package com.sirma.sep.model.management.deploy;

import java.util.Set;

/**
 * Request that specifies what model instances should be deployed up to specific version and available for use by the end clients.
 *
 * @author <a href="mailto:borislav.bonev@sirma.bg">Borislav Bonev</a>
 * @since 01/08/2018
 */
public class DeployModelRequest {

	private Set<String> pathsToDeploy;
	private Long version;

	public Set<String> getPathsToDeploy() {
		return pathsToDeploy;
	}

	public void setPathsToDeploy(Set<String> pathsToDeploy) {
		this.pathsToDeploy = pathsToDeploy;
	}

	public Long getVersion() {
		return version;
	}

	public void setVersion(Long version) {
		this.version = version;
	}

	@Override
	public String toString() {
		return "DeployModelRequest{" +
				"pathsToDeploy=" + pathsToDeploy +
				", version=" + version +
				'}';
	}
}
