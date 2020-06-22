package com.sirma.itt.emf.semantic.persistence;

import static com.sirma.itt.seip.collections.CollectionUtils.addNonNullValue;
import static com.sirma.itt.seip.collections.CollectionUtils.isEmpty;
import static com.sirma.itt.seip.domain.definition.PropertyDefinition.resolveUri;
import static com.sirma.itt.seip.util.EqualsHelper.diffCollections;
import static com.sirma.itt.seip.util.EqualsHelper.getOrDefault;
import static com.sirma.itt.seip.util.EqualsHelper.nullSafeEquals;

import java.io.Serializable;
import java.lang.invoke.MethodHandles;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import java.util.stream.Stream.Builder;

import org.eclipse.rdf4j.model.IRI;
import org.eclipse.rdf4j.model.Literal;
import org.eclipse.rdf4j.model.Resource;
import org.eclipse.rdf4j.model.Statement;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sirma.itt.seip.Pair;
import com.sirma.itt.seip.Uri;
import com.sirma.itt.seip.convert.TypeConverter;
import com.sirma.itt.seip.definition.InverseRelationProvider;
import com.sirma.itt.seip.definition.SemanticDefinitionService;
import com.sirma.itt.seip.domain.definition.DataTypeDefinition;
import com.sirma.itt.seip.domain.definition.PropertyDefinition;
import com.sirma.itt.seip.domain.instance.Instance;
import com.sirma.itt.seip.domain.instance.InstanceReference;
import com.sirma.itt.seip.domain.instance.PropertyInstance;
import com.sirma.itt.seip.domain.semantic.persistence.MultiLanguageValue;

import rx.functions.Func7;

/**
 * PersistStep represents a statement generator for one or more {@link LocalStatement}s based on given data.
 *
 * @author BBonev
 */
abstract class PersistStep {

	protected final IRI currentInstance;
	protected final Object uri;

	protected Serializable newValue;
	protected Serializable oldValue;
	protected final StatementBuilder statementBuilder;

	/**
	 * Instantiates a new persist step.
	 *
	 * @param currentInstance
	 *            the current instance
	 * @param uri
	 *            the uri
	 * @param newValue
	 *            the new value
	 * @param oldValue
	 *            the old value
	 * @param statementBuilder
	 *            the statement builder
	 */
	protected PersistStep(IRI currentInstance, Object uri, Serializable newValue, Serializable oldValue,
			StatementBuilder statementBuilder) {
		this.currentInstance = currentInstance;
		this.uri = uri;
		this.newValue = newValue;
		this.oldValue = oldValue;
		this.statementBuilder = statementBuilder;
	}

	/**
	 * Empty persist step that does nothing
	 *
	 * @return the persist step
	 */
	public static PersistStep empty() {
		return EmptyPersistStep.EMPTY_INSTANCE;
	}

	private static boolean isMultivalue(Serializable value) {
		return value instanceof Collection;
	}

	private static boolean isMultilangValue(Serializable value) {
		return value instanceof MultiLanguageValue;
	}

	private static boolean isObjectPropertyValue(Serializable value) {
		if (value instanceof Resource || value instanceof Instance || value instanceof InstanceReference) {
			return true;
		} else if (value instanceof String) {
			// if string only accept full URIs because the short could be random string and we can get an exception if
			// guessed wrong
			// probably is better to use dedicated validator that checks the prefix for short URIs for validity
			return value.toString().startsWith("http") && value.toString().contains(":");
		} else if (isMultivalue(value)) {
			return isCollectionObjectProperty(value);
		}
		return false;
	}

	private static boolean isCollectionObjectProperty(Serializable value) {
		Iterator<?> it = ((Collection<?>) value).iterator();
		// check at least one collection element
		if (it.hasNext()) {
			Object next = it.next();
			if (next instanceof Serializable) {
				return isObjectPropertyValue((Serializable) next);
			}
		}
		return false;
	}

	/**
	 * Gets stream of statements that need to be added or removed from the database
	 *
	 * @return the statements to add/remove or empty stream
	 */
	public abstract Stream<LocalStatement> getStatements();

	/**
	 * Builds the statement, using the provided {@link StatementBuilder}, that has the argument as it's object. The
	 * subject is the {@link #currentInstance} and the predicate is the current {@link #uri}.
	 *
	 * @param value
	 *            the value to add to the statement
	 * @return the statement
	 */
	protected Statement buildStatement(Serializable value) {
		return statementBuilder.build(currentInstance, uri, value);
	}

