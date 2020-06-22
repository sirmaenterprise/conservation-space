package com.sirma.itt.emf.semantic.persistence;

import static com.sirma.itt.emf.semantic.queries.SPARQLQueryHelper.CONTEXT_PREDICATE;
import static com.sirma.itt.emf.semantic.queries.SPARQLQueryHelper.IS_NOT_DELETED;
import static com.sirma.itt.emf.semantic.queries.SPARQLQueryHelper.LINE_SEPARATOR;
import static com.sirma.itt.emf.semantic.queries.SPARQLQueryHelper.OBJECT;
import static com.sirma.itt.emf.semantic.queries.SPARQLQueryHelper.OBJECT_VARIABLE;
import static com.sirma.itt.emf.semantic.queries.SPARQLQueryHelper.RELATIONS_PREDICATE;
import static com.sirma.itt.emf.semantic.queries.SPARQLQueryHelper.STATEMENT_SEPARATOR;
import static com.sirma.itt.emf.semantic.queries.SPARQLQueryHelper.VARIABLE;
import static com.sirma.itt.seip.collections.CollectionUtils.isEmpty;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.Set;
import java.util.function.BiPredicate;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.stream.Collectors;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import org.eclipse.rdf4j.model.IRI;
import org.eclipse.rdf4j.model.Model;
import org.eclipse.rdf4j.model.Resource;
import org.eclipse.rdf4j.model.Value;
import org.eclipse.rdf4j.model.ValueFactory;
import org.eclipse.rdf4j.model.impl.LinkedHashModel;
import org.eclipse.rdf4j.model.vocabulary.RDF;
import org.eclipse.rdf4j.query.BindingSet;
import org.eclipse.rdf4j.query.BooleanQuery;
import org.eclipse.rdf4j.query.MalformedQueryException;
import org.eclipse.rdf4j.query.QueryEvaluationException;
import org.eclipse.rdf4j.query.QueryLanguage;
import org.eclipse.rdf4j.query.TupleQuery;
import org.eclipse.rdf4j.query.TupleQueryResult;
import org.eclipse.rdf4j.query.Update;
import org.eclipse.rdf4j.query.UpdateExecutionException;
import org.eclipse.rdf4j.repository.RepositoryConnection;
import org.eclipse.rdf4j.repository.RepositoryException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.apache.commons.lang3.StringUtils;
import com.sirma.itt.emf.extension.PersistentProperties.PersistentPropertyKeys;
import com.sirma.itt.emf.semantic.exception.SemanticPersistenceException;
import com.sirma.itt.emf.semantic.queries.SPARQLQueryHelper;
import com.sirma.itt.emf.semantic.search.TupleQueryResultIterator;
import com.sirma.itt.seip.Pair;
import com.sirma.itt.seip.collections.CollectionUtils;
import com.sirma.itt.seip.configuration.Options;
import com.sirma.itt.seip.convert.TypeConversionException;
import com.sirma.itt.seip.db.DatabaseIdManager;
import com.sirma.itt.seip.db.SemanticDb;
import com.sirma.itt.seip.definition.SemanticDefinitionService;
import com.sirma.itt.seip.domain.ObjectTypes;
import com.sirma.itt.seip.domain.definition.DataTypeDefinition;
import com.sirma.itt.seip.domain.definition.DefinitionModel;
import com.sirma.itt.seip.domain.instance.DefaultProperties;
import com.sirma.itt.seip.domain.instance.Instance;
import com.sirma.itt.seip.domain.instance.InstanceReference;
import com.sirma.itt.seip.domain.instance.PropertyInstance;
import com.sirma.itt.seip.domain.search.SearchArguments;
import com.sirma.itt.seip.domain.search.SearchDialects;
import com.sirma.itt.seip.domain.util.InstanceUtil;
import com.sirma.itt.seip.event.EventService;
import com.sirma.itt.seip.instance.relation.AbstractLinkService;
import com.sirma.itt.seip.instance.relation.LinkAddedEvent;
import com.sirma.itt.seip.instance.relation.LinkInstance;
import com.sirma.itt.seip.instance.relation.LinkReference;
import com.sirma.itt.seip.instance.relation.LinkRemovedEvent;
import com.sirma.itt.seip.instance.relation.LinkSearchArguments;
import com.sirma.itt.seip.instance.state.PrimaryStates;
import com.sirma.itt.seip.instance.state.StateService;
import com.sirma.itt.seip.search.NamedQueries;
import com.sirma.itt.seip.search.SearchService;
import com.sirma.itt.seip.time.TimeTracker;
import com.sirma.itt.seip.util.DigestUtils;
import com.sirma.itt.seip.util.ReflectionUtils;
import com.sirma.itt.semantic.NamespaceRegistryService;
import com.sirma.itt.semantic.model.vocabulary.EMF;
import com.sirma.itt.semantic.queries.QueryBuilder;

/**
 * Semantic implementation for the {@link com.sirma.itt.seip.instance.relation.LinkService}
 *
 * @author kirq4e
 * @author Valeri Tishev
 * @author BBonev
 */
@ApplicationScoped
@SemanticDb
@SuppressWarnings("squid:ClassCyclomaticComplexity")
public class SemanticLinkServiceImpl extends AbstractLinkService {

	private static final String CONTEXT_VARIABLE = VARIABLE + CONTEXT_PREDICATE;
	private static final String CONTEXT_VARIABLE_BINDING = "( %s as " + CONTEXT_VARIABLE + ")";
	private static final String RELATION_TYPE = "relationType";
	private static final String DESTINATION = "destination";
	private static final String SOURCE = "source";

	private static final Logger LOGGER = LoggerFactory.getLogger(SemanticLinkServiceImpl.class);

	private final Map<String, Class<? extends Instance>> typeMapping = new HashMap<>(32);
	private static final Set<String> NO_LINKS = Collections.<String> emptySet();

	private static final long serialVersionUID = 8909476744495202331L;

	@Inject
	private NamespaceRegistryService namespaceRegistryService;

	@Inject
	private ValueFactory valueFactory;

	@Inject
	private RepositoryConnection repositoryConnection;

	@Inject
	private QueryBuilder queryBuilder;

	@Inject
	private SemanticPropertiesReadConverter readConverter;

	@Inject
	private StateService stateService;

	@Inject
	private SemanticPropertiesWriteConverter writeConverter;

	@Inject
	private DatabaseIdManager idManager;

	@Inject
	private SearchService searchService;

	@Inject
	private SemanticDefinitionService semanticDefinitionService;

	@Inject
	private EventService eventService;

	@Override
	public Pair<Serializable, Serializable> link(Instance from, Instance to, String mainLinkId, String reverseLinkId,
			Map<String, Serializable> properties) {

		// the method is overridden because the linkInternal works better with instances when you have them
		return linkInternal(from, to, mainLinkId, reverseLinkId, properties);
	}

