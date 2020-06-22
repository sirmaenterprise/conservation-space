package com.sirma.itt.seip.annotations;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import org.eclipse.rdf4j.model.IRI;

import com.sirma.itt.emf.mocks.BaseDefinitionServiceMock;
import com.sirma.itt.seip.annotations.model.Annotation;
import com.sirma.itt.seip.definition.dozer.DefinitionsDozerProvider;
import com.sirma.itt.seip.domain.definition.DefinitionModel;
import com.sirma.itt.seip.domain.definition.GenericDefinition;
import com.sirma.itt.seip.domain.instance.Instance;
import com.sirma.itt.seip.instance.dozer.CommonDozerProvider;
import com.sirma.itt.semantic.model.vocabulary.OA;

/**
 * Mock service to handle definitions loading for {@link Annotation}s.
 *
 * @author BBonev
 */
public class AnnotationDefinitionServiceMock extends BaseDefinitionServiceMock {

	private static final long serialVersionUID = 4262767500002778553L;

	private static final String DEFINITION_REPOSITORY = "/definitions/";

	@Override
	protected void initializeInstanceUries(Map<String, IRI> instanceUris) {
		instanceUris.put("com.sirma.itt.seip.annotations.model.Annotation", OA.ANNOTATION);
		instanceUris.put("annotation", OA.ANNOTATION);
	}

	@Override
	protected void initializeInstanceClasses(Map<IRI, String> instanceClasses) {
		instanceClasses.put(OA.ANNOTATION, "com.sirma.itt.seip.annotations.model.Annotation");
	}

	@Override
	protected void initializeBeanRegistry(Map<Class<?>, BeanConfiguration> beanRegistry) {
		beanRegistry.put(GenericDefinition.class,
				new BeanConfiguration(com.sirma.itt.seip.definition.jaxb.Definition.class,
						com.sirma.itt.seip.definition.model.GenericDefinitionImpl.class));
	}

	@Override
	protected Collection<String> provideDozerMappings() {
		List<String> mappingUries = new ArrayList<>();
		mappingUries.addAll(new CommonDozerProvider().getMappingUries());
		mappingUries.addAll(new DefinitionsDozerProvider().getMappingUries());
		return mappingUries;
	}

	@Override
	protected InputStream resolveDefinitionFile(String defId) {
		InputStream file = getClass().getResourceAsStream(DEFINITION_REPOSITORY + defId + ".xml");
		if (file != null) {
			return file;
		}
		return null;
	}

	@Override
	protected DefinitionModel resolveInstanceDefinition(Instance instance) {
		if (instance instanceof Annotation) {
			return find("genericAnnotationDefinition");
		}
		return null;
	}
}
