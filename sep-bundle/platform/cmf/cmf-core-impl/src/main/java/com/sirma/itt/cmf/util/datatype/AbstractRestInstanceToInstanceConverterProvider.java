package com.sirma.itt.cmf.util.datatype;

import java.io.Serializable;
import java.io.UnsupportedEncodingException;
import java.util.Map;

import javax.inject.Inject;

import com.sirma.itt.cmf.beans.ByteArrayFileDescriptor;
import com.sirma.itt.cmf.beans.model.DocumentInstance;
import com.sirma.itt.cmf.constants.DocumentProperties;
import com.sirma.itt.commons.utils.string.StringUtils;
import com.sirma.itt.emf.converter.TypeConverter;
import com.sirma.itt.emf.converter.TypeConverterProvider;
import com.sirma.itt.emf.db.SequenceEntityGenerator;
import com.sirma.itt.emf.definition.DictionaryService;
import com.sirma.itt.emf.definition.model.DataTypeDefinition;
import com.sirma.itt.emf.domain.model.DefinitionModel;
import com.sirma.itt.emf.entity.LinkSourceId;
import com.sirma.itt.emf.instance.dao.InstanceService;
import com.sirma.itt.emf.instance.dao.ServiceRegister;
import com.sirma.itt.emf.instance.model.InitializedInstance;
import com.sirma.itt.emf.instance.model.Instance;
import com.sirma.itt.emf.properties.PropertiesService;
import com.sirma.itt.emf.rest.model.RestInstance;
import com.sirma.itt.emf.state.operation.Operation;

/**
 * Abstract {@link RestInstance} to {@link Instance} converter provider. The class provides some
 * commonly used methods for the conversions.
 */
public abstract class AbstractRestInstanceToInstanceConverterProvider implements
		TypeConverterProvider {

	/** The service register. */
	@Inject
	protected ServiceRegister serviceRegister;
	/** The dictionary service. */
	@Inject
	protected DictionaryService dictionaryService;

	/** The type converter. */
	@Inject
	protected TypeConverter typeConverter;

	@Inject
	protected PropertiesService propertiesService;

	/**
	 * Sets the document content in document properties using a file locator so the new content will
	 * be saved.
	 *
	 * @param properties
	 *            document properties.
	 * @param content
	 *            content to set.
	 */
	protected void setDocumentContent(Map<String, Serializable> properties, String content) {
		if (StringUtils.isNullOrEmpty(content)) {
			// cannot set null content
			return;
		}
		try {
			byte[] contentAsArray = content.getBytes("UTF-8");
			properties.put(DocumentProperties.FILE_LOCATOR, new ByteArrayFileDescriptor(
					(String) properties.get(DocumentProperties.NAME), contentAsArray));
			properties.put(DocumentProperties.FILE_SIZE, contentAsArray.length);
		} catch (UnsupportedEncodingException e) {
			throw new IllegalArgumentException(e);
		}
	}

	/**
	 * Load instance.
	 *
	 * @param rest
	 *            the rest
	 * @return the instance
	 */
	protected Instance getOrCreateInstance(RestInstance rest) {
		if (!StringUtils.isNullOrEmpty(rest.getType())) {
			DataTypeDefinition typeDefinition = dictionaryService.getDataTypeDefinition(rest
					.getType());
			if (SequenceEntityGenerator.isPersisted(rest)) {
				InitializedInstance initializedInstance = typeConverter.convert(
						InitializedInstance.class, new LinkSourceId(rest.getId().toString(),
								typeDefinition));
				if (initializedInstance != null) {
					return initializedInstance.getInstance();
				}
			} else if (StringUtils.isNotNullOrEmpty(rest.getIdentifier())) {
				Instance owningInstance = null;
				if (StringUtils.isNotNullOrEmpty(rest.getOwningInstanceId())
						&& StringUtils.isNotNullOrEmpty(rest.getOwningInstanceType())) {
					DataTypeDefinition dataTypeDefinition = dictionaryService
							.getDataTypeDefinition(rest.getOwningInstanceType());
					InitializedInstance initializedInstance = typeConverter.convert(
							InitializedInstance.class, new LinkSourceId(rest.getOwningInstanceId()
									.toString(), dataTypeDefinition));
					if (initializedInstance != null) {
						owningInstance = initializedInstance.getInstance();
					}
				}
				DefinitionModel definitionModel;
				InstanceService<Instance, DefinitionModel> instanceService = serviceRegister
						.getInstanceService(typeDefinition.getJavaClass());

				// REVIEW: some day this should be simplified
				// for document instance for now the default instance creation is more
				// complicated...
				if (DocumentInstance.class.equals(typeDefinition.getJavaClass())) {
					DocumentInstance documentInstance = new DocumentInstance();
					documentInstance.setIdentifier(rest.getIdentifier());
					documentInstance.setStandalone(true);
					documentInstance.setRevision(0L);
					definitionModel = dictionaryService.getInstanceDefinition(documentInstance);
				} else {
					definitionModel = dictionaryService.getDefinition(
							instanceService.getInstanceDefinitionClass(), rest.getIdentifier());
				}
				// TODO: create the instance from RestInstance
				Instance instance = instanceService.createInstance(definitionModel, owningInstance,
						new Operation(rest.getCurrentOperation()));
				if (org.apache.commons.lang.StringUtils.isNotBlank(rest.getId())){
					SequenceEntityGenerator.unregister(instance.getId());
					instance.setId(rest.getId());
				}
				return instance;
			}
		}
		return null;
	}

}