	@SuppressWarnings("squid:MethodCyclomaticComplexity")
	@Override
	protected Pair<Serializable, Serializable> linkInternal(Object from, Object to, String mainLinkId,
			String reverseLinkId, Map<String, Serializable> properties) {
		if (Options.DO_NOT_PERSIST_IN_SD.isEnabled() || from == null || to == null) {
			LOGGER.warn("Tried to create link with a missing end point from=" + (from == null ? "null" : "not_null")
					+ ", to=" + (to == null ? "null" : "not_null"));
			return Pair.nullPair();
		}

		if (mainLinkId == null && reverseLinkId == null) {
			LOGGER.warn("Did not create any relation because the relation ids were null");
			return Pair.nullPair();
		}

		TimeTracker timeTracker = new TimeTracker();
		if (LOGGER.isDebugEnabled()) {
			timeTracker.begin();
			LOGGER.debug(
					"\n=== SemanticLinkService.link ============================\n" + "From IRI = {}; class = {}\n"
							+ "To IRI = {}; class = {}\nmainLinkId = {}\nreverseLinkId = {}\n" + "properties = {}\n"
							+ "=========================================================",
					getInstanceResource(from), getType(from), getInstanceResource(to), getType(to), mainLinkId,
					reverseLinkId, properties);
		}
		Pair<Serializable, Serializable> ids = new Pair<>(null, null);

		Resource subject = getInstanceResource(from);

		Resource object = getInstanceResource(to);

		Model model = new LinkedHashModel(32);
		Model removeModel = new LinkedHashModel();

		Resource first = null;
		if (mainLinkId != null) {
			// create and add main link to model
			String iri = buildId(subject, object, mainLinkId);
			ids.setFirst(iri);
			String primaryId = buildProperLinkId(mainLinkId);

			first = createRelation(model, removeModel, iri, primaryId, properties, subject, object);

			addInstanceTypeToModel(from, model);
			addInstanceTypeToModel(to, model);

			// create simple relation of the same type
			// does not create the reverse - leave semantic to handle it
			model.add(subject, namespaceRegistryService.buildUri(primaryId), object);

			eventService.fire(new LinkAddedEvent(buildLinkReferenceForEvent(from, to, mainLinkId)));
		}

		String invertId = reverseLinkId;
		if (invertId == null && mainLinkId != null) {
			// force inverse relation creation as it will be created by the semantic anyway
			invertId = semanticDefinitionService.getInverseRelationProvider().inverseOf(mainLinkId);
		}

		if (invertId != null) {
			// build reverse id where the object is at the subject position
			String iri = buildId(object, subject, invertId);
			ids.setSecond(iri);
			Resource second = createRelation(model, removeModel, iri, invertId, properties, object, subject);
			if (first != null && !ids.getFirst().equals(iri)) {
				model.add(first, EMF.INVERSE_RELATION, second);
			}
			// explicitly create the inverse simple relation to show in the instance properties
			model.add(object, namespaceRegistryService.buildUri(invertId), subject);

			eventService.fire(new LinkAddedEvent(buildLinkReferenceForEvent(to, from, invertId)));
		}

		try {
			IRI context = getContext();
			SemanticPersistenceHelper.removeModel(repositoryConnection, removeModel);
			SemanticPersistenceHelper.saveModel(repositoryConnection, model, context);

			LOGGER.trace("Added model [{}] to semantic repository.", model);
		} finally {
			LOGGER.debug("Semantic db link creation took {} s for {}", timeTracker.stopInSeconds(), ids);
		}

		return ids;
	}

	private static LinkReference buildLinkReferenceForEvent(Object from, Object to, String linkId) {
		LinkReference linkReference = new LinkReference();
		linkReference.setFrom(getReference(from));
		linkReference.setTo(getReference(to));
		linkReference.setIdentifier(linkId);
		return linkReference;
	}

	/**
	 * Adds the instance type to model. The method inserts the semantic class of non created instances when creating
	 * relations to them so the relation search could be performed after the insert.
	 *
	 * @param src
	 *            the src
	 * @param model
	 *            the model
	 */
	private void addInstanceTypeToModel(Object src, Model model) {
		String type = null;
		Serializable id = null;
		String typeName = null;
		if (src instanceof InstanceReference) {
			InstanceReference reference = (InstanceReference) src;
			id = reference.getId();
			typeName = reference.getReferenceType().getName();
			type = reference.getType() != null ? reference.getType().getId().toString()
					: reference.getReferenceType().getFirstUri();
		} else if (src instanceof Instance) {
			// first we check if there is specific type for the given instance if so use it
			// otherwise use the first defined semantic class
			Instance instance = (Instance) src;
			id = instance.getId();
			type = instance.type().getId().toString();
			// this is used for class instantiation later
			// this should be changed to use semantic properties writer
			typeName = instance.type().getProperty(EMF.DEFINITION_ID.getLocalName());
		}
		// add type statement if object is not persisted, yet
		if (type != null && id != null && !InstanceUtil.isIdPersisted(id)) {
			Resource resource = getInstanceResource(src);
			model.add(resource, RDF.TYPE, namespaceRegistryService.buildUri(type));
			if (typeName != null) {
				model.add(resource, EMF.INSTANCE_TYPE, valueFactory.createLiteral(typeName));
			}
			LOGGER.trace("Adding semantic class [{}] for IRI [{}] ", type, id);
		}
	}

	/**
	 * Builds unique link id.
	 *
	 * @param subject
	 *            the subject
	 * @param object
	 *            the object
	 * @param mainLinkId
	 *            the main link id
	 * @return the string
	 */
	private String buildId(Resource subject, Resource object, String mainLinkId) {
		if (Options.GENERATE_RANDOM_LINK_ID.isEnabled()) {
			return idManager.generateId().toString();
		}
		String digest = DigestUtils.calculateDigest("" + subject + object + expandLinkIdentifier(mainLinkId));
		return EMF.PREFIX + ":" + digest;
	}

	/**
	 * Gets the type.
	 *
	 * @param object
	 *            the object
	 * @return the type
	 */
	private static Object getType(Object object) {
		if (object == null) {
			return null;
		}
		if (object instanceof InstanceReference) {
			InstanceReference from = (InstanceReference) object;
			return from.getType();
		} else if (object instanceof Instance) {
			return ((Instance) object).type();
		}
		// this method is used only for logging
		// if the use is changed check the default return value if OK
		return object.getClass().getSimpleName();
	}

