package com.sirma.itt.cmf.util.datatype;

import java.io.Serializable;
import java.util.Map;

import javax.inject.Inject;

import org.apache.commons.lang.StringUtils;

import com.sirma.itt.cmf.beans.model.DocumentInstance;
import com.sirma.itt.cmf.constants.DocumentProperties;
import com.sirma.itt.cmf.constants.NonPersistentProperties;
import com.sirma.itt.emf.converter.Converter;
import com.sirma.itt.emf.converter.TypeConverter;
import com.sirma.itt.emf.definition.DictionaryService;
import com.sirma.itt.emf.definition.model.DataTypeDefinition;
import com.sirma.itt.emf.domain.model.DefinitionModel;
import com.sirma.itt.emf.properties.PropertiesService;
import com.sirma.itt.emf.rest.model.RestInstance;
import com.sirma.itt.emf.rest.model.ViewInstance;

/**
 * Converter provider for converting instance to {@link RestInstance} for Rest services.
 * 
 * @author BBonev
 */
public class InstanceToRestInstanceConverterProvider extends
		AbstractInstanceToRestInstanceConverterProvider {

	/** The dictionary service. */
	@Inject
	DictionaryService dictionaryService;

	/** The properties service. */
	@Inject
	private PropertiesService propertiesService;

	private TypeConverter converter;

	/**
	 * Converter for {@link DocumentInstance} to {@link RestInstance}.
	 * 
	 * @author BBonev
	 */
	public class DocumentInstanceToRestInstanceConverter implements
			Converter<DocumentInstance, RestInstance> {

		/**
		 * {@inheritDoc}
		 */
		@SuppressWarnings("unchecked")
		@Override
		public RestInstance convert(DocumentInstance source) {
			RestInstance idoc = new RestInstance();
			if (source != null) {
				idoc.setId(convertString(source.getId()));
				idoc.setIdentifier(source.getIdentifier());
				DataTypeDefinition typeDefinition = dictionaryService.getDataTypeDefinition(source
						.getClass().getName());
				idoc.setType(typeDefinition.getName());

				if ((source.getOwningReference() != null)
						&& (source.getOwningReference().getIdentifier() != null)) {
					idoc.setOwningInstanceId(source.getOwningReference().getIdentifier());
					idoc.setOwningInstanceType(source.getOwningReference().getReferenceType()
							.getName());
				}

				Map<String, Serializable> documentProperties = null;
				String version = (String) source.getProperties().get(NonPersistentProperties.LOAD_VIEW_VERSION);
				if (StringUtils.isNotBlank(version)) {
					DocumentInstance oldVersion = documentService.getDocumentVersion(source, version);
					idoc.setContent(loadContent(oldVersion));
					idoc.setLoadedViewVersion(version);
					documentProperties = oldVersion.getProperties();
				} else {
					idoc.setContent(loadContent(source));
					documentProperties = source.getProperties();
				}
				documentProperties.remove(DocumentProperties.CLONED_DMS_ID);

				DefinitionModel definitionModel = dictionaryService.getInstanceDefinition(source);
				Map<String, Object> properties = (Map<String, Object>) propertiesService
						.convertToExternalModel(source, definitionModel);

				idoc.setProperties(properties);
				idoc.setDefaultHeader((String) source.getProperties().get("default_header"));
			}
			return idoc;
		}
	}

	/**
	 * @author BBonev
	 */
	public class DocumentInstanceToViewInstanceConverter implements
			Converter<DocumentInstance, ViewInstance> {

		/**
		 * {@inheritDoc}
		 */
		@Override
		public ViewInstance convert(DocumentInstance source) {
			ViewInstance instance = new ViewInstance();
			instance.setLockedBy(source.getLockedBy());
			instance.setViewReference(source.toReference());
			return instance;
		}

	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void register(TypeConverter converter) {
		this.converter = converter;
		converter.addConverter(DocumentInstance.class, RestInstance.class,
				new DocumentInstanceToRestInstanceConverter());
		converter.addConverter(DocumentInstance.class, ViewInstance.class,
				new DocumentInstanceToViewInstanceConverter());
	}

}
