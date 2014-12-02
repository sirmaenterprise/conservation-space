package com.sirma.itt.emf.semantic.persistence;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import javax.annotation.PostConstruct;
import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import org.apache.log4j.Logger;
import org.openrdf.model.Model;
import org.openrdf.model.Resource;
import org.openrdf.model.Statement;
import org.openrdf.model.URI;
import org.openrdf.model.Value;
import org.openrdf.model.ValueFactory;
import org.openrdf.model.vocabulary.RDF;

import com.sirma.itt.commons.utils.string.StringUtils;
import com.sirma.itt.emf.converter.TypeConverter;
import com.sirma.itt.emf.db.SequenceEntityGenerator;
import com.sirma.itt.emf.definition.DictionaryService;
import com.sirma.itt.emf.definition.model.DataTypeDefinition;
import com.sirma.itt.emf.definition.model.PropertyDefinition;
import com.sirma.itt.emf.definition.model.RegionDefinition;
import com.sirma.itt.emf.definition.model.RegionDefinitionModel;
import com.sirma.itt.emf.domain.model.DefinitionModel;
import com.sirma.itt.emf.domain.model.Entity;
import com.sirma.itt.emf.domain.model.Purposable;
import com.sirma.itt.emf.extension.PersistentProperties.PersistentPropertyKeys;
import com.sirma.itt.emf.instance.model.DMSInstance;
import com.sirma.itt.emf.instance.model.Instance;
import com.sirma.itt.emf.instance.model.InstanceReference;
import com.sirma.itt.emf.instance.model.OwnedModel;
import com.sirma.itt.emf.instance.model.TenantAware;
import com.sirma.itt.emf.plugin.ExtensionPoint;
import com.sirma.itt.emf.properties.DefaultProperties;
import com.sirma.itt.emf.properties.dao.SemanticNonPersistentPropertiesExtension;
import com.sirma.itt.emf.semantic.model.EmptyModel;
import com.sirma.itt.emf.util.CollectionUtils;
import com.sirma.itt.emf.util.EqualsHelper;
import com.sirma.itt.emf.util.EqualsHelper.MapValueComparison;
import com.sirma.itt.semantic.NamespaceRegistryService;
import com.sirma.itt.semantic.model.vocabulary.EMF;
import com.sirma.itt.semantic.model.vocabulary.Proton;

/**
 * Utility class for building semantic persistent model used to store instance changes to database.
 * 
 * @author BBonev
 */
@ApplicationScoped
public class SemanticPropertiesWriteConverter extends BasePropertiesConverter {

	/** The Constant LOGGER. */
	private static final Logger LOGGER = Logger.getLogger(SemanticPropertiesWriteConverter.class);
	/** The rdf type. */
	private String rdfType;
	/** The Constant FORBIDDEN_PROPERTIES. */
	private Set<String> FORBIDDEN_PROPERTIES;

	/** The value factory. */
	@Inject
	private ValueFactory valueFactory;

	@Inject
	private DictionaryService dictionaryService;

	/** The type converter. */
	@Inject
	private TypeConverter typeConverter;

	/** Define and initialize RDF URIs of all known so far instance classes. */
	private static Map<String, URI> instanceUris = new HashMap<>();

	/** The non persistent properties. */
	@Inject
	@ExtensionPoint(value = SemanticNonPersistentPropertiesExtension.TARGET_NAME)
	private Iterable<SemanticNonPersistentPropertiesExtension> nonPersistentProperties;

	private static final Model EMPTY_MODEL = new EmptyModel();

	/**
	 * Initializes instance variables.
	 */
	@PostConstruct
	public void postConstruct() {
		rdfType = namespaceRegistryService.getShortUri(RDF.TYPE);

		FORBIDDEN_PROPERTIES = new LinkedHashSet<>(50);
		for (SemanticNonPersistentPropertiesExtension extension : nonPersistentProperties) {
			FORBIDDEN_PROPERTIES.addAll(extension.getNonPersistentProperties());
		}
	}