	/**
	 * Creates the relation.
	 *
	 * @param model
	 *            the model
	 * @param removeModel
	 *            removed model
	 * @param uriId
	 *            the IRI id
	 * @param linkId
	 *            the link id
	 * @param properties
	 *            the properties
	 * @param subject
	 *            the source of the relation
	 * @param object
	 *            the destination of the relation
	 * @return the resource
	 */
	private Resource createRelation(Model model, Model removeModel, String uriId, String linkId,
			Map<String, Serializable> properties, Resource subject, Resource object) {
		LinkReference linkReference = new LinkReference();
		linkReference.setType(semanticDefinitionService.getClassInstance(EMF.RELATION.toString()).type());
		linkReference.setId(uriId);

		if (properties != null) {
			linkReference.addAllProperties(properties);
		}
		linkReference.add(EMF.RELATION_TYPE.getLocalName(), linkId);
		linkReference.add(EMF.IS_ACTIVE.getLocalName(), Boolean.TRUE);
		linkReference.add(EMF.SOURCE.getLocalName(), subject);
		linkReference.add(EMF.DESTINATION.getLocalName(), object);

		Resource resource = writeConverter.buildModelForInstance(linkReference, null, model, removeModel);

		// ensure removal of the old value
		removeModel.add(resource, EMF.IS_ACTIVE, valueFactory.createLiteral(Boolean.FALSE));
		return resource;
	}

	/**
	 * Builds the proper link id.
	 *
	 * @param linkId
	 *            the link id
	 * @return the string
	 */
	private String buildProperLinkId(String linkId) {
		String link = linkId;
		if (!linkId.startsWith("http")) {
			if (linkId.contains(":")) {
				link = namespaceRegistryService.buildFullUri(linkId);
			} else {
				link = EMF.NAMESPACE + linkId;
			}
		}
		return link;
	}

	/**
	 * Gets the instance resource.
	 *
	 * @param object
	 *            the object
	 * @return the instance resource
	 */
	private Resource getInstanceResource(Object object) {
		if (object == null) {
			return null;
		}
		String iri = null;
		if (object instanceof Instance) {
			Instance instance = (Instance) object;
			if (instance.getId() instanceof Long || instance.getId() == null) {
				iri = (String) instance.get(PersistentPropertyKeys.URI.getKey());
			} else {
				iri = instance.getId().toString();
			}
		} else if (object instanceof InstanceReference) {
			iri = ((InstanceReference) object).getId();
		}
		if (iri == null) {
			LOGGER.error("Invalid instance resource provided: {}", object);
			return null;
		}
		return valueFactory.createURI(namespaceRegistryService.buildFullUri(iri));
	}

	private static InstanceReference getReference(Object object) {
		if (object == null) {
			return null;
		}
		if (object instanceof InstanceReference) {
			return (InstanceReference) object;
		} else if (object instanceof Instance) {
			return ((Instance) object).toReference();
		}
		LOGGER.warn("Not supported link target: {}", object);
		return null;
	}

	/**
	 * Gets the context IRI.
	 *
	 * @return the context IRI
	 */
	private IRI getContext() {
		return namespaceRegistryService.getDataGraph();
	}

	@Override
	public List<LinkReference> getLinks(InstanceReference from, Set<String> linkIds) {
		return getLinkReferencesInternal(from, null, linkIds);
	}

	@Override
	public List<LinkReference> getLinksTo(InstanceReference to) {
		return getLinkReferencesInternal(null, to, NO_LINKS);
	}

	/**
	 * Gets the link references internal.
	 *
	 * @param from
	 *            the from
	 * @param to
	 *            the to
	 * @param linkIds
	 *            the link ids
	 * @return the link references internal
	 */
	private List<LinkReference> getLinkReferencesInternal(Object from, Object to, Collection<String> linkIds) {
		TimeTracker tracker = new TimeTracker().begin();
		InstanceReference fromRef = null;
		InstanceReference toRef = null;
		double executionTime = -1;
		try {
			Map<String, Serializable> bindings = CollectionUtils.createHashMap(2);
			bindings.put(SOURCE, getInstanceResource(from));
			if (from instanceof InstanceReference) {
				fromRef = (InstanceReference) from;
			} else {
				fromRef = typeConverter.convert(InstanceReference.class, from);
			}
			bindings.put(DESTINATION, getInstanceResource(to));
			if (to instanceof InstanceReference) {
				toRef = (InstanceReference) to;
			} else {
				toRef = typeConverter.convert(InstanceReference.class, to);
			}

			TupleQuery preparedTupleQuery = prepareSparqlQuery(bindings, linkIds, repositoryConnection, true);

			LOGGER.trace("Executing query for links: {}\n{}", linkIds, preparedTupleQuery);

			tracker.begin();
			TupleQueryResult result = preparedTupleQuery.evaluate();
			executionTime = tracker.stopInSeconds();

			return buildResultReferences(repositoryConnection, result, fromRef, toRef, null, false);
		} catch (RepositoryException | QueryEvaluationException e) {
			throw new SemanticPersistenceException(e);
		} finally {
			String links = linkIds != null && !linkIds.isEmpty() ? " with links " + linkIds : "";
			LOGGER.debug("Semantic Db link seach {} {} {} took {} s. The query only took {} s",
					fromRef == null ? "" : " from " + fromRef.getReferenceType().getName() + "=" + fromRef.getId(),
					toRef == null ? "" : " to " + toRef.getReferenceType().getName() + "=" + toRef.getId(), links,
					tracker.stopInSeconds(), executionTime);
		}
	}

	/**
	 * Builds the result references.
	 *
	 * @param connection
	 *            the connection
	 * @param result
	 *            the result
	 * @param fromRef
	 *            the from ref
	 * @param toRef
	 *            the to ref
	 * @param linkId
	 *            the link id
	 * @param loadProperties
	 *            the load properties
	 * @return the list
	 */
	private List<LinkReference> buildResultReferences(RepositoryConnection connection, TupleQueryResult result,
			InstanceReference fromRef, InstanceReference toRef, String linkId, boolean loadProperties) {

		Collection<BiPredicate<LinkReference, BindingSet>> operations = new ArrayList<>();
		operations.add((link, row) -> readRelationEnd(row, fromRef, "subjectType", SOURCE, link::setFrom));
		operations.add((link, row) -> readRelationEnd(row, toRef, "destType", DESTINATION, link::setTo));
		operations.add(this::readRelationDbId);
		operations.add((link, row) -> readRelationId(linkId, link, row));
		operations.add(this::readCreatedByProperty);

		try (TupleQueryResultIterator iterator = new TupleQueryResultIterator(result)) {
			List<LinkReference> linkedLinkReferences = iterator
					.stream(false)
						.peek(this::logRow)
						.map(row -> readResultRow(operations, row))
						.filter(Objects::nonNull)
						.collect(Collectors.toCollection(LinkedList::new));

			if (loadProperties || Options.LOAD_LINK_PROPERTIES.isEnabled()) {
				fetchLinkReferenceProperties(connection, linkedLinkReferences);
			}

			return removeDuplicates(linkedLinkReferences);
		}
	}

	private static LinkReference readResultRow(Collection<BiPredicate<LinkReference, BindingSet>> operations,
			BindingSet row) {
		LinkReference link = new LinkReference();
		for (BiPredicate<LinkReference, BindingSet> operation : operations) {
			if (!operation.test(link, row)) {
				return null;
			}
		}
		return link;
	}

