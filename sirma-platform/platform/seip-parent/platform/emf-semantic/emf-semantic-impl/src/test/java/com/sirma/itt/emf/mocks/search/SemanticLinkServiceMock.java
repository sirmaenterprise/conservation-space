/**
 *
 */
package com.sirma.itt.emf.mocks.search;

import static org.mockito.Mockito.mock;

import java.util.Map;

import org.eclipse.rdf4j.repository.RepositoryConnection;

import com.sirma.itt.seip.util.ReflectionUtils;
import com.sirma.itt.emf.mocks.DefinitionServiceMock;
import com.sirma.itt.emf.mocks.NamespaceRegistryMock;
import com.sirma.itt.emf.semantic.persistence.SemanticLinkServiceImpl;
import com.sirma.itt.seip.event.EventService;
import com.sirma.itt.semantic.ConnectionFactory;

/**
 * @author kirq4e
 */
public class SemanticLinkServiceMock extends SemanticLinkServiceImpl {

	public SemanticLinkServiceMock(Map<String, Object> context) {
		final ConnectionFactory connectionFactory = (ConnectionFactory) context.get("connectionFactory");

		RepositoryConnection transactionalRepositoryConnection = connectionFactory.produceManagedConnection();

		ReflectionUtils.setFieldValue(this, "repositoryConnection", transactionalRepositoryConnection);

		ReflectionUtils.setFieldValue(this, "namespaceRegistryService", new NamespaceRegistryMock(context));
		ReflectionUtils.setFieldValue(this, "valueFactory", connectionFactory.produceValueFactory());
		ReflectionUtils.setFieldValue(this, "queryBuilder", new QueryBuilderMock(context));
		ReflectionUtils.setFieldValue(this, "definitionService", new DefinitionServiceMock());
		ReflectionUtils.setFieldValue(this, "eventService", mock(EventService.class));
		// TODO init other services
		// stateService
		// readConverter
		// writeConverter
	}
}
