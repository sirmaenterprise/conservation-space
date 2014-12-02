package com.sirma.itt.cmf.util.datatype;


import java.util.Date;

import javax.enterprise.context.ApplicationScoped;

import com.sirma.itt.cmf.beans.model.DocumentInstance;
import com.sirma.itt.emf.converter.Converter;
import com.sirma.itt.emf.converter.TypeConverter;
import com.sirma.itt.emf.domain.model.DefinitionModel;
import com.sirma.itt.emf.instance.model.Instance;
import com.sirma.itt.emf.properties.DefaultProperties;
import com.sirma.itt.emf.rest.model.RestInstance;

/**
 * Converter provider for CMF to add support for conversion between {@link RestInstance} to concrete
 * CMF {@link Instance} implementations.
 *
 * @author BBonev
 */
@ApplicationScoped
public class RestInstanceToInstanceConverterProvider extends
		AbstractRestInstanceToInstanceConverterProvider {

	/**
	 * The Class RestToDocumentConverter.
	 * 
	 * @author BBonev
	 */
	public class RestToDocumentConverter implements Converter<RestInstance, DocumentInstance> {

		/**
		 * {@inheritDoc}
		 */
		@Override
		public DocumentInstance convert(RestInstance source) {
			Instance instance = getOrCreateInstance(source);
			if (!(instance instanceof DocumentInstance)) {
				return null;
			}
			DocumentInstance documentInstance = (DocumentInstance) instance;

			setDocumentContent(documentInstance.getProperties(), source.getContent());

			DefinitionModel definitionModel = dictionaryService.getInstanceDefinition(instance);

			instance.getProperties().putAll(
					propertiesService.convertToInternalModel(source.getProperties(), definitionModel));
			instance.getProperties().putAll(
					propertiesService.convertToInternalModel(source.getViewProperties(), definitionModel));
			if (source.isOverwrite()) {
				Date lastModifiedDate = new Date();
				instance.getProperties().put(DefaultProperties.MODIFIED_ON, lastModifiedDate);
			}

			return (DocumentInstance) instance;
		}
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void register(TypeConverter converter) {
		converter.addConverter(RestInstance.class, DocumentInstance.class,
				new RestToDocumentConverter());
	}

}