	private void logRow(BindingSet row) {
		if (LOGGER.isTraceEnabled()) {
			trace(bindingSetToString(row));
		}
	}

	private boolean readRelationEnd(BindingSet row, InstanceReference currentEnd, String instanceType,
			String instanceId, Consumer<InstanceReference> resultConsumer) {
		InstanceReference ref = buildReference(row, instanceType, instanceId, currentEnd);
		if (ref == null) {
			LOGGER.warn("Invalid relation {} has been returned. Ignoring result.", instanceId);
			return false;
		}
		resultConsumer.accept(ref);
		return true;
	}

	@SuppressWarnings("squid:UnusedPrivateMethod")
	private boolean readCreatedByProperty(LinkReference link, BindingSet row) {
		Value value = row.getValue("createdBy");
		if (value != null) {
			Serializable convertValue = ValueConverter.convertValue(value);
			String state = stateService.getState(PrimaryStates.OPENED, ObjectTypes.LINK_REFERENCE);
			link.add(DefaultProperties.STATUS, state);
			link.add(DefaultProperties.CREATED_BY, namespaceRegistryService.getShortUri(convertValue.toString()));
		}
		return true;
	}

	@SuppressWarnings("squid:UnusedPrivateMethod")
	private boolean readRelationId(String linkId, LinkReference link, BindingSet row) {
		Value value = row.getValue(RELATION_TYPE);
		if (value instanceof IRI) {
			link.setIdentifier(namespaceRegistryService.getShortUri((IRI) value));
		} else if (value == null) {
			if (linkId != null) {
				link.setIdentifier(linkId);
			} else {
				LOGGER.warn("Missing relation type from semantic query.");
			}
		} else {
			LOGGER.warn("Invalid relation type was returned {} but extected IRI", value);
			return false;
		}
		return true;
	}

	@SuppressWarnings("squid:UnusedPrivateMethod")
	private boolean readRelationDbId(LinkReference link, BindingSet row) {
		Value value = row.getValue("relation");
		// when building simple references there is no ID to set
		if (value instanceof IRI) {
			link.setId(namespaceRegistryService.getShortUri((IRI) value));
		} else {
			if (value != null) {
				LOGGER.warn("Invalid relation id was returned {} but expected IRI", value);
			} // if null it's probably simple link
		}
		return true;
	}

	/**
	 * Removes the duplicates.
	 *
	 * @param linkedLinkReferences
	 *            the linked link references
	 * @return the list
	 */
	private static List<LinkReference> removeDuplicates(List<LinkReference> linkedLinkReferences) {
		if (linkedLinkReferences.size() <= 1) {
			return linkedLinkReferences;
		}
		Map<String, List<LinkReference>> mapping = CollectionUtils.createLinkedHashMap(linkedLinkReferences.size());
		for (LinkReference linkReference : linkedLinkReferences) {
			StringBuilder id = new StringBuilder();
			InstanceReference from = linkReference.getFrom();
			InstanceReference to = linkReference.getTo();
			id.append(from.getId()).append(linkReference.getIdentifier()).append(to.getId());
			CollectionUtils.addValueToMap(mapping, id.toString(), linkReference);
		}
		List<LinkReference> result = new ArrayList<>(mapping.size());
		for (Entry<String, List<LinkReference>> entry : mapping.entrySet()) {
			filterDuplicates(result, entry.getValue());
		}
		return result;
	}

	private static void filterDuplicates(List<LinkReference> result, List<LinkReference> toFilter) {
		LinkReference toAdd = null;
		boolean addedComplex = false;
		for (LinkReference linkReference : toFilter) {
			if (toAdd == null && linkReference.getId() == null) {
				toAdd = linkReference;
			}
			if (linkReference.getId() != null) {
				result.add(linkReference);
				addedComplex = true;
			}
		}
		if (!addedComplex && toAdd != null) {
			result.add(toAdd);
		}
	}

	/**
	 * Fetch the link reference properties from the semantic repository for all links.
	 *
	 * @param connection
	 *            the connection
	 * @param linkReferences
	 *            the link references
	 */
	@SuppressWarnings("squid:MethodCyclomaticComplexity")
	private void fetchLinkReferenceProperties(RepositoryConnection connection,
			Collection<LinkReference> linkReferences) {

		TimeTracker tracker = TimeTracker.createAndStart();

		Map<Serializable, LinkReference> mapping = linkReferences.stream().filter(ref -> ref.getId() != null).collect(
				Collectors.toMap(LinkReference::getId, Function.identity()));

		if (mapping.isEmpty()) {
			// no complex relations to load properties
			return;
		}

		// fix the query execution to handle queries bigger than 1024 items for loading

		List<Pair<String, Object>> params = new ArrayList<>(1);
		params.add(new Pair<>(NamedQueries.Params.URIS, mapping.keySet()));
		String query = queryBuilder.buildQueryByName(NamedQueries.LOAD_PROPERTIES, params);

		TupleQueryResult result = null;
		try {
			TupleQuery tupleQuery = SPARQLQueryHelper.prepareTupleQuery(connection, query, Collections.emptyMap(),
					false);
			result = tupleQuery.evaluate();

			if (!result.hasNext()) {
				LOGGER.debug("No properties found");
				return;
			}

			Map<String, Map<Value, Set<Value>>> resultModel = readConverter.buildQueryResultModel(result);
			String state = stateService.getState(PrimaryStates.OPENED, ObjectTypes.LINK_REFERENCE);

			for (Entry<String, Map<Value, Set<Value>>> entry : resultModel.entrySet()) {
				LinkReference reference = mapping.get(entry.getKey());
				DefinitionModel definition = definitionService.getInstanceDefinition(reference);

				Map<String, Set<Value>> convertedKeys = readConverter.convertPropertiesNames(entry.getValue());
				readConverter.convertPropertiesFromSemanticToInternalModel(definition, convertedKeys,
						reference.getProperties());

				reference.addIfNotPresent(DefaultProperties.STATUS, state);
			}

		} catch (QueryEvaluationException e) {
			throw new SemanticPersistenceException(e);
		} finally {
			LOGGER.debug("Link properties fetch for {} entries took {} ms", linkReferences.size(), tracker.stop());
			if (result != null) {
				try {
					result.close();
				} catch (QueryEvaluationException e) {
					LOGGER.error("Failed closing tuple query result.", e);
				}
			}
		}
	}

	/**
	 * Builds the reference.
	 *
	 * @param row
	 *            the row
	 * @param type
	 *            the type
	 * @param id
	 *            the id
	 * @param preBuild
	 *            the pre build
	 * @return the instance reference
	 */
	InstanceReference buildReference(BindingSet row, String type, String id, InstanceReference preBuild) {
		if (preBuild != null) {
			return preBuild;
		}
		InstanceReference ref = null;
		Value value = row.getValue(type);
		try {
			ref = typeConverter.convert(InstanceReference.class, value.stringValue());
		} catch (TypeConversionException e) {
			String message = "Failed to convert {} to {} due to {}";
			LOGGER.warn(message, InstanceReference.class, value.stringValue(), e.getMessage());
			LOGGER.trace(message, InstanceReference.class, value.stringValue(), e.getMessage(), e);
			return null;
		}

		value = row.getValue(id);
		ref.setId(namespaceRegistryService.getShortUri((IRI) value));
		return ref;
	}

