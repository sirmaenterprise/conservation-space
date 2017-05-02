/**
 *
 */
package com.sirma.itt.emf.mocks.search;

import static org.mockito.Mockito.mock;

import java.util.Map;

import com.sirma.itt.commons.utils.reflection.ReflectionUtils;
import com.sirma.itt.emf.mocks.DictionaryServiceMock;
import com.sirma.itt.emf.mocks.NamespaceRegistryMock;
import com.sirma.itt.emf.mocks.TransactionalRepositoryConnectionMock;
import com.sirma.itt.emf.semantic.persistence.SemanticLinkServiceImpl;
import com.sirma.itt.seip.event.EventService;
import com.sirma.itt.seip.testutil.mocks.InstanceProxyMock;
import com.sirma.itt.semantic.ConnectionFactory;
import com.sirma.itt.semantic.TransactionalRepositoryConnection;

/**
 * @author kirq4e
 */
public class SemanticLinkServiceMock extends SemanticLinkServiceImpl {

	public SemanticLinkServiceMock(Map<String, Object> context) {
		final ConnectionFactory connectionFactory = (ConnectionFactory) context.get("connectionFactory");

		TransactionalRepositoryConnection transactionalRepositoryConnection = new TransactionalRepositoryConnectionMock(
				context);

		ReflectionUtils.setField(this, "repositoryConnection",
				new InstanceProxyMock<TransactionalRepositoryConnection>(transactionalRepositoryConnection));

		ReflectionUtils.setField(this, "namespaceRegistryService", new NamespaceRegistryMock(context));
		ReflectionUtils.setField(this, "valueFactory", connectionFactory.produceValueFactory());
		ReflectionUtils.setField(this, "queryBuilder", new QueryBuilderMock(context));
		ReflectionUtils.setField(this, "dictionaryService", new DictionaryServiceMock());
		ReflectionUtils.setField(this, "eventService", mock(EventService.class));
		// TODO init other services
		// stateService
		// readConverter
		// writeConverter

	}
}
