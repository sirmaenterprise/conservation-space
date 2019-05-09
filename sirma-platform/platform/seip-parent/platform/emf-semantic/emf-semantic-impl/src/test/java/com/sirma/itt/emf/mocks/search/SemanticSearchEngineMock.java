package com.sirma.itt.emf.mocks.search;

import static org.mockito.Mockito.mock;

import java.util.Date;
import java.util.Map;

import org.mockito.Matchers;
import org.mockito.Mockito;

import com.sirma.itt.emf.mocks.DefinitionServiceMock;
import com.sirma.itt.emf.mocks.NamespaceRegistryMock;
import com.sirma.itt.emf.semantic.search.SearchQueryBuilder;
import com.sirma.itt.emf.semantic.search.SemanticSearchEngine;
import com.sirma.itt.seip.domain.codelist.CodelistService;
import com.sirma.itt.seip.domain.search.tree.Condition;
import com.sirma.itt.seip.domain.util.DateConverter;
import com.sirma.itt.seip.domain.util.DateConverterImpl;
import com.sirma.itt.seip.permissions.action.AuthorityService;
import com.sirma.itt.seip.resources.EmfUser;
import com.sirma.itt.seip.resources.User;
import com.sirma.itt.seip.security.UserPreferences;
import com.sirma.itt.seip.security.context.SecurityContext;
import com.sirma.itt.seip.security.context.SecurityContextManager;
import com.sirma.itt.seip.util.ReflectionUtils;
import com.sirma.itt.semantic.ConnectionFactory;
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
		ConnectionFactory connectionFactory = (ConnectionFactory) context.get("connectionFactory");

		ReflectionUtils.setFieldValue(this, "connection", connectionFactory.produceReadOnlyConnection());
		ReflectionUtils.setFieldValue(this, "namespaceRegistryService", new NamespaceRegistryMock(context));
		ReflectionUtils.setFieldValue(this, "definitionService", new DefinitionServiceMock());
		ReflectionUtils.setFieldValue(this, "configurations", new SemanticSearchConfigurationsMock());
		ReflectionUtils.setFieldValue(this, "semanticConfiguration", new SemanticConfigurationMock());

		FTSQueryParser parser = Mockito.mock(FTSQueryParser.class);
		Mockito.when(parser.prepare(Matchers.anyString())).then(
				invocation -> invocation.getArgumentAt(0, String.class));
		ReflectionUtils.setFieldValue(this, "parser", parser);

		SearchQueryBuilder builder = Mockito.mock(SearchQueryBuilder.class);
		Mockito.when(builder.build(Matchers.any(Condition.class))).thenReturn("test query");
		ReflectionUtils.setFieldValue(this, "searchQueryBuilder", builder);

		DateConverter dateConverter = Mockito.mock(DateConverterImpl.class);
		Mockito.when(dateConverter.parseDate(Matchers.anyString())).thenReturn(new Date());

		UserPreferences userPreferences = Mockito.mock(UserPreferences.class);
		Mockito.when(userPreferences.getLanguage()).thenReturn("en");
		ReflectionUtils.setFieldValue(dateConverter, "userPreferences", userPreferences);
		ReflectionUtils.setFieldValue(this, "dateConverter", dateConverter);

		AuthorityService authorityService = Mockito.mock(AuthorityService.class);
		Mockito.when(authorityService.isAdminOrSystemUser()).thenReturn(Boolean.FALSE);

		ReflectionUtils.setFieldValue(this, "authorityService", authorityService);

		SecurityContext securityContext = mock(SecurityContext.class);

		User user = new EmfUser();
		user.setId("emf:daniela.todorova");
		Mockito.when(securityContext.getEffectiveAuthentication()).thenReturn(user);

		ReflectionUtils.setFieldValue(this, "securityContext", securityContext);
		SecurityContextManager securityContextManager = new SecurityContextManagerMock();
		ReflectionUtils.setFieldValue(this, "securityContextManager", securityContextManager);
		ReflectionUtils.setFieldValue(this, "semanticDefinitionService", context.get("semanticDefinitionService"));

		CodelistService codelistService = Mockito.mock(CodelistService.class);
		ReflectionUtils.setFieldValue(this, "codelistService", codelistService);

		init();
	}
}
