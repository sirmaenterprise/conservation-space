package com.sirma.itt.emf.semantic.persistence;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.transaction.Transactional;
import javax.transaction.Transactional.TxType;

import org.openrdf.model.Literal;
import org.openrdf.model.Model;
import org.openrdf.model.Resource;
import org.openrdf.model.URI;
import org.openrdf.model.Value;
import org.openrdf.model.ValueFactory;
import org.openrdf.model.impl.LinkedHashModel;
import org.openrdf.model.vocabulary.DCTERMS;
import org.openrdf.model.vocabulary.RDF;
import org.openrdf.query.QueryEvaluationException;
import org.openrdf.query.TupleQuery;
import org.openrdf.query.TupleQueryResult;
import org.openrdf.repository.RepositoryConnection;
import org.openrdf.repository.RepositoryException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sirma.itt.commons.utils.string.StringUtils;
import com.sirma.itt.emf.semantic.exception.SemanticPersistenceException;
import com.sirma.itt.emf.semantic.queries.SPARQLQueryHelper;
import com.sirma.itt.emf.semantic.search.TupleQueryResultIterator;
import com.sirma.itt.seip.Entity;
import com.sirma.itt.seip.Pair;
import com.sirma.itt.seip.collections.CollectionUtils;
import com.sirma.itt.seip.configuration.Options;
import com.sirma.itt.seip.db.AbstractDbDao;
import com.sirma.itt.seip.db.SemanticDb;
import com.sirma.itt.seip.definition.DictionaryService;
import com.sirma.itt.seip.definition.SemanticDefinitionService;
import com.sirma.itt.seip.domain.definition.DataTypeDefinition;
import com.sirma.itt.seip.domain.definition.DefinitionModel;
import com.sirma.itt.seip.domain.instance.CMInstance;
import com.sirma.itt.seip.domain.instance.DMSInstance;
import com.sirma.itt.seip.domain.instance.DefaultProperties;
import com.sirma.itt.seip.domain.instance.Instance;
import com.sirma.itt.seip.instance.InstanceTypes;
import com.sirma.itt.seip.search.NamedQueries;
import com.sirma.itt.seip.security.context.SecurityContext;
import com.sirma.itt.seip.time.TimeTracker;
import com.sirma.itt.seip.util.EqualsHelper;
import com.sirma.itt.seip.util.ReflectionUtils;
import com.sirma.itt.semantic.NamespaceRegistryService;
import com.sirma.itt.semantic.TransactionalRepositoryConnection;
import com.sirma.itt.semantic.model.vocabulary.EMF;
import com.sirma.itt.semantic.queries.QueryBuilder;

/**
 * DBDao implementation for semantic database.
 *
 * @author kirq4e
 */
@SemanticDb
@ApplicationScoped
public class SemanticDbDaoImpl extends AbstractDbDao {

	private static final long serialVersionUID = -4449835928429136986L;
	private static final Logger LOGGER = LoggerFactory.getLogger(SemanticDbDaoImpl.class);

	/** The dictionary service. */
	@Inject
	private DictionaryService dictionaryService;

	/** The value factory. */
	@Inject
	private ValueFactory valueFactory;

	/** The namespace registry service. */
	@Inject
	private NamespaceRegistryService namespaceRegistryService;

	/** The repository connection. */
	@Inject
	private javax.enterprise.inject.Instance<TransactionalRepositoryConnection> repositoryConnection;

	/** The query builder. */
	@Inject
	private QueryBuilder queryBuilder;

	@Inject
	private SemanticPropertiesReadConverter readConverter;

	@Inject
	private SemanticPropertiesWriteConverter writeConverter;

	@Inject
	private SecurityContext securityContext;

	@Inject
	private InstanceTypes instanceTypes;

	@Inject
	private SemanticDefinitionService semanticDefinitionService;

	@Override
	@SuppressWarnings("unchecked")
	@Transactional(TxType.REQUIRED)
	public <E extends Entity<? extends Serializable>> E saveOrUpdate(E entity) {
		if (!Options.DO_NOT_PERSIST_IN_SD.isEnabled() && entity instanceof Instance) {
			return (E) saveOrUpdateInternal((Instance) entity, null);
		}
		return entity;
	}

