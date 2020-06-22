package com.sirma.itt.emf.mocks;

import java.util.Map;

import com.sirma.itt.emf.semantic.NamespaceRegistry;
import com.sirma.itt.seip.context.ContextualReference;
import com.sirma.itt.seip.testutil.mocks.ConfigurationPropertyMock;
import com.sirma.itt.seip.testutil.mocks.InstanceProxyMock;
import com.sirma.itt.seip.util.ReflectionUtils;
import com.sirma.itt.semantic.ConnectionFactory;
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

		ReflectionUtils.setFieldValue(this, "cache", ContextualReference.create());
		ReflectionUtils.setFieldValue(this, "propertiesCache", ContextualReference.create());

		ConnectionFactory connectionFactory = (ConnectionFactory) context.get("connectionFactory");
		ReflectionUtils.setFieldValue(this, "repositoryConnection", connectionFactory.produceReadOnlyConnection());
		ReflectionUtils.setFieldValue(this, "context", new ConfigurationPropertyMock<>(EMF.DATA_CONTEXT));
		ReflectionUtils.setFieldValue(this, "valueFactory", context.get("valueFactory"));

		initAndSchedule();
		observeReloadDefinitionEvent(null);
	}

}