	/**
	 * Builds local statement for adding to the database that have the given value as it's object.
	 *
	 * @param value
	 *            the value to add to the database
	 * @return the local statement
	 * @see #buildStatement(Serializable)
	 */
	protected LocalStatement addStatement(Serializable value) {
		if (value == null) {
			return null;
		}
		return statementBuilder.buildAddStatement(currentInstance, uri, value);
	}

	/**
	 * Builds local statement that need to be removed from the database that have the given value as it's object.
	 *
	 * @param value
	 *            the value to remove from the database
	 * @return the local statement
	 * @see #buildStatement(Serializable)
	 */
	protected LocalStatement removeStatement(Serializable value) {
		if (value == null) {
			return null;
		}
		return statementBuilder.buildRemoveStatement(currentInstance, uri, value);
	}

	static Serializable getUriFrom(Serializable value) {
		Serializable uri = null;
		if (value instanceof String || value instanceof Resource || value instanceof Literal) {
			uri = value;
		} else if (value instanceof Instance) {
			uri = ((Instance) value).getId();
		} else if (value instanceof InstanceReference) {
			uri = ((InstanceReference) value).getId();
		} else if (value instanceof Uri) {
			uri = value.toString();
		}
		return uri;
	}

	/**
	 * Factory for producing {@link PersistStep}s. The implementation is stateful!
	 *
	 * @author BBonev
	 */
	static class PersistStepFactory {

		private static final Logger LOGGER = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

		private final StatementBuilderProvider statementBuilder;
		private final InverseRelationProvider inverseRelationProvider;
		private final SemanticDefinitionService semanticDefinitionService;
		private final TypeConverter typeConverter;

		private IRI subject;
		private ValueProvider newValueProvider;
		private ValueProvider oldValueProvider;

		PersistStepFactory(StatementBuilderProvider statementBuilder, InverseRelationProvider inverseRelationProvider,
				SemanticDefinitionService semanticDefinitionService, TypeConverter typeConverter) {

			this.statementBuilder = Objects.requireNonNull(statementBuilder,
					"Cannot initialize factory for null statement builder");
			this.inverseRelationProvider = Objects.requireNonNull(inverseRelationProvider,
					"Cannot initialize factory for null inverse relation provider");
			this.semanticDefinitionService = Objects.requireNonNull(semanticDefinitionService,
					"Cannot initialize factory for null semantic definition service");
			this.typeConverter = Objects.requireNonNull(typeConverter,
					"Cannot initialize factory for null type converter");
		}

		void setSourceData(IRI instanceId, ValueProvider newData, ValueProvider oldData) {
			subject = Objects.requireNonNull(instanceId, "Cannot initialize factory for null instance id");
			newValueProvider = newData;
			oldValueProvider = oldData;
		}

		/**
		 * Creates {@link PersistStep} instance based on the given {@link PropertyDefinition}. The definition will be
		 * used to determine if the field is object property and if should be multi value property
		 *
		 * @param definition
		 *            the definition
		 * @return the persist step that can handle the property value identified for the given
		 *         {@link PropertyDefinition}
		 */
		public PersistStep create(PropertyDefinition definition) {
			Serializable newValue = newValueProvider.getValue(definition);
			Serializable oldValue = oldValueProvider.getValue(definition);
			LOGGER.trace("New persist step using def {} +={} -={}",  definition.getIdentifier(), newValue, oldValue);

			// if both are null or they are equal no need to do anything
			if (nullSafeEquals(newValue, oldValue)) {
				return EmptyPersistStep.EMPTY_INSTANCE;
			}

			boolean isMultiValue = definition.isMultiValued() || isMultivalue(newValue)
					|| isMultivalue(oldValue);
			boolean isObjectProperty = BasePropertiesConverter.isUriField(definition);
			boolean isMultilangValue = isMultilangValue(newValue) || isMultilangValue(oldValue);

			String uri = resolveUri().apply(definition);

			return getStepImpl(isObjectProperty, isMultiValue, isMultilangValue).call(subject, uri, newValue, oldValue, statementBuilder,
					inverseRelationProvider, value -> ensureProperType(definition, value));
		}

		private Serializable ensureProperType(PropertyDefinition definition, Serializable value) {
			DataTypeDefinition dataType = definition.getDataType();
			if (dataType.getJavaClass() == null) {
				return value;
			}
			Serializable converted = (Serializable) typeConverter.tryConvert(dataType.getJavaClass(), value);
			return getOrDefault(converted, value);
		}

