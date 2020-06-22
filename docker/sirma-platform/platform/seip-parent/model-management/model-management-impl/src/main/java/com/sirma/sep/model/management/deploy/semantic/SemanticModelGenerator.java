package com.sirma.sep.model.management.deploy.semantic;

import static com.sirma.itt.emf.semantic.persistence.SemanticPersistenceHelper.*;

import java.io.Serializable;
import java.lang.invoke.MethodHandles;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;
import java.util.stream.Collectors;

import javax.inject.Inject;

import org.eclipse.rdf4j.model.IRI;
import org.eclipse.rdf4j.model.Literal;
import org.eclipse.rdf4j.model.Statement;
import org.eclipse.rdf4j.model.ValueFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sirma.itt.seip.util.EqualsHelper;
import com.sirma.itt.semantic.NamespaceRegistryService;
import com.sirma.sep.model.management.Path;
import com.sirma.sep.model.management.deploy.ChangeSetAggregator;
import com.sirma.sep.model.management.deploy.ModelChangeSetExtension;
import com.sirma.sep.model.management.operation.ModelChangeSet;
import com.sirma.sep.model.management.operation.ModelChangeSetInfo;

/**
 * Generate statements for semantic database update. <br>
 * The implementation groups the input changes by predicate and inserts only the last value for each predicate. All
 * other changes to the same predicate are ignored. If the change is for map attribute then for each map key new change
 * is generated and they are processed individually by the same logic described above.
 *
 * @author <a href="mailto:borislav.bonev@sirma.bg">Borislav Bonev</a>
 * @since 20/08/2018
 */
class SemanticModelGenerator {

	private static final Logger LOGGER = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

	@Inject
	private NamespaceRegistryService registryService;

	@Inject
	private ValueFactory valueFactory;

	/**
	 * Parse the given class or property changes and generate statements for addition and removal based on the given changes.
	 * <p>
	 * If a change set is not applicable, i.e the path lacks an attribute node, it is skipped because there is no information for which
	 * predicate to generate statements.
	 *
	 * @param modelChangeSetInfos the changes to parse
	 * @return list of {@link SemanticChange} containing {@link Statement} for insert and remove
	 */
	List<SemanticChange> generateDatabaseChanges(List<ModelChangeSetInfo> modelChangeSetInfos) {
		// groups multiple changes for a single attribute so we can determine the first and the last change for that
		// attribute, we are not interested for all intermediate changes for that predicate
		Map<String, List<ModelChangeSetExtension>> predicateChangeMapping = ChangeSetAggregator.groupChanges(modelChangeSetInfos,
				this::createLiteral);

		List<SemanticChange> resultChanges = new LinkedList<>();
		for (List<ModelChangeSetExtension> changes : predicateChangeMapping.values()) {

			ModelChangeSet firstChange = changes.get(0);
			if (!isChangeSetApplicable(firstChange)) {
				String path = firstChange.getPath().prettyPrint();
				LOGGER.debug("Skipping statement generation for {} ", path);
				continue;
			}

			Object oldValue = firstChange.getOldValue();
			Object newValue = changes.get(changes.size() - 1).getNewValue();
			boolean hasOneChangeOnly = changes.size() == 1;

			// If there are multiple changes, compare the new and the old value and if they are the same, do not deploy
			// that changes. However, there are cases in which, for example, a new field is created and the oldValue is
			// a default value and the new value is also the default value. In such cases changes should be deployed.
			if (!EqualsHelper.nullSafeEquals(oldValue, newValue) || hasOneChangeOnly) {
				SemanticChange semanticChange = new SemanticChange(getOriginalChanges(changes));

				IRI subject = getSubject(firstChange);
				IRI predicate = getPredicate(firstChange);

				// If there is only one change, it is better to not remove the oldValue, because it may cause an
				// inconsistency warning.
				// we trigger value removal in these cases
				// - values are not equal - we definitely need to remove the old value no matter what
				// - value are equal but we have more than one change
				if (!EqualsHelper.nullSafeEquals(oldValue, newValue) || !hasOneChangeOnly) {
					addToModel(subject, predicate, oldValue, semanticChange::toRemove);
				}
				addToModel(subject, predicate, newValue, semanticChange::toAdd);

				resultChanges.add(semanticChange);
			}
		}
		return resultChanges;
	}

	private List<ModelChangeSetInfo> getOriginalChanges(List<ModelChangeSetExtension> changes) {
		return changes.stream().map(ModelChangeSetExtension::getDelegate).collect(Collectors.toList());
	}

	private IRI getSubject(ModelChangeSet change) {
		Path path = change.getPath();
		return registryService.buildUri(path.getValue());
	}

	private boolean isChangeSetApplicable(ModelChangeSet change) {
		// Change set is not applicable if the property path lacks an attribute (the predicate and object)
		return change.getPath().hasNext();
	}

	private IRI getPredicate(ModelChangeSet change) {
		Path path = change.getPath();
		return registryService.buildUri(path.next().getValue());
	}

	private Literal createLiteral(String language, Object value) {
		if (value != null && !value.toString().isEmpty()) {
			return valueFactory.createLiteral(value.toString(), language);
		}
		return null;
	}

	private void addToModel(IRI subject, IRI predicate, Object value, Consumer<Statement> consumer) {
		if (value == null) {
			// skip null values as they are invalid statement elements
			return;
		}
		consumer.accept(createStatement(subject, predicate, (Serializable) value, registryService, valueFactory));
	}

}
