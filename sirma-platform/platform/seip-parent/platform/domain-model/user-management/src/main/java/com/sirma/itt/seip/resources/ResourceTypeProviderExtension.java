package com.sirma.itt.seip.resources;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import com.sirma.itt.seip.instance.CommonInstance;
import com.sirma.itt.seip.instance.properties.EntityType;
import com.sirma.itt.seip.instance.properties.EntityTypeProviderExtension;
import com.sirma.itt.seip.plugin.Extension;

/**
 * Default Emf extension for entity type provider.
 *
 * @author BBonev
 */
@Extension(target = EntityTypeProviderExtension.TARGET_NAME, order = 11)
public class ResourceTypeProviderExtension implements EntityTypeProviderExtension {

	/** The Constant ALLOWED_CLASSES. */
	private static final List<Class> ALLOWED_CLASSES = new ArrayList<>(Arrays.asList(EmfUser.class, EmfGroup.class));

	@Override
	public List<Class> getSupportedObjects() {
		return ALLOWED_CLASSES;
	}

	@Override
	public EntityType getEntityType(Object object) {
		return ResourceEntityIdType.getType(object);
	}

	@Override
	public EntityType getEntityType(Class<?> object) {
		if (object.equals(EmfUser.class)) {
			return ResourceEntityIdType.USER;
		} else if (object.equals(EmfGroup.class)) {
			return ResourceEntityIdType.GROUP;
		}
		return ResourceEntityIdType.UNKNOWN;
	}

	/**
	 * Entity types for resources
	 *
	 * @author BBonev
	 */
	public enum ResourceEntityIdType implements EntityType {

		/** The unknown. */
		UNKNOWN(0), /** The user. */
		USER(6), /** The {@link CommonInstance} representation type. */
		GROUP(12); /** The archived instance. */

		/** The id. */
		private int id;

		/**
		 * Instantiates a new entity id type.
		 *
		 * @param id
		 *            the id
		 */
		private ResourceEntityIdType(int id) {
			this.id = id;
		}

		/**
		 * Gets the type.
		 *
		 * @return the type
		 */
		public int getType() {
			return id;
		}

		/**
		 * Gets the {@link ResourceEntityIdType} by the given object instance. If the object instance is not recognized then
		 * {@link #UNKNOWN} type will be returned.
		 * <p>
		 * NOTE: this is implementation specific method!
		 *
		 * @param object
		 *            the object
		 * @return the type
		 */
		public static ResourceEntityIdType getType(Object object) {
			if (object instanceof EmfUser) {
				return USER;
			} else if (object instanceof EmfGroup) {
				return GROUP;
			} else {
				return UNKNOWN;
			}
		}

		@Override
		public int getTypeId() {
			return getType();
		}

		@Override
		public String getName() {
			return toString();
		}
	}

}
