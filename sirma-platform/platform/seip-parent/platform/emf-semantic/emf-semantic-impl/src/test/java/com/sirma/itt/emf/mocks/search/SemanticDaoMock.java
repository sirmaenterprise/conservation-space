package com.sirma.itt.emf.mocks.search;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.Map;

import org.eclipse.rdf4j.model.impl.SimpleValueFactory;

import com.sirma.itt.emf.mocks.DefinitionServiceMock;
import com.sirma.itt.emf.mocks.NamespaceRegistryMock;
import com.sirma.itt.emf.semantic.persistence.SemanticDbDaoImpl;
import com.sirma.itt.emf.semantic.persistence.SemanticPropertiesReadConverter;
import com.sirma.itt.emf.semantic.persistence.SemanticPropertiesWriteConverter;
import com.sirma.itt.seip.monitor.NoOpStatistics;
import com.sirma.itt.seip.resources.EmfUser;
import com.sirma.itt.seip.security.context.SecurityContext;
import com.sirma.itt.seip.util.ReflectionUtils;

/**
 * Working mock class of semantic db dao
 *
 * @author BBonev
 */
public class SemanticDaoMock extends SemanticDbDaoImpl {
	private static final long serialVersionUID = 4833498049067815195L;

	/**
	 * Instantiates a new semantic dao mock.
	 *
	 * @param context
	 *            the context
	 */
	public SemanticDaoMock(Map<String, Object> context) {
		NamespaceRegistryMock namespaceRegistryMock = new NamespaceRegistryMock(context);
		ReflectionUtils.setFieldValue(this, "namespaceRegistryService", namespaceRegistryMock);
		ReflectionUtils.setFieldValue(this, "definitionService",
				context.computeIfAbsent("definitionService", key -> new DefinitionServiceMock()));
		ReflectionUtils.setFieldValue(this, "valueFactory", SimpleValueFactory.getInstance());

		ReflectionUtils.setFieldValue(this, "idManager", context.get("idManager"));

		ReflectionUtils.setFieldValue(this, "queryBuilder", new QueryBuilderMock(context));

		SemanticPropertiesReadConverter readConverter = new SemanticPropertiesReadConverter();
		ReflectionUtils.setFieldValue(readConverter, "namespaceRegistryService", namespaceRegistryMock);

		SemanticPropertiesWriteConverter writeConverter = new SemanticPropertiesWriteConverterMock(context);

		ReflectionUtils.setFieldValue(this, "readConverter", readConverter);
		ReflectionUtils.setFieldValue(this, "writeConverter", writeConverter);

		SecurityContext securityCtx = mock(SecurityContext.class);
		EmfUser user = new EmfUser();
		user.setId("emf:admin");
		when(securityCtx.getAuthenticated()).thenReturn(user);
		ReflectionUtils.setFieldValue(this, "securityContext", securityCtx);
		ReflectionUtils.setFieldValue(this, "statistics", new NoOpStatistics());
	}
}