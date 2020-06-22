package com.sirma.itt.emf.semantic.persistence;

import java.util.Map;

import org.eclipse.rdf4j.model.impl.SimpleValueFactory;

import com.sirma.itt.emf.mocks.NamespaceRegistryMock;
import com.sirma.itt.emf.mocks.SemanticDefinitionServiceMock;
import com.sirma.itt.seip.convert.DefaultTypeConverter;
import com.sirma.itt.seip.convert.TypeConverterImpl;
import com.sirma.itt.seip.util.ReflectionUtils;

/**
 * Public mock of the {@link PersistStepFactoryBuilder}
 *
 * @author BBonev
 */
public class PersistStepFactoryBuilderMock extends PersistStepFactoryBuilder {

	public PersistStepFactoryBuilderMock(Map<String, Object> context) {
		NamespaceRegistryMock namespaceRegistryMock = new NamespaceRegistryMock(context);
		TypeConverterImpl typeConverter = new TypeConverterImpl();
		new DefaultTypeConverter().register(typeConverter);
		new ValueConverter().register(typeConverter);

		ReflectionUtils.setFieldValue(this, "typeConverter", typeConverter);
		ReflectionUtils.setFieldValue(this, "statementBuilderProvider",
				new StatementBuilderProviderImpl(namespaceRegistryMock, SimpleValueFactory.getInstance()));
		ReflectionUtils.setFieldValue(this, "semanticDefinitionService", new SemanticDefinitionServiceMock(context));
	}

}
