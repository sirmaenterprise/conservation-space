package com.sirma.itt.emf.mocks;

import java.util.Map;

import com.sirma.itt.commons.utils.reflection.ReflectionUtils;
import com.sirma.itt.emf.semantic.NamespaceRegistry;
import com.sirma.itt.seip.context.ContextualReference;
import com.sirma.itt.seip.testutil.mocks.ConfigurationPropertyMock;
import com.sirma.itt.seip.testutil.mocks.InstanceProxyMock;
import com.sirma.itt.semantic.model.vocabulary.EMF;

/**
 * Initializes the service NamespaceRegistry for tests
 *
 * @author kirq4e
 */
public class NamespaceRegistryMock extends NamespaceRegistry {
	/**
	 * Initializes the service NamespaceRegistry for tests
	 *
	 * @param context
	 *            parameters needed for initialization
	 */
	public NamespaceRegistryMock(Map<String, Object> context) {

		ReflectionUtils.setField(this, "cache", ContextualReference.create());

		ReflectionUtils.setField(this, "repositoryConnection",
				new InstanceProxyMock<>(new TransactionalRepositoryConnectionMock(context)));
		ReflectionUtils.setField(this, "context", new ConfigurationPropertyMock<>(EMF.DATA_CONTEXT));
		ReflectionUtils.setField(this, "valueFactory", context.get("valueFactory"));

		initAndSchedule();
		observeReloadDefinitionEvent(null);
	}

}