	/**
	 * Builds add and remove models from the new and old instances. The method creates diffs based
	 * on the given instances and updates respected models with statements needed to be executed in
	 * order to save the changes detected in the instances. If the old instance is <code>null</code>
	 * the method will build the model in such way only to add the new instance without duplicates
	 * in semantic database.
	 * <p>
	 * NOTE: For more accurate and optimal persist of an instance the old instance is strongly
	 * recommended.
	 * 
	 * @param newInstance
	 *            the new instance
	 * @param oldInstance
	 *            the old instance if any (optional)
	 * @param addModel
	 *            the model that will be added to database
	 * @param removeModel
	 *            the model to will be removed from database
	 * @return the target instance URI as resource
	 */
	public Resource buildModelForInstance(Instance newInstance, Instance oldInstance,
			Model addModel, Model removeModel) {
		if (newInstance.getProperties().containsKey(DefaultProperties.TRANSIENT_SEMANTIC_INSTANCE)) {
			trace(LOGGER, "Skipped instance marked for persist in semantic DB with id=",
					newInstance.getId(), " of type ", newInstance.getClass());
			return null;
		}

		// remove properties that should not be persisted
		newInstance.getProperties().keySet().removeAll(FORBIDDEN_PROPERTIES);
		if (oldInstance != null) {
			oldInstance.getProperties().keySet().removeAll(FORBIDDEN_PROPERTIES);
		}

		// if the passed instance is newly created we should generate its identifier
		SequenceEntityGenerator.generateStringId(newInstance, true);

		// set the subject's type
		Resource subject = valueFactory.createURI(namespaceRegistryService.buildFullUri(newInstance
				.getId().toString()));

		if (oldInstance == null) {
			addModel.add(subject, RDF.TYPE, getInstanceUri(newInstance));
		}

		// add instance's member variables
		setInstanceMemberVariables(newInstance, oldInstance, addModel, removeModel, subject);

		Map<String, MapValueComparison> comparison;
		if (oldInstance == null) {
			comparison = EqualsHelper.getMapComparison(newInstance.getProperties(),
					Collections.<String, Serializable> emptyMap());
		} else {
			comparison = EqualsHelper.getMapComparison(newInstance.getProperties(),
					oldInstance.getProperties());
		}

		// add instance properties to RDF model
		RegionDefinitionModel regionDefinitionModel = (RegionDefinitionModel) dictionaryService
				.getInstanceDefinition(newInstance);
		populateModel(regionDefinitionModel, newInstance, oldInstance, comparison, addModel,
				removeModel, subject);

		return subject;
	}

