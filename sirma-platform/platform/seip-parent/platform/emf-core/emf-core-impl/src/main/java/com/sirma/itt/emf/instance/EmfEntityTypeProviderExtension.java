package com.sirma.itt.emf.instance;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import com.sirma.itt.emf.entity.EmfEntityIdType;
import com.sirma.itt.emf.link.entity.LinkEntity;
import com.sirma.itt.seip.domain.instance.ArchivedInstance;
import com.sirma.itt.seip.instance.CommonInstance;
import com.sirma.itt.seip.instance.archive.ArchivedEntity;
import com.sirma.itt.seip.instance.properties.EntityType;
import com.sirma.itt.seip.instance.properties.EntityTypeProviderExtension;
import com.sirma.itt.seip.instance.relation.LinkInstance;
import com.sirma.itt.seip.instance.relation.LinkReference;
import com.sirma.itt.seip.plugin.Extension;

/**
 * Default Emf extension for entity type provider.
 *
 * @author BBonev
 */
@Extension(target = EntityTypeProviderExtension.TARGET_NAME, order = 10)
public class EmfEntityTypeProviderExtension implements EntityTypeProviderExtension {

	/** The Constant ALLOWED_CLASSES. */
	private static final List<Class> ALLOWED_CLASSES = new ArrayList<>(Arrays.asList(CommonInstance.class,
			LinkEntity.class, LinkInstance.class, LinkReference.class, ArchivedEntity.class, ArchivedInstance.class));

	/**
	 * {@inheritDoc}
	 */
	@Override
	public List<Class> getSupportedObjects() {
		return ALLOWED_CLASSES;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public EntityType getEntityType(Object object) {
		return EmfEntityIdType.getType(object);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public EntityType getEntityType(Class<?> object) {
		if (object.equals(CommonInstance.class)) {
			return EmfEntityIdType.INSTANCE;
		} else if (object.equals(LinkEntity.class) || object.equals(LinkReference.class)
				|| object.equals(LinkInstance.class)) {
			return EmfEntityIdType.LINK_INSTANCE;
		}
		return EmfEntityIdType.UNKNOWN;
	}

}