	@Override
	@SuppressWarnings("unchecked")
	@Transactional(TxType.REQUIRED)
	public <E extends Entity<? extends Serializable>> E saveOrUpdate(E entity, E oldEntity) {
		if (!Options.DO_NOT_PERSIST_IN_SD.isEnabled() && entity instanceof Instance) {
			if (oldEntity == null) {
				return (E) saveOrUpdateInternal((Instance) entity, null);
				// we process the other argument only if the DB ids match
			} else if (EqualsHelper.nullSafeEquals(entity.getId(), oldEntity.getId())) {
				return (E) saveOrUpdateInternal((Instance) entity, (Instance) oldEntity);
			}
		}
		return entity;
	}

	/**
	 * Save or update internal.
	 *
	 * @param entity
	 *            the entity
	 * @param oldEntity
	 *            the old entity
	 * @return the instance
	 */
	protected Instance saveOrUpdateInternal(Instance entity, Instance oldEntity) {
		// does not persist instances that are marked as transient
		if (Options.DO_NOT_PERSIST_IN_SD.isEnabled()) {
			LOGGER.trace("Skipped instance marked for persist in semantic DB with id={} of type {}", entity.getId(),
					entity.getClass());
			return entity;
		}
		LOGGER.trace("Begin instance save {}", entity.getId());

		TimeTracker timeTracker = new TimeTracker().begin();

		// create the RDF model to be stored in the repository
		final Model addModel = new LinkedHashModel();
		Model removeModel = new LinkedHashModel();

		Resource subject = writeConverter.buildModelForInstance(entity, oldEntity, addModel, removeModel);

		URI dataGraph = namespaceRegistryService.getDataGraph();

		RepositoryConnection connection = repositoryConnection.get();
		try {
			// if there is no old entity we should not remove anything
			if (oldEntity != null) {
				Model diff = new LinkedHashModel(addModel);
				diff.retainAll(removeModel);
				// remove duplicate entries from both models to minimize
				// computation. No need to remove something that will be added
				// back again
				removeModel.removeAll(diff);
				addModel.removeAll(diff);
				// remove statements build in the remove model
				SemanticPersistenceHelper.removeModel(connection, removeModel);
			} else {
				// ensure the is deleted is set to false
				addModel.add(subject, EMF.IS_DELETED, valueFactory.createLiteral(false), dataGraph);
			}

			// add the new model to the repository
			SemanticPersistenceHelper.saveModel(connection, addModel, dataGraph);

			// update the id
			idManager.persisted(entity);
			if (!addModel.isEmpty()) {
				LOGGER.trace("Model [{}]  added to reporitory.", addModel);
			}
			if (!removeModel.isEmpty()) {
				LOGGER.trace("Model [{}]  removed from reporitory.", removeModel);
			}
		} finally {
			LOGGER.trace("End saving {} = {} in SemanticDb and it took {} ms", entity.getClass().getSimpleName(),
					entity.getId(), timeTracker.stop());
		}
		return entity;
	}

	/**
	 * Builds URI object from the given short URI in string representation.
	 *
	 * @param shortUri
	 *            the short uri
	 * @return the URI or <code>null</code> if the argument is <code>null</code> .
	 */
	private URI buildUri(String shortUri) {
		if (shortUri == null) {
			return null;
		}
		return namespaceRegistryService.buildUri(shortUri);
	}

	@Override
	public <E extends Entity<? extends Serializable>> E find(Class<E> clazz, Object id) {
		if (id == null || "null".equals(id)) {
			// non existent entity
			return null;
		}

		Collection<Serializable> objectUris = new ArrayList<>(1);
		objectUris.add((Serializable) id);
		List<Pair<String, Object>> parameters = new ArrayList<>();
		parameters.add(new Pair<String, Object>(NamedQueries.Params.URIS, objectUris));

		List<Object> fetchWithNamed = fetchWithNamed(NamedQueries.SELECT_BY_IDS, parameters);

		if (fetchWithNamed.isEmpty()) {
			return null;
		}

		return clazz.cast(fetchWithNamed.get(0));
	}