		/**
		 * Creates {@link PersistStep} instance that can handle persist of property identified by the given property key
		 * located in the stored {@link ValueProvider}s
		 *
		 * @param name
		 *            the name of the property. It should have IRI format. It will act as predicate for the generated
		 *            statement
		 * @return the persist step that can handle the property value identified for the given name
		 */
		public PersistStep create(String name) {
			Serializable newValue = newValueProvider.getValue(name);
			Serializable oldValue = oldValueProvider.getValue(name);
			LOGGER.trace("New persist step for {} +={} -={}",  name, newValue, oldValue);

			// if both are null or they are equal no need to do anything
			if (nullSafeEquals(newValue, oldValue)) {
				return EmptyPersistStep.EMPTY_INSTANCE;
			}
			boolean isMultiValue = isMultivalue(newValue) || isMultivalue(oldValue);
			boolean isObjectProperty = isObjectPropertyValue(newValue) || isObjectPropertyValue(oldValue)
					|| isObjectPropertyPredicate(name);
			boolean isMultilangValue = isMultilangValue(newValue) || isMultilangValue(oldValue);

			return getStepImpl(isObjectProperty, isMultiValue, isMultilangValue).call(subject, name, newValue, oldValue, statementBuilder,
					inverseRelationProvider, ValueTypeConverter.noConvert());
		}

		/**
		 * Checks if predicate with the given name is object property or not. This is mainly used when the type could
		 * not be detected by the value itself
		 *
		 * @param predicate
		 *            to check if it's for object property
		 * @return <code>true</code> if it's a object property and <code>false</code> if not
		 */
		private boolean isObjectPropertyPredicate(Object predicate) {
			if (predicate == null) {
				return false;
			}
			PropertyInstance relation = semanticDefinitionService.getRelation(predicate.toString());
			return relation != null;
		}

		/**
		 * Creates {@link PersistStep} instance that can handle persist of property identified by the given predicate
		 * and given old and new value.
		 *
		 * @param uri
		 *            the predicate for the expected statement
		 * @param newValue
		 *            the new value
		 * @param oldValue
		 *            the old value
		 * @return the persist step that can handle the property value identified for the given name
		 */
		public PersistStep create(Object uri, Serializable newValue, Serializable oldValue) {
			LOGGER.trace("New explicit persist step for {} +={} -={}", uri, newValue, oldValue);
			// if both are null or they are equal no need to do anything
			if (nullSafeEquals(newValue, oldValue)) {
				return EmptyPersistStep.EMPTY_INSTANCE;
			}
			boolean isMultiValue = isMultivalue(newValue) || isMultivalue(oldValue);
			boolean isObjectProperty = isObjectPropertyValue(newValue) || isObjectPropertyValue(oldValue)
					|| isObjectPropertyPredicate(uri);
			boolean isMultilangValue = isMultilangValue(newValue) || isMultilangValue(oldValue);

			return getStepImpl(isObjectProperty, isMultiValue, isMultilangValue).call(subject, uri, newValue, oldValue, statementBuilder,
					inverseRelationProvider, ValueTypeConverter.noConvert());
		}

		private static Func7<IRI, Object, Serializable, Serializable, StatementBuilderProvider, InverseRelationProvider, ValueTypeConverter, PersistStep> getStepImpl(
				boolean isObjectProperty, boolean isMultiValue, boolean isMultilangValue) {
			if (isObjectProperty) {
				if (isMultiValue) {
					return (instanceId, uri, newValue, oldValue, statementBuilderProvider, inverseRelationProvider,
							valueTypeConverter) -> new MultiValueObjectPropertyPersistStep(instanceId, uri, newValue,
									oldValue, statementBuilderProvider, inverseRelationProvider);
				}
				return (instanceId, uri, newValue, oldValue, statementBuilderProvider, inverseRelationProvider,
						valueTypeConverter) -> new ObjectPropertyPersistStep(instanceId, uri, newValue, oldValue,
								statementBuilderProvider, inverseRelationProvider);
			}

			if (isMultilangValue) {
				return (instanceId, uri, newValue, oldValue, statementBuilderProvider, inverseRelationProvider,
						valueTypeConverter) -> new MultilangValueLiteralPersistStep(instanceId, uri, newValue, oldValue,
						statementBuilderProvider);
			}

			if (isMultiValue) {
				return (instanceId, uri, newValue, oldValue, statementBuilderProvider, inverseRelationProvider,
						valueTypeConverter) -> new MultiValueLiteralPersistStep(instanceId, uri, newValue, oldValue,
								statementBuilderProvider, valueTypeConverter);
			}

			return (instanceId, uri, newValue, oldValue, statementBuilderProvider, inverseRelationProvider,
					valueTypeConverter) -> new LiteralPersistStep(instanceId, uri, newValue, oldValue,
							statementBuilderProvider, valueTypeConverter);
		}
	}

