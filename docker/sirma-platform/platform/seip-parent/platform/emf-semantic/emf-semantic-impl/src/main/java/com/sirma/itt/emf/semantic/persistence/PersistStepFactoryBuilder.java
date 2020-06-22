package com.sirma.itt.emf.semantic.persistence;

import javax.inject.Inject;
import javax.inject.Singleton;

import org.eclipse.rdf4j.model.IRI;

import com.sirma.itt.emf.semantic.persistence.PersistStep.PersistStepFactory;
import com.sirma.itt.emf.semantic.persistence.PersistStep.ValueProvider;
import com.sirma.itt.seip.convert.TypeConverter;
import com.sirma.itt.seip.definition.SemanticDefinitionService;
import com.sirma.itt.seip.domain.instance.Instance;

/**
 * Builder that produces {@link PersistStepFactory} instances
 * 
 * @author BBonev
 */
@Singleton
class PersistStepFactoryBuilder {
	@Inject
	private StatementBuilderProvider statementBuilderProvider;

	@Inject
	private SemanticDefinitionService semanticDefinitionService;

	@Inject
	private TypeConverter typeConverter;

	/**
	 * Creates {@link PersistStepFactory} that produces {@link PersistStep} instances based on the provided instance
	 * data.
	 *
	 * @param instanceId
	 *            the instance id to act as subject to the generated statements
	 * @param newData
	 *            the new instance data
	 * @param oldData
	 *            the old instance data if any or <code>null</code> for new instance
	 * @return {@link PersistStepFactory} instance
	 */
	PersistStepFactory build(IRI instanceId, Instance newData, Instance oldData) {
		PersistStepFactory factory = new PersistStepFactory(statementBuilderProvider,
				semanticDefinitionService.getInverseRelationProvider(), semanticDefinitionService, typeConverter);
		factory.setSourceData(instanceId, ValueProvider.instance(newData), ValueProvider.instance(oldData));
		return factory;
	}
}