	@Override
	public <E extends Entity<? extends Serializable>> E refresh(E entity) {
		return entity;
	}

	@Override
	@SuppressWarnings("unchecked")
	public <R, E extends Pair<String, Object>> List<R> fetchWithNamed(String namedQuery, List<E> params) {
		TimeTracker timeTracker = new TimeTracker().begin();
		String query = queryBuilder.buildQueryByName(namedQuery, params);
		if (StringUtils.isNullOrEmpty(query)) {
			throw new SemanticPersistenceException("Undefined named query [" + namedQuery + "]");
		}
		LOGGER.debug("Executing tuple query:\n{}", query);

		try (TransactionalRepositoryConnection connection = repositoryConnection.get()) {
			TupleQuery tupleQuery = SPARQLQueryHelper.prepareTupleQuery(connection, query, Collections.emptyMap(),
					false);
			try (TupleQueryResultIterator queryResult = new TupleQueryResultIterator(tupleQuery.evaluate())) {
				return (List<R>) convertQueryResults(queryResult);
			}

		} catch (QueryEvaluationException | RepositoryException e) {
			throw new SemanticPersistenceException("Failed evaluating named query [" + namedQuery + "]", e);
		} finally {
			LOGGER.debug("SemanticDb load took {} ms", timeTracker.stop());
		}
	}

	private List<Instance> convertQueryResults(TupleQueryResult queryResult) throws QueryEvaluationException {
		if (!queryResult.hasNext()) {
			return Collections.emptyList();
		}

		return readConverter
				.buildQueryResultModel(queryResult)
					.entrySet()
					.stream()
					.map(entry -> convertToInstance(entry.getKey(), entry.getValue()))
					.filter(Objects::nonNull)
					.collect(Collectors.toCollection(LinkedList::new));
	}

	private Instance convertToInstance(String uri, Map<Value, Set<Value>> instanceProperties) {

		Set<Value> rdfTypes = instanceProperties.remove(RDF.TYPE);
		Set<Value> types = instanceProperties.remove(EMF.INSTANCE_TYPE);
		DataTypeDefinition dataTypeDefinition = resolveDataType(types);

		if (dataTypeDefinition == null) {
			LOGGER.error("Invalid entity {} = {}", EMF.INSTANCE_TYPE, types);
			return null;
		}

		Class<?> javaClass = dataTypeDefinition.getJavaClass();
		Instance instance = (Instance) ReflectionUtils.newInstance(javaClass);

		instance.setId(namespaceRegistryService.getShortUri(uri));

		// this may be removed at some point
		Value value = removeAndGetFirstValue(EMF.DMS_ID, instanceProperties);
		if (value != null) {
			DMSInstance.setDmsId(instance, value.stringValue());
		}

		value = getFirstValue(DCTERMS.IDENTIFIER, instanceProperties);
		if (value != null) {
			CMInstance.setContentManagementId(instance, value.stringValue());
		}

		DefinitionModel definition = resolveInstanceDefinition(instance, instanceProperties);

		Map<String, Set<Value>> convertedKeys = readConverter.convertPropertiesNames(instanceProperties);

		readConverter.convertPropertiesFromSemanticToInternalModel(definition, convertedKeys,
				instance.getOrCreateProperties());

		instance.addIfNotNullOrEmpty(DefaultProperties.SEMANTIC_TYPE, resolveRdfType(rdfTypes));
		instanceTypes.from(instance);

		return instance;
	}

	private DefinitionModel resolveInstanceDefinition(Instance instance, Map<Value, Set<Value>> instanceProperties) {
		// pre fetch and check the type if present not to continue
		Stream<Value> types = getInstanceTypes(instanceProperties);
		DefinitionModel definition = resolveInstanceDefinition(types, instance);
		if (definition == null) {
			LOGGER.warn("No definition found for Instance with URI = {}, definition type={}", instance.getId(),
					instance.getIdentifier());
		}
		return definition;
	}

