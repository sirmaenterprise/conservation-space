package com.sirma.itt.emf.semantic.persistence;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import javax.annotation.PostConstruct;
import javax.ejb.Stateless;
import javax.ejb.TransactionAttribute;
import javax.ejb.TransactionAttributeType;
import javax.inject.Inject;

import org.openrdf.model.Model;
import org.openrdf.model.Resource;
import org.openrdf.model.URI;
import org.openrdf.model.Value;
import org.openrdf.model.ValueFactory;
import org.openrdf.model.impl.LinkedHashModel;
import org.openrdf.model.vocabulary.DCTERMS;
import org.openrdf.model.vocabulary.RDF;
import org.openrdf.query.MalformedQueryException;
import org.openrdf.query.QueryEvaluationException;
import org.openrdf.query.QueryLanguage;
import org.openrdf.query.TupleQuery;
import org.openrdf.query.TupleQueryResult;
import org.openrdf.repository.RepositoryConnection;
import org.openrdf.repository.RepositoryException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sirma.itt.commons.utils.string.StringUtils;
import com.sirma.itt.emf.configuration.Config;
import com.sirma.itt.emf.configuration.EmfConfigurationProperties;
import com.sirma.itt.emf.converter.TypeConverter;
import com.sirma.itt.emf.db.AbstractDbDao;
import com.sirma.itt.emf.db.SemanticDb;
import com.sirma.itt.emf.db.SequenceEntityGenerator;
import com.sirma.itt.emf.definition.DictionaryService;
import com.sirma.itt.emf.definition.model.DataTypeDefinition;
import com.sirma.itt.emf.domain.Pair;
import com.sirma.itt.emf.domain.model.DefinitionModel;
import com.sirma.itt.emf.domain.model.Entity;
import com.sirma.itt.emf.extension.PersistentProperties.PersistentPropertyKeys;
import com.sirma.itt.emf.instance.dao.InstanceService;
import com.sirma.itt.emf.instance.dao.ServiceRegister;
import com.sirma.itt.emf.instance.model.CMInstance;
import com.sirma.itt.emf.instance.model.DMSInstance;
import com.sirma.itt.emf.instance.model.Instance;
import com.sirma.itt.emf.instance.model.InstanceReference;
import com.sirma.itt.emf.instance.model.OwnedModel;
import com.sirma.itt.emf.instance.model.TenantAware;
import com.sirma.itt.emf.properties.DefaultProperties;
import com.sirma.itt.emf.semantic.exception.SemanticPersistenceException;
import com.sirma.itt.emf.semantic.info.SemanticOperationLogger;
import com.sirma.itt.emf.semantic.queries.QueryBuilder;
import com.sirma.itt.emf.time.TimeTracker;
import com.sirma.itt.emf.util.CollectionUtils;
import com.sirma.itt.emf.util.EqualsHelper;
import com.sirma.itt.emf.util.ReflectionUtils;
import com.sirma.itt.semantic.NamespaceRegistryService;
import com.sirma.itt.semantic.configuration.SemanticConfigurationProperties;
import com.sirma.itt.semantic.model.vocabulary.EMF;

/**
 * DBDao implementation for semantic database.
 *
 * @author kirq4e
 */
@Stateless
@SemanticDb
public class SemanticDbDaoImpl extends AbstractDbDao {

	private static final Logger LOGGER = LoggerFactory.getLogger(SemanticDbDaoImpl.class);

	/** The Constant serialVersionUID. */
	private static final long serialVersionUID = 1401930156101749016L;

	/** The dictionary service. */
	@Inject
	private DictionaryService dictionaryService;

	/** The type converter. */
	@Inject
	private TypeConverter typeConverter;

	/** The value factory. */
	@Inject
	private ValueFactory valueFactory;

	/** The namespace registry service. */
	@Inject
	private NamespaceRegistryService namespaceRegistryService;

	/** The repository connection. */
	@Inject
	private javax.enterprise.inject.Instance<RepositoryConnection> repositoryConnection;

	/** The query builder. */
	@Inject
	private QueryBuilder queryBuilder;

	/** The context name. */
	@Inject
	@Config(name = SemanticConfigurationProperties.SEMANTIC_DB_CONTEXT_NAME, defaultValue = "http://ittruse.ittbg.com/data/enterpriseManagementFramework")
	private String contextName;

	/** The context. */
	private URI context;

	/** The default container. */
	@Inject
	@Config(name = EmfConfigurationProperties.DEFAULT_CONTAINER)
	private String defaultContainer;

