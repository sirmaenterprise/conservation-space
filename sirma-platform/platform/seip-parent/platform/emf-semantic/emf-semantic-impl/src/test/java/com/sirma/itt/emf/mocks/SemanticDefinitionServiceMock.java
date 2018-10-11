package com.sirma.itt.emf.mocks;

import java.util.Map;

import org.mockito.Mockito;

import com.sirma.itt.seip.util.ReflectionUtils;
import com.sirma.itt.emf.mocks.search.SearchServiceMock;
import com.sirma.itt.emf.semantic.SemanticDefinitionServiceImpl;
import com.sirma.itt.seip.context.ContextualReference;
import com.sirma.itt.seip.instance.HeadersService;

/**
 * @author kirq4e
 */
public class SemanticDefinitionServiceMock extends SemanticDefinitionServiceImpl {

	/**
	 * Mocks the external services for this class
	 *
	 * @param context
	 *            Context for semantics
	 */
	public SemanticDefinitionServiceMock(Map<String, Object> context) {
		ReflectionUtils.setFieldValue(this, "cache", ContextualReference.create());
		// put the reference in the context as it's used by the SearchServiceMock -> SemanticSearchEngineMock
		context.put("semanticDefinitionService", this);

		ReflectionUtils.setFieldValue(this, "searchService", new SearchServiceMock(context));
		ReflectionUtils.setFieldValue(this, "namespaceRegistryService", new NamespaceRegistryMock(context));
		ReflectionUtils.setFieldValue(this, "definitionService", new DefinitionServiceMock());
		HeadersService headersService = Mockito.mock(HeadersService.class);
		ReflectionUtils.setFieldValue(this, "headersService", headersService);

		initialize();
	}
}
