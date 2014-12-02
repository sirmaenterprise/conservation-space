package com.sirma.itt.emf.semantic;

import javax.ejb.DependsOn;
import javax.ejb.Singleton;
import javax.ejb.Startup;
import javax.inject.Inject;

import com.sirma.itt.emf.patch.PatchDbService;
import com.sirma.itt.semantic.NamespaceRegistryService;

/**
 * Singleton bean that will trigger namespace synchronization before all other
 * 
 * @author BBonev
 */
@Singleton
@Startup
@DependsOn(PatchDbService.SERVICE_NAME)
public class NamespaceRegistryInitializer {

	/** The namespace registry service. */
	@Inject
	private NamespaceRegistryService namespaceRegistryService;

}
