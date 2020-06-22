package com.sirma.itt.seip.instance.convert;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import org.apache.commons.lang3.StringUtils;
import com.sirma.itt.seip.convert.Converter;
import com.sirma.itt.seip.convert.TypeConverter;
import com.sirma.itt.seip.convert.TypeConverterProvider;
import com.sirma.itt.seip.domain.instance.InitializedInstance;
import com.sirma.itt.seip.domain.instance.Instance;
import com.sirma.itt.seip.domain.instance.InstanceReference;
import com.sirma.itt.seip.domain.instance.InstanceReferenceImpl;
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
	 * Converts the {@link InstanceReference} specific implementation to instance then to initialized instance it before
	 * return.
	 *
	 * @param <T>
	 *            the specific subtype of {@link InstanceReference}
	 * @author BBonev
	 */
	public class InstanceRefrecenceToInitializedInstanceConverter<T extends InstanceReference>
			implements Converter<T, InitializedInstance> {

		@Override
		public InitializedInstance convert(T source) {
			String id = source.getId();
			if (StringUtils.isBlank(id)) {
				throw new EmfConfigurationException("Reference id is required to load instance");
			}
			Instance loadedInstance = instanceService.loadByDbId(id);
			copyFromReferenceToInstance(source, loadedInstance);
			return new InitializedInstance(loadedInstance);
		}

		private void copyFromReferenceToInstance(InstanceReference source, Instance target) {
			if (target == null || source == null) {
				return;
			}
			// TODO do a setReference method for each instance
			// copy the type of the instance
			if (target.type() == null) {
				target.setType(source.getType());
			}
		}
	}

	@Override
	public void register(TypeConverter converterParam) {
		converterParam.addConverter(LinkSourceId.class, InitializedInstance.class,
				new InstanceRefrecenceToInitializedInstanceConverter<LinkSourceId>());
		converterParam.addConverter(InstanceReferenceImpl.class, InitializedInstance.class,
				new InstanceRefrecenceToInitializedInstanceConverter<InstanceReferenceImpl>());

	}

}
