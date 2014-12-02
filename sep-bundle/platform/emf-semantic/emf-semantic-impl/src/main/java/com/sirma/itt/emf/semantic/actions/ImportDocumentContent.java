package com.sirma.itt.emf.semantic.actions;

import static com.sirma.itt.emf.executors.ExecutableOperationProperties.CTX_TARGET;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.inject.Instance;
import javax.inject.Inject;

import org.json.JSONObject;
import org.openrdf.model.Literal;
import org.openrdf.model.URI;
import org.openrdf.model.ValueFactory;
import org.openrdf.repository.RepositoryConnection;
import org.openrdf.repository.RepositoryException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sirma.itt.emf.configuration.Config;
import com.sirma.itt.emf.executors.OperationResponse;
import com.sirma.itt.emf.instance.actions.BaseInstanceExecutor;
import com.sirma.itt.emf.instance.model.InstanceReference;
import com.sirma.itt.emf.plugin.Extension;
import com.sirma.itt.emf.properties.DefaultProperties;
import com.sirma.itt.emf.scheduler.SchedulerContext;
import com.sirma.itt.emf.util.JsonUtil;
import com.sirma.itt.semantic.NamespaceRegistryService;
import com.sirma.itt.semantic.configuration.SemanticConfigurationProperties;
import com.sirma.itt.semantic.model.vocabulary.EMF;

/**
 * Operation used for synchronizing a document content with semantic DB. The operation remove the
 * old content field for the target instance and adds the new value.
 * 
 * @author BBonev
 */
@ApplicationScoped
@Extension(target = ImportDocumentContent.TARGET_NAME, order = 35.5)
public class ImportDocumentContent extends BaseInstanceExecutor {

	/** The Constant LOGGER. */
	private static final Logger LOGGER = LoggerFactory.getLogger(ImportDocumentContent.class);

	/** The connection. */
	@Inject
	private Instance<RepositoryConnection> connection;

	/** The namespace registry service. */
	@Inject
	private NamespaceRegistryService namespaceRegistryService;

	/** The value factory. */
	@Inject
	private ValueFactory valueFactory;

	/** The context name. */
	@Inject
	@Config(name = SemanticConfigurationProperties.SEMANTIC_DB_CONTEXT_NAME, defaultValue = "http://ittruse.ittbg.com/data/enterpriseManagementFramework")
	private String contextName;

	/** The context. */
	private URI semanticContext;

	/**
	 * {@inheritDoc}
	 */
	@Override
	public String getOperation() {
		return "addContent";
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public SchedulerContext parseRequest(JSONObject data) {
		SchedulerContext context = super.parseRequest(data);

		String content = JsonUtil.getStringValue(data, DefaultProperties.CONTENT);
		context.put(DefaultProperties.CONTENT, content);

		return context;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public OperationResponse execute(SchedulerContext context) {

		InstanceReference reference = context.getIfSameType(CTX_TARGET, InstanceReference.class);
		String fullUri = namespaceRegistryService.buildFullUri(reference.getIdentifier());
		URI subject = valueFactory.createURI(fullUri);
		RepositoryConnection repositoryConnection = connection.get();

		try {
			repositoryConnection.remove(subject, EMF.CONTENT, null);
			String content = context.getIfSameType(DefaultProperties.CONTENT, String.class);
			Literal literal = valueFactory.createLiteral(content);
			repositoryConnection.add(subject, EMF.CONTENT, literal, getContext());
		} catch (RepositoryException e) {
			LOGGER.warn("Failed to change content for instance " + reference.getIdentifier(), e);
		}

		return null;
	}

	@Override
	public boolean rollback(SchedulerContext data) {
		// nothing to do
		return true;
	}

	@Override
	public boolean couldBeAsynchronous(SchedulerContext data) {
		return true;
	}

	/**
	 * Gets the context URI.
	 * 
	 * @return the context URI
	 */
	private URI getContext() {
		if (semanticContext == null) {
			semanticContext = valueFactory.createURI(contextName);
		}
		return semanticContext;
	}

}
