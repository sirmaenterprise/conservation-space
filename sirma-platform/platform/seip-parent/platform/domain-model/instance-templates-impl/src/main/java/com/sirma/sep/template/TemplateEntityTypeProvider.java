package com.sirma.sep.template;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import com.sirma.itt.seip.instance.properties.EntityType;
import com.sirma.itt.seip.instance.properties.EntityTypeProviderExtension;
import com.sirma.itt.seip.plugin.Extension;
import com.sirma.itt.seip.template.Template;
import com.sirma.itt.seip.template.db.TemplateEntity;

/**
 * Entity type provider extension for templates.
 *
 * @author BBonev
 */
@SuppressWarnings("rawtypes")
@Extension(target = EntityTypeProviderExtension.TARGET_NAME, order = 20)
public class TemplateEntityTypeProvider implements EntityTypeProviderExtension {

	private static final List<Class> ALLOWED_CLASSES = new ArrayList<>(
			Arrays.asList(Template.class, TemplateEntity.class));

	@Override
	public List<Class> getSupportedObjects() {
		return ALLOWED_CLASSES;
	}

	@Override
	public EntityType getEntityType(Object object) {
		return EntityIdType.getType(object);
	}

	@Override
	public EntityType getEntityType(Class<?> object) {
		if (object.equals(Template.class) || object.equals(TemplateEntity.class)) {
			return EntityIdType.TEMPLATE_INSTANCE;
		}

		return EntityIdType.UNKNOWN;
	}

	private enum EntityIdType implements EntityType {

		UNKNOWN(0), TEMPLATE_INSTANCE(14);

		private int id;

		private EntityIdType(int id) {
			this.id = id;
		}

		/**
		 * Gets the {@link EntityIdType} by the given object instance. If the object instance is not recognized then
		 * {@link #UNKNOWN} type will be returned.
		 * <p>
		 * NOTE: this is implementation specific method!
		 *
		 * @param object
		 *            the object
		 * @return the type
		 */
		static EntityIdType getType(Object object) {
			if (object instanceof Template || object instanceof TemplateEntity) {
				return TEMPLATE_INSTANCE;
			}

			return UNKNOWN;
		}

		@Override
		public int getTypeId() {
			return id;
		}

		@Override
		public String getName() {
			return toString();
		}
	}
}