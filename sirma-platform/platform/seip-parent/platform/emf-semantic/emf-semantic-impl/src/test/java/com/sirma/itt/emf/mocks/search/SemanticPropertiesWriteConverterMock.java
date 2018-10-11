package com.sirma.itt.emf.mocks.search;

import static org.mockito.Mockito.mock;

import java.util.HashSet;
import java.util.Map;

import com.sirma.itt.emf.mocks.DefinitionServiceMock;
import com.sirma.itt.emf.mocks.NamespaceRegistryMock;
import com.sirma.itt.emf.mocks.SemanticDefinitionServiceMock;
import com.sirma.itt.emf.semantic.persistence.PersistStepFactoryBuilderMock;
import com.sirma.itt.emf.semantic.persistence.SemanticPropertiesWriteConverter;
import com.sirma.itt.seip.event.EventService;
import com.sirma.itt.seip.monitor.Statistics;
import com.sirma.itt.seip.util.ReflectionUtils;

/**
 * Mock class for {@link SemanticPropertiesWriteConverter} that uses other mocks for integrated semantic testing
 *
 * @author BBonev
 */
public class SemanticPropertiesWriteConverterMock extends SemanticPropertiesWriteConverter {

	/**
	 * Instantiates a new semantic properties write converter mock.
	 *
	 * @param context
	 *            the context
	 */
	public SemanticPropertiesWriteConverterMock(Map<String, Object> context) {
		NamespaceRegistryMock namespaceRegistryMock = new NamespaceRegistryMock(context);

		ReflectionUtils.setFieldValue(this, "namespaceRegistryService", namespaceRegistryMock);
		ReflectionUtils.setFieldValue(this, "forbiddenProperties", new HashSet<String>());
		ReflectionUtils.setFieldValue(this, "definitionService",
				context.computeIfAbsent("definitionService", key -> new DefinitionServiceMock()));
		ReflectionUtils.setFieldValue(this, "semanticDefinitionService", new SemanticDefinitionServiceMock(context));
		ReflectionUtils.setFieldValue(this, "idManager", context.get("idManager"));
		ReflectionUtils.setFieldValue(this, "eventService", mock(EventService.class));
		ReflectionUtils.setFieldValue(this, "statistics", Statistics.NO_OP);
		ReflectionUtils.setFieldValue(this, "persistStepFactoryBuilder", new PersistStepFactoryBuilderMock(context));
	}
}