	@Override
	public List<LinkReference> getLinksTo(InstanceReference to, String linkId) {
		return getLinkReferencesInternal(null, to, Collections.singletonList(linkId));
	}

	@Override
	public boolean removeLinksFor(InstanceReference instance, Set<String> linkIds) {
		List<LinkReference> list = getLinkReferencesInternal(instance, null, linkIds);
		return unlinkInternal(list);
	}

	@Override
	public boolean unlink(InstanceReference from, InstanceReference to, String linkId, String reverseLinkid) {
		List<String> linkIds = new ArrayList<>(1);
		boolean unlinkInternal = false;
		if (StringUtils.isNotBlank(linkId)) {
			linkIds.add(linkId);
			unlinkInternal = unlinkInternal(from, to, linkIds);
		}

		if (StringUtils.isNotBlank(reverseLinkid)) {
			linkIds.clear();
			linkIds.add(reverseLinkid);
			unlinkInternal |= unlinkInternal(to, from, linkIds);
		}

		return unlinkInternal;
	}

	/**
	 * Unlink internal.
	 *
	 * @param from
	 *            the from
	 * @param to
	 *            the to
	 * @param linkIds
	 *            the link ids
	 * @return true, if successful
	 */
	private boolean unlinkInternal(InstanceReference from, InstanceReference to, Collection<String> linkIds) {
		// get the existing link references between the given instances
		List<LinkReference> linkReferences = getLinkReferencesInternal(from, to, linkIds);
		return unlinkInternal(linkReferences);
	}

	/**
	 * Unlink internal.
	 *
	 * @param links
	 *            the links
	 * @return true, if successful
	 */
	// auto close on transaction end
	private boolean unlinkInternal(List<LinkReference> links) {
		boolean error = false;
		// prepare and execute statements
		for (LinkReference linkReference : links) {
			if (linkReference.getId() == null) {
				continue;
			}
			try {
				eventService.fire(new LinkRemovedEvent(linkReference));
				unlinkInternalById(repositoryConnection, getInstanceResource(linkReference));
				// notify for removed link
				addDeletedLinkId(linkReference.getId());
			} catch (RepositoryException e) {
				LOGGER.error("Failed removing relation with IRI [" + linkReference.getId() + "]", e);
				error = true;
			}
		}
		return !links.isEmpty() && !error;
	}

	/**
	 * Unlink internal by id.
	 *
	 * @param connection
	 *            the connection
	 * @param linkReferenceId
	 *            the link reference id
	 */
	private void unlinkInternalById(RepositoryConnection connection, Serializable linkReferenceId) {
		if (linkReferenceId == null) {
			return;
		}
		try {
			IRI iri = namespaceRegistryService.buildUri(linkReferenceId.toString());
			StringBuilder deleteQuery = new StringBuilder()
					.append(namespaceRegistryService.getNamespaces())
						.append("delete {")
						.append("?instance emf:isActive \"true\"^^xsd:boolean.")
						.append("?reverse emf:isActive \"true\"^^xsd:boolean. ")
						.append("?source ?type ?destination. ")
						.append("?destination ?type ?source. ")
						.append(" } insert { GRAPH <")
						.append(getContext().toString())
						.append("> { ?instance emf:isActive \"false\"^^xsd:boolean.")
						.append("	?reverse emf:isActive \"false\"^^xsd:boolean.} } where { BIND(<")
						.append(iri.toString())
						.append("> as ?instance). ?instance a emf:Relation; emf:source ?source; emf:destination ?destination; emf:relationType ?type. OPTIONAL { ?instance emf:inverseRelation ?reverse. ?reverse a emf:Relation .} }");
			Update update = connection.prepareUpdate(QueryLanguage.SPARQL, deleteQuery.toString());
			update.setIncludeInferred(true);
			update.execute();
		} catch (MalformedQueryException e) {
			LOGGER.debug("Failed to parse update query", e);
		} catch (UpdateExecutionException e) {
			LOGGER.debug("Failed to delete relation with id=" + linkReferenceId, e);
		}
	}

	/**
	 * Dumps the passed string messages into the log if it is debug enabled.
	 *
	 * @param messages
	 *            the messages
	 */
	private static void trace(Object... messages) {
		if (LOGGER.isTraceEnabled()) {
			StringBuilder builder = new StringBuilder();
			for (Object message : messages) {
				builder.append(message == null ? "null" : message);
			}
			LOGGER.trace(builder.toString());
		}
	}

	/**
	 * Prepare SPARQL query.
	 *
	 * @param bindings
	 *            the bindings
	 * @param linkIds
	 *            the link id
	 * @param connection
	 *            the connection
	 * @param includeSimple
	 *            the include simple
	 * @return the tuple query
	 * @throws RepositoryException
	 *             the repository exception
	 * @throws MalformedQueryException
	 *             the malformed query exception
	 */
	private TupleQuery prepareSparqlQuery(Map<String, Serializable> bindings, Collection<String> linkIds,
			RepositoryConnection connection, boolean includeSimple) {
		StringBuilder builder = new StringBuilder();

		if (includeSimple) {
			builder
					.append("\nSELECT DISTINCT ?source ?subjectType ?relation ?relationType ?destination ?destType ?createdBy WHERE {")
						.append(" {?relation    emf:source ?source.  ?relation    a emf:Relation ;  emf:isActive \"true\"^^xsd:boolean ;emf:relationType ?relationType;emf:destination ?destination. optional {?relation emf:createdBy ?createdBy}.");
			appendComplexRelationTypeFilter(linkIds, builder);

			builder.append("\n} UNION { ");
			appendSimpleRelationTypeFilter(linkIds, builder);

			builder
					.append(" } ")
						.append("\n ?source emf:instanceType ?subjectType.  ")
						.append("\n ?destination emf:isDeleted \"false\"^^xsd:boolean. ")
						.append("\n ?destination emf:instanceType ?destType. ");
		} else {
			builder
					.append("\nSELECT DISTINCT ?source ?subjectType ?relation ?relationType ?destination ?destType ?createdBy WHERE {")
						.append("\n ?relation    emf:source ?source. ")
						.append("\n ?relation    a emf:Relation ;")
						.append("\n              emf:isActive \"true\"^^xsd:boolean ; ")
						.append("\n              emf:relationType ?relationType; ")
						.append("\n              emf:destination ?destination. ")
						.append("\n optional {?relation emf:createdBy ?createdBy}. ")
						.append("\n ?source emf:instanceType ?subjectType.  ")
						.append("\n ?destination emf:isDeleted \"false\"^^xsd:boolean. ")
						.append("\n ?destination emf:instanceType ?destType.  ");

			appendComplexRelationTypeFilter(linkIds, builder);
		}

		builder.append("\n}");
		return SPARQLQueryHelper.prepareTupleQuery(connection, builder.toString(), bindings, true);
	}