	/**
	 * Populate region definition model.
	 * 
	 * @param definition
	 *            the definition
	 * @param instance
	 *            the instance
	 * @param old
	 *            the old
	 * @param diff
	 *            the diff
	 * @param model
	 *            the model
	 * @param removeModel
	 *            the remove model
	 * @param subject
	 *            the subject
	 */
	private void populateModel(RegionDefinitionModel definition, Instance instance, Instance old,
			Map<String, MapValueComparison> diff, Model model, Model removeModel, Resource subject) {
		Set<String> persistedProperties = CollectionUtils.createLinkedHashSet(instance
				.getProperties().size());
		Set<String> forbiddenProperties = new LinkedHashSet<>();

		// if there is a definition for the instance we will create a model for it otherwise will
		// use base model creation
		if (definition != null) {
			populateModel(definition, instance, old, diff, model, removeModel, subject,
					persistedProperties, forbiddenProperties);
			for (RegionDefinition regionDefinition : definition.getRegions()) {
				populateModel(regionDefinition, instance, old, diff, model, removeModel, subject,
						persistedProperties, forbiddenProperties);
			}
		}

		// save the remaining extra properties
		for (Entry<String, Serializable> entry : instance.getProperties().entrySet()) {
			String name = entry.getKey();
			if (persistedProperties.contains(name) || forbiddenProperties.contains(name)) {
				continue;
			}
			if (!name.contains(NamespaceRegistryService.SHORT_URI_DELIMITER)) {
				// we have a property that is not a URI and cannot be saved
				continue;
			}

			MapValueComparison comparison = diff.get(name);
			if (comparison == null) {
				// the value is not present in neither of the instances
				continue;
			}

			Serializable value = entry.getValue();
			if (value instanceof Instance) {
				Instance oldInstance = null;
				if ((old != null) && (old.getProperties().get(name) instanceof Instance)) {
					oldInstance = (Instance) old.getProperties().get(name);
				}
				value = buildModelForInstance((Instance) value, oldInstance, model, removeModel);
			} else if (value instanceof Collection) {
				LinkedList<Object> linkedList = new LinkedList<>();
				for (Object object : (Collection<?>) value) {
					if (object instanceof Instance) {
						Instance oldInstance = null;
						if ((old != null) && (old.getProperties().get(name) instanceof Instance)) {
							oldInstance = (Instance) old.getProperties().get(name);
						}
						// FIXME: here should be changed how is determined the second (old)
						// instance, because passing the original oldInstance is wrong
						Serializable subElement = buildModelForInstance((Instance) object,
								oldInstance, model, removeModel);
						linkedList.add(subElement);
					} else {
						linkedList.add(object);
					}
				}
				value = linkedList;
			}
			Object statements;
			switch (comparison) {
				case LEFT_ONLY:
					statements = createStatementForLiteralValue(subject, value, name);
					addStatementsToModel(model, statements);
					if (old == null) {
						// if the old instance is not passed we should mark the statements for
						// removal but if the model is present then this is just an add
						addStatementsToModel(removeModel, statements);
					}
					break;
				case RIGHT_ONLY:
					// remove the old value
					statements = createStatementForLiteralValue(subject,
							old.getProperties().get(name), name);
					addStatementsToModel(removeModel, statements);
					break;
				case NOT_EQUAL:
					// remove the old value and add the new one
					statements = createStatementForLiteralValue(subject,
							old.getProperties().get(name), name);
					addStatementsToModel(removeModel, statements);

					statements = createStatementForLiteralValue(subject, value, name);
					addStatementsToModel(model, statements);
					break;
				default:
					break;
			}
		}
	}

	/**
	 * Populate single definition model.
	 * 
	 * @param definition
	 *            the definition
	 * @param instance
	 *            the instance
	 * @param old
	 *            the old
	 * @param diff
	 *            the diff
	 * @param model
	 *            the model
	 * @param removeModel
	 *            the remove model
	 * @param subject
	 *            the subject
	 * @param persistedProperties
	 *            the persisted properties
	 * @param forbiddenProperties
	 *            the forbidden properties
	 */
	private void populateModel(DefinitionModel definition, Instance instance, Instance old,
			Map<String, MapValueComparison> diff, Model model, Model removeModel, Resource subject,
			Set<String> persistedProperties, Set<String> forbiddenProperties) {

		for (PropertyDefinition field : definition.getFields()) {
			// if field URI is not empty
			// or its URI is not declared as not used
			// its value is to be persisted in the semantic repository
			String name = field.getIdentifier();
			if (StringUtils.isNullOrEmpty(field.getUri())
					|| EqualsHelper.nullSafeEquals(field.getUri(),
							DefaultProperties.NOT_USED_PROPERTY_VALUE, false)
					|| rdfType.equals(name)) {
				forbiddenProperties.add(name);
				continue;
			}

			MapValueComparison comparison = diff.get(name);
			if (comparison == null) {
				// the value is not present in neither of the instances
				continue;
			}
			Object statements;
			switch (comparison) {
				case LEFT_ONLY:
					statements = buildStatements(field, instance.getProperties().get(name), null,
							subject, persistedProperties, model, removeModel);
					addStatementsToModel(model, statements);
					if (old == null) {
						// if the old instance is not passed we should mark the statements for
						// removal
						// but if the model is present then this is just an add
						addStatementsToModel(removeModel, statements);
					}
					break;
				case RIGHT_ONLY:
					statements = buildStatements(field, old.getProperties().get(name), null,
							subject, persistedProperties, model, removeModel);
					addStatementsToModel(removeModel, statements);
					break;
				case NOT_EQUAL:
					Serializable oldValue = old.getProperties().get(name);
					Serializable currentValue = instance.getProperties().get(name);

					statements = buildStatements(field, currentValue, oldValue, subject,
							persistedProperties, model, removeModel);
					addStatementsToModel(model, statements);

					if (oldValue instanceof Instance) {
						// if the value is instance then we does not need to process the whole
						// instance again but only the URI of the instance if changed for that
						// reason an empty model is used as arguments of the method
						statements = buildStatements(field, oldValue, null, subject,
								persistedProperties, EMPTY_MODEL, EMPTY_MODEL);
					} else {
						statements = buildStatements(field, oldValue, null, subject,
								persistedProperties, model, removeModel);
					}
					addStatementsToModel(removeModel, statements);
					break;
				default:
					break;
			}
		}
	}

