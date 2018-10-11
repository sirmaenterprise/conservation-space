package com.sirma.itt.seip.instance.headers.batch;

import static com.sirma.itt.seip.domain.instance.DefaultProperties.HEADER_LABEL;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import javax.batch.api.chunk.AbstractItemWriter;
import javax.inject.Inject;
import javax.inject.Named;

import org.eclipse.rdf4j.model.IRI;
import org.eclipse.rdf4j.model.Literal;
import org.eclipse.rdf4j.model.Model;
import org.eclipse.rdf4j.model.Statement;
import org.eclipse.rdf4j.model.ValueFactory;
import org.eclipse.rdf4j.model.impl.LinkedHashModel;
import org.eclipse.rdf4j.repository.RepositoryConnection;

import com.sirma.itt.emf.semantic.persistence.SemanticPersistenceHelper;
import com.sirma.itt.seip.instance.dao.InstanceService;
import com.sirma.itt.semantic.NamespaceRegistryService;
import com.sirma.itt.semantic.model.vocabulary.EMF;

/**
 * Batch {@link javax.batch.api.chunk.ItemWriter} that stores the generated instance headers for the pass batchlet.
 *
 * @author <a href="mailto:borislav.bonev@sirma.bg">Borislav Bonev</a>
 * @since 27/11/2017
 */
@Named
public class InstanceHeaderItemWriter extends AbstractItemWriter {

	@Inject
	private RepositoryConnection repositoryConnection;

	@Inject
	private NamespaceRegistryService namespaceRegistryService;

	@Inject
	private ValueFactory valueFactory;

	@Inject
	private InstanceService instanceService;

	@Override
	public void writeItems(List<Object> items) throws Exception {
		List<Serializable> affectedInstances = new ArrayList<>(items.size());
		Model model = new LinkedHashModel();
		for (Object item : items) {
			GeneratedHeaderData headerItem = (GeneratedHeaderData) item;

			IRI instanceId = namespaceRegistryService.buildUri(headerItem.getInstanceId());
			Literal header = valueFactory.createLiteral(headerItem.getHeader());
			model.add(instanceId, EMF.ALT_TITLE, header);

			affectedInstances.add(headerItem.getInstanceId());
		}
		for (Statement statement : model) {
			repositoryConnection.remove(statement.getSubject(), statement.getPredicate(), null);
		}
		SemanticPersistenceHelper.saveModel(repositoryConnection, model, namespaceRegistryService.getDataGraph());

		// removed the affected instances from the cache so that they can be loaded with fresh values next time
		instanceService.touchInstance(affectedInstances);
	}
}
