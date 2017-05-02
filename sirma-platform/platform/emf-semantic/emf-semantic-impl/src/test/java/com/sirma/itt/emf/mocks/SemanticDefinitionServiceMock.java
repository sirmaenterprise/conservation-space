package com.sirma.itt.emf.mocks;

import java.util.Map;

import org.mockito.Mockito;

import com.sirma.itt.commons.utils.reflection.ReflectionUtils;
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

		ReflectionUtils.setField(this, "cache", ContextualReference.create());

		ReflectionUtils.setField(this, "searchService", new SearchServiceMock(context));
		ReflectionUtils.setField(this, "namespaceRegistryService", new NamespaceRegistryMock(context));
		ReflectionUtils.setField(this, "dictionaryService", new DictionaryServiceMock());
		HeadersService headersService = Mockito.mock(HeadersService.class);
		ReflectionUtils.setField(this, "headersService", headersService);

		initialize();
	}

}
