package com.sirma.itt.emf.mocks.search;

import static org.mockito.Mockito.mock;

import java.util.Date;
import java.util.Map;

import org.mockito.Matchers;
import org.mockito.Mockito;

import com.sirma.itt.commons.utils.reflection.ReflectionUtils;
import com.sirma.itt.emf.mocks.DictionaryServiceMock;
import com.sirma.itt.emf.mocks.NamespaceRegistryMock;
import com.sirma.itt.emf.mocks.TransactionalRepositoryConnectionMock;
import com.sirma.itt.emf.semantic.search.SearchQueryBuilder;
import com.sirma.itt.emf.semantic.search.SemanticSearchEngine;
import com.sirma.itt.seip.domain.search.tree.Condition;
import com.sirma.itt.seip.domain.util.DateConverter;
import com.sirma.itt.seip.domain.util.DateConverterImpl;
import com.sirma.itt.seip.monitor.NoOpStatistics;
import com.sirma.itt.seip.permissions.action.AuthorityService;
import com.sirma.itt.seip.resources.EmfUser;
import com.sirma.itt.seip.resources.User;
import com.sirma.itt.seip.security.UserPreferences;
import com.sirma.itt.seip.security.context.SecurityContext;
import com.sirma.itt.seip.security.context.SecurityContextManager;
import com.sirma.itt.seip.testutil.mocks.InstanceProxyMock;
import com.sirma.itt.semantic.TransactionalRepositoryConnection;
import com.sirma.itt.semantic.search.FTSQueryParser;

/**
 * Semantic search engine mock - initializes the service for tests
 *
 * @author kirq4e
 */
public class SemanticSearchEngineMock extends SemanticSearchEngine {

	/**
	 * Initializes the service for the tests. Sets all needed fields of the service
	 *
	 * @param context
	 *            needed for initialization
	 */
	public SemanticSearchEngineMock(Map<String, Object> context) {
		TransactionalRepositoryConnection transactionalRepositoryConnection = new TransactionalRepositoryConnectionMock(
				context);

		ReflectionUtils.setField(this, "connection", new InstanceProxyMock<>(transactionalRepositoryConnection));
		ReflectionUtils.setField(this, "namespaceRegistryService", new NamespaceRegistryMock(context));
		ReflectionUtils.setField(this, "dictionaryService", new DictionaryServiceMock());
		ReflectionUtils.setField(this, "configurations", new SemanticSearchConfigurationsMock());
		ReflectionUtils.setField(this, "semanticConfiguration", new SemanticConfigurationMock());
		ReflectionUtils.setField(this, "statistics", new NoOpStatistics());

		FTSQueryParser parser = Mockito.mock(FTSQueryParser.class);
		Mockito.when(parser.prepare(Matchers.anyString())).then(
				invocation -> invocation.getArgumentAt(0, String.class));
		ReflectionUtils.setField(this, "parser", parser);

		SearchQueryBuilder builder = Mockito.mock(SearchQueryBuilder.class);
		Mockito.when(builder.build(Matchers.any(Condition.class))).thenReturn("test query");
		ReflectionUtils.setField(this, "searchQueryBuilder", builder);

		DateConverter dateConverter = Mockito.mock(DateConverterImpl.class);
		Mockito.when(dateConverter.parseDate(Matchers.anyString())).thenReturn(new Date());

		UserPreferences userPreferences = Mockito.mock(UserPreferences.class);
		Mockito.when(userPreferences.getLanguage()).thenReturn("en");
		ReflectionUtils.setField(dateConverter, "userPreferences", userPreferences);
		ReflectionUtils.setField(this, "dateConverter", dateConverter);

		AuthorityService authorityService = Mockito.mock(AuthorityService.class);
		Mockito.when(authorityService.isAdminOrSystemUser()).thenReturn(Boolean.FALSE);

		ReflectionUtils.setField(this, "authorityService", authorityService);

		SecurityContext securityContext = mock(SecurityContext.class);

		User user = new EmfUser();
		user.setId("emf:daniela.todorova");
		Mockito.when(securityContext.getEffectiveAuthentication()).thenReturn(user);

		ReflectionUtils.setField(this, "securityContext", securityContext);
		SecurityContextManager securityContextManager = new SecurityContextManagerMock();
		ReflectionUtils.setField(this, "securityContextManager", securityContextManager);

		init();
	}
}