	private DefinitionModel resolveInstanceDefinition(Stream<Value> definitionIds, Instance instance) {
		DefinitionModel definition = definitionIds.filter(Objects::nonNull).map(defId -> {
			instance.setIdentifier(defId.stringValue());
			DefinitionModel def = dictionaryService.getInstanceDefinition(instance);
			return def;
		}).filter(Objects::nonNull).findFirst().orElseGet(() -> {
			instance.setIdentifier(null);
			return dictionaryService.getInstanceDefinition(instance);
		});

		// this case is when we have an instance that uses some generic definition that is not persisted in the
		// database and is resolved only by type (like topic)
		if (definition != null) {
			instance.setIdentifier(definition.getIdentifier());
			instance.setRevision(definition.getRevision());
		}
		return definition;
	}

	private String resolveRdfType(Set<Value> rdfTypes) {
		if (CollectionUtils.isEmpty(rdfTypes)) {
			return null;
		}

		Set<String> types = rdfTypes.stream().map(Value::stringValue).collect(Collectors.toSet());
		return semanticDefinitionService.getMostConcreteClass(types);
	}

	private DataTypeDefinition resolveDataType(Set<Value> types) {
		if (CollectionUtils.isEmpty(types)) {
			LOGGER.error("Invalid entity: no emf:instanceType present!");
			return null;
		}

		return types
				.stream()
					.map(value -> dictionaryService.getDataTypeDefinition(value.stringValue()))
					.filter(Objects::nonNull)
					.findFirst()
					.orElse(null);
	}

	private static Stream<Value> getInstanceTypes(Map<Value, Set<Value>> instanceProperties) {
		Set<Value> definitionIds = instanceProperties.getOrDefault(EMF.DEFINITION_ID, Collections.emptySet());
		Set<Value> types = instanceProperties.getOrDefault(EMF.TYPE, Collections.emptySet());
		return Stream.concat(definitionIds.stream(), types.stream());
	}

	/**
	 * Gets the first value.
	 *
	 * @param key
	 *            the key
	 * @param map
	 *            the map
	 * @return the first value
	 */
	private static Value getFirstValue(Value key, Map<Value, Set<Value>> map) {
		Set<Value> set = map.get(key);
		if (set != null && !set.isEmpty()) {
			return set.iterator().next();
		}
		return null;
	}

