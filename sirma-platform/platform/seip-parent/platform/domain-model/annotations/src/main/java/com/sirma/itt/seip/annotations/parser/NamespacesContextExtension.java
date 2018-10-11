package com.sirma.itt.seip.annotations.parser;

import java.util.Map;

import javax.inject.Inject;

import com.sirma.itt.emf.semantic.NamespaceRegistry;

/**
 * NamespaceRegistry provided and cached namespaces getter.
 * This class is instantiated at startup  
 * @see {@link com.sirma.itt.seip.annotations.parser.CDIContextBuilderExtension} 
 * 
 * @author tdossev
 */
public class NamespacesContextExtension implements ContextBuilderProvider {

	@Inject
	private NamespaceRegistry namespaceRegistry;

	@SuppressWarnings("unchecked")
	@Override
	public Map<String, String> getSpecificContext() {
		return namespaceRegistry.getProvidedNamespaces();
	}

}