	/**
	 * Append simple relation type filter.
	 *
	 * @param linkIds
	 *            the link ids
	 * @param builder
	 *            the builder
	 */
	private void appendSimpleRelationTypeFilter(Collection<String> linkIds, StringBuilder builder) {
		if (linkIds != null && !linkIds.isEmpty()) {
			String subQueries = linkIds
					.stream()
						.map(this::buildValidRelationType)
						.map(SemanticLinkServiceImpl::buildSimpleRelationSubQuery)
						.collect(Collectors.joining("\n UNION "));
			builder.append("{").append(subQueries).append("} ?relationType emf:isSearchable \"true\"^^xsd:boolean.");
		} else {
			builder.append(
					"?source ?relationType ?destination. ?relationType rdf:type owl:ObjectProperty. ?relationType emf:isSearchable \"true\"^^xsd:boolean.");
		}
	}

	/**
	 * Builds the valid relation type.
	 *
	 * @param value
	 *            the value
	 * @return the string
	 */
	@SuppressWarnings("squid:UnusedPrivateMethod")
	private String buildValidRelationType(String value) {
		String shortUri = value;
		if (shortUri.startsWith("http")) {
			shortUri = namespaceRegistryService.getShortUri(shortUri);
		} else if (!shortUri.contains(":")) {
			shortUri = EMF.PREFIX + ":" + shortUri;
		}
		return shortUri;
	}

	@SuppressWarnings("squid:UnusedPrivateMethod")
	private static String buildSimpleRelationSubQuery(String shortUri) {
		return new StringBuilder(64)
				.append("{\n select DISTINCT ?source (")
					.append(shortUri)
					.append(" as ?relationType) ?destination where { ?source ")
					.append(shortUri)
					.append(" ?destination. }}")
					.toString();
	}

	/**
	 * Append complex relation type filter.
	 *
	 * @param linkIds
	 *            the link ids
	 * @param builder
	 *            the builder
	 */
	private void appendComplexRelationTypeFilter(Collection<String> linkIds, StringBuilder builder) {
		if (linkIds != null && !linkIds.isEmpty()) {
			String linksJoin = linkIds
					.stream()
						.filter(Objects::nonNull)
						.map(this::buildValidRelationType)
						.map(linkId -> "\n { ?relation emf:relationType " + linkId + ". }")
						.collect(Collectors.joining("\n UNION "));
			builder.append(linksJoin);
		}
	}

	/**
	 * Creates binding set's string representation with short URIs.
	 *
	 * @param row
	 *            the binding set
	 * @return the the binding set's string representation
	 */
	private String bindingSetToString(BindingSet row) {
		StringBuilder builder = new StringBuilder();
		Set<String> bindingNames = row.getBindingNames();

		Value value;
		for (String binding : bindingNames) {
			value = row.getValue(binding);
			if (value != null) {
				builder.append("[" + binding + " = ");
				if (value instanceof IRI) {
					builder.append(namespaceRegistryService.getShortUri((IRI) value));
				} else {
					builder.append(value.stringValue());
				}
				builder.append("]");
			}
		}

		return builder.toString();
	}

	@Override
	public boolean linkSimple(InstanceReference from, InstanceReference to, String linkId) {
		return linkSimpleInternal(from, to, linkId, null);
	}

	@Override
	public boolean linkSimple(InstanceReference from, InstanceReference to, String linkId, String reverseId) {
		return linkSimpleInternal(from, to, linkId, reverseId);
	}

	/**
	 * Link simple internal.
	 *
	 * @param from
	 *            the from
	 * @param to
	 *            the to
	 * @param linkId
	 *            the link id
	 * @param reverseId
	 *            the reverse id
	 * @return true, if successful
	 */
	private boolean linkSimpleInternal(InstanceReference from, InstanceReference to, String linkId, String reverseId) {
		if (Options.DO_NOT_PERSIST_IN_SD.isEnabled() || from == null || StringUtils.isBlank(linkId)
				|| to == null) {
			return false;
		}
		Resource sourceUri = getInstanceResource(from);
		IRI relation = namespaceRegistryService.buildUri(linkId);
		Model model = new LinkedHashModel(10);
		Resource toResource = getInstanceResource(to);
		model.add(sourceUri, relation, toResource);

		eventService.fire(new LinkAddedEvent(buildLinkReferenceForEvent(from, to, linkId)));

		String inverseId = reverseId;
		if (inverseId == null && linkId != null) {
			inverseId = semanticDefinitionService.getInverseRelationProvider().inverseOf(linkId);
		}
		if (StringUtils.isNotBlank(inverseId)) {
			model.add(toResource, namespaceRegistryService.buildUri(inverseId), sourceUri);
			eventService.fire(new LinkAddedEvent(buildLinkReferenceForEvent(to, from, inverseId)));
		}

		try {
			repositoryConnection.add(model, getContext());
		} catch (RepositoryException e) {
			throw new SemanticPersistenceException("Failed creating relations.", e);
		}
		return true;
	}

	@Override
	public List<LinkReference> getSimpleLinks(InstanceReference from, String linkId) {
		if (StringUtils.isBlank(linkId)) {
			return Collections.emptyList();
		}
		return getSimpleReferencesInternal(from, null, linkId);
	}

	@Override
	public List<LinkReference> getSimpleLinksTo(InstanceReference to, String linkId) {
		if (StringUtils.isBlank(linkId)) {
			return Collections.emptyList();
		}
		return getSimpleReferencesInternal(null, to, linkId);
	}

	@Override
	public List<LinkReference> getSimpleLinks(InstanceReference from, Set<String> linkIds) {
		if (isEmpty(linkIds)) {
			return Collections.emptyList();
		}
		return getSimpleReferencesInternal(from, linkIds);
	}

	private List<LinkReference> getSimpleReferencesInternal(InstanceReference from, Set<String> linkIds) {
		if (from == null) {
			return Collections.emptyList();
		}
		StringBuilder getSimpleReferencesQuery = new StringBuilder();

		getSimpleReferencesQuery
				.append("SELECT DISTINCT ?source ?subjectType ?relationType ?destination ?destType WHERE {")
					.append("?source emf:instanceType ?subjectType. ")
					.append("?destination emf:instanceType ?destType. ")
					.append("?source ?relationType ?destination. ")
					.append(linkIds
							.stream()
								.map(namespaceRegistryService::getShortUri)
								.map(linkId -> " { ?source " + linkId + " ?destination. } ")
								.collect(Collectors.joining("UNION")))
					.append(" ?destination emf:isDeleted \"false\"^^xsd:boolean . ")
					.append("}");

		Map<String, Serializable> bindings = CollectionUtils.createHashMap(3);
		bindings.put(SOURCE, getInstanceResource(from));

		TimeTracker tracker = TimeTracker.createAndStart();
		try {
			return executeQuery(from, null, null, getSimpleReferencesQuery, bindings);
		} finally {
			LOGGER.debug("Simple link search from={} took {} s", from.getId(), tracker.stopInSeconds());
		}
	}