	/**
	 * Builds the statements for the given value and property definition
	 * 
	 * @param field
	 *            the field
	 * @param value
	 *            the value
	 * @param oldValue
	 *            old value if any
	 * @param subject
	 *            the subject
	 * @param persistedProperties
	 *            the persisted properties
	 * @param addModel
	 *            the add model (used for chained calls)
	 * @param removeModel
	 *            the remove model (used for chained calls)
	 * @return the created statement/list of statements or <code>null</code>
	 */
	private Object buildStatements(PropertyDefinition field, Serializable value,
			Serializable oldValue, Resource subject, Set<String> persistedProperties,
			Model addModel, Model removeModel) {
		// initialize predicate and object to be stored in semantic
		// model if value is set in the case instance property map
		if (value != null) {
			// mark the properties as processed event if the data conversion does not pass it
			// will not pass later also
			persistedProperties.add(field.getIdentifier());

			if (isResourceField(field)) {
				return handleUserData(field, value, subject);
			} else if (isUriField(field)) {
				if ((value instanceof String) || (value instanceof Resource)) {
					return createStatementForUriValue(subject, value, field.getUri());
				} else if (value instanceof Entity) {
					return createStatementForUriValue(subject, ((Entity<?>) value).getId(),
							field.getUri());
				} else if (value instanceof InstanceReference) {
					return createStatementForUriValue(subject,
							((InstanceReference) value).getIdentifier(), field.getUri());
				} else {
					LOGGER.warn("Trying to persist not supported element as URI.");
				}
			} else if (isSubInstanceField(field)) {
				if (value instanceof Instance) {
					Instance oldInstance = null;
					if (oldValue instanceof Instance) {
						oldInstance = (Instance) oldValue;
					}
					// iterate a sub instance and create the models for it
					Resource resource = buildModelForInstance((Instance) value, oldInstance,
							addModel, removeModel);
					if (resource != null) {
						return createStatementForUriValue(subject, resource, field.getUri());
					}
					debug(LOGGER, "Passed a transient instance to persist for key=",
							field.getName(), " and uri=", field.getUri());
				} else if (value instanceof Entity) {
					return createStatementForUriValue(subject, ((Entity<?>) value).getId(),
							field.getUri());
				} else if (value instanceof String) {
					return createStatementForUriValue(subject, value, field.getUri());
				} else if (value instanceof InstanceReference) {
					return createStatementForUriValue(subject,
							((InstanceReference) value).getIdentifier(), field.getUri());

				}
				LOGGER.warn("Trying to persist not supported element as Instance URI.");
			} else {
				// convert to proper type before adding to semantic
				Object converted = typeConverter.convert(field.getDataType().getJavaClass(), value);
				return createStatementForLiteralValue(subject, (Serializable) converted,
						field.getUri());
			}
		}
		return null;
	}

	/**
	 * Handle user data.
	 * 
	 * @param field
	 *            the field
	 * @param value
	 *            the value
	 * @param subject
	 *            the subject
	 * @return the object
	 */
	private Object handleUserData(PropertyDefinition field, Serializable value, Resource subject) {
		if (Boolean.TRUE.equals(field.isMultiValued()) && (value instanceof Collection)) {
			Collection<?> resources = (Collection<?>) value;
			List<Statement> result = new ArrayList<>(resources.size());
			for (Object user : resources) {
				Serializable uri = getUserUri((Serializable) user);
				if (uri != null) {
					Statement statement = createStatementForUriValue(subject, uri, field.getUri());
					if (statement != null) {
						result.add(statement);
					}
				}
			}
			return result;
		}

		Serializable uri = getUserUri(value);
		if (uri != null) {
			Statement statement = createStatementForUriValue(subject, uri, field.getUri());
			if (statement != null) {
				return statement;
			}
		}
		// if not a resource or something else save it at least as literal
		return createStatementForLiteralValue(subject, value, field.getUri());
	}

