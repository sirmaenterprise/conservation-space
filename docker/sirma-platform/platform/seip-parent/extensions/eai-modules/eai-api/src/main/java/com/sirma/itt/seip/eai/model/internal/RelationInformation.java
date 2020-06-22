package com.sirma.itt.seip.eai.model.internal;

import com.sirma.itt.seip.domain.instance.Instance;
import com.sirma.itt.seip.instance.relation.LinkConstants;
import com.sirma.itt.seip.util.EqualsHelper;

/**
 * Represents a relation information bean, holding the source and target objects and additional information needed by
 * the integration services.
 * 
 * @author bbanchev
 */
public class RelationInformation {
	private final Instance sourceInstance;
	private final Object targetInstance;
	private final String relationUri;

	/**
	 * Default constructor. Parameters should not be null.
	 *
	 * @param sourceInstance
	 *            the source instance for relation
	 * @param targetInstance
	 *            the target object for relation. Could be {@link Instance} or {@link ResolvableInstance}
	 * @param relationUri
	 *            the relation uri to represent. Its is internal system id, as one in {@link LinkConstants}.
	 */
	public RelationInformation(Instance sourceInstance, Object targetInstance, String relationUri) {
		this.sourceInstance = sourceInstance;
		this.targetInstance = targetInstance;
		this.relationUri = relationUri;
	}

	/**
	 * Getter method for sourceInstance.
	 *
	 * @return the sourceInstance
	 */
	public Instance getSourceInstance() {
		return sourceInstance;
	}

	/**
	 * Getter method for targetInstance.
	 *
	 * @return the targetInstance
	 */
	public Object getTargetInstance() {
		return targetInstance;
	}

	/**
	 * Getter method for relationUri.
	 *
	 * @return the relationUri
	 */
	public String getRelationUri() {
		return relationUri;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((relationUri == null) ? 0 : relationUri.hashCode());
		result = prime * result + ((sourceInstance == null) ? 0 : sourceInstance.hashCode());
		result = prime * result + ((targetInstance == null) ? 0 : targetInstance.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		if (obj == null) {
			return false;
		}
		if (!(obj instanceof RelationInformation)) {
			return false;
		}
		RelationInformation other = (RelationInformation) obj;
		return EqualsHelper.nullSafeEquals(sourceInstance, other.sourceInstance)
				&& EqualsHelper.nullSafeEquals(targetInstance, other.targetInstance)
				&& EqualsHelper.nullSafeEquals(relationUri, other.relationUri);
	}

	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append("RelationInformation [");
		builder.append("source=");
		if (sourceInstance != null) {
			builder.append(sourceInstance.getId());
		} else {
			builder.append(sourceInstance);
		}
		builder.append(", ");
		builder.append("target=");
		if (targetInstance instanceof ResolvableInstance) {
			builder.append(targetInstance);
		} else if (targetInstance instanceof Instance) {
			builder.append(((Instance) targetInstance).getId());
		} else {
			builder.append(targetInstance);
		}
		builder.append(", ");
		builder.append("relation=");
		builder.append(relationUri);
		builder.append("]");
		return builder.toString();
	}

}