	/**
	 * Gets the simple references internal.
	 *
	 * @param from
	 *            the from
	 * @param to
	 *            the to
	 * @param linkId
	 *            the link id
	 * @return the simple references internal
	 */
	private List<LinkReference> getSimpleReferencesInternal(InstanceReference from, InstanceReference to,
			String linkId) {
		if (from == null && to == null) {
			return Collections.emptyList();
		}
		StringBuilder getSimpleReferencesQuery = new StringBuilder();

		getSimpleReferencesQuery
				.append("SELECT DISTINCT ?source ?subjectType ?relationType ?destination ?destType WHERE {")
					.append("?source ?relationType ?destination. ")
					.append("?source emf:instanceType ?subjectType. ")
					.append("?destination emf:instanceType ?destType. ");
		// check for the reversed instance not to be deleted
		if (to != null && from == null) {
			getSimpleReferencesQuery.append(" ?source emf:isDeleted \"false\"^^xsd:boolean .");
		} else {
			getSimpleReferencesQuery.append(" ?destination emf:isDeleted \"false\"^^xsd:boolean . ");
		}
		getSimpleReferencesQuery.append("}");

		IRI relationType = valueFactory.createIRI(namespaceRegistryService.buildFullUri(linkId));
		Map<String, Serializable> bindings = CollectionUtils.createHashMap(3);
		// set bindings
		bindings.put(SOURCE, getInstanceResource(from));
		bindings.put(DESTINATION, getInstanceResource(to));
		bindings.put(RELATION_TYPE, relationType);

		TimeTracker tracker = TimeTracker.createAndStart();
		try {
			return executeQuery(from, to, linkId, getSimpleReferencesQuery, bindings);
		} finally {
			LOGGER.debug("Simple link search from={} to {} took {} s", from != null ? from.getId() : null,
					to != null ? to.getId() : null, tracker.stopInSeconds());
		}
	}

	private List<LinkReference> executeQuery(InstanceReference from, InstanceReference to, String linkId,
			StringBuilder stringQueryBuilder, Map<String, Serializable> bindings) {
		try {
			TupleQuery preparedQuery = SPARQLQueryHelper.prepareTupleQuery(repositoryConnection, stringQueryBuilder.toString(),
					bindings, true);
			TupleQueryResult result = preparedQuery.evaluate();

			// build result
			return buildResultReferences(repositoryConnection, result, from, to, linkId, true);
		} catch (RepositoryException | QueryEvaluationException e) {
			throw new SemanticPersistenceException(e);
		}
	}

	@Override
	public boolean isLinked(InstanceReference from, InstanceReference to, String linkId) {
		if (from == null && to == null || linkId == null) {
			return false;
		}
		StringBuilder checkIfRelationExistsQuery = new StringBuilder();

		checkIfRelationExistsQuery
				.append("ASK WHERE {")
					.append("?relation emf:source ?source ;")
					//
					.append(" emf:destination ?destination ;")
					.append(" emf:relationType ")
					.append(shrinkLinkIdentifier(linkId))
					.append(" ;")
					.append(" a emf:Relation ; emf:isActive \"true\"^^xsd:boolean. ")
					.append("}");

		TimeTracker tracker = TimeTracker.createAndStart();
		boolean result = false;
		try {
			// set bindings
			Map<String, Serializable> bindings = CollectionUtils.createHashMap(2);
			bindings.put(SOURCE, getInstanceResource(from));
			bindings.put(DESTINATION, getInstanceResource(to));
			result = evaluateBooleanQuery(checkIfRelationExistsQuery.toString(), bindings);
			return result;
		} finally {
			LOGGER.debug("Link test from={} to={} took {} ms and evaluated to {}", from != null ? from.getId() : null,
					to != null ? to.getId() : null, tracker.stop(), result);
		}
	}

	/**
	 * Gets the simple references internal.
	 *
	 * @param from
	 *            the from
	 * @param to
	 *            the to
	 * @param linkId
	 *            the link id
	 * @return the simple references internal
	 */
	private boolean isLinkedSimpleInternal(InstanceReference from, InstanceReference to, String linkId) {
		if (from == null && to == null) {
			return false;
		}
		StringBuilder checkIfRelationExistsQuery = new StringBuilder();

		checkIfRelationExistsQuery
				.append("ASK WHERE {")
					.append("?source ")
					.append(namespaceRegistryService.getShortUri(linkId))
					.append(" ?destination .")
					.append(" ?source emf:isDeleted \"false\"^^xsd:boolean .")
					.append(" ?destination emf:isDeleted \"false\"^^xsd:boolean . ")
					.append("}");

		// set bindings
		Map<String, Serializable> bindings = CollectionUtils.createHashMap(2);
		bindings.put(SOURCE, getInstanceResource(from));
		bindings.put(DESTINATION, getInstanceResource(to));
		TimeTracker tracker = TimeTracker.createAndStart();
		boolean result = false;
		try {
			result = evaluateBooleanQuery(checkIfRelationExistsQuery.toString(), bindings);
			return result;
		} finally {
			LOGGER.debug("Simple link test from={} to={} took {} ms and evaluared to {}",
					from != null ? from.getId() : null, to != null ? to.getId() : null, tracker.stop(), result);
		}
	}

	private boolean evaluateBooleanQuery(String queryString, Map<String, Serializable> bindings) {
		try {
			BooleanQuery query = SPARQLQueryHelper.prepareBooleanQuery(repositoryConnection, queryString, bindings, true, 0);
			return query.evaluate();
		} catch (RepositoryException | QueryEvaluationException e) {
			throw new SemanticPersistenceException(e);
		}
	}

	@Override
	public void unlinkSimple(InstanceReference from, String linkId) {
		if (from == null || StringUtils.isBlank(linkId)) {
			LOGGER.warn("Missing arguments for unlinkSimple {}", linkId);
			return;
		}
		String instanceUri = namespaceRegistryService.getShortUri((IRI) getInstanceResource(from));
		String linkUri = namespaceRegistryService.getShortUri(linkId);
		String deleteSimpleRelationQuery = "delete { $instanceUri $linkUri ?other. } where { $instanceUri $linkUri ?other. }";
		deleteSimpleRelationQuery = namespaceRegistryService.getNamespaces()
				+ deleteSimpleRelationQuery.replaceAll("\\$instanceUri", instanceUri).replaceAll("\\$linkUri", linkUri);
		try {
			Update update = repositoryConnection.prepareUpdate(QueryLanguage.SPARQL, deleteSimpleRelationQuery);
			update.execute();
			eventService.fire(new LinkRemovedEvent(buildLinkReferenceForEvent(from, null, linkId)));
		} catch (RepositoryException | MalformedQueryException | UpdateExecutionException e) {
			throw new SemanticPersistenceException(
					"Could not delete simple relation for instance " + instanceUri + " and link " + linkUri, e);
		}
	}

