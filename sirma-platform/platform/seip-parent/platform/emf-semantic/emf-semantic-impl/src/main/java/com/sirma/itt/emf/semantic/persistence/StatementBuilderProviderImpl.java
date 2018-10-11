package com.sirma.itt.emf.semantic.persistence;

import java.io.Serializable;

import javax.inject.Inject;
import javax.inject.Singleton;

import org.eclipse.rdf4j.model.Statement;
import org.eclipse.rdf4j.model.ValueFactory;

import com.sirma.itt.semantic.NamespaceRegistryService;

/**
 * Default implementation for the {@link StatementBuilderProvider}
 *
 * @author BBonev
 */
@Singleton
public class StatementBuilderProviderImpl implements StatementBuilderProvider {

	private final StatementBuilder literalBuilder;
	private final StatementBuilder relationBuilder;

	/**
	 * Instantiates a new statement builder provider impl.
	 *
	 * @param namespaceRegistryService
	 *            the namespace registry service
	 * @param valueFactory
	 *            the value factory
	 */
	@Inject
	public StatementBuilderProviderImpl(NamespaceRegistryService namespaceRegistryService, ValueFactory valueFactory) {
		literalBuilder = new LiteralBuilder(namespaceRegistryService, valueFactory);
		relationBuilder = new RelationBuilder(namespaceRegistryService, valueFactory);
	}

	@Override
	public StatementBuilder literalStatementBuilder() {
		return literalBuilder;
	}

	@Override
	public StatementBuilder relationStatementBuilder() {
		return relationBuilder;
	}

	/**
	 * {@link StatementBuilder} that produces literal statements
	 *
	 * @author BBonev
	 */
	private static class LiteralBuilder implements StatementBuilder {

		private final NamespaceRegistryService namespaceService;
		private final ValueFactory factory;

		private LiteralBuilder(NamespaceRegistryService namespaceService, ValueFactory factory) {
			this.namespaceService = namespaceService;
			this.factory = factory;
		}

		@Override
		public Statement build(Object subject, Object predicate, Serializable value) {
			return SemanticPersistenceHelper.createLiteralStatement(subject, predicate, value, namespaceService,
					factory);
		}
	}

	/**
	 * {@link StatementBuilder} that produces relation statements if possible
	 *
	 * @author BBonev
	 */
	private static class RelationBuilder implements StatementBuilder {

		private final ValueFactory factory;
		private final NamespaceRegistryService namespaceService;

		private RelationBuilder(NamespaceRegistryService namespaceService, ValueFactory factory) {
			this.namespaceService = namespaceService;
			this.factory = factory;
		}

		@Override
		public Statement build(Object subject, Object predicate, Serializable value) {
			return SemanticPersistenceHelper.createStatement(subject, predicate, value, namespaceService, factory);
		}
	}
}
