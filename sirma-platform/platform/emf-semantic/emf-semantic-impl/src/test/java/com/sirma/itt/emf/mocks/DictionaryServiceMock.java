package com.sirma.itt.emf.mocks;

import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import javax.enterprise.inject.Alternative;

import org.openrdf.model.URI;

import com.sirma.itt.cmf.dozer.provider.GenericDefinitionDozerProvider;
import com.sirma.itt.seip.definition.DictionaryService;
import com.sirma.itt.seip.definition.dozer.DefinitionsDozerProvider;
import com.sirma.itt.seip.domain.definition.DefinitionModel;
import com.sirma.itt.seip.domain.definition.GenericDefinition;
import com.sirma.itt.seip.domain.instance.ClassInstance;
import com.sirma.itt.seip.domain.instance.EmfInstance;
import com.sirma.itt.seip.domain.instance.Instance;
import com.sirma.itt.seip.instance.ObjectInstance;
import com.sirma.itt.seip.instance.dozer.CommonDozerProvider;
import com.sirma.itt.seip.instance.relation.LinkReference;
import com.sirma.itt.semantic.model.vocabulary.EMF;
import com.sirma.itt.semantic.model.vocabulary.Proton;

/**
 * Mock implementation of {@link DictionaryService} interface
 *
 * @author Valeri Tishev
 */
@Alternative
public class DictionaryServiceMock extends BaseDictionaryServiceMock {

	private static final long serialVersionUID = 6505248230297996019L;

	private static final String DEFINITION_REPOSITORY = "src/test/resources/definitions/";

	@Override
	protected void initializeInstanceUries(Map<String, URI> instanceUris) {
		instanceUris.put(ObjectInstance.class.getName(), Proton.OBJECT);
		instanceUris.put(ObjectInstance.class.getSimpleName().toLowerCase(), Proton.OBJECT);
		instanceUris.put("com.sirma.itt.seip.resources.EmfUser", EMF.USER);
		instanceUris.put("com.sirma.itt.seip.resources.model.EmfGroup", EMF.GROUP);
		instanceUris.put("com.sirma.itt.seip.instance.relation.LinkReference", EMF.RELATION);
	}

	@Override
	protected void initializeInstanceClasses(Map<URI, String> instanceClasses) {
		instanceClasses.put(Proton.OBJECT, ObjectInstance.class.getName());
		instanceClasses.put(EMF.CASE_SECTION, ObjectInstance.class.getName());
		instanceClasses.put(EMF.CASE, ObjectInstance.class.getName());
		instanceClasses.put(EMF.PROJECT, ObjectInstance.class.getName());
		instanceClasses.put(EMF.DOCUMENT, ObjectInstance.class.getName());
		instanceClasses.put(EMF.MEDIA, ObjectInstance.class.getName());
		instanceClasses.put(EMF.IMAGE, ObjectInstance.class.getName());
		instanceClasses.put(EMF.AUDIO, ObjectInstance.class.getName());
		instanceClasses.put(EMF.VIDEO, ObjectInstance.class.getName());
		instanceClasses.put(EMF.CLASS_DESCRIPTION, "com.sirma.itt.seip.domain.instance.ClassInstance");
		instanceClasses.put(EMF.USER, "com.sirma.itt.seip.resources.EmfUser");
		instanceClasses.put(EMF.GROUP, "com.sirma.itt.seip.resources.EmfGroup");
		instanceClasses.put(EMF.RELATION, "com.sirma.itt.seip.instance.relation.LinkReference");
	}

	@Override
	protected void initializeBeanRegistry(Map<Class<?>, BeanConfiguration> beanRegistry) {

		beanRegistry.put(GenericDefinition.class,
				new BeanConfiguration(com.sirma.itt.seip.definition.jaxb.Definition.class,
						com.sirma.itt.cmf.beans.definitions.impl.GenericDefinitionImpl.class));
	}

	@Override
	protected Collection<String> provideDozerMappings() {
		List<String> mappingUries = new ArrayList<>();

		mappingUries.addAll(new CommonDozerProvider().getMappingUries());
		mappingUries.addAll(new DefinitionsDozerProvider().getMappingUries());
		mappingUries.addAll(new GenericDefinitionDozerProvider().getMappingUries());
		return mappingUries;
	}

	@Override
	protected File resolveDefinitionFile(String defId) {
		File file = new File(DEFINITION_REPOSITORY + "generic/" + defId + ".xml");
		if (file.exists()) {
			return file;
		}
		throw new IllegalArgumentException("Uknown definition [" + defId + "]");
	}

	@Override
	protected DefinitionModel resolveInstanceDefinition(Instance instance) {
		if (instance instanceof ClassInstance || instance instanceof ObjectInstance
				|| instance instanceof EmfInstance) {
			return find(instance.getIdentifier() == null ? "classDefinition" : instance.getIdentifier());
		} else if (instance instanceof LinkReference) {
			return find("genericLinkDefinition");
		}
		throw new UnsupportedOperationException(
				"Unknown instance definition" + "for type [" + instance.getClass() + "]");
	}
}