	/** The service register. */
	@Inject
	private ServiceRegister serviceRegister;

	/** The rdf type. */
	private String RDF_TYPE;

	@Inject
	private SemanticPropertiesReadConverter readConverter;

	@Inject
	private SemanticPropertiesWriteConverter writeConverter;

	/**
	 * {@inheritDoc}
	 */
	@PostConstruct
	public void postConstruct() {
		RDF_TYPE = namespaceRegistryService.getShortUri(RDF.TYPE);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	@SuppressWarnings("unchecked")
	@TransactionAttribute(TransactionAttributeType.REQUIRED)
	public <S extends Serializable, E extends Entity<S>> E saveOrUpdate(E entity) {
		if (entity instanceof Instance) {
			return (E) saveOrUpdateInternal((Instance) entity, null);
		}
		return entity;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	@SuppressWarnings("unchecked")
	@TransactionAttribute(TransactionAttributeType.REQUIRED)
	public <S extends Serializable, E extends Entity<S>> E saveOrUpdate(E entity, E oldEntity) {
		if (entity instanceof Instance) {
			if (oldEntity == null) {
				return (E) saveOrUpdateInternal((Instance) entity, null);
				// we process the other argument only if the same class as the original
				// also the DB id's (URIs) should match at least
			} else if (entity.getClass().equals(oldEntity.getClass())
					&& EqualsHelper.nullSafeEquals(entity.getId(), oldEntity.getId())) {
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
		if (entity.getProperties().containsKey(DefaultProperties.TRANSIENT_SEMANTIC_INSTANCE)) {
			LOGGER.trace("Skipped instance marked for persist in semantic DB with id="
					+ entity.getId() + " of type " + entity.getClass());
			return entity;
		}
		TimeTracker timeTracker = new TimeTracker().begin();

		// create the RDF model to be stored in the repository
		Model addModel = new LinkedHashModel();
		Model removeModel = new LinkedHashModel();

		Resource subject = writeConverter.buildModelForInstance(entity, oldEntity, addModel,
				removeModel);

		RepositoryConnection connection = repositoryConnection.get();
		try {
			// if there is no old entity we should not remove anything
			if (oldEntity != null) {
				Model diff = new LinkedHashModel(addModel);
				diff.retainAll(removeModel);
				// remove duplicate entries from both models to minimize
				// computation. No need to remove something that will be added back again
				removeModel.removeAll(diff);
				addModel.removeAll(diff);
				// remove statements build in the remove model
				connection.remove(removeModel, getContext());

				if (SemanticOperationLogger.getIsEnabled()) {
					SemanticOperationLogger.addLogOperation("D", removeModel, null);
				}

			} else {
				// add the full URI as a data property
				// of the subject to be synchronised with Solr
				// TODO : to be removed on subsequent
				// Ontotext Forest Solr Connector release
				addModel.add(subject, EMF.URI, valueFactory.createLiteral(namespaceRegistryService
						.buildFullUri(entity.getId().toString())), getContext());
				// ensure the is deleted is set to false
				addModel.add(subject, EMF.IS_DELETED, valueFactory.createLiteral(false),
						getContext());
			}

			// add the new model to the repository
			connection.add(addModel, getContext());

			if (SemanticOperationLogger.getIsEnabled()) {
				SemanticOperationLogger.addLogOperation("A", addModel, null);
			}

			// update the id
			SequenceEntityGenerator.persisted(entity);
			if (LOGGER.isDebugEnabled() && !addModel.isEmpty()) {
				// skip printing the thumbnail image from the model
				String modelString = addModel.toString();
				if (modelString.contains("thumbnail=")) {
					int thumbnailIndex = modelString.indexOf("thumbnail=");
					int afterThumbnailIndex = modelString.indexOf(',', thumbnailIndex);
					if (afterThumbnailIndex == -1) {
						afterThumbnailIndex = modelString.length() - 2;
					}

					modelString = modelString.substring(0, thumbnailIndex + 30) + "..."
							+ modelString.substring(afterThumbnailIndex, modelString.length());
				}
				LOGGER.debug("Model [{}]  added to reporitory.", modelString);
			}
		} catch (RepositoryException e) {
			try {
				connection.rollback();
			} catch (RepositoryException e1) {
				LOGGER.error("Failed to rollback connection " + e1.getMessage());
				debug(e.getMessage(), e);
			}
			LOGGER.error("Failed adding following statements to repository: ", e);
			throw new SemanticPersistenceException(e);
		} finally {
			if (LOGGER.isDebugEnabled()) {
				debug(entity.getClass().getSimpleName(), " save in SemanticDb took ",
						timeTracker.stopInSeconds(), " s");
			}
		}
		return entity;
	}

	/**
	 * Builds URI object from the given short URI in string representation.
	 *
	 * @param shortUri
	 *            the short uri
	 * @return the URI or <code>null</code> if the argument is <code>null</code>.
	 */
	private URI buildUri(String shortUri) {
		if (shortUri == null) {
			return null;
		}
		return valueFactory.createURI(namespaceRegistryService.buildFullUri(shortUri));
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	@TransactionAttribute(TransactionAttributeType.NOT_SUPPORTED)
	public <S extends Serializable, E extends Entity<S>> E find(Class<E> clazz, Object id) {
		if ((id == null) || "null".equals(id)) {
			// non existent entity
			return null;
		}

		Collection<Serializable> objectUris = new ArrayList<>(1);
		objectUris.add((Serializable) id);
		List<Pair<String, Object>> parameters = new ArrayList<>();
		parameters.add(new Pair<String, Object>("URIS", objectUris));

		List<Object> fetchWithNamed = fetchWithNamed(QueryBuilder.SELECT_BY_IDS, parameters);

		if (fetchWithNamed.isEmpty()) {
			return null;
		}

		return clazz.cast(fetchWithNamed.get(0));
	}

	/**
	 * Execute tuple query.
	 *
	 * @param query
	 *            the query
	 * @return the tuple query result
	 */
	private TupleQueryResult executeTupleQuery(String query) {
		RepositoryConnection connection = null;
		try {
			if (LOGGER.isDebugEnabled()) {
				debug("Executing tuple query:\n", query.substring(query.indexOf("SELECT")));
			}

			connection = repositoryConnection.get();
			TupleQuery tupleQuery = connection.prepareTupleQuery(QueryLanguage.SPARQL,
					query.toString());
			tupleQuery.setIncludeInferred(false);
			return tupleQuery.evaluate();
		} catch (RepositoryException e) {
			throw new SemanticPersistenceException(e);
		} catch (MalformedQueryException e) {
			throw new SemanticPersistenceException(e);
		} catch (QueryEvaluationException e) {
			throw new SemanticPersistenceException(e);
		} finally {
			if (connection != null) {
				try {
					connection.close();
				} catch (RepositoryException e) {
					LOGGER.error("Failed to close connection", e);
				}
			}
		}
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	@TransactionAttribute(TransactionAttributeType.NOT_SUPPORTED)
	public <S extends Serializable, E extends Entity<S>> E refresh(E entity) {
		return entity;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	@TransactionAttribute(TransactionAttributeType.REQUIRED)
	public <S extends Serializable, E extends Entity<S>> void delete(Class<E> clazz,
			Serializable entityId) {
		if (entityId != null) {
			debug("Deleting resource/s [", entityId.toString(), "]");
			RepositoryConnection connection = repositoryConnection.get();
			if (entityId instanceof Collection) {
				Model model = new LinkedHashModel();
				Model removeModel = new LinkedHashModel();
				// collect all ids from the collection to the model
				for (Object object : (Collection<?>) entityId) {
					deleteInternal(object.toString(), model, removeModel, connection, false);
				}
				// flush the model to db
				deleteInternal(null, model, removeModel, connection, true);
			} else {
				deleteInternal(entityId.toString(), null, null, connection, true);
			}
		}
	}

	/**
	 * Adds a triplet that marks an instance identified by the given URI that the instance is
	 * deleted. The method could add the statement directly to db or populate the given model and
	 * then flush at the end. If model is <code>null</code> the method will populate the connection
	 * directly
	 *
	 * @param shortUri
	 *            the short uri to add or <code>null</code> to ignore
	 * @param model
	 *            the model to populate or <code>null</code> if not needed
	 * @param removeModel
	 *            the remove model
	 * @param connection
	 *            the connection to use for uri/model flush. Required when model is not present or
	 *            flush is required.
	 * @param flush
	 *            to flush or not the given model using the given connection. The value is ignored
	 *            if no model is passed
	 */
	private void deleteInternal(String shortUri, Model model, Model removeModel,
			RepositoryConnection connection, boolean flush) {
		if (shortUri != null) {
			try {
				URI resource = buildUri(shortUri);
				if (model == null) {
					connection.add(resource, EMF.IS_DELETED, valueFactory.createLiteral(true),
							getContext());
					connection.remove(resource, EMF.IS_DELETED, valueFactory.createLiteral(false));
				} else {
					model.add(resource, EMF.IS_DELETED, valueFactory.createLiteral(true));
					removeModel.add(resource, EMF.IS_DELETED, valueFactory.createLiteral(false));
				}
			} catch (RepositoryException e) {
				throw new SemanticPersistenceException("Failed marking resource with URI ["
						+ shortUri + "] as deleted", e);
			}
		}
		if (flush && (model != null)) {
			try {
				connection.remove(removeModel, getContext());
				connection.add(model, getContext());
			} catch (RepositoryException e) {
				throw new SemanticPersistenceException("Failed add model [" + model + "] to DB", e);
			}
		}
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	@SuppressWarnings("unchecked")
	@TransactionAttribute(TransactionAttributeType.NOT_SUPPORTED)
	public <R, E extends Pair<String, Object>> List<R> fetchWithNamed(String namedQuery,
			List<E> params) {
		TimeTracker timeTracker = new TimeTracker().begin();
		String query = queryBuilder.buildQueryByName(namedQuery, params);
		if (StringUtils.isNullOrEmpty(query)) {
			throw new SemanticPersistenceException("Undefined named query [" + namedQuery + "]");
		}

		TupleQueryResult result = executeTupleQuery(query);

		try {
			// if nothing is found
			if (!result.hasNext()) {
				debug("No data found for query: ", namedQuery, " and took ",
						timeTracker.elapsedTimeInSeconds(), " s");
				return Collections.emptyList();
			}

			Map<String, Map<Value, Set<Value>>> instances = readConverter
					.buildQueryResultModel(result);

			return (List<R>) convertQueryResults(instances);

		} catch (QueryEvaluationException e) {
			throw new SemanticPersistenceException("Failed evaluating named query [" + namedQuery
					+ "]", e);
		} finally {
			if (LOGGER.isDebugEnabled()) {
				debug("SemanticDb load took ", String.valueOf(timeTracker.stopInSeconds()), " s");
			}
			try {
				result.close();
			} catch (QueryEvaluationException e) {
				LOGGER.error("Failed closing result iteration.");
			}
		}
	}

	/**
	 * Convert query results.
	 *
	 * @param instances
	 *            the instances
	 * @return the list
	 */
	private List<Instance> convertQueryResults(Map<String, Map<Value, Set<Value>>> instances) {
		List<Instance> results = new ArrayList<>(instances.size());

		for (Entry<String, Map<Value, Set<Value>>> entry : instances.entrySet()) {
			Map<Value, Set<Value>> instanceProperties = entry.getValue();

			Set<Value> rdfTypes = instanceProperties.remove(RDF.TYPE);
			if (rdfTypes == null) {
				LOGGER.error("Invalid entity: no rdf:type present!");
				continue;
			}
			DataTypeDefinition dataTypeDefinition = null;
			Value rdfTypeValue = null;
			for (Value value : rdfTypes) {
				dataTypeDefinition = dictionaryService.getDataTypeDefinition(value.stringValue());
				if (dataTypeDefinition != null) {
					rdfTypeValue = value;
					break;
				}
			}
			if (dataTypeDefinition == null) {
				LOGGER.error("Invalid entity " + RDF.TYPE + " = " + rdfTypes);
				continue;
			}
			// pre fetch and check the type if present not to continue
			Set<Value> types = instanceProperties.get(EMF.DEFINITION_ID);
			if ((types == null) || types.isEmpty()) {
				types = instanceProperties.get(EMF.TYPE);
				if ((types == null) || types.isEmpty()) {
					LOGGER.error("No definition type specified " + EMF.TYPE + " = " + rdfTypeValue);
					continue;
				}
			}

			Class<?> javaClass = dataTypeDefinition.getJavaClass();
			Instance instance = (Instance) ReflectionUtils.newInstance(javaClass);

			Value parentUri = removeAndGetFirstValue(EMF.PARENT, instanceProperties);
			Value parentType = removeAndGetFirstValue(EMF.PARENT_TYPE, instanceProperties);
			if ((parentUri != null) && (parentType != null) && (instance instanceof OwnedModel)) {
				// set owning reference
				InstanceReference owningReference = typeConverter.convert(InstanceReference.class,
						parentType.stringValue());
				owningReference.setIdentifier(namespaceRegistryService.getShortUri(parentUri
						.stringValue()));
				((OwnedModel) instance).setOwningReference(owningReference);
			}

			instance.setId(namespaceRegistryService.getShortUri(entry.getKey()));

			Value value = null;
			if (instance instanceof DMSInstance) {
				value = removeAndGetFirstValue(EMF.DMS_ID, instanceProperties);
				if (value != null) {
					((DMSInstance) instance).setDmsId(value.stringValue());
				}
			}

			if (instance instanceof CMInstance) {
				value = getFirstValue(DCTERMS.IDENTIFIER, instanceProperties);
				if (value != null) {
					((CMInstance) instance).setContentManagementId(value.stringValue());
				}
			}

			if (instance instanceof TenantAware) {
				value = removeAndGetFirstValue(EMF.CONTAINER, instanceProperties);
				if (value != null) {
					((TenantAware) instance).setContainer(value.stringValue());
				} else {
					((TenantAware) instance).setContainer(defaultContainer);
				}
			}

			value = removeAndGetFirstValue(EMF.REVISION, instanceProperties);
			DefinitionModel definition = null;
			if (value != null) {
				if (value instanceof ValueProxy) {
					value = ((ValueProxy) value).getValue();
				}
				instance.setRevision((Long) ValueConverter.convertValue(value));
				for (Value type : types) {
					instance.setIdentifier(type.stringValue());
					definition = dictionaryService.getInstanceDefinition(instance);
					if (definition != null) {
						break;
					}
				}
			} else {
				// if the revision is missing then get the latest revision for the specified
				// definition
				InstanceService<Instance, DefinitionModel> service = serviceRegister
						.getInstanceService(instance);
				for (Value type : types) {
					instance.setIdentifier(type.stringValue());
					definition = dictionaryService.getDefinition(
							service.getInstanceDefinitionClass(), instance.getIdentifier());
					if (definition != null) {
						break;
					}
				}
				if (definition != null) {
					instance.setRevision(definition.getRevision());
				}
			}
			if (definition == null) {
				LOGGER.warn("No definition found for Instance with URI = " + instance.getId()
						+ ", definition type=" + instance.getIdentifier() + ", revision="
						+ instance.getRevision());
			}
			if (StringUtils.isNullOrEmpty(instance.getIdentifier())) {
				LOGGER.warn("No definition identifier found when loading the the Instance with URI = "
						+ instance.getId());
				continue;
			}

			Map<String, Serializable> properties = CollectionUtils
					.createLinkedHashMap(instanceProperties.size());

			Map<String, Set<Value>> convertedKeys = readConverter
					.convertPropertiesNames(instanceProperties);

			readConverter.convertPropertiesFromSemanticToInternalModel(definition, convertedKeys,
					properties);
			properties.put(RDF_TYPE, rdfTypeValue.stringValue());

			properties.put(PersistentPropertyKeys.URI.getKey(),
					namespaceRegistryService.buildFullUri((String) instance.getId()));
			instance.setProperties(properties);
			results.add(instance);
		}

		return results;
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
	private Value getFirstValue(Value key, Map<Value, Set<Value>> map) {
		Set<Value> set = map.get(key);
		if ((set != null) && !set.isEmpty()) {
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
	private Value removeAndGetFirstValue(Value key, Map<Value, Set<Value>> map) {
		Set<Value> set = map.remove(key);
		if ((set != null) && !set.isEmpty()) {
			return set.iterator().next();
		}
		return null;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	@TransactionAttribute(TransactionAttributeType.NOT_SUPPORTED)
	public <R, E extends Pair<String, Object>> List<R> fetch(String query, List<E> params) {
		return null;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	@TransactionAttribute(TransactionAttributeType.REQUIRED)
	public <E extends Pair<String, Object>> int executeUpdate(String namedQuery, List<E> params) {
		return 0;
	}

	/**
	 * Gets the context URI.
	 *
	 * @return the context URI
	 */
	private URI getContext() {
		if (context == null) {
			context = valueFactory.createURI(contextName);
		}
		return context;
	}

	/**
	 * Dumps the passed string messages into the log if it is debug enabled.
	 *
	 * @param messages
	 *            the messages
	 */
	private void debug(Object... messages) {
		if (LOGGER.isDebugEnabled()) {
			StringBuilder builder = new StringBuilder();
			for (Object message : messages) {
				builder.append(message);
			}
			LOGGER.debug(builder.toString());
		}
	}
}
