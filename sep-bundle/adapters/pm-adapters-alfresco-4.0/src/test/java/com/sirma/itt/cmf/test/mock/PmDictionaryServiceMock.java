package com.sirma.itt.cmf.test.mock;

import java.util.List;

import com.sirma.itt.emf.definition.model.PropertyDefinition;
import com.sirma.itt.emf.domain.DisplayType;
import com.sirma.itt.emf.domain.model.DefinitionModel;
import com.sirma.itt.emf.instance.model.Instance;
import com.sirma.itt.pm.domain.definitions.ProjectDefinition;
import com.sirma.itt.pm.domain.model.ProjectInstance;

/**
 * The PmDictionaryServiceMock extends the dictionary mock and provides additional information about
 * {@link ProjectDefinition}
 */
public class PmDictionaryServiceMock extends DictionaryServiceMock {
	/**
	 * Initialize the pm dictionary service
	 *
	 * @param mockupProvider
	 *            is the mockup engine
	 */
	public PmDictionaryServiceMock(PmMockupProvider mockupProvider) {
		super(mockupProvider);
	}

	/** The Constant serialVersionUID. */
	private static final long serialVersionUID = 5795169207218404984L;

	@Override
	public DefinitionModel getInstanceDefinition(Instance instance) {
		if (instance instanceof ProjectInstance) {
			ProjectDefinition projectDefinition = ((PmMockupProvider) mockupProvider)
					.createProjectDefinition("PEG100001");
			List<PropertyDefinition> fields = projectDefinition.getFields();
			fields.add(getFieldDefinition("name", "cm:name", DisplayType.EDITABLE));
			fields.add(getFieldDefinition("title", "cm:title", DisplayType.EDITABLE));
			fields.add(getFieldDefinition("type", "emf:type", DisplayType.EDITABLE));
			fields.add(getFieldDefinition("identifier", "emf:identifier", DisplayType.EDITABLE));
			return projectDefinition;
		}
		return super.getInstanceDefinition(instance);
	}

	/*
	 * (non-Javadoc)
	 * @see com.sirma.itt.cmf.services.DictionaryService#getDefinition(java.lang.Class,
	 * java.lang.String)
	 */
	@SuppressWarnings("unchecked")
	@Override
	public <E extends DefinitionModel> E getDefinition(Class<E> ref, String defId) {
		if (ProjectDefinition.class.equals(ref)) {
			ProjectDefinition createProjectDefinition = ((PmMockupProvider) mockupProvider)
					.createProjectDefinition("PEG100001");
			createProjectDefinition.setDmsId(defId);
			return (E) createProjectDefinition;
		}
		return super.getDefinition(ref, defId);
	}

}
