package com.sirma.itt.imports.converters;

import java.io.Serializable;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Map.Entry;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import com.sirma.itt.cmf.beans.model.DocumentInstance;
import com.sirma.itt.cmf.constants.DocumentProperties;
import com.sirma.itt.emf.annotation.Proxy;
import com.sirma.itt.emf.configuration.Config;
import com.sirma.itt.emf.configuration.EmfConfigurationProperties;
import com.sirma.itt.emf.converter.Converter;
import com.sirma.itt.emf.converter.TypeConverter;
import com.sirma.itt.emf.converter.TypeConverterProvider;
import com.sirma.itt.emf.db.SequenceEntityGenerator;
import com.sirma.itt.emf.definition.DictionaryService;
import com.sirma.itt.emf.definition.model.PropertyDefinition;
import com.sirma.itt.emf.domain.model.DefinitionModel;
import com.sirma.itt.emf.domain.model.Node;
import com.sirma.itt.emf.domain.model.PathElement;
import com.sirma.itt.emf.event.EventService;
import com.sirma.itt.emf.event.instance.InstanceEventProvider;
import com.sirma.itt.emf.instance.PropertiesUtil;
import com.sirma.itt.emf.instance.actions.ActionTypeConstants;
import com.sirma.itt.emf.instance.dao.InstanceService;
import com.sirma.itt.emf.instance.dao.InstanceServiceProvider;
import com.sirma.itt.emf.instance.dao.ServiceRegistry;
import com.sirma.itt.emf.instance.model.CommonInstance;
import com.sirma.itt.emf.instance.model.EmfInstance;
import com.sirma.itt.emf.instance.model.Instance;
import com.sirma.itt.emf.link.LinkReference;
import com.sirma.itt.emf.state.StateService;
import com.sirma.itt.emf.state.operation.Operation;
import com.sirma.itt.emf.util.PathHelper;
import com.sirma.itt.imports.configuration.DocumentImportConfiguration;
import com.sirma.itt.objects.domain.definitions.ObjectDefinition;
import com.sirma.itt.objects.domain.model.ObjectInstance;

/**
 * Type converter provider for default conversions and filling default data for imported instances.
 *
 * @author BBonev
 */
@ApplicationScoped
public class EmfInstanceConverterProvider implements TypeConverterProvider {

	/** The dictionary service. */
	@Inject
	private DictionaryService dictionaryService;

	/** The default container. */
	@Inject
	@Config(name = EmfConfigurationProperties.DEFAULT_CONTAINER)
	private String defaultContainer;

	/** The default document type. */
	@Inject
	@Config(name = DocumentImportConfiguration.DEFAULT_DOCUMENT_DEFINITION, defaultValue = "commonDocument")
	private String defaultDocumentType;

	/** The default object type. */
	@Inject
	@Config(name = DocumentImportConfiguration.DEFAULT_OBJECT_DEFINITION, defaultValue = "GEO10001")
	private String defaultObjectType;

	/** The state service. */
	@Inject
	private StateService stateService;

	@Inject
	@Proxy
	private InstanceServiceProvider serviceProvider;

	@Inject
	private ServiceRegistry register;

	@Inject
	private EventService eventService;

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void register(TypeConverter converter) {
		converter.addConverter(EmfInstance.class, DocumentInstance.class,
				new EmfInstanceToDocumentConverter());
		converter.addConverter(EmfInstance.class, ObjectInstance.class,
				new EmfInstanceToObjectConverter());
		converter.addConverter(EmfInstance.class, CommonInstance.class,
				new EmfInstanceToCommonConverter());
		converter.addConverter(EmfInstance.class, LinkReference.class,
				new EmfInstanceToLinkRefConverter());
	}

	/**
	 * Sets the dms id.
	 *
	 * @param source
	 *            the source
	 * @param target
	 *            the target
	 */
	private void setDmsId(EmfInstance source, EmfInstance target) {
		target.setDmsId(source.getDmsId());
		if (target.getDmsId() == null) {
			target.setDmsId((String) target.getProperties().remove("emf:dmsId"));
		}
	}

	/**
	 * Sets the container.
	 *
	 * @param source
	 *            the source
	 * @param target
	 *            the target
	 */
	private void setContainer(EmfInstance source, EmfInstance target) {
		target.setContainer(source.getContainer());
		if (target.getContainer() == null) {
			target.setContainer(defaultContainer);
		}
	}

	/**
	 * Sets the definition id from the source, if not present tries to set from properties or uses
	 * the defaultValue at last.
	 *
	 * @param source
	 *            the source
	 * @param target
	 *            the target
	 * @param defaultValue
	 *            the default value
	 */
	private void setDefinitionId(EmfInstance source, Instance target, String defaultValue) {
		target.setIdentifier(source.getIdentifier());
		if (target.getIdentifier() == null) {
			Serializable type = target.getProperties().remove("emf:type");
			if (type == null) {
				type = defaultValue;
			}
			target.setIdentifier((String) type);
		}
	}

	/**
	 * Sets the db id.
	 *
	 * @param source
	 *            the source
	 * @param target
	 *            the target
	 */
	private void setDbId(EmfInstance source, Instance target) {
		target.setId(source.getId());
		if (target.getId() == null) {
			SequenceEntityGenerator.generateStringId(target, true);
			// update the source to have the same id as the result object
			source.setId(target.getId());
		}
	}

	/**
	 * Converter to {@link DocumentInstance}
	 *
	 * @author BBonev
	 */
	public class EmfInstanceToDocumentConverter implements Converter<EmfInstance, DocumentInstance> {

