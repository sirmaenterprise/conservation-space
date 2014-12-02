package com.sirma.itt.objects.converter;

import java.io.Serializable;
import java.util.Date;
import java.util.Map;
import java.util.UUID;

import javax.inject.Inject;

import com.sirma.itt.cmf.beans.definitions.DocumentDefinitionTemplate;
import com.sirma.itt.cmf.beans.definitions.impl.DocumentDefinitionRefProxy;
import com.sirma.itt.cmf.beans.model.DocumentInstance;
import com.sirma.itt.cmf.constants.CmfConfigurationProperties;
import com.sirma.itt.cmf.services.DocumentService;
import com.sirma.itt.cmf.util.datatype.AbstractRestInstanceToInstanceConverterProvider;
import com.sirma.itt.commons.utils.string.StringUtils;
import com.sirma.itt.emf.configuration.Config;
import com.sirma.itt.emf.converter.Converter;
import com.sirma.itt.emf.converter.TypeConverter;
import com.sirma.itt.emf.definition.DictionaryService;
import com.sirma.itt.emf.domain.model.DefinitionModel;
import com.sirma.itt.emf.instance.model.Instance;
import com.sirma.itt.emf.properties.DefaultProperties;
import com.sirma.itt.emf.properties.PropertiesService;
import com.sirma.itt.emf.rest.model.RestInstance;
import com.sirma.itt.objects.constants.ObjectProperties;
import com.sirma.itt.objects.domain.model.ObjectInstance;

/**
 * Specific provider for {@link ObjectInstance} to {@link RestInstance} converter.
 *
 * @author BBonev
 */
public class RestInstanceToInstanceConverterProvider extends
		AbstractRestInstanceToInstanceConverterProvider {

	/** The dictionary service. */
	@Inject
	private DictionaryService dictionaryService;

	/** The properties service. */
	@Inject
	private PropertiesService propertiesService;

	@Inject
	private DocumentService documentService;

	@Inject
	@Config(name = CmfConfigurationProperties.CODELIST_DOCUMENT_DEFAULT_ATTACHMENT_TYPE, defaultValue = "OT210027")
	private String genericDocumentType;


		/**
	 * Converter from {@link RestInstance} to {@link ObjectInstance}.
	 *
	 * @author BBonev
	 */
	public class RestInstanceToObjectInstanceConverter implements
			Converter<RestInstance, ObjectInstance> {

		/**
		 * {@inheritDoc}
		 */
		@Override
		public ObjectInstance convert(RestInstance source) {
			Instance instance = getOrCreateInstance(source);
			if (!(instance instanceof ObjectInstance)) {
				return null;
			}
			DefinitionModel definitionModel = dictionaryService.getInstanceDefinition(instance);
			Map<String, Serializable> fromRest = propertiesService.convertToInternalModel(source.getProperties(), definitionModel);
			instance.getProperties().putAll(fromRest);
			Date lastModified = new Date();
			if (source.isOverwrite()) {
				instance.getProperties().put(DefaultProperties.MODIFIED_ON, lastModified);
			}

			// first try to load the view if any
			DocumentInstance view = null;
			if (source.getViewId() != null) {
				view = documentService.loadByDbId(source.getViewId());
			} else {
				String dmsId = (String) instance.getProperties().get(
						ObjectProperties.DEFAULT_VIEW_LOCATION);
				if (dmsId != null) {
					view = documentService.load(dmsId);
				}
			}

			// if we have a view loaded we will update it with the incoming data
			if (view != null) {
				// ensure the view id is set
				source.setViewId(view.getId().toString());
				// get the definition for the view
				DefinitionModel instanceDefinition = dictionaryService.getInstanceDefinition(view);

				// convert the view properties and set them the the view instance
				Map<String, Serializable> viewProperties = propertiesService.convertToInternalModel(
						source.getViewProperties(), instanceDefinition);

				view.getProperties().putAll(viewProperties);
				// update content if any
				setDocumentContent(view.getProperties(), source.getContent());
				instance.getProperties().put(ObjectProperties.DEFAULT_VIEW, view);
				if (source.isOverwrite()) {
					view.getProperties().put(DefaultProperties.MODIFIED_ON, lastModified);
				}
			} else {
				// if view is not found we will try to create one new
				String viewDefinitionId = (String) instance.getProperties().get(
						ObjectProperties.OBJECT_VIEW_DEFINITION);
				if (StringUtils.isNullOrEmpty(viewDefinitionId)) {
					viewDefinitionId = genericDocumentType;
				}
				DocumentDefinitionTemplate template = dictionaryService.getDefinition(
						DocumentDefinitionTemplate.class, viewDefinitionId);
				view = documentService.createInstance(new DocumentDefinitionRefProxy(template),
						instance);
				view.setDocumentRefId(viewDefinitionId);
				// view.setStandalone(true);
				Map<String, Serializable> viewProperties = propertiesService.convertToInternalModel(
						source.getViewProperties(), template);
				view.getProperties().putAll(viewProperties);

				if (view.getProperties().get(DefaultProperties.NAME) == null) {
					view.getProperties().put(DefaultProperties.NAME,
							UUID.randomUUID().toString() + ".xml");
				}

				setDocumentContent(view.getProperties(), source.getContent());
				// convert the view properties and set them the the view instance
				// added creation of document instance and populate his properties.
				// then add it to the instance
				instance.getProperties().put(ObjectProperties.DEFAULT_VIEW, view);
			}
			return (ObjectInstance) instance;
		}
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void register(TypeConverter converter) {
		converter.addConverter(RestInstance.class, ObjectInstance.class,
				new RestInstanceToObjectInstanceConverter());
	}
}
