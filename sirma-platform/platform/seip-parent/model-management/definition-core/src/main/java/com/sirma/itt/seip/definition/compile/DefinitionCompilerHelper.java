package com.sirma.itt.seip.definition.compile;

import java.io.File;
import java.lang.invoke.MethodHandles;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.Callable;
import java.util.stream.Collectors;

import javax.enterprise.inject.Any;
import javax.enterprise.inject.Instance;
import javax.inject.Inject;
import javax.inject.Singleton;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sirma.itt.seip.collections.CollectionUtils;
import com.sirma.itt.seip.definition.DefinitionService;
import com.sirma.itt.seip.definition.MutableDefinitionService;
import com.sirma.itt.seip.definition.RegionDefinition;
import com.sirma.itt.seip.definition.RegionDefinitionModel;
import com.sirma.itt.seip.definition.StateTransition;
import com.sirma.itt.seip.definition.StateTransitionalModel;
import com.sirma.itt.seip.definition.TransitionDefinition;
import com.sirma.itt.seip.definition.Transitional;
import com.sirma.itt.seip.definition.ValidationLoggingUtil;
import com.sirma.itt.seip.definition.WritablePropertyDefinition;
import com.sirma.itt.seip.definition.model.ControlDefinitionImpl;
import com.sirma.itt.seip.definition.model.PropertyDefinitionProxy;
import com.sirma.itt.seip.definition.model.RegionDefinitionImpl;
import com.sirma.itt.seip.definition.model.TransitionDefinitionImpl;
import com.sirma.itt.seip.definition.util.DefinitionUtil;
import com.sirma.itt.seip.definition.util.PathHelper;
import com.sirma.itt.seip.definition.validator.DefinitionValidator;
import com.sirma.itt.seip.domain.BidirectionalMapping;
import com.sirma.itt.seip.domain.Identity;
import com.sirma.itt.seip.domain.PathElement;
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

	private static final Logger LOGGER = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

	@Inject
	private MutableDefinitionService mutableDefinitionService;

	@Inject
	private DefinitionService definitionService;

	@Inject
	@Any
	private Instance<DefinitionValidator> validators;

	@Inject
	private TransactionSupport transactionSupport;

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
}
