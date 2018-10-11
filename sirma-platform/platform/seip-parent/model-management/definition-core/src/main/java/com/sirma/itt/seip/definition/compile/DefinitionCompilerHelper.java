package com.sirma.itt.seip.definition.compile;

import java.io.File;
import java.lang.invoke.MethodHandles;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import javax.enterprise.inject.Any;
import javax.enterprise.inject.Instance;
import javax.inject.Inject;
import javax.inject.Singleton;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sirma.itt.seip.Pair;
import com.sirma.itt.seip.collections.CollectionUtils;
import com.sirma.itt.seip.definition.DefinitionService;
import com.sirma.itt.seip.definition.Mergeable;
import com.sirma.itt.seip.definition.MutableDefinitionService;
import com.sirma.itt.seip.definition.RegionDefinition;
import com.sirma.itt.seip.definition.RegionDefinitionModel;
import com.sirma.itt.seip.definition.StateTransition;
import com.sirma.itt.seip.definition.StateTransitionalModel;
import com.sirma.itt.seip.definition.TransitionDefinition;
import com.sirma.itt.seip.definition.Transitional;
import com.sirma.itt.seip.definition.ValidationLoggingUtil;
import com.sirma.itt.seip.definition.WritablePropertyDefinition;
import com.sirma.itt.seip.definition.model.ConditionDefinitionImpl;
import com.sirma.itt.seip.definition.model.ControlDefinitionImpl;
import com.sirma.itt.seip.definition.model.PropertyDefinitionProxy;
import com.sirma.itt.seip.definition.model.RegionDefinitionImpl;
import com.sirma.itt.seip.definition.model.TransitionDefinitionImpl;
import com.sirma.itt.seip.definition.util.DefinitionUtil;
import com.sirma.itt.seip.definition.util.PathHelper;
import com.sirma.itt.seip.definition.util.hash.HashCalculator;
import com.sirma.itt.seip.definition.validator.DefinitionValidator;
import com.sirma.itt.seip.domain.BidirectionalMapping;
import com.sirma.itt.seip.domain.Identity;
import com.sirma.itt.seip.domain.PathElement;
import com.sirma.itt.seip.domain.definition.Condition;
import com.sirma.itt.seip.domain.definition.Conditional;
import com.sirma.itt.seip.domain.definition.ControlDefinition;
import com.sirma.itt.seip.domain.definition.ControlParam;
import com.sirma.itt.seip.domain.definition.DataTypeDefinition;
import com.sirma.itt.seip.domain.definition.DefinitionModel;
import com.sirma.itt.seip.domain.definition.DisplayType;
import com.sirma.itt.seip.domain.definition.GenericDefinition;
import com.sirma.itt.seip.domain.definition.PropertyDefinition;
import com.sirma.itt.seip.domain.definition.label.Displayable;
import com.sirma.itt.seip.tx.TransactionSupport;
import com.sirma.sep.xml.JAXBHelper;

/**
 * Helper class for loading, validating and converting objects. Contains methods mostly used by concrete implementations
 * of {@link GenericDefinitionCompilerCallback}.
 *
 * @author BBonev
 */
@Singleton
public class DefinitionCompilerHelper {

	public static final String DEFAULT_VALUE_PATTERN_TYPE = "default_value_pattern";
	private static final Pattern ASCII_CHARACTER_PATTERN = Pattern.compile("^[\\x20-\\x7E\\r\\n\\t]+$");
	private static final Pattern SP_CHARACTER_PATTERN = Pattern.compile("[\\s][\\s]+");