	@Override
	public void unlinkSimple(InstanceReference from, InstanceReference to, String linkId) {
		unlinkSimpleInternal(from, to, linkId, null);
	}

	/**
	 * Unlink simple internal.
	 *
	 * @param from
	 *            the from
	 * @param to
	 *            the to
	 * @param linkId
	 *            the link id
	 * @param reverseId
	 *            the reverse id
	 */
	// auto close on transaction end
	private void unlinkSimpleInternal(InstanceReference from, InstanceReference to, String linkId, String reverseId) {
		if (from == null || to == null || StringUtils.isBlank(linkId)) {
			LOGGER.warn("Missing arguments for unlinkSimple {}", linkId);
			return;
		}
		Resource sourceUri = getInstanceResource(from);
		Resource destinationUri = getInstanceResource(to);
		IRI relation = namespaceRegistryService.buildUri(linkId);
		try {
			repositoryConnection.remove(sourceUri, relation, destinationUri);
			eventService.fire(new LinkRemovedEvent(buildLinkReferenceForEvent(from, to, linkId)));
			if (StringUtils.isNotBlank(reverseId)) {
				IRI reverseRelation = namespaceRegistryService.buildUri(reverseId);
				repositoryConnection.remove(destinationUri, reverseRelation, sourceUri);
				eventService.fire(new LinkRemovedEvent(buildLinkReferenceForEvent(to, from, reverseId)));
			}
		} catch (RepositoryException e) {
			throw new SemanticPersistenceException(
					"Failed removing triplet {" + from.getId() + ", " + linkId + ", " + to.getId() + "} ", e);
		}
	}

	@Override
	public void unlinkSimple(InstanceReference from, InstanceReference to, String linkId, String reverseId) {
		unlinkSimpleInternal(from, to, linkId, reverseId);
	}

	@Override
	public boolean isLinkedSimple(InstanceReference from, InstanceReference to, String linkId) {
		return isLinkedSimpleInternal(from, to, linkId);
	}

	@Override
	protected String shrinkLinkIdentifier(String identifier) {
		return namespaceRegistryService.getShortUri(identifier);
	}

	@Override
	protected List<LinkReference> getLinksInternal(Object from, Object to, Collection<String> linkids) {
		return getLinkReferencesInternal(from, to, linkids);
	}

	@Override
	protected String expandLinkIdentifier(String identifier) {
		try {
			String id = identifier;
			if (!identifier.contains(":")) {
				id = "emf:" + identifier;
			}
			return namespaceRegistryService.buildFullUri(id);
		} catch (RuntimeException e) {
			LOGGER.warn("Failed to expand link identifier.", e);
			return identifier;
		}
	}

	@Override
	protected String getReverseLinkType(String relationType) {
		PropertyInstance relation = semanticDefinitionService.getRelation(relationType);
		if (relation != null) {
			return relation.getAsString("inverseRelation");
		}
		return null;
	}

	@Override
	public LinkSearchArguments searchLinks(LinkSearchArguments arguments) {
		SearchArguments<Instance> args = new SearchArguments<>();
		boolean isFrom = false;

		String fromIdentifier = OBJECT_VARIABLE;
		if (arguments.getFrom() != null) {
			fromIdentifier = arguments.getFrom().getId();
			arguments.getArguments().put(OBJECT, fromIdentifier);
		}

		String relationIdentifier = VARIABLE + RELATIONS_PREDICATE;
		if (arguments.getLinkId() != null) {
			relationIdentifier = arguments.getLinkId();
		}

		String toIdentifier = CONTEXT_VARIABLE;
		String projection = CONTEXT_VARIABLE;
		StringBuilder query = new StringBuilder();
		if (arguments.getTo() != null) {
			isFrom = true;
			toIdentifier = arguments.getTo().getId();
			projection = String.format(CONTEXT_VARIABLE_BINDING, toIdentifier);
		}

		query
				.append(fromIdentifier)
					.append(" ")
					.append(relationIdentifier)
					.append(" ")
					.append(toIdentifier)
					.append(STATEMENT_SEPARATOR)
					.append(LINE_SEPARATOR);
		query.append(toIdentifier).append(IS_NOT_DELETED).append(LINE_SEPARATOR);
		query.append(toIdentifier).append(" emf:instanceType ?contextType . ").append(LINE_SEPARATOR);

		if (!arguments.getArguments().isEmpty()) {
			args.getArguments().putAll(arguments.getArguments());
		}
		if (!arguments.getQueryConfigurations().isEmpty()) {
			args.setQueryConfigurations(arguments.getQueryConfigurations());
		}
		args.setProjection(projection + " ?contextType");
		args.setStringQuery(query.toString());
		args.setQueryName("searchLinks");

		args.setPermissionsType(arguments.getPermissionsType());
		args.setPageNumber(arguments.getPageNumber());
		args.setPageSize(arguments.getPageSize());
		args.setMaxSize(arguments.getMaxSize());
		args.setDialect(SearchDialects.SPARQL);
		args.addSorters(arguments.getSorters());

		searchService.searchAndLoad(Instance.class, args);
		arguments.setIsFrom(isFrom);

		if (CollectionUtils.isEmpty(args.getResult())) {
			arguments.setResult(Collections.emptyList());
			arguments.setTotalItems(0);
			return arguments;
		}

		List<Instance> result = args.getResult();
		List<LinkInstance> links = result.stream().map(this::buildLinkInstance).collect(
				Collectors.toCollection(LinkedList::new));
		arguments.setResult(links);
		arguments.setTotalItems(args.getTotalItems());

		return arguments;
	}

	@SuppressWarnings("squid:UnusedPrivateMethod")
	private LinkInstance buildLinkInstance(Instance instance) {
		LinkInstance linkInstance = new LinkInstance();
		linkInstance.setIdentifier(instance.getString(RELATIONS_PREDICATE));
		linkInstance.setFrom(instance);

		Instance instanceTo = ReflectionUtils.newInstance(getType(instance.getString("contextType")));
		instanceTo.setId(instance.getString(CONTEXT_PREDICATE));
		linkInstance.setTo(instanceTo);
		return linkInstance;
	}

	/**
	 * Gets the actual type by name.
	 *
	 * @param typeName
	 *            the type name
	 * @return the type
	 */
	@SuppressWarnings("unchecked")
	private Class<? extends Instance> getType(String typeName) {
		return typeMapping.computeIfAbsent(typeName, type -> {
			DataTypeDefinition definition = definitionService.getDataTypeDefinition(type);
			if (definition != null) {
				return (Class<? extends Instance>) definition.getJavaClass();
			}
			LOGGER.warn("Could not load class for type {}", type);
			return null;
		});
	}

}
