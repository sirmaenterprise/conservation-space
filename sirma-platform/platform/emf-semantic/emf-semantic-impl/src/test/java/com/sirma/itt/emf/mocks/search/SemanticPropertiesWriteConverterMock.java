package com.sirma.itt.emf.mocks.search;

import static org.mockito.Mockito.mock;

import java.util.HashSet;
import java.util.Map;

import com.sirma.itt.commons.utils.reflection.ReflectionUtils;
import com.sirma.itt.emf.mocks.DictionaryServiceMock;
import com.sirma.itt.emf.mocks.NamespaceRegistryMock;
import com.sirma.itt.emf.mocks.SemanticDefinitionServiceMock;
import com.sirma.itt.emf.semantic.persistence.PersistStepFactoryBuilderMock;
import com.sirma.itt.emf.semantic.persistence.SemanticPropertiesWriteConverter;
import com.sirma.itt.seip.event.EventService;
import com.sirma.itt.seip.monitor.Statistics;

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

		ReflectionUtils.setField(this, "namespaceRegistryService", namespaceRegistryMock);
		ReflectionUtils.setField(this, "forbiddenProperties", new HashSet<String>());
		ReflectionUtils.setField(this, "dictionaryService",
				context.computeIfAbsent("dictionaryService", key -> new DictionaryServiceMock()));
		ReflectionUtils.setField(this, "semanticDefinitionService", new SemanticDefinitionServiceMock(context));
		ReflectionUtils.setField(this, "idManager", context.get("idManager"));
		ReflectionUtils.setField(this, "eventService", mock(EventService.class));
		ReflectionUtils.setField(this, "statistics", Statistics.NO_OP);
		ReflectionUtils.setField(this, "persistStepFactoryBuilder", new PersistStepFactoryBuilderMock(context));

	}
}