	/**
	 * Defines a converter that can be used for transform the values to proper type in the database
	 *
	 * @author BBonev
	 */
	private interface ValueTypeConverter {

		/**
		 * Converter instance that does no conversion
		 *
		 * @return the value type converter
		 */
		static ValueTypeConverter noConvert() {
			return value -> value;
		}

		/**
		 * Convert the value to proper type for persistence
		 *
		 * @param value
		 *            the value
		 * @return the serializable
		 */
		Serializable convert(Serializable value);
	}

	/**
	 * Provides common means of accessing instance data. It also hides the source of the data
	 *
	 * @author BBonev
	 */
	static class ValueProvider {
		private final Instance source;

		private ValueProvider(Instance source) {
			this.source = source;
		}

		/**
		 * Creates new instance of the {@link ValueProvider} from the given instance
		 *
		 * @param instance
		 *            the instance
		 * @return the value provider
		 */
		static ValueProvider instance(Instance instance) {
			return new ValueProvider(instance);
		}

		/**
		 * Gets the value represented by the given {@link PropertyDefinition}
		 *
		 * @param field
		 *            the field
		 * @return the value
		 */
		Serializable getValue(PropertyDefinition field) {
			if (source == null) {
				return null;
			}
			String name = field.getName();
			Serializable value = source.get(name);
			if (value == null) {
				String uri = resolveUri().apply(field);
				value = source.get(uri);
			}
			return value;
		}

		/**
		 * Gets the value represented by the given property name
		 *
		 * @param name
		 *            the name
		 * @return the value
		 */
		Serializable getValue(String name) {
			if (source == null) {
				return null;
			}
			return source.get(name);
		}
	}

	/**
	 * Step that does not do anything and return no statements
	 *
	 * @author BBonev
	 */
	private static class EmptyPersistStep extends PersistStep {

		static final PersistStep EMPTY_INSTANCE = new EmptyPersistStep();

		private EmptyPersistStep() {
			super(null, null, null, null, null);
		}

		@Override
		public Stream<LocalStatement> getStatements() {
			return Stream.empty();
		}
	}

	/**
	 * Base class for implementing multi value persist steps. Converts the value arguments to collections for easy
	 * comparison. Note that no difference is created unless the
	 * {@link com.sirma.itt.seip.util.EqualsHelper#diffCollections(Collection, Collection)} is called.
	 *
	 * @author BBonev
	 */
	private abstract static class BaseMultiValuePersistStep extends PersistStep {

		protected Collection<Serializable> newValues;
		protected Collection<Serializable> oldValues;

		BaseMultiValuePersistStep(IRI currentInstance, Object uri, Serializable newValue, Serializable oldValue,
				StatementBuilder statementBuilder) {
			super(currentInstance, uri, newValue, oldValue, statementBuilder);
			// convert values to collections if not already
			newValues = getAsCollection(newValue);
			oldValues = getAsCollection(oldValue);
		}

		@SuppressWarnings("unchecked")
		private static Collection<Serializable> getAsCollection(Serializable newValue) {
			Collection<Serializable> result;
			if (newValue instanceof Collection<?>) {
				result = (Collection<Serializable>) newValue;
			} else if (newValue == null) {
				result = Collections.emptySet();
			} else {
				result = Collections.singleton(newValue);
			}
			return result;
		}
	}

	/**
	 * Persist step for storing a single literal value
	 *
	 * @author BBonev
	 */
	private static class LiteralPersistStep extends PersistStep {

		private ValueTypeConverter valueTypeConverter;

		LiteralPersistStep(IRI instanceId, Object name, Serializable newValue, Serializable oldValue,
				StatementBuilderProvider statementBuilder, ValueTypeConverter valueTypeConverter) {
			super(instanceId, name, newValue, oldValue, statementBuilder.literalStatementBuilder());
			this.valueTypeConverter = valueTypeConverter;
		}

