package com.sirma.itt.objects.converter;

import java.util.Map;

import javax.inject.Inject;

import com.sirma.itt.cmf.beans.model.DocumentInstance;
import com.sirma.itt.cmf.constants.NonPersistentProperties;
import com.sirma.itt.cmf.services.DocumentService;
import com.sirma.itt.cmf.services.DocumentTemplateService;
import com.sirma.itt.cmf.util.datatype.AbstractInstanceToRestInstanceConverterProvider;
import com.sirma.itt.commons.utils.string.StringUtils;
import com.sirma.itt.emf.configuration.Config;
import com.sirma.itt.emf.converter.Converter;
import com.sirma.itt.emf.converter.TypeConverter;
import com.sirma.itt.emf.definition.DictionaryService;
import com.sirma.itt.emf.definition.model.DataTypeDefinition;
import com.sirma.itt.emf.domain.model.DefinitionModel;
import com.sirma.itt.emf.instance.model.InstanceReference;
import com.sirma.itt.emf.properties.PropertiesService;
import com.sirma.itt.emf.rest.model.RestInstance;
import com.sirma.itt.emf.rest.model.ViewInstance;
import com.sirma.itt.emf.template.TemplateInstance;
import com.sirma.itt.objects.constants.ObjectProperties;
import com.sirma.itt.objects.constants.ObjectsConfigProperties;
import com.sirma.itt.objects.domain.model.ObjectInstance;

/**
 * Specific provider for {@link ObjectInstance} to {@link RestInstance} converter.
 *
 * @author BBonev
 */
public class InstanceToRestInstanceConverterProvider extends
		AbstractInstanceToRestInstanceConverterProvider {

	/** The dictionary service. */
	@Inject
	private DictionaryService dictionaryService;

	/** The properties service. */
	@Inject
	private PropertiesService propertiesService;

	@Inject
	private DocumentTemplateService templateService;

	@Inject
	private DocumentService documentService;

	@Inject
	@Config(name = ObjectsConfigProperties.DEFAULT_OBJECT_TEMPLATE, defaultValue = "GEO10001")
	private String defaultObjectTemplate;

	private TypeConverter converter;

	/**
	 * Specific provider for {@link ObjectInstance} to {@link RestInstance} converter.
	 *
	 * @author BBonev
	 */
	public class ObjectToRestInstanceConverter implements Converter<ObjectInstance, RestInstance> {

		/**
		 * {@inheritDoc}
		 */
		@Override
		@SuppressWarnings("unchecked")
		public RestInstance convert(ObjectInstance source) {
			RestInstance restInstance = new RestInstance();
			restInstance.setId(convertString(source.getId()));
			DataTypeDefinition typeDefinition = dictionaryService.getDataTypeDefinition(source
					.getClass().getName());
			restInstance.setType(typeDefinition.getName());
			restInstance.setIdentifier(source.getIdentifier());
			restInstance.setOwningInstanceId(convertString(source.getId()));
			restInstance.setOwningInstanceType(typeDefinition.getName());
			DefinitionModel definitionModel = dictionaryService.getInstanceDefinition(source);
			Map<String, Object> forRest = (Map<String, Object>) propertiesService.convertToExternalModel(
					source, definitionModel);
			restInstance.setProperties(forRest);

			String viewLocation = (String) source.getProperties().get(
					ObjectProperties.DEFAULT_VIEW_LOCATION);
			if (viewLocation != null) {
				DocumentInstance view = documentService.load(viewLocation);
				// if the view is not found we will create new one later
				if (view != null) {
					restInstance.setViewId(view.getId().toString());

					String version = (String) source.getProperties().get(NonPersistentProperties.LOAD_VIEW_VERSION);
					if (StringUtils.isNotNullOrEmpty(version)) {
						restInstance.setLoadedViewVersion(version);
						DocumentInstance oldVersion = documentService.getDocumentVersion(view, version);
						restInstance.setContent(loadContent(oldVersion));
					} else {
						restInstance.setContent(loadContent(view));
					}
				}
			}

			// load a template for the object
			if (restInstance.getContent() == null) {
				// find the primary template for the object if nay
				TemplateInstance templateInstance = templateService.getPrimaryTemplate(source
						.getIdentifier());

				if (templateInstance == null) {
					templateInstance = templateService.getTemplate(defaultObjectTemplate);
				}
				TemplateInstance template = templateService.loadContent(templateInstance);
				if (template != null) {
					restInstance.setContent(template.getContent());
				} else {
					// just in case if there is not default template defined
					restInstance.setContent("<div><span data-mce-bogus=\"1\"><br /></span></div>");
				}
			}
			
			restInstance.setDefaultHeader((String) source.getProperties().get("default_header"));
			return restInstance;
		}
	}

	/**
	 * @author BBonev
	 */
	public class ObjectToViewInstanceConverter implements Converter<ObjectInstance, ViewInstance> {

		/**
		 * {@inheritDoc}
		 */
		@Override
		public ViewInstance convert(ObjectInstance source) {
			ViewInstance instance = new ViewInstance();

			DocumentInstance view = (DocumentInstance) source.getProperties().get(
					ObjectProperties.DEFAULT_VIEW);
			if (view == null) {
				String dmsId = (String) source.getProperties().get(
						ObjectProperties.DEFAULT_VIEW_LOCATION);
				view = documentService.load(dmsId);
			}

			if (view != null) {
				instance.setLockedBy(view.getLockedBy());
				InstanceReference reference = view.toReference();
				instance.setViewReference(reference);
			}
			return instance;
		}

	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void register(TypeConverter converter) {
		this.converter = converter;
		converter.addConverter(ObjectInstance.class, RestInstance.class,
				new ObjectToRestInstanceConverter());
		converter.addConverter(ObjectInstance.class, ViewInstance.class,
				new ObjectToViewInstanceConverter());
	}
}