	/**
	 * Gets the user uri.
	 * 
	 * @param value
	 *            the value
	 * @return the user uri
	 */
	private Serializable getUserUri(Serializable value) {
		// will try to load it as regular user
		com.sirma.itt.emf.resources.model.Resource resource = resourceService.findResource(value);
		if (resource != null) {
			return resource.getId();
		}
		return null;
	}

	/**
	 * Creates the statement for uri value.
	 * 
	 * @param subject
	 *            the subject
	 * @param value
	 *            the id
	 * @param propertyUri
	 *            the field
	 * @return the created statement
	 */
	private Statement createStatementForUriValue(Resource subject, Serializable value,
			String propertyUri) {
		if (value == null) {
			return null;
		}
		Resource object;
		if (value instanceof Resource) {
			object = (Resource) value;
		} else {
			object = buildUri(value.toString());
		}
		// create the predicate
		URI predicate = valueFactory.createURI(namespaceRegistryService.buildFullUri(propertyUri));

		trace(LOGGER, "Creating statement for [", subject.stringValue(), "] with predicate [",
				predicate.stringValue(), "] and value [", object.toString(), "]");

		return valueFactory.createStatement(subject, predicate, object);
	}

	/**
	 * Creates the statement for literal value.
	 * 
	 * @param subject
	 *            the subject
	 * @param value
	 *            the value
	 * @param uriPredicate
	 *            the uri predicate
	 * @return the created statement or list of statements or <code>null</code>
	 */
	private Object createStatementForLiteralValue(Resource subject, Serializable value,
			String uriPredicate) {
		// if multi value we will iterate and add every single value to the model
		if (value instanceof Collection) {
			Collection<?> values = (Collection<?>) value;
			List<Object> result = new ArrayList<>(values.size());
			for (Object object : values) {
				if (object instanceof Serializable) {
					Object literalValue = createStatementForLiteralValue(subject,
							(Serializable) object, uriPredicate);
					if (literalValue != null) {
						result.add(literalValue);
					}
				} else {
					LOGGER.warn("Ignored non serializable value from collection: " + object);
				}
			}
			return result;
		} else if ((value instanceof Instance) && !(value instanceof Resource)) {
			// generally it should not come here, but if it does we will only save the URI
			LOGGER.warn("Trying to save an instance " + value.getClass().getSimpleName()
					+ " as property. Going to save only the URI " + ((Instance) value).getId());
			LOGGER.debug("The instance is: " + value);
			return createStatementForUriValue(subject, ((Instance) value).getId(), uriPredicate);
		}
		Value object;
		if (value instanceof Resource) {
			object = (Value) value;
		} else {
			object = ValueConverter.createLiteral(value);
		}
		if (object != null) {
			trace(LOGGER, "Creating literal for property with uri [", uriPredicate, "]");

			// create the predicate
			URI predicate = buildUri(uriPredicate);

			trace(LOGGER, "Creating statement for [", subject.stringValue(), "] with predicate [",
					predicate.stringValue(), "] and value [", object.toString(), "]");

			return valueFactory.createStatement(subject, predicate, object);
		}
		return null;
	}

	/**
	 * Adds the statements to model.
	 * 
	 * @param model
	 *            the model
	 * @param object
	 *            the object
	 */
	private void addStatementsToModel(Model model, Object object) {
		if (object instanceof Statement) {
			model.add((Statement) object);
		} else if (object instanceof Collection) {
			for (Object statement : (Collection<?>) object) {
				addStatementsToModel(model, statement);
			}
		} else if (object != null) {
			LOGGER.warn("Trying to add not a statement to the model");
		}
	}

