package com.sirma.itt.seip.eai.cs.model.internal;

import com.esotericsoftware.kryo.serializers.TaggedFieldSerializer.Tag;
import com.sirma.itt.seip.eai.model.internal.ExternalInstanceIdentifier;
import com.sirma.itt.seip.util.EqualsHelper;

/**
 * Extends the base identifier by adding source system id.
 *
 * @author bbanchev
 */
public class CSExternalInstanceId extends ExternalInstanceIdentifier {
	private static final long serialVersionUID = 1208568842353584797L;
	@Tag(2)
	private String sourceSystemId;

	/**
	 * Instantiates new empty object
	 */
	public CSExternalInstanceId() {
		// needed for kryo
	}

	/**
	 * Instantiates a new CS external instance id.
	 *
	 * @param externalId
	 *            - some uid of instance
	 * @param sourceSystemId
	 *            the source system id - cs requirement
	 */
	public CSExternalInstanceId(String externalId, String sourceSystemId) {
		super(externalId);
		this.sourceSystemId = sourceSystemId;
	}

	/**
	 * Gets the source system id.
	 *
	 * @return the source system id
	 */
	public String getSourceSystemId() {
		return sourceSystemId;
	}

	/**
	 * Sets the source system id.
	 *
	 * @param sourceSystemId
	 *            the new source system id
	 */
	public void setSourceSystemId(String sourceSystemId) {
		this.sourceSystemId = sourceSystemId;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((externalId == null) ? 0 : externalId.toLowerCase().hashCode());
		result = prime * result + ((sourceSystemId == null) ? 0 : sourceSystemId.toLowerCase().hashCode());
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
		if (!(obj instanceof CSExternalInstanceId)) {
			return false;
		}
		CSExternalInstanceId other = (CSExternalInstanceId) obj;
		return EqualsHelper.nullSafeEquals(sourceSystemId, other.sourceSystemId, true)
				&& EqualsHelper.nullSafeEquals(externalId, other.externalId, true);
	}

	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append("Record");
		builder.append("[id=");
		builder.append(externalId);
		builder.append(", source=");
		builder.append(sourceSystemId);
		builder.append("]");
		return builder.toString();
	}

}