		@Override
		public Stream<LocalStatement> getStatements() {
			// convert to proper persist type if needed
			newValue = valueTypeConverter.convert(newValue);
			oldValue = valueTypeConverter.convert(oldValue);

			// these checks here prevent LocalStatement and array building when calling the Stream.of method with more
			// than one argument if not necessary

			// note also that both values cannot be null so these checks are fine
			if (newValue == null) {
				return Stream.of(removeStatement(oldValue));
			}
			if (oldValue == null) {
				return Stream.of(addStatement(newValue));
			}
			return Stream.of(addStatement(newValue), removeStatement(oldValue));
		}
	}

	/**
	 * Persist step for storing multiple literal values.
	 *
	 * @author BBonev
	 */
	private static class MultiValueLiteralPersistStep extends BaseMultiValuePersistStep {

		private ValueTypeConverter valueTypeConverter;

		MultiValueLiteralPersistStep(IRI instanceId, Object name, Serializable newValue, Serializable oldValue,
				StatementBuilderProvider statementBuilder, ValueTypeConverter valueTypeConverter) {
			super(instanceId, name, newValue, oldValue, statementBuilder.literalStatementBuilder());
			this.valueTypeConverter = valueTypeConverter;
		}

		@Override
		public Stream<LocalStatement> getStatements() {

			// convert the values to proper types
			newValues = newValues.stream().map(valueTypeConverter::convert).collect(Collectors.toList());
			oldValues = oldValues.stream().map(valueTypeConverter::convert).collect(Collectors.toList());

			Pair<Set<Serializable>, Set<Serializable>> valuesDiff = diffCollections(newValues, oldValues);
			newValues = valuesDiff.getFirst();
			oldValues = valuesDiff.getSecond();

			// the values could be empty collections after the diff so we better check them
			if (isEmpty(newValues) && isEmpty(oldValues)) {
				return Stream.empty();
			}

			Stream<LocalStatement> newValuesStream = newValues
					.stream()
						.map(this::buildStatement)
						.filter(Objects::nonNull)
						.map(LocalStatement::toAdd);
			Stream<LocalStatement> oldValuesStream = oldValues
					.stream()
						.map(this::buildStatement)
						.filter(Objects::nonNull)
						.map(LocalStatement::toRemove);
			return Stream.concat(newValuesStream, oldValuesStream);
		}
	}

	/**
	 * Persist step for {@link MultiLanguageValue} values. If one of the values is {@link String} it will be handled as such along with
	 * the {@link MultiLanguageValue} one.
	 *
	 * @author Mihail Radkov
	 */
	private static class MultilangValueLiteralPersistStep extends PersistStep {

		MultilangValueLiteralPersistStep(IRI instanceId, Object name, Serializable newValue, Serializable oldValue,
				StatementBuilderProvider statementBuilder) {
			super(instanceId, name, newValue, oldValue, statementBuilder.literalStatementBuilder());
		}

		@Override
		public Stream<LocalStatement> getStatements() {
			Collection<Statement> newStatements = getStatements(newValue);
			Collection<Statement> oldStatements = getStatements(oldValue);

			Pair<Set<Statement>, Set<Statement>> statementsDiff = diffCollections(newStatements, oldStatements);
			newStatements = statementsDiff.getFirst();
			oldStatements = statementsDiff.getSecond();

			Stream<LocalStatement> newValuesStream = newStatements
					.stream()
					.filter(Objects::nonNull)
					.map(LocalStatement::toAdd);
			Stream<LocalStatement> oldValuesStream = oldStatements
					.stream()
					.filter(Objects::nonNull)
					.map(LocalStatement::toRemove);
			return Stream.concat(newValuesStream, oldValuesStream);
		}

		private Collection<Statement> getStatements(Serializable value) {
			Collection<Statement> statements = new LinkedList<>();
			if (PersistStep.isMultilangValue(value)) {
				((MultiLanguageValue) value).forEach((lang, label) -> statements.add(buildStatement(label, lang)));
			} else if (value instanceof String) {
				statements.add(buildStatement(value.toString()));
			} else if (value != null) {
				throw new IllegalArgumentException("Unexpected type " + value.getClass().getSimpleName());
			}
			return statements;
		}

		private Statement buildStatement(String language, String value) {
			return statementBuilder.build(currentInstance, uri, language, value);
		}
	}

	/**
	 * Persist step for storing single object property
	 *
	 * @author BBonev
	 */
	private static class ObjectPropertyPersistStep extends PersistStep {

		private final InverseRelationProvider inverseRelationProvider;

