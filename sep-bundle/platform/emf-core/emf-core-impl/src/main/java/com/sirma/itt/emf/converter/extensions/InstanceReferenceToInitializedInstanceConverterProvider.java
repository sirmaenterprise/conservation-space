package com.sirma.itt.emf.converter.extensions;

import java.io.Serializable;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import com.sirma.itt.commons.utils.string.StringUtils;
import com.sirma.itt.emf.annotation.Proxy;
import com.sirma.itt.emf.converter.Converter;
import com.sirma.itt.emf.converter.TypeConverter;
import com.sirma.itt.emf.converter.TypeConverterProvider;
import com.sirma.itt.emf.definition.model.DataTypeDefinition;
import com.sirma.itt.emf.domain.model.DefinitionModel;
import com.sirma.itt.emf.entity.LinkSourceId;
import com.sirma.itt.emf.exceptions.EmfConfigurationException;
import com.sirma.itt.emf.instance.InstanceServiceProxy;
import com.sirma.itt.emf.instance.dao.InstanceDao;
import com.sirma.itt.emf.instance.dao.InstanceService;
import com.sirma.itt.emf.instance.dao.ServiceRegister;
import com.sirma.itt.emf.instance.model.InitializedInstance;
import com.sirma.itt.emf.instance.model.Instance;

/**
 * Type converter that converts from {@link LinkSourceId} objects (.
 * {@link com.sirma.itt.emf.instance.model.InstanceReference} implementation) directly to
 * initialized instance objects
 * 
 * @author BBonev
 */
@ApplicationScoped
public class InstanceReferenceToInitializedInstanceConverterProvider implements
		TypeConverterProvider {

	/** The instance service. */
	@Inject
	@Proxy
	protected InstanceServiceProxy instanceService;

	/** The service register. */
	@Inject
	protected ServiceRegister serviceRegister;

	/** The converter. */
	private TypeConverter converter;

	/**
	 * Converts the link source to instance then then initialize it before return.
	 * 
	 * @author BBonev
	 */
	public class InstanceToInitializedInstanceConverter implements
			Converter<LinkSourceId, InitializedInstance> {

		private static final String CANNOT_CONVERT = "Cannot convert ";

		/**
		 * {@inheritDoc}
		 */
		@Override
		@SuppressWarnings("unchecked")
		public InitializedInstance convert(LinkSourceId source) {
			DataTypeDefinition type = source.getSourceType();
			if (type == null) {
				throw new EmfConfigurationException(CANNOT_CONVERT
						+ LinkSourceId.class.getCanonicalName() + " to unknown type!");
			}
			String id = source.getSourceId();
			if (StringUtils.isNullOrEmpty(id)) {
				throw new EmfConfigurationException(CANNOT_CONVERT
						+ LinkSourceId.class.getCanonicalName() + " to non persisted "
						+ type.getJavaClassName() + " type!");
			}
			Class<Instance> clazz = converter.convert(Class.class, type.getJavaClassName());
			if (clazz != null) {
				InstanceService<Instance, DefinitionModel> service = instanceService
						.getService(clazz);
				if (service != null) {
					return new InitializedInstance(loadInstance(id, clazz, service));
				}
			}
			throw new EmfConfigurationException("Convertion of "
					+ LinkSourceId.class.getCanonicalName() + " to " + type.getJavaClassName()
					+ " is not supported!");
		}

		/**
		 * Load instance.
		 * 
		 * @param id
		 *            the id
		 * @param clazz
		 *            the clazz
		 * @param service
		 *            the service
		 * @return the instance
		 */
		private Instance loadInstance(String id, Class<Instance> clazz,
				InstanceService<Instance, DefinitionModel> service) {
			Instance instance;
			try {
				InstanceDao<Instance> dao = serviceRegister.getInstanceDao(clazz);
				Serializable primaryId;
				if (dao != null) {
					// NOTE: if the primary type is compatible with the value then
					Class<Serializable> primaryIdType = dao.getPrimaryIdType();
					primaryId = converter.convert(primaryIdType, id);
				} else {
					primaryId = Long.parseLong(id);
				}
				instance = service.loadByDbId(primaryId);
			} catch (NumberFormatException e) {
				instance = service.load(id);
			}
			return instance;
		}
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void register(TypeConverter converterParam) {
		this.converter = converterParam;
		converterParam.addConverter(LinkSourceId.class, InitializedInstance.class,
				getConverterImplementation());
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