	private static final Logger LOGGER = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());
	private static final String FROM = "] from [";

	@Inject
	private MutableDefinitionService mutableDefinitionService;

	@Inject
	private DefinitionService definitionService;

	@Inject
	@Any
	private Instance<DefinitionValidator> validators;

	@Inject
	private TransactionSupport transactionSupport;

	@Inject
	private HashCalculator hashCalculator;

	/**
	 * Loads The content of the given file and converts it using JAXB unmarshaller.
	 *
	 * @param <S>
	 *            the generic type
	 * @param file
	 *            the file
	 * @param src
	 *            the src
	 * @return the loaded object
	 */

	@SuppressWarnings("static-method")
	public <S> S load(File file, Class<S> src) {
		return JAXBHelper.load(file, src);
	}

	/**
	 * Normalize fields.
	 *
	 * @param fields
	 *            the fields
	 * @param pathElement
	 *            the path element
	 * @param clearFieldId
	 *            the clear field id
	 * @param container
	 *            the container
	 */
	public void normalizeFields(List<PropertyDefinition> fields, PathElement pathElement, boolean clearFieldId,
			String container) {
		if (fields == null) {
			return;
		}
		DefinitionUtil.sort(fields);
		for (PropertyDefinition definition : fields) {
			if (clearFieldId) {
				definition.setId(null);
			}
			normalizeFields((WritablePropertyDefinition) definition, pathElement, container);

			// normalize control fields after the current field
			if (definition.getControlDefinition() != null) {
				normalizeFields(definition.getControlDefinition().getFields(), definition.getControlDefinition(),
						clearFieldId, container);
			}
		}
	}

	private void normalizeFields(WritablePropertyDefinition fieldDefinitionImpl, PathElement pathElement,
			String container) {
		// clear any extra white spaces in the value
		if (fieldDefinitionImpl.getDefaultValue() != null) {
			fieldDefinitionImpl.setValue(fieldDefinitionImpl.getDefaultValue().trim());
		}
		fieldDefinitionImpl.setContainer(container);
		fieldDefinitionImpl.setParentPath(PathHelper.getPath(pathElement));
		String type = fieldDefinitionImpl.getType();
		if (fieldDefinitionImpl.getDataType() == null && StringUtils.isNotBlank(type)) {
			TypeParser parser = TypeParser.parse(type);
			// updates the type name
			fieldDefinitionImpl.setType(parser.type);
			String definitionName = parser.getDataTypeDefinitionName();
			DataTypeDefinition dataTypeDefinition = definitionService.getDataTypeDefinition(definitionName);
			if (dataTypeDefinition == null) {
				String message = "No definition found for " + definitionName;
				LOGGER.error(message);
				ValidationLoggingUtil.addErrorMessage(message);
				// this should not happen if the type definitions are successful
				return;
			}
			fieldDefinitionImpl.setDataType(dataTypeDefinition);
			if (parser.alpha || parser.numeric) {
				fieldDefinitionImpl.setMaxLength(parser.maxLength);
			}
		}
	}

	/**
	 * Sets the property revision.
	 *
	 * @param documentDefinition
	 *            the document definition
	 * @param revision
	 *            the revision
	 */
	public void setPropertyRevision(DefinitionModel documentDefinition, Long revision) {
		if (documentDefinition.getFields() != null) {
			for (PropertyDefinition definition : documentDefinition.getFields()) {
				((WritablePropertyDefinition) definition).setRevision(revision);
				if (definition.getControlDefinition() != null) {
					setPropertyRevision(definition.getControlDefinition(), revision);
				}
			}
		}
	}

	/**
	 * Sets the property revision.
	 *
	 * @param transitional
	 *            the transitional
	 * @param revision
	 *            the revision
	 */
	public void setTransactionalPropertyRevision(Transitional transitional, Long revision) {
		for (TransitionDefinition transitionDefinition : transitional.getTransitions()) {
			setPropertyRevision(transitionDefinition, revision);
		}
	}

	/**
	 * Sets the property revision.
	 *
	 * @param documentDefinition
	 *            the document definition
	 * @param revision
	 *            the revision
	 */
	public void setPropertyRevision(RegionDefinitionModel documentDefinition, Long revision) {
		if (documentDefinition.getFields() != null) {
			for (PropertyDefinition definition : documentDefinition.getFields()) {
				setPropertyRevision(definition, revision);
			}
		}
		for (RegionDefinition definition : documentDefinition.getRegions()) {
			setPropertyRevision(definition, revision);
			if (definition.getControlDefinition() != null) {
				setPropertyRevision(definition.getControlDefinition(), revision);
			}
		}
	}

	private void setPropertyRevision(PropertyDefinition definition, Long revision) {
		WritablePropertyDefinition impl = (WritablePropertyDefinition) definition;
		impl.setRevision(revision);
		if (impl.getControlDefinition() != null) {
			setPropertyRevision(impl.getControlDefinition(), revision);
		}
	}

	/**
	 * Removes the deleted elements. Removes elements that implement displayable and have display type set to
	 * {@link DisplayType#DELETE}. The method processed the {@link RegionDefinitionModel}, {@link DefinitionModel} and
	 * {@link Transitional} interfaces.
	 *
	 * @param base
	 *            the base
	 */
	public void removeDeletedElements(Object base) {
		if (base instanceof RegionDefinitionModel) {
			removeDeletedElements(((RegionDefinitionModel) base).getRegions());
		}
		if (base instanceof DefinitionModel) {
			removeDeletedElements(((DefinitionModel) base).getFields());
		}
		if (base instanceof Transitional) {
			removeDeletedElements(((Transitional) base).getTransitions());
		}
	}

	private <E extends Identity> void removeDeletedElements(Collection<E> collection) {
		for (Iterator<E> it = collection.iterator(); it.hasNext();) {
			E definition = it.next();
			if (canRemoveElement(definition)) {
				it.remove();
				LOGGER.debug("Removing element of type {} and id=[{}] due to it was marked for deletion: displayType={}",
						definition.getClass(), definition.getIdentifier(), DisplayType.DELETE);
			} else {
				removeDeletedElements(definition);
			}
		}
	}

	private static boolean canRemoveElement(Object object) {
		return object instanceof Displayable && ((Displayable) object).getDisplayType() == DisplayType.DELETE;
	}

	/**
	 * Removes all regions that are mark as DisplayType = SYSTEM.
	 *
	 * @param model
	 *            the region model definition
	 */
	public List<String> synchRegionProperties(GenericDefinition definition) {
		for (Iterator<RegionDefinition> regionIt = definition.getRegions().iterator(); regionIt.hasNext();) {
			RegionDefinition region = regionIt.next();
			if (region.getDisplayType() == DisplayType.SYSTEM) {
				regionIt.remove();
				LOGGER.debug("Removing disabled region [{}] from [{}]", region.getIdentifier(),
						PathHelper.getPath(definition));
			}
		}

		List<String> errors = new ArrayList<>();

		Map<String, PropertyInfo> fieldsInfo = collectFieldsInfo(definition);
		for (PropertyInfo info : fieldsInfo.values()) {
			if (info.getVisible().isEmpty()) {
				noVisible(info, errors);
			} else if (info.getVisible().size() == 1) {
				oneVisible(info);
			} else {
				// more then one visible means that the definition is invalid
				moreThanOneVisible(info, errors);
			}
		}

		return errors;
	}

	private void noVisible(PropertyInfo info, List<String> errors) {
		if (info.getSystem().size() == 1) {
			// we have only one field so we a good to go and no need to continue
			return;
		}
		// no visible fields found - so all are system we could check if all of them are
		// identical and leave one
		int hash = 0;
		boolean allEquals = true;
		for (Pair<PropertyDefinition, DefinitionModel> pair : info.getSystem()) {
			Integer currentHash = hashCalculator.computeHash(pair.getFirst());
			if (hash == 0) {
				hash = currentHash;
			} else if (hash != currentHash) {
				// found non equal field and no need to continue
				// the definition will be marked as invalid later in the validation
				allEquals = false;
				break;
			}
		}
		if (!allEquals) {
			printErrorMessagesForSystemFields(info, errors);
			return;
		}
		List<Pair<PropertyDefinition, DefinitionModel>> list = info.getSystem();
		removeFields(list.subList(1, list.size()));
	}

	@SuppressWarnings("unchecked")
	private static void oneVisible(PropertyInfo info) {
		// we can copy all data from the system field to the visible one except the
		// visibility
		PropertyDefinition propertyDefinition = info.getVisible().get(0).getFirst();
		for (Pair<PropertyDefinition, DefinitionModel> pair : info.getSystem()) {
			if (propertyDefinition instanceof Mergeable) {
				((Mergeable<PropertyDefinition>) propertyDefinition).mergeFrom(pair.getFirst());
			}
		}
		// remove all system fields we have only one visible
		removeFields(info.getSystem());
	}

	private static void moreThanOneVisible(PropertyInfo info, List<String> errors) {
		for (Pair<PropertyDefinition, DefinitionModel> pair : info.getVisible()) {
			String message;
			if (pair.getSecond() instanceof RegionDefinition) {
				message = "Found duplicate VISIBLE field [" + pair.getFirst().getIdentifier() + "] in ["
						+ PathHelper.getPath((PathElement) pair.getSecond()) + "/" + pair.getSecond().getIdentifier()
						+ "]";
			} else {
				message = "Found duplicate VISIBLE field [" + pair.getFirst().getIdentifier() + FROM
						+ PathHelper.getPath((PathElement) pair.getSecond()) + "]";
			}
			errors.add(message);
			ValidationLoggingUtil.addErrorMessage(message);
			LOGGER.error(message);
		}

		printErrorMessagesForSystemFields(info, errors);
	}

	private static void printErrorMessagesForSystemFields(PropertyInfo info, List<String> errors) {
		for (Pair<PropertyDefinition, DefinitionModel> pair : info.getSystem()) {
			String message;
			if (pair.getSecond() instanceof RegionDefinition) {
				message = "Found duplicate field [" + pair.getFirst().getIdentifier() + "] in ["
						+ PathHelper.getPath((PathElement) pair.getSecond()) + "/" + pair.getSecond().getIdentifier()
						+ "] that cannot be auto removed because both are system fields!";
			} else {
				message = "Found duplicate field [" + pair.getFirst().getIdentifier() + FROM
						+ PathHelper.getPath((PathElement) pair.getSecond())
						+ "] that cannot be auto removed because both are system fields!";
			}
			errors.add(message);
			ValidationLoggingUtil.addWarningMessage(message);
			LOGGER.warn(message);
		}
	}


	private static void removeFields(Collection<Pair<PropertyDefinition, DefinitionModel>> subList) {
		for (Pair<PropertyDefinition, DefinitionModel> pair : subList) {
			boolean removed = pair.getSecond().getFields().remove(pair.getFirst());
			String path;
			if (pair.getSecond() instanceof RegionDefinition) {
				path = pair.getFirst().getIdentifier() + FROM + PathHelper.getPath((PathElement) pair.getSecond()) + "/"
						+ pair.getSecond().getIdentifier() + "]";
			} else {
				path = pair.getFirst().getIdentifier() + FROM + PathHelper.getPath((PathElement) pair.getSecond())
						+ "]";
			}
			if (removed) {
				LOGGER.debug("Removed duplicate field [{}", path);
			} else {
				LOGGER.error("Failed to remove field [{}", path);
			}
		}
	}

	private static void collectFieldsInfo(DefinitionModel model, Map<String, PropertyInfo> mapping) {
		for (PropertyDefinition propertyDefinition : model.getFields()) {
			PropertyInfo info = mapping.computeIfAbsent(propertyDefinition.getIdentifier(), k -> new PropertyInfo());
			Pair<PropertyDefinition, DefinitionModel> pair = new Pair<>(propertyDefinition, model);
			if (propertyDefinition.getDisplayType() == DisplayType.SYSTEM) {
				info.getSystem().add(pair);
			} else {
				info.getVisible().add(pair);
			}
		}
	}

	private static Map<String, PropertyInfo> collectFieldsInfo(RegionDefinitionModel model) {
		Map<String, PropertyInfo> mapping = new HashMap<>();
		collectFieldsInfo(model, mapping);
		for (RegionDefinition regionDefinition : model.getRegions()) {
			collectFieldsInfo(regionDefinition, mapping);
		}
		return mapping;
	}

	/**
	 * Sets the default properties of a definition, its regions and transitions.
	 *
	 * @param definition definition where to set default properties
	 */
	public void setDefaultProperties(GenericDefinition definition) {
		definition.fieldsStream().forEach(DefinitionCompilerHelper::setDefaultProperties);

		for (RegionDefinition region : definition.getRegions()) {
			RegionDefinitionImpl impl = (RegionDefinitionImpl) region;
			impl.setDefaultProperties();

			if (region.getControlDefinition() != null) {
				setDefaultProperties(region.getControlDefinition());
			}
		}

		for (TransitionDefinition transition : definition.getTransitions()) {
			TransitionDefinitionImpl impl = (TransitionDefinitionImpl) transition;
			setDefaultProperties(impl);
			impl.setDefaultProperties();
		}
	}

	private static void setDefaultProperties(DefinitionModel definition) {
		definition.fieldsStream().forEach(DefinitionCompilerHelper::setDefaultProperties);
	}

	private static void setDefaultProperties(PropertyDefinition definition) {
		WritablePropertyDefinition impl = (WritablePropertyDefinition) definition;
		impl.setDefaultProperties();
		if (impl.getControlDefinition() != null) {
			setDefaultProperties(impl.getControlDefinition());
		}
	}

	/**
	 * Validate expression based on the given region definition model. If all fields defined in the expression are
	 * editable in the given definition model then the expression is valid.
	 *
	 * @param model
	 *            the model
	 * @param expression
	 *            the expression
	 * @return true, if valid
	 */
	public boolean validateExpression(DefinitionModel model, String expression) {
		if (expression == null) {
			return true;
		}
		// if the expression is not present then is not valid
		// if the target model does not have any fields or regions then we have
		// nothing to do for the expression
		// we also validate the expression for non ASCII characters
		boolean hasRegions = model instanceof RegionDefinitionModel
				&& ((RegionDefinitionModel) model).getRegions().isEmpty();
		if (StringUtils.isBlank(expression) || model.getFields().isEmpty() && !hasRegions) {
			LOGGER.warn("No fields in the target model to support the expression: {}", expression);
			return false;
		}
		if (!ASCII_CHARACTER_PATTERN.matcher(expression).matches()) {
			LOGGER.warn("Found cyrillic characters in the expression: {}", expression);
			return false;
		}
		// we first collect all fields from the expression
		Set<String> fields = new LinkedHashSet<>(DefinitionUtil.getRncFields(expression));
		// if no fields are found then the expression is not valid
		if (fields.isEmpty()) {
			LOGGER.warn("No fields found into the expression: {}", expression);
			// TODO: Fix FIELD_PATTERN for accepting new condition
			// +n[i-documentType], +n[o-documentType] and negative values
		}
		Set<DisplayType> allowedTypes = EnumSet.of(DisplayType.EDITABLE, DisplayType.READ_ONLY, DisplayType.HIDDEN,
				DisplayType.SYSTEM);
		Set<String> modelFields = collectFieldNames(model, allowedTypes);
		// if the expression contains all fields then it's OK
		boolean containsAll = modelFields.containsAll(fields);
		if (!containsAll) {
			// we will check which fields are missing
			fields.removeAll(modelFields);
			LOGGER.warn("The fields {} in the expression {} are not found into the target model!", fields, expression);
		}
		return containsAll;
	}

	private static Set<String> collectFieldNames(DefinitionModel model, Set<DisplayType> allowedTypes) {
		return model
				.fieldsStream()
					.flatMap(PropertyDefinition::stream)
					.filter(property -> allowedTypes.contains(property.getDisplayType()))
					.map(PropertyDefinition::getName)
					.collect(Collectors.toSet());
	}

	/**
	 * Validate expressions. against the given target model. If any of conditions is invalid it will be removed from the
	 * list of conditions
	 *
	 * @param targetModel
	 *            the target model
	 * @param conditions
	 *            the conditions
	 */
	public void validateExpressions(DefinitionModel targetModel, Conditional conditions) {
		if (conditions.getConditions() == null || conditions.getConditions().isEmpty()) {
			return;
		}
		for (Iterator<Condition> it = conditions.getConditions().iterator(); it.hasNext();) {
			Condition condition = it.next();
			if (condition.getExpression() == null || !validateExpression(targetModel, condition.getExpression())) {
				it.remove();

				LOGGER.warn(" !!! Expression in condition: {} is not valid and will be removed !!!", condition);
			} else {
				// the expression is valid so we will check the white space
				Matcher matcher = SP_CHARACTER_PATTERN.matcher(condition.getExpression());
				String updatedExpression = matcher.replaceAll(" ");
				if (LOGGER.isTraceEnabled() && !condition.getExpression().equals(updatedExpression)) {
					LOGGER.trace("Updated expression to: " + updatedExpression);
				}
				if (condition instanceof ConditionDefinitionImpl) {
					((ConditionDefinitionImpl) condition).setExpression(updatedExpression);
				}
			}
		}
	}

	/**
	 * Save properties of the given region definition model. First saves the root properties then for each region
	 *
	 * @param model
	 *            the model to iterate
	 * @param refModel
	 *            the reference model
	 */
	public void saveProperties(RegionDefinitionModel model, RegionDefinitionModel refModel) {
		transactionSupport.invokeInNewTx(new SaveDefinitionProperties<DefinitionModel>(model, refModel, true));
	}

	/**
	 * Save properties of the given model using the second model as reference.
	 *
	 * @param model
	 *            the model to save
	 * @param refModel
	 *            the reference model to use if any
	 */
	public void saveProperties(DefinitionModel model, DefinitionModel refModel) {
		transactionSupport.invokeInNewTx(new SaveDefinitionProperties<>(model, refModel, false));
	}

	/**
	 * Optimize state transitions. The method should remove any duplicate transitions and sort the transitions with
	 * conditions and the one without
	 *
	 * @param definition
	 *            the definition
	 */
	@SuppressWarnings("static-method")
	public void optimizeStateTransitions(StateTransitionalModel definition) {
		List<StateTransition> stateTransitions = definition.getStateTransitions();
		List<StateTransition> sortedTransitions = new ArrayList<>(stateTransitions.size());
		Map<String, List<StateTransition>> transitionsMapping = CollectionUtils
				.createLinkedHashMap(stateTransitions.size());
		for (StateTransition stateTransition : stateTransitions) {
			String identifier = stateTransition.getFromState() + "|" + stateTransition.getTransitionId();
			CollectionUtils.addValueToMap(transitionsMapping, identifier, stateTransition);
		}

		for (Entry<String, List<StateTransition>> entry : transitionsMapping.entrySet()) {
			List<StateTransition> list = entry.getValue();
			if (list.size() == 1) {
				sortedTransitions.add(list.get(0));
			} else {
				List<StateTransition> sorted = sortAndRemoveDuplicateStateTransitions(list);
				if (list.size() != sorted.size()) {
					LOGGER.debug("Removed duplicate state transition {} from [{}]", sorted.get(0),
							definition.getIdentifier());
					LOGGER.trace("For [{}] the old transitions are {} and the new are {}", definition.getIdentifier(),
							list, sorted);
				}
				sortedTransitions.addAll(sorted);
			}
		}
		if (LOGGER.isTraceEnabled() && !definition.getStateTransitions().equals(sortedTransitions)) {
			LOGGER.trace(
					"Performed transition optimizations on definition [{}]. Results are:\n\tbefore:{}\n\tafter :{}",
					definition.getIdentifier(), definition.getStateTransitions(), sortedTransitions);
		}
		definition.getStateTransitions().clear();
		definition.getStateTransitions().addAll(sortedTransitions);
	}

	private static List<StateTransition> sortAndRemoveDuplicateStateTransitions(List<StateTransition> list) {
		List<StateTransition> sorted = new LinkedList<>();
		Map<String, StateTransition> withNoConditions = new LinkedHashMap<>();
		for (StateTransition transition : list) {
			if (transition.getConditions().isEmpty()) {
				if (!withNoConditions.containsKey(transition.getToState())) {
					withNoConditions.put(transition.getToState(), transition);
				}
			} else {
				sorted.add(transition);
			}
		}
		// add all operations without conditions at the end
		sorted.addAll(withNoConditions.values());
		return sorted;
	}

	/**
	 * Inserts control-params from controls that contain expression templates. If the definition contains default
	 * expression template similar to "some text $[propertyBinding] ${functionBinding}" we will extract the
	 * $[propertyBinding] and ${functionBinding} as control-params. This is done so that we don't have to parse the
	 * expression multiple times, for example in the UI as well.
	 *
	 * @param definition
	 *            the definition model.
	 */
	public void prepareDefaultValueSuggests(GenericDefinition definition) {
		definition
				.fieldsStream()
					.filter(field -> field.getControlDefinition() != null)
					.map(PropertyDefinition::getControlDefinition)
					.forEach(control -> {
						List<ControlParam> bindings = computeBindingsIfTemplatePresent(control);
						bindings.addAll(control.getControlParams());
						((ControlDefinitionImpl) control).setControlParams(bindings);
						((ControlDefinitionImpl) control).initBidirection();
					});
	}

	/**
	 * Finds controls with type default_value_pattern and name template and passes them for further processing.
	 *
	 * @param control
	 *            {@link ControlDefinition}
	 * @return the list of the generated bindings.
	 */
	private static List<ControlParam> computeBindingsIfTemplatePresent(ControlDefinition control) {
		if (control.getControlParams() == null) {
			return Collections.emptyList();
		}

		return control
				.getControlParams()
					.stream()
					.filter(param -> DEFAULT_VALUE_PATTERN_TYPE.equalsIgnoreCase(param.getType()))
					.filter(param -> "template".equals(param.getIdentifier()))
					.flatMap(param -> parseExpressionAndGenerateParams(param).stream())
					.collect(Collectors.toList());
	}

	private static List<ControlParam> parseExpressionAndGenerateParams(ControlParam controlParam) {
		List<ControlParam> result = new ArrayList<>();
		String value = controlParam.getValue();
		result.addAll(DefaultValueSuggestUtil.constructPropertyBindings(value));
		result.addAll(DefaultValueSuggestUtil.constructFunctionControlParams(value));
		return result;
	}

	class SaveDefinitionProperties<V extends DefinitionModel> implements Callable<Void> {

		/** The model. */
		private V model;
		/** The ref model. */
		private V refModel;
		/** The is region. */
		private boolean isRegion;

		/**
		 * Instantiates a new save definition properties.
		 *
		 * @param model
		 *            the model
		 * @param refModel
		 *            the ref model
		 * @param isRegion
		 *            the is region
		 */
		SaveDefinitionProperties(V model, V refModel, boolean isRegion) {
			this.model = model;
			this.refModel = refModel;
			this.isRegion = isRegion;
		}

		@Override
		public Void call() throws Exception {
			savePropertiesInternal(model, refModel);
			saveRegions();
			return null;
		}

		private void saveRegions() {
			if (isRegion) {
				List<RegionDefinition> regions = ((RegionDefinitionModel) model).getRegions();
				for (RegionDefinition regionDefinition : regions) {
					RegionDefinition refModelRegionDefinition = null;
					if (refModel != null) {
						refModelRegionDefinition = PathHelper.find(((RegionDefinitionModel) refModel).getRegions(),
								regionDefinition.getIdentifier());
					}
					savePropertiesInternal(regionDefinition, refModelRegionDefinition);
				}
			}
		}

		private void savePropertiesInternal(DefinitionModel model, DefinitionModel refModel) {
			// he we collect all fields that we processes so later we can set them
			List<PropertyDefinition> updatedFields = new ArrayList<>(model.getFields().size());

			for (PropertyDefinition definition : model.getFields()) {
				PropertyDefinition oldProperty = null;
				if (refModel != null) {
					oldProperty = PathHelper.find(refModel.getFields(), definition.getIdentifier());
				}
				// first we check if we have any control to process before we continue to saving the
				// actual definition
				if (definition.getControlDefinition() != null) {
					ControlDefinition refControl = null;
					if (oldProperty != null) {
						refControl = oldProperty.getControlDefinition();
					}
					saveProperties(definition.getControlDefinition(), refControl);
					if (definition.getControlDefinition() instanceof BidirectionalMapping) {
						((BidirectionalMapping) definition.getControlDefinition()).initBidirection();
					}
				}
				((PropertyDefinitionProxy) definition).setBaseDefinition(null);
				// save or update property if needed
				PropertyDefinition changed = mutableDefinitionService.savePropertyIfChanged(definition, oldProperty);
				updatedFields.add(changed);
			}
			// we update all references from the list
			model.getFields().clear();
			model.getFields().addAll(updatedFields);
		}

	}

	/**
	 * Class used for collecting and storing field visibility information.
	 *
	 * @author bbonev
	 */
	private static class PropertyInfo extends
			Pair<List<Pair<PropertyDefinition, DefinitionModel>>, List<Pair<PropertyDefinition, DefinitionModel>>> {

		/**
		 * Comment for serialVersionUID.
		 */
		private static final long serialVersionUID = 6298677791297526462L;

		/**
		 * Instantiates a new property info.
		 */
		PropertyInfo() {
			super(new LinkedList<>(), new LinkedList<>());
		}

		/**
		 * Gets the visible fields.
		 *
		 * @return the visible
		 */
		public List<Pair<PropertyDefinition, DefinitionModel>> getVisible() {
			return getFirst();
		}

		/**
		 * Gets the system fields.
		 *
		 * @return the system
		 */
		public List<Pair<PropertyDefinition, DefinitionModel>> getSystem() {
			return getSecond();
		}
	}
}
