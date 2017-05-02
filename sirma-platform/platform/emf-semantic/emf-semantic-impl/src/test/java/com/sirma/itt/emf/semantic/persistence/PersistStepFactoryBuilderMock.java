package com.sirma.itt.emf.semantic.persistence;

import java.util.Map;

import org.openrdf.model.impl.ValueFactoryImpl;

import com.sirma.itt.commons.utils.reflection.ReflectionUtils;
import com.sirma.itt.emf.mocks.NamespaceRegistryMock;
import com.sirma.itt.emf.mocks.SemanticDefinitionServiceMock;
import com.sirma.itt.seip.convert.DefaultTypeConverter;
import com.sirma.itt.seip.convert.TypeConverterImpl;

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

		ReflectionUtils.setField(this, "typeConverter", typeConverter);
		ReflectionUtils.setField(this, "statementBuilderProvider",
				new StatementBuilderProviderImpl(namespaceRegistryMock, new ValueFactoryImpl()));
		ReflectionUtils.setField(this, "semanticDefinitionService", new SemanticDefinitionServiceMock(context));
	}

}