		/**
		 * {@inheritDoc}
		 */
		@Override
		public DocumentInstance convert(EmfInstance source) {
			DocumentInstance instance = new DocumentInstance();
			instance.setProperties(PropertiesUtil.cloneProperties(source.getProperties()));
			setDmsId(source, instance);
			setDbId(source, instance);

			if (instance.getDmsId() != null) {
				InstanceService<DocumentInstance, DefinitionModel> instanceService = serviceProvider
						.getService(DocumentInstance.class);
				DocumentInstance oldInstance = instanceService.load(instance.getDmsId());
				if (oldInstance != null) {
					// copy all new properties to the old instance
					PropertiesUtil.mergeProperties(instance.getProperties(),
							oldInstance.getProperties(), false);
					instance = oldInstance;
				}
			}

			setContainer(source, instance);

			if (instance.getDmsId() != null) {
				// set the location to the file if any
				instance.getProperties().put(DocumentProperties.ATTACHMENT_LOCATION,
						instance.getDmsId());
			}

			setDefinitionId(source, instance, defaultDocumentType);
			DefinitionModel definition = dictionaryService.getInstanceDefinition(instance);
			// if this is a proxy it's very likely not to be null
			if ((definition == null) || (definition.getIdentifier() == null)) {
				instance.setIdentifier(defaultDocumentType);
				instance.setRevision(0L);
			} else {
				instance.setRevision(definition.getRevision());
			}
			updateCommonInstanceProperties(instance);

			return instance;
		}
	}

	/**
	 * Converter to {@link ObjectInstance}
	 *
	 * @author BBonev
	 */
	public class EmfInstanceToObjectConverter implements Converter<EmfInstance, ObjectInstance> {

		/**
		 * {@inheritDoc}
		 */
		@Override
		public ObjectInstance convert(EmfInstance source) {
			ObjectInstance instance = new ObjectInstance();
			instance.setProperties(PropertiesUtil.cloneProperties(source.getProperties()));
			setDbId(source, instance);
			setContainer(source, instance);
			setDmsId(source, instance);

			setDefinitionId(source, instance, defaultObjectType);
			ObjectDefinition definition = dictionaryService.getDefinition(ObjectDefinition.class,
					instance.getIdentifier());
			if (definition != null) {
				instance.setRevision(definition.getRevision());
			} else {
				instance.setIdentifier(defaultObjectType);
				definition = dictionaryService.getDefinition(ObjectDefinition.class,
						instance.getIdentifier());
				if (definition != null) {
					instance.setRevision(definition.getRevision());
				}
			}

			updateCommonInstanceProperties(instance);

			stateService.changeState(instance, new Operation(ActionTypeConstants.CREATE_OBJECT));
			stateService.changeState(instance, new Operation(ActionTypeConstants.CREATE_OBJECT));
			return instance;
		}
	}

	/**
	 * Converter for {@link EmfInstance} to {@link CommonInstance}
	 *
	 * @author BBonev
	 */
	public class EmfInstanceToCommonConverter implements Converter<EmfInstance, CommonInstance> {

		/**
		 * {@inheritDoc}
		 */
		@Override
		public CommonInstance convert(EmfInstance source) {
			CommonInstance instance = new CommonInstance();
			instance.setProperties(PropertiesUtil.cloneProperties(source.getProperties()));
			setDbId(source, instance);
			setDefinitionId(source, instance, "ComplexValue");
			instance.setRevision(0L);
			return instance;
		}

	}

	/**
	 * Converter from {@link EmfInstance} to {@link LinkReference}
	 *
	 * @author BBonev
	 */
	public class EmfInstanceToLinkRefConverter implements Converter<EmfInstance, LinkReference> {

		/**
		 * {@inheritDoc}
		 */
		@Override
		public LinkReference convert(EmfInstance source) {
			LinkReference reference = new LinkReference();
			setDbId(source, reference);
			reference.setProperties(PropertiesUtil.cloneProperties(source.getProperties()));
			reference.setIdentifier((String) reference.getProperties().get("emf:relationType"));
			return reference;
		}

	}

	/**
	 * Update common instance properties.
	 * 
	 * @param instance
	 *            the instance
	 */
	protected void updateCommonInstanceProperties(Instance instance) {
		Map<String, Serializable> updated = new LinkedHashMap<>();
		for (Entry<String, Serializable> entry : instance.getProperties().entrySet()) {
			if (entry.getValue() instanceof CommonInstance) {
				CommonInstance value = (CommonInstance) entry.getValue();
				String key = entry.getKey();
				int indexOf = key.indexOf(":");
				if (indexOf > 0) {
					key = key.substring(indexOf + 1);
				}
				if (value.getPath() == null) {
					value.setPath(PathHelper.getPath(instance) + PathElement.PATH_SEPARATOR + key);
					value.setRevision(instance.getRevision());
				}
				DefinitionModel definition = dictionaryService.getInstanceDefinition(instance);
				Node child = definition.getChild(key);
				if ((child instanceof PropertyDefinition)
						&& (((PropertyDefinition) child).getControlDefinition() != null)) {
					for (PropertyDefinition field : (((PropertyDefinition) child)
							.getControlDefinition()).getFields()) {

						Serializable serializable = value.getProperties().remove(field.getUri());
						if (serializable != null) {
							value.getProperties().put(field.getIdentifier(), serializable);
						}
					}
				}
				InstanceEventProvider<Instance> provider = register.getEventProvider(value);
				if (provider != null) {
					eventService.fire(provider.createChangeEvent(value));
				}

				updated.put(entry.getKey(), value);
			} else if (entry.getValue() instanceof Instance) {
				updateCommonInstanceProperties((Instance) entry.getValue());
			}
		}
		instance.getProperties().putAll(updated);
	}

}
