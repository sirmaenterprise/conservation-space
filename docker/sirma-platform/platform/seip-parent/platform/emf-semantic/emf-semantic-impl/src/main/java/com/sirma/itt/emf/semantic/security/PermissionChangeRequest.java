package com.sirma.itt.emf.semantic.security;

import static com.sirma.itt.seip.util.EqualsHelper.nullSafeEquals;

import java.io.Serializable;

import com.esotericsoftware.kryo.serializers.TaggedFieldSerializer.Tag;

/**
 * Represents a requested permission change assignment
 *
 * @author BBonev
 */
abstract class PermissionChangeRequest implements Serializable {

	private static final long serialVersionUID = -4418856784527939810L;

	@Tag(1)
	private String targetInstance;
	@Tag(3)
	private String targetResource;

	/**
	 * Instantiates a new permission change request.
	 */
	PermissionChangeRequest() {
		// needed by Kryo to instantiate the instance
	}

	/**
	 * Instantiates a new permission change request.
	 *
	 * @param targetInstance the target instance
	 * @param targetResource the target resource
	 */
	PermissionChangeRequest(String targetInstance, String targetResource) {
		this.targetInstance = targetInstance;
		this.targetResource = targetResource;
	}

	public String getTargetInstance() {
		return targetInstance;
	}

	public String getTargetResource() {
		return targetResource;
	}

	@Override
	public String toString() {
		final StringBuilder sb = new StringBuilder("PermissionChangeRequest{");
		sb.append("targetInstance='").append(targetInstance).append('\'');
		sb.append(", targetResource='").append(targetResource).append('\'');
		sb.append('}');
		return sb.toString();
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + (targetInstance == null ? 0 : targetInstance.hashCode());
		result = prime * result + (targetResource == null ? 0 : targetResource.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		if (!(obj instanceof PermissionChangeRequest)) {
			return false;
		}
		PermissionChangeRequest other = (PermissionChangeRequest) obj;
		return nullSafeEquals(targetInstance, other.targetInstance)
				&& nullSafeEquals(targetResource, other.targetResource);
	}

}