	/**
	 * Removes the and get first value.
	 *
	 * @param key
	 *            the key
	 * @param map
	 *            the map
	 * @return the value
	 */
	private static Value removeAndGetFirstValue(Value key, Map<Value, Set<Value>> map) {
		Set<Value> set = map.remove(key);
		if (set != null && !set.isEmpty()) {
			return set.iterator().next();
		}
		return null;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public <R, E extends Pair<String, Object>> List<R> fetch(String query, List<E> params) {
		return Collections.emptyList();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	@Transactional(TxType.REQUIRED)
	public <E extends Pair<String, Object>> int executeUpdate(String namedQuery, List<E> params) {
		return 0;
	}

	@Override
	@SuppressWarnings("resource")
	protected <E extends Entity<? extends Serializable>> int deleteInternal(Class<E> clazz, Serializable entityId,
			boolean softDelete) {
		if (Options.DO_NOT_PERSIST_IN_SD.isEnabled() || entityId == null) {
			return 0;
		}
		LOGGER.debug("Deleting resource/s [{}]", entityId);
		RepositoryConnection connection = repositoryConnection.get();
		if (softDelete) {
			if (entityId instanceof Collection) {
				Model model = new LinkedHashModel();
				Model removeModel = new LinkedHashModel();
				// collect all ids from the collection to the model
				for (Object object : (Collection<?>) entityId) {
					deleteInternal(object.toString(), model, removeModel, connection, false);
				}
				// flush the model to db
				deleteInternal(null, model, removeModel, connection, true);
				return ((Collection<?>) entityId).size();
			}
			deleteInternal(entityId.toString(), null, null, connection, true);
		} else {
			if (entityId instanceof Collection) {
				for (Object object : (Collection<?>) entityId) {
					deleteHard(object.toString(), connection);
				}
				return ((Collection<?>) entityId).size();
			}
			deleteHard(entityId.toString(), connection);
		}
		return 1;
	}

	private void deleteHard(String uri, RepositoryConnection connection) {
		URI toDelete = buildUri(uri);
		if (toDelete == null) {
			// we do not want to delete to whole database!
			return;
		}
		try {
			URI dataGraph = namespaceRegistryService.getDataGraph();
			connection.remove(toDelete, null, null, dataGraph);
			connection.remove((URI) null, null, toDelete, dataGraph);
		} catch (RepositoryException e) {
			throw new SemanticPersistenceException("Failed remove resource with URI [" + uri + "]", e);
		}
	}

	/**
	 * Adds a triplet that marks an instance identified by the given URI that the instance is deleted. The method could
	 * add the statement directly to db or populate the given model and then flush at the end. If model is
	 * <code>null</code> the method will populate the connection directly
	 *
	 * @param shortUri
	 *            the short uri to add or <code>null</code> to ignore
	 * @param model
	 *            the model to populate or <code>null</code> if not needed
	 * @param removeModel
	 *            the remove model
	 * @param connection
	 *            the connection to use for uri/model flush. Required when model is not present or flush is required.
	 * @param flush
	 *            to flush or not the given model using the given connection. The value is ignored if no model is passed
	 */
	private void deleteInternal(String shortUri, Model model, Model removeModel, RepositoryConnection connection,
			boolean flush) {
		URI dataGraph = namespaceRegistryService.getDataGraph();
		URI resource = buildUri(shortUri);
		if (resource != null) {
			try {
				addDataToModelOrConnection(model, removeModel, connection, dataGraph, resource);
			} catch (RepositoryException e) {
				throw new SemanticPersistenceException("Failed marking resource with URI [" + shortUri + "] as deleted",
						e);
			}
		}
		if (flush && model != null) {
			SemanticPersistenceHelper.removeModel(connection, removeModel, dataGraph);
			SemanticPersistenceHelper.saveModel(connection, model, dataGraph);
		}
	}

	/**
	 * Adds the data to model or connection.
	 *
	 * @param model
	 *            the model
	 * @param removeModel
	 *            the remove model
	 * @param connection
	 *            the connection
	 * @param dataGraph
	 *            the data graph
	 * @param resource
	 *            the resource
	 * @throws RepositoryException
	 *             the repository exception
	 */
	private void addDataToModelOrConnection(Model model, Model removeModel, RepositoryConnection connection,
			URI dataGraph, URI resource) throws RepositoryException {
		URI deletedBy = getDeletedBy();
		Literal trueLiteral = valueFactory.createLiteral(true);
		Literal falseLiteral = valueFactory.createLiteral(false);
		Literal currentDateLiteral = valueFactory.createLiteral(new Date());

		if (model == null) {
			connection.add(resource, EMF.DELETED_ON, currentDateLiteral, dataGraph);
			if (deletedBy != null) {
				connection.add(resource, EMF.DELETED_BY, deletedBy, dataGraph);
			}
			connection.add(resource, EMF.IS_DELETED, trueLiteral, dataGraph);
			connection.remove(resource, EMF.IS_DELETED, falseLiteral);
		} else {
			model.add(resource, EMF.IS_DELETED, trueLiteral);
			model.add(resource, EMF.DELETED_ON, currentDateLiteral);
			if (deletedBy != null) {
				model.add(resource, EMF.DELETED_BY, deletedBy);
			}
			removeModel.add(resource, EMF.IS_DELETED, falseLiteral);
		}
	}

	/**
	 * Gets the deleted by.
	 *
	 * @return the deleted by
	 */
	private URI getDeletedBy() {
		return namespaceRegistryService.buildUri((String) securityContext.getAuthenticated().getSystemId());
	}

}