		ObjectPropertyPersistStep(IRI instanceId, Object name, Serializable newValue, Serializable oldValue,
				StatementBuilderProvider statementBuilder, InverseRelationProvider inverseRelationProvider) {
			super(instanceId, name, newValue, oldValue, statementBuilder.relationStatementBuilder());
			this.inverseRelationProvider = inverseRelationProvider;
		}

		@Override
		public Stream<LocalStatement> getStatements() {
			Serializable newUri = getUriFrom(newValue);
			Serializable oldUri = getUriFrom(oldValue);

			LocalStatement addStatement = addStatement(newUri);
			LocalStatement removeStatement = removeStatement(oldUri);

			if (addStatement != null && addStatement.isSame(removeStatement)) {
				// for identical statements we have no actual changes
				// just the data was in different formats
				return Stream.empty();
			}

			Builder<LocalStatement> builder = Stream.builder();
			// upon valid add statement we should calculate the possible inverse statement
			if (addNonNullValue(builder, addStatement)) {
				// we can also do this in an observer of AddRelationEvent and provide the inverse via the event
				String inverseRelation = inverseRelationProvider.inverseOf(uri.toString());
				if (inverseRelation != null) {
					addNonNullValue(builder,
							statementBuilder.buildAddStatement(newUri, inverseRelation, currentInstance));
					// if complex relations are needed they should go here
				}
			}

			if (addNonNullValue(builder, removeStatement)) {
				// we can also do this in an observer of AddRelationEvent and provide the inverse via the event
				String inverseRelation = inverseRelationProvider.inverseOf(uri.toString());
				if (inverseRelation != null) {
					addNonNullValue(builder,
							statementBuilder.buildRemoveStatement(oldUri, inverseRelation, currentInstance));
					// if complex relations are needed they should go here
				}
			}

			return builder.build();
		}

	}

	/**
	 * Persist step for storing multiple object properties
	 *
	 * @author BBonev
	 */
	private static class MultiValueObjectPropertyPersistStep extends BaseMultiValuePersistStep {

		private final InverseRelationProvider inverseRelationProvider;

		MultiValueObjectPropertyPersistStep(IRI instanceId, Object name, Serializable newValue, Serializable oldValue,
				StatementBuilderProvider statementBuilder, InverseRelationProvider inverseRelationProvider) {
			super(instanceId, name, newValue, oldValue, statementBuilder.relationStatementBuilder());
			this.inverseRelationProvider = inverseRelationProvider;
		}

		@Override
		public Stream<LocalStatement> getStatements() {
			Collection<Statement> newStatements = convertToStatements(newValues);
			Collection<Statement> oldStatements = convertToStatements(oldValues);

			// perform the diff over the statements due to the data could be in different formats
			// also some of the conversion happen in the statement building so we cannot compare the values before that
			Pair<Set<Statement>, Set<Statement>> diff = diffCollections(newStatements, oldStatements);
			newStatements = diff.getFirst();
			oldStatements = diff.getSecond();

			if (isEmpty(newStatements) && isEmpty(oldStatements)) {
				return Stream.empty();
			}

			Builder<LocalStatement> builder = Stream.builder();

			for (Statement statement : newStatements) {
				builder.add(LocalStatement.toAdd(statement));

				if (isObjectPropertyValue(statement.getObject())) {
					String inverseRelation = inverseRelationProvider.inverseOf(statement.getPredicate().toString());
					if (inverseRelation != null) {
						builder.add(statementBuilder.buildAddStatement(statement.getObject(), inverseRelation,
								currentInstance));
						// if complex relations are needed they should go here
					}
				}
			}

			for (Statement statement : oldStatements) {
				builder.add(LocalStatement.toRemove(statement));

				if (isObjectPropertyValue(statement.getObject())) {
					String inverseRelation = inverseRelationProvider.inverseOf(statement.getPredicate().toString());
					if (inverseRelation != null) {
						// this will trigger event fire for the removal of the inverse statement
						builder.add(statementBuilder.buildRemoveStatement(statement.getObject(), inverseRelation,
								currentInstance));
						// if complex relations are needed they should go here
					}
				}
			}
			return builder.build();
		}

		private Collection<Statement> convertToStatements(Collection<Serializable> values) {
			Collection<Statement> result = new ArrayList<>(values.size());
			for (Serializable value : values) {
				addNonNullValue(result, buildStatement(getUriFrom(value)));
			}
			return result;
		}
	}
}