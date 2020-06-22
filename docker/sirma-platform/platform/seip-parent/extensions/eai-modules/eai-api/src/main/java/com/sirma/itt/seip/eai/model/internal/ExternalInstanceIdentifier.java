package com.sirma.itt.seip.eai.model.internal;

import java.io.Serializable;

import com.esotericsoftware.kryo.serializers.TaggedFieldSerializer.Tag;
import com.sirma.itt.seip.util.EqualsHelper;

/**
 * Basic representation of external instance identifier holding all needed information. If class is subclassed,
 * equals/hashCode should be overridden
 * 
 * @author bbanchev
 */
public class ExternalInstanceIdentifier implements Serializable {
	private static final long serialVersionUID = 846401182348807016L;
	@Tag(1)
	protected String externalId;

	/**
	 * Default empty constructor.
	 */
	protected ExternalInstanceIdentifier() {
		// needed for kryo
	}

	/**
	 * Instantiates a new external instance identifier.
	 *
	 * @param externalId
	 *            - some uid of instance
	 */
	public ExternalInstanceIdentifier(String externalId) {
		this.externalId = externalId;
	}

	/**
	 * Get the id as string - some uid.
	 *
	 * @return the uid
	 */
	public String getExternalId() {
		return externalId;
	}

	/**
	 * Sets the external id as string value.
	 *
	 * @param externalId
	 *            the external id to set
	 */
	public void setExternalId(String externalId) {
		this.externalId = externalId;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((externalId == null) ? 0 : externalId.toLowerCase().hashCode());
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
		if (!(obj instanceof ExternalInstanceIdentifier)) {
			return false;
		}
		return EqualsHelper.nullSafeEquals(externalId, ((ExternalInstanceIdentifier) obj).externalId, true);
	}

	@Override
	public String toString() {
		return externalId;
	}

}
