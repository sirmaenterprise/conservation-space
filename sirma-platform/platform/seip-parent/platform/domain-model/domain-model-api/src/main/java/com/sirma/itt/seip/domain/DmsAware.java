package com.sirma.itt.seip.domain;

import static com.sirma.itt.seip.util.EqualsHelper.nullSafeEquals;

import java.io.Serializable;

/**
 * The Interface DmsAware marks an entity that has a DMS representation. The method {@link #getDmsId()} should return
 * the id in DMS. The interface does not limit the type of the object/entity supported.
 *
 * @author BBonev
 */
public interface DmsAware {
	/**
	 * Gets the DMS id.
	 *
	 * @return the DMS id
	 */
	String getDmsId();

	/**
	 * Sets the DMS id.
	 *
	 * @param dmsId
	 *            the new DMS id
	 */
	void setDmsId(String dmsId);

	/**
	 * Sets the dms id to the given target object only if the given object implements the {@link DmsAware} interface.
	 *
	 * @param target
	 *            the target object to set the id to
	 * @param newId
	 *            the new id to set
	 */
	static void setDmsId(Object target, String newId) {
		if (target instanceof DmsAware) {
			((DmsAware) target).setDmsId(newId);
		}
	}

	/**
	 * Gets the dms id from the given target object only if the given object implements the {@link DmsAware} interface.
	 *
	 * @param target            the target object to set the id to
	 * @param defaultValue            the default value to return if not a dms aware instance.
	 * @return the dms id or the default value if the given target is not of type {@link DmsAware}
	 */
	static String getDmsId(Object target, String defaultValue) {
		if (target instanceof DmsAware) {
			return ((DmsAware) target).getDmsId();
		}
		return defaultValue;
	}

	/**
	 * Creates a {@link DmsAware} instance that can carry only it's dms id.
	 *
	 * @return the dms aware instance
	 */
	static DmsAware create() {
		return new SimpleDmsAware();
	}

	/**
	 * Creates a {@link DmsAware} instance for the given dms id.
	 *
	 * @param dmsId
	 *            the dms id
	 * @return the dms aware instance
	 */
	static DmsAware create(String dmsId) {
		return new SimpleDmsAware(dmsId);
	}

	/**
	 * Simple implementation of the interface {@link DmsAware} for use where more complex objects are not needed.
	 *
	 * @author BBonev
	 */
	class SimpleDmsAware implements DmsAware, Serializable {

		private static final long serialVersionUID = -8124680092369939896L;
		private String dmsId;

		/**
		 * Instantiates a new simple dms aware.
		 */
		public SimpleDmsAware() {
			this(null);
		}

		/**
		 * Instantiates a new simple dms aware.
		 *
		 * @param dmsId
		 *            the dms id
		 */
		public SimpleDmsAware(String dmsId) {
			this.dmsId = dmsId;
		}

		/**
		 * Gets the dms id.
		 *
		 * @return the dms id
		 */
		@Override
		public String getDmsId() {
			return dmsId;
		}

		/**
		 * Sets the dms id.
		 *
		 * @param dmsId
		 *            the new dms id
		 */
		@Override
		public void setDmsId(String dmsId) {
			this.dmsId = dmsId;
		}

		@Override
		public int hashCode() {
			final int prime = 31;
			int result = 1;
			result = prime * result + (dmsId == null ? 0 : dmsId.hashCode());
			return result;
		}

		@Override
		public boolean equals(Object obj) {
			if (this == obj) {
				return true;
			}
			if (!(obj instanceof DmsAware)) {
				return false;
			}
			DmsAware other = (DmsAware) obj;
			return nullSafeEquals(dmsId, other.getDmsId());
		}

		@Override
		public String toString() {
			return new StringBuilder(100).append("SimpleDmsAware [dmsId=").append(dmsId).append("]").toString();
		}
	}
}
