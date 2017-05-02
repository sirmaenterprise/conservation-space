package com.sirma.itt.emf.mocks.search;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.Map;

import org.openrdf.model.impl.ValueFactoryImpl;

import com.sirma.itt.commons.utils.reflection.ReflectionUtils;
import com.sirma.itt.emf.mocks.DictionaryServiceMock;
import com.sirma.itt.emf.mocks.NamespaceRegistryMock;
import com.sirma.itt.emf.semantic.persistence.SemanticDbDaoImpl;
import com.sirma.itt.emf.semantic.persistence.SemanticPropertiesReadConverter;
import com.sirma.itt.emf.semantic.persistence.SemanticPropertiesWriteConverter;
import com.sirma.itt.seip.resources.EmfUser;
import com.sirma.itt.seip.security.context.SecurityContext;

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
		ReflectionUtils.setField(this, "namespaceRegistryService", namespaceRegistryMock);
		ReflectionUtils.setField(this, "dictionaryService",
				context.computeIfAbsent("dictionaryService", key -> new DictionaryServiceMock()));
		ReflectionUtils.setField(this, "valueFactory", new ValueFactoryImpl());

		ReflectionUtils.setField(this, "idManager", context.get("idManager"));

		ReflectionUtils.setField(this, "queryBuilder", new QueryBuilderMock(context));

		SemanticPropertiesReadConverter readConverter = new SemanticPropertiesReadConverter();
		ReflectionUtils.setField(readConverter, "namespaceRegistryService", namespaceRegistryMock);

		SemanticPropertiesWriteConverter writeConverter = new SemanticPropertiesWriteConverterMock(context);

		ReflectionUtils.setField(this, "readConverter", readConverter);
		ReflectionUtils.setField(this, "writeConverter", writeConverter);

		SecurityContext securityCtx = mock(SecurityContext.class);
		EmfUser user = new EmfUser();
		user.setId("emf:admin");
		when(securityCtx.getAuthenticated()).thenReturn(user);
		ReflectionUtils.setField(this, "securityContext", securityCtx);
	}
}
