/*
 *
 */
package com.sirma.itt.cmf.test.mock;

import java.io.Serializable;
import java.util.List;
import java.util.Map;

import com.sirma.itt.cmf.beans.definitions.CaseDefinition;
import com.sirma.itt.cmf.beans.definitions.DocumentDefinitionRef;
import com.sirma.itt.cmf.beans.definitions.DocumentDefinitionTemplate;
import com.sirma.itt.cmf.beans.definitions.SectionDefinition;
import com.sirma.itt.cmf.beans.definitions.impl.DocumentDefinitionRefProxy;
import com.sirma.itt.cmf.beans.definitions.impl.SectionDefinitionImpl;
import com.sirma.itt.cmf.beans.model.CaseInstance;
import com.sirma.itt.cmf.beans.model.DocumentInstance;
import com.sirma.itt.emf.definition.DictionaryService;
import com.sirma.itt.emf.definition.MutableDictionaryService;
import com.sirma.itt.emf.definition.model.DataTypeDefinition;
import com.sirma.itt.emf.definition.model.FieldDefinitionImpl;
import com.sirma.itt.emf.definition.model.PropertyDefinition;
import com.sirma.itt.emf.definition.model.PrototypeDefinition;
import com.sirma.itt.emf.domain.DisplayType;
import com.sirma.itt.emf.domain.model.DefinitionModel;
import com.sirma.itt.emf.domain.model.PathElement;
import com.sirma.itt.emf.instance.model.Instance;

/**
 * The DictionaryServiceMock providing facade for working with definitions
 */
public class DictionaryServiceMock implements DictionaryService {

	/** The Constant serialVersionUID. */
	private static final long serialVersionUID = 5795169207218404984L;

	/** The mockup provider. */
	protected MockupProvider mockupProvider;

	/**
	 * Instantiates a new dictionary service mock.
	 *
	 * @param mockupProvider
	 *            the mockup provider
	 */
	public DictionaryServiceMock(MockupProvider mockupProvider) {
		this.mockupProvider = mockupProvider;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public PropertyDefinition getProperty(String currentQName, Long revision,
			PathElement pathElement) {
		return null;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public Long getPropertyId(String propertyName, Long revision, PathElement pathElement,
			Serializable serializable) {
		return null;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public String getPropertyById(Long propertyId) {
		return null;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public DataTypeDefinition getDataTypeDefinition(String name) {
		return null;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public Map<String, Serializable> filterProperties(DefinitionModel model,
			Map<String, Serializable> properties, DisplayType displayType) {
		return null;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public DefinitionModel getInstanceDefinition(Instance instance) {
		if (instance instanceof CaseInstance) {
			return mockupProvider.getOrCreateCaseDefinition(instance.getIdentifier());
		} else if (instance instanceof DocumentInstance) {
			DocumentInstance doc = (DocumentInstance) instance;
			String documentRefId = doc.getIdentifier();
			DocumentDefinitionRef ref = null;
			if (doc.isStandalone()) {
				DocumentDefinitionTemplate createDocumentDefinition = mockupProvider
						.getOrCreateDocumentTemplateDefinition(documentRefId);
				ref = new DocumentDefinitionRefProxy(createDocumentDefinition);
			} else {
				ref = mockupProvider
						.getOrCreateDocumentDefinition(documentRefId);
			}
			return ref;
		}
		return null;
	}

	/**
	 * Gets the section definition.
	 *
	 * @param string
	 *            the string
	 * @return the section definition
	 */
	protected SectionDefinition getSectionDefinition(String string) {
		SectionDefinitionImpl sectionDefinitionImpl = new SectionDefinitionImpl();
		sectionDefinitionImpl.setIdentifier(string);
		return sectionDefinitionImpl;
	}

	/**
	 * Gets the field definition.
	 *
	 * @param id
	 *            the id
	 * @param dmsId
	 *            the dms id
	 * @param type
	 *            the type
	 * @return the field definition
	 */
	protected PropertyDefinition getFieldDefinition(String id, String dmsId, DisplayType type) {
		FieldDefinitionImpl fieldDefinitionImpl = new FieldDefinitionImpl();
		fieldDefinitionImpl.setIdentifier(id);
		fieldDefinitionImpl.setDmsType(dmsId);
		fieldDefinitionImpl.setDisplayType(type);
		return fieldDefinitionImpl;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public PrototypeDefinition getProperty(Long propertyId) {
		return null;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public PropertyDefinition getDefinitionByValue(String propertyName, Serializable serializable) {
		return null;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public PrototypeDefinition getPrototype(String currentQName, Long revision,
			PathElement pathElement) {
		return null;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public <E extends DefinitionModel> List<E> getAllDefinitions(Class<E> ref) {
		return null;
	}

	/**
	 * {@inheritDoc}
	 */
	@SuppressWarnings("unchecked")
	@Override
	public <E extends DefinitionModel> E getDefinition(Class<E> ref, String defId) {
		if (CaseDefinition.class.equals(ref)) {
			return (E) mockupProvider.getOrCreateCaseDefinition(defId);
		} else if (DocumentDefinitionRef.class.equals(ref)) {
			DocumentDefinitionRef createDocumentDefinition = mockupProvider
					.getOrCreateDocumentDefinition(defId);
			return (E) createDocumentDefinition;
		} else if (DocumentDefinitionTemplate.class.equals(ref)) {
			DocumentDefinitionTemplate createDocumentDefinition = mockupProvider
					.getOrCreateDocumentTemplateDefinition(defId);
			return (E) createDocumentDefinition;
		} else {
			return null;
		}
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public <E extends DefinitionModel> E getDefinition(Class<E> ref, String defId, Long version) {
		return null;
	}
	/**
	 * {@inheritDoc}
	 */
	@Override
	public <E extends DefinitionModel> List<E> getDefinitionVersions(Class<E> ref, String defId) {
		return null;
	}

	@Override
	public MutableDictionaryService getMutableInstance() {
		return null;
	}

}
