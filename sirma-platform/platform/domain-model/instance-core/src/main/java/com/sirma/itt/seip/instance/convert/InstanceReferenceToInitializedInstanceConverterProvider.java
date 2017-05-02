package com.sirma.itt.seip.instance.convert;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import com.sirma.itt.commons.utils.string.StringUtils;
import com.sirma.itt.seip.convert.Converter;
import com.sirma.itt.seip.convert.TypeConverter;
import com.sirma.itt.seip.convert.TypeConverterProvider;
import com.sirma.itt.seip.domain.instance.InitializedInstance;
import com.sirma.itt.seip.domain.instance.Instance;
import com.sirma.itt.seip.domain.instance.InstanceReference;
import com.sirma.itt.seip.domain.instance.OwnedModel;
import com.sirma.itt.seip.exception.EmfConfigurationException;
import com.sirma.itt.seip.instance.dao.InstanceService;
import com.sirma.itt.seip.model.LinkSourceId;

/**
 * Type converter that converts from {@link LinkSourceId} objects (.
 * {@link com.sirma.itt.seip.domain.instance.InstanceReference} implementation) directly to initialized instance objects
 *
 * @author BBonev
 */
@ApplicationScoped
public class InstanceReferenceToInitializedInstanceConverterProvider implements TypeConverterProvider {
	@Inject
	protected InstanceService instanceService;

	/**
	 * Converts the link source to instance then then initialize it before return.
	 *
	 * @author BBonev
	 */
	public class InstanceToInitializedInstanceConverter implements Converter<LinkSourceId, InitializedInstance> {

		@Override
		public InitializedInstance convert(LinkSourceId source) {
			String id = source.getIdentifier();
			if (StringUtils.isNullOrEmpty(id)) {
				throw new EmfConfigurationException("Reference id is required to load instance");
			}
			Instance loadedInstance = instanceService.loadByDbId(id);
			copyFromReferenceToInstance(source, loadedInstance);
			return new InitializedInstance(loadedInstance);
		}

		private void copyFromReferenceToInstance(LinkSourceId source, Instance instance) {
			// copy the type of the instance
			if (instance != null && instance.type() == null) {
				instance.setType(source.getType());
			}
			// copy the parent if known
			if (instance instanceof OwnedModel && !InstanceReference.ROOT_REFERENCE.equals(source.getParent())) {
				((OwnedModel) instance).setOwningReference(source.getParent());
			}
		}
	}

	@Override
	public void register(TypeConverter converterParam) {
		converterParam.addConverter(LinkSourceId.class, InitializedInstance.class, getConverterImplementation());
	}

	/**
	 * Gets the converter implementation.
	 *
	 * @return the converter implementation
	 */
	private Converter<LinkSourceId, InitializedInstance> getConverterImplementation() {
		return new InstanceToInitializedInstanceConverter();
	}

}