	/**
	 * Gets the instance {@link URI}.
	 * 
	 * @param instance
	 *            the instance
	 * @return the instance {@link URI}
	 */
	public URI getInstanceUri(Instance instance) {
		Serializable serializable = instance.getProperties().get(rdfType);
		if (serializable != null) {
			return valueFactory.createURI(serializable.toString());
		}
		String name = instance.getClass().getName();
		URI uri = instanceUris.get(name);
		if (uri == null) {
			DataTypeDefinition typeDefinition = dictionaryService.getDataTypeDefinition(name);
			if ((typeDefinition != null) && (typeDefinition.getFirstUri() != null)) {
				uri = valueFactory.createURI(typeDefinition.getFirstUri());
				instanceUris.put(name, uri);
			}
		}
		if (uri == null) {
			throw new IllegalArgumentException("Uknown instance type [" + name + "]");
		}
		return uri;
	}

	/**
	 * Add instance's member variables and owning reference to model if applicable and present.
	 * 
	 * @param entity
	 *            the entity
	 * @param old
	 *            the old instance if any
	 * @param model
	 *            the model
	 * @param removeModel
	 *            the remove model
	 * @param subject
	 *            the subject
	 */
	private void setInstanceMemberVariables(Instance entity, Instance old, Model model,
			Model removeModel, Resource subject) {
		if ((entity.getRevision() != null) && (entity.getRevision().intValue() != 0)) {
			if (old != null) {
				addToModel(model, removeModel, subject, EMF.REVISION, entity.getRevision(),
						old.getRevision());
			} else {
				addMemberVariable(model, subject, EMF.REVISION, entity.getRevision());
				addMemberVariable(removeModel, subject, EMF.REVISION, entity.getRevision());
			}
		}
		if (entity.getIdentifier() != null) {
			if (old != null) {
				addToModel(model, removeModel, subject, EMF.DEFINITION_ID, entity.getIdentifier(),
						old.getIdentifier());
			} else {
				addMemberVariable(model, subject, EMF.DEFINITION_ID, entity.getIdentifier());
				addMemberVariable(removeModel, subject, EMF.DEFINITION_ID, entity.getIdentifier());
			}
		}
		if (entity instanceof DMSInstance) {
			if (old != null) {
				addToModel(model, removeModel, subject, EMF.DMS_ID,
						((DMSInstance) entity).getDmsId(), ((DMSInstance) old).getDmsId());
			} else {
				addMemberVariable(model, subject, EMF.DMS_ID, ((DMSInstance) entity).getDmsId());
				addMemberVariable(removeModel, subject, EMF.DMS_ID,
						((DMSInstance) entity).getDmsId());
			}
		}
		if (entity instanceof TenantAware) {
			if (old != null) {
				addToModel(model, removeModel, subject, EMF.CONTAINER,
						((TenantAware) entity).getContainer(), ((TenantAware) old).getContainer());
			} else {
				addMemberVariable(model, subject, EMF.CONTAINER,
						((TenantAware) entity).getContainer());
				addMemberVariable(removeModel, subject, EMF.CONTAINER,
						((TenantAware) entity).getContainer());
			}
		}
		if (entity instanceof Purposable) {
			if (old != null) {
				addToModel(model, removeModel, subject, EMF.PURPOSE,
						((Purposable) entity).getPurpose(), ((Purposable) old).getPurpose());
			} else {
				addMemberVariable(model, subject, EMF.PURPOSE, ((Purposable) entity).getPurpose());
				addMemberVariable(removeModel, subject, EMF.PURPOSE,
						((Purposable) entity).getPurpose());
			}
		}

		// set parent of entity if available and present
		if (entity instanceof OwnedModel) {
			// we cannot determine whether the parent has been changed or it's attached to new
			// parent but without removing the old one so the only thing we can do is the ensure the
			// link is not duplicated
			// NOTE: if add to removeModel is added all part of relations disappear so we only add
			// the partOf relations and the autolinkObserver adds the simple links
			URI referenceUri = getOwningReferenceUri(entity);
			if (old == null) {
				addMemberVariable(model, subject, Proton.PART_OF, referenceUri);
			}
		}
	}

