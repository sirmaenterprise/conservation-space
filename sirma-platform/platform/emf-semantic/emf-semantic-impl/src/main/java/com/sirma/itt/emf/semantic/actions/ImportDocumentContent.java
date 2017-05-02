package com.sirma.itt.emf.semantic.actions;

import static com.sirma.itt.seip.instance.actions.ExecutableOperationProperties.CTX_TARGET;

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

import com.sirma.itt.seip.domain.instance.DefaultProperties;
import com.sirma.itt.seip.domain.instance.InstanceReference;
import com.sirma.itt.seip.instance.actions.BaseInstanceExecutor;
import com.sirma.itt.seip.instance.actions.ExecutableOperation;
import com.sirma.itt.seip.instance.actions.OperationContext;
import com.sirma.itt.seip.instance.actions.OperationResponse;
import com.sirma.itt.seip.json.JsonUtil;
import com.sirma.itt.seip.plugin.Extension;
import com.sirma.itt.semantic.NamespaceRegistryService;
import com.sirma.itt.semantic.model.vocabulary.EMF;

/**
 * Operation used for synchronizing a document content with semantic DB. The operation remove the old content field for
 * the target instance and adds the new value.
 *
 * @author BBonev
 */
@ApplicationScoped
@Extension(target = ExecutableOperation.TARGET_NAME, order = 35.5)
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
	public OperationContext parseRequest(JSONObject data) {
		OperationContext context = super.parseRequest(data);

		String content = JsonUtil.getStringValue(data, DefaultProperties.CONTENT);
		context.put(DefaultProperties.CONTENT, content);

		return context;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public OperationResponse execute(OperationContext context) {

		InstanceReference reference = context.getIfSameType(CTX_TARGET, InstanceReference.class);
		URI subject = namespaceRegistryService.buildUri(reference.getIdentifier());
		RepositoryConnection repositoryConnection = connection.get();

		try {
			repositoryConnection.remove(subject, EMF.CONTENT, null);
			String content = context.getIfSameType(DefaultProperties.CONTENT, String.class);
			Literal literal = valueFactory.createLiteral(content);
			repositoryConnection.add(subject, EMF.CONTENT, literal, namespaceRegistryService.getDataGraph());
		} catch (RepositoryException e) {
			LOGGER.warn("Failed to change content for instance " + reference.getIdentifier(), e);
		}

		return null;
	}

	@Override
	public boolean rollback(OperationContext data) {
		// nothing to do
		return true;
	}

	@Override
	public boolean couldBeAsynchronous(OperationContext data) {
		return true;
	}

}
