package com.sirma.itt.cmf.instance;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import com.sirma.itt.cmf.beans.entity.EntityIdType;
import com.sirma.itt.emf.entity.EmfEntityIdType;
import com.sirma.itt.seip.instance.properties.EntityType;
import com.sirma.itt.seip.instance.properties.EntityTypeProviderExtension;
import com.sirma.itt.seip.plugin.Extension;
import com.sirma.itt.seip.template.TemplateEntity;
import com.sirma.itt.seip.template.TemplateInstance;

/**
 * Default Cmf extension for entity type provider.
 *
 * @author BBonev
 */
@Extension(target = EntityTypeProviderExtension.TARGET_NAME, order = 20)
@SuppressWarnings("rawtypes")
public class CmfEntityTypeProviderExtension implements EntityTypeProviderExtension {

	private static final List<Class> ALLOWED_CLASSES = new ArrayList<>(
			Arrays.asList(TemplateInstance.class, TemplateEntity.class));

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
		if (object.equals(TemplateInstance.class) || object.equals(TemplateEntity.class)) {
			return EntityIdType.TEMPLATE_INSTANCE;
		}
		return EmfEntityIdType.UNKNOWN;
	}
}