	/**
	 * Compares the given source values and determines to witch model to update. The method checks
	 * if which model to add the triplets.
	 * <p>
	 * <b>NOTE:</b> If there is no old value to check against at the first place this method SHOULD
	 * <b> NOT</b> called!
	 * <p>
	 * Use: <code>
	 * <pre>
	 * if (old != null) {
	 *    addToModel(addModel, removeModel, subject, predicate, newData, oldData);
	 * } else {
	 *    addMemberVariable(addModel, subject, predicate, newData);
	 *    addMemberVariable(removeModel, subject, predicate, newData);
	 * }
	 * </pre>
	 * </code>
	 * 
	 * @param addModel
	 *            the add model used for adding triplets
	 * @param removeModel
	 *            the remove model used for removing triplets
	 * @param subject
	 *            the target subject for the triplet to be added/removed
	 * @param predicate
	 *            the predicate predicate used for adding/removing
	 * @param addValue
	 *            the value that has been added
	 * @param removeValue
	 *            the old value
	 */
	private void addToModel(Model addModel, Model removeModel, Resource subject, URI predicate,
			Serializable addValue, Serializable removeValue) {
		MapValueComparison diffValues = EqualsHelper.diffValues(addValue, removeValue);
		if (diffValues == null) {
			// both are null nothing to do
			return;
		}
		switch (diffValues) {
			case LEFT_ONLY:
				addMemberVariable(removeModel, subject, predicate, removeValue);
				break;
			case RIGHT_ONLY:
				addMemberVariable(addModel, subject, predicate, addValue);
				break;
			case NOT_EQUAL:
				addMemberVariable(removeModel, subject, predicate, removeValue);
				addMemberVariable(addModel, subject, predicate, addValue);
				break;
			case EQUAL:
				// nothing is changed
				break;
			default:
				break;
		}
	}

	/**
	 * Adds a member variable if it's value is not {@code null}.
	 * 
	 * @param model
	 *            the model
	 * @param subject
	 *            the subject
	 * @param predicate
	 *            the predicate
	 * @param value
	 *            the value
	 */
	private void addMemberVariable(Model model, Resource subject, URI predicate, Serializable value) {
		if (value != null) {
			if (value instanceof URI) {
				model.add(subject, predicate, (URI) value);
			} else {
				model.add(subject, predicate, ValueConverter.createLiteral(value));
			}
		}
	}

	/**
	 * Gets the owning reference URI from the given instance or <code>null</code> if not present.
	 * 
	 * @param instance
	 *            the instance
	 * @return the owning reference URI or <code>null</code>.
	 */
	private URI getOwningReferenceUri(Instance instance) {
		String parentUri = null;
		if (instance instanceof OwnedModel) {
			OwnedModel ownedModel = (OwnedModel) instance;
			if ((ownedModel.getOwningReference() != null)
					&& StringUtils
							.isNotNullOrEmpty(ownedModel.getOwningReference().getIdentifier())) {
				String identifier = ownedModel.getOwningReference().getIdentifier();
				try {
					// REVIEW: probably these checks could be removed as we migrated data to only
					// String ids.
					// some optimizations
					if (!Character.isDigit(identifier.charAt(0))) {
						parentUri = identifier;
					} else {
						// if RDB entity then the URI is in the properties of the instance
						Long.valueOf(identifier);
						parentUri = (String) ownedModel.getOwningInstance().getProperties()
								.get(PersistentPropertyKeys.URI.getKey());
					}
				} catch (NumberFormatException e) {
					// otherwise is the reference identifier
					parentUri = identifier;
				}
			}
			// if not filled try with the instance
			if (ownedModel.getOwningInstance() != null) {
				Serializable id = ownedModel.getOwningInstance().getId();
				if (id instanceof String) {
					parentUri = id.toString();
				} else if (id instanceof Long) {
					parentUri = (String) ownedModel.getOwningInstance().getProperties()
							.get(PersistentPropertyKeys.URI.getKey());
				}
			}
		}
		return buildUri(parentUri);
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

}
