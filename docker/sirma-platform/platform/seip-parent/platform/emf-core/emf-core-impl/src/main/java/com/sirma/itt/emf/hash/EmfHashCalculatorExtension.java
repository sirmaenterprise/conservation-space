package com.sirma.itt.emf.hash;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import com.sirma.itt.seip.definition.AllowedChildConfiguration;
import com.sirma.itt.seip.definition.AllowedChildDefinition;
import com.sirma.itt.seip.definition.RegionDefinition;
import com.sirma.itt.seip.definition.StateTransition;
import com.sirma.itt.seip.definition.TransitionDefinition;
import com.sirma.itt.seip.definition.WritablePropertyDefinition;
import com.sirma.itt.seip.definition.model.AllowedChildConfigurationImpl;
import com.sirma.itt.seip.definition.model.AllowedChildDefinitionImpl;
import com.sirma.itt.seip.definition.model.ConditionDefinitionImpl;
import com.sirma.itt.seip.definition.model.ControlDefinitionImpl;
import com.sirma.itt.seip.definition.model.ControlParamImpl;
import com.sirma.itt.seip.definition.model.FieldDefinitionImpl;
import com.sirma.itt.seip.definition.model.PropertyDefinitionProxy;
import com.sirma.itt.seip.definition.model.RegionDefinitionImpl;
import com.sirma.itt.seip.definition.model.StateTransitionImpl;
import com.sirma.itt.seip.definition.model.TransitionDefinitionImpl;
import com.sirma.itt.seip.definition.model.TransitionGroupDefinitionImpl;
import com.sirma.itt.seip.definition.util.PathHelper;
import com.sirma.itt.seip.definition.util.hash.HashCalculator;
import com.sirma.itt.seip.definition.util.hash.HashCalculatorExtension;
import com.sirma.itt.seip.definition.util.hash.HashHelper;
import com.sirma.itt.seip.domain.DmsAware;
import com.sirma.itt.seip.domain.PathElement;
import com.sirma.itt.seip.domain.definition.ControlDefinition;
import com.sirma.itt.seip.domain.definition.ControlParam;
import com.sirma.itt.seip.domain.definition.DataTypeDefinition;
import com.sirma.itt.seip.domain.definition.DefinitionModel;
import com.sirma.itt.seip.domain.instance.CMInstance;
import com.sirma.itt.seip.domain.instance.Instance;
import com.sirma.itt.seip.model.DataType;
import com.sirma.itt.seip.plugin.Extension;

/**
 * Hash calculator extension to provide hash computation for the base Emf classes.
 *
 * @author BBonev
 */
@Extension(target = HashCalculatorExtension.TARGET_NAME, order = 10)
public class EmfHashCalculatorExtension implements HashCalculatorExtension {

	private static final String NAME = "/Name";
	private static final String URI = "/Uri";
	private static final String TRANSITION_ID = "/TransitionId";
	private static final String TOOLTIP_ID = "/TooltipId";
	private static final String ORDER = "/Order";
	private static final String CODELIST = "/Codelist";
	private static final String LABEL_ID = "/LabelId";
	private static final String DISPLAY_TYPE = "/DisplayType";

	/** The Constant PRIME. */
	private static final int PRIME = HashHelper.PRIME;

	/** The Constant SUPPORTED_OBJECTS. */
	private static final List<Class> SUPPORTED_OBJECTS = new ArrayList<>(Arrays.asList(ControlDefinitionImpl.class,
			ConditionDefinitionImpl.class, DataType.class, FieldDefinitionImpl.class, PropertyDefinitionProxy.class,
			TransitionDefinitionImpl.class, TransitionGroupDefinitionImpl.class, RegionDefinitionImpl.class,
			AllowedChildDefinitionImpl.class, AllowedChildConfigurationImpl.class, StateTransitionImpl.class,
			Instance.class, ControlParamImpl.class));

	/**
	 * {@inheritDoc}
	 */
	@Override
	public List<Class> getSupportedObjects() {
		return SUPPORTED_OBJECTS;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public Integer computeHash(HashCalculator calculator, Object object) {
		if (object instanceof Instance) {
			return computeHashCode((Instance) object, calculator);
		} else if (object instanceof ControlDefinition) {
			return computeHashCode((ControlDefinition) object, calculator);
		} else if (object instanceof ConditionDefinitionImpl) {
			return computeHashCode((ConditionDefinitionImpl) object);
		} else if (object instanceof DataType) {
			return computeHashCode((DataType) object);
		} else if (object instanceof TransitionDefinitionImpl) {
			return computeHashCode((TransitionDefinitionImpl) object, calculator);
		} else if (object instanceof TransitionGroupDefinitionImpl) {
			return computeHashCode((TransitionGroupDefinitionImpl) object);
		} else if (object instanceof RegionDefinitionImpl) {
			return computeHashCode((RegionDefinitionImpl) object, calculator);
		} else if (object instanceof WritablePropertyDefinition) {
			return computeHashCode((WritablePropertyDefinition) object, calculator);
		} else if (object instanceof AllowedChildConfiguration) {
			return computeHashCode((AllowedChildConfiguration) object);
		} else if (object instanceof AllowedChildDefinitionImpl) {
			return computeHashCode((AllowedChildDefinitionImpl) object, calculator);
		} else if (object instanceof StateTransitionImpl) {
			return computeHashCode((StateTransition) object, calculator);
		} else if (object instanceof ControlParam) {
			return computeHashCode((ControlParam) object);
		}
		return null;
	}

	/**
	 * Compute hash code for instance.
	 *
	 * @param instance
	 *            the instance
	 * @param calculator
	 *            the calculator
	 * @return the calculated hash
	 */
	protected int computeHashCode(Instance instance, HashCalculator calculator) {
		int result = 1;

		result = HashHelper.computeHash(result, instance.getId(), "Id");
		result = HashHelper.computeHash(result, instance.getIdentifier(), "Identifier");
		result = HashHelper.computeHash(result, instance.getPath(), "Path(" + instance.getPath() + ")");

		result = HashHelper.computeHash(result, instance.getProperties(), calculator, "Properties");

		if (instance instanceof DmsAware) {
			result = HashHelper.computeHash(result, ((DmsAware) instance).getDmsId(), "DmsId");
		}
		if (instance instanceof CMInstance) {
			result = HashHelper.computeHash(result, ((CMInstance) instance).getContentManagementId(),
					"ContentManagementId");
		}

		return result;
	}

	/**
	 * Compute hash code.
	 *
	 * @param definition
	 *            the definition
	 * @param calculator
	 *            the calculator
	 * @return the int
	 */
	protected int computeHashCode(ControlDefinition definition, HashCalculator calculator) {
		int result = 1;

		String path = PathHelper.getPath(definition);
		result = HashHelper.computeHash(result, definition.getIdentifier(), path + "/ControlId");
		result = HashHelper.computeHash(result, definition.getPath(), path + "/Path(" + definition.getPath() + ")");

		result = HashHelper.computeHash(result, definition.getControlParams(), calculator, path + "/ControlParams");
		result = HashHelper.computeHash(result, definition.getUiParams(), calculator, path + "/UiParams");
		result = HashHelper.computeGenericDefinitionInterfaces(result, definition, calculator);

		return result;
	}

	/**
	 * Compute hash code.
	 *
	 * @param definition
	 *            the definition
	 * @return the int
	 */
	protected int computeHashCode(ControlParam definition) {
		int result = 1;

		String path = PathHelper.getPath(definition);
		result = HashHelper.computeHash(result, definition.getIdentifier(), path + "/ParamId");
		result = HashHelper.computeHash(result, definition.getName(), path + NAME);
		result = HashHelper.computeHash(result, definition.getValue(), path + "/Value");
		result = HashHelper.computeHash(result, definition.getPath(), path + "/Path");
		result = HashHelper.computeHash(result, definition.getType(), path + "/Type");

		return result;
	}

	/**
	 * Compute hash code for single {@link FieldDefinitionImpl}.
	 *
	 * @param definition
	 *            the definition
	 * @param calculator
	 *            the calculator
	 * @return the int
	 */
	protected int computeHashCode(WritablePropertyDefinition definition, HashCalculator calculator) {
		int result = 1;

		String path = PathHelper.getPath(definition) + "/" + definition.getIdentifier();
		result = HashHelper.computeHash(result, definition.getName(), path + NAME);
		result = HashHelper.computeHash(result, definition.getDmsType(), path + "/DmsType");
		result = HashHelper.computeHash(result, definition.getRnc(), path + "/Rnc");
		result = HashHelper.computeHash(result, definition.getType(), path + "/Type");
		result = HashHelper.computeHash(result, definition.getDefaultValue(), path + "/DefaultValue");
		result = HashHelper.computeHash(result, definition.getDisplayType(), path + DISPLAY_TYPE);
		result = HashHelper.computeHash(result, definition.getLabelId(), path + LABEL_ID);
		result = HashHelper.computeHash(result, definition.isPreviewEnabled(), path + "/PreviewEnabled");
		result = HashHelper.computeHash(result, definition.getTooltipId(), path + TOOLTIP_ID);
		result = HashHelper.computeHash(result, definition.isMandatory(), path + "/isMandatory");
		result = HashHelper.computeHash(result, definition.isMandatoryEnforced(), path + "/isMandatoryEnforced");
		result = HashHelper.computeHash(result, definition.getMaxLength(), path + "/MaxLength");
		result = HashHelper.computeHash(result, definition.getCodelist(), path + CODELIST);
		result = HashHelper.computeHash(result, definition.getOrder(), path + ORDER);
		result = HashHelper.computeHash(result, definition.isMultiValued(), path + "/isMultiValued");
		result = HashHelper.computeHash(result, definition.isOverride(), path + "/isOverride");
		result = HashHelper.computeHash(result, definition.getContainer(), path + "/Container");
		result = HashHelper.computeHash(result, definition.getUri(), path + URI);
		result = HashHelper.computeHash(result, definition.isUnique(), path + "/unique");
		// CMF-1547: forgot to add result * prime
		result = result * PRIME + calculator.computeHash(definition.getDataType());
		HashHelper.log(path + "/DataType", "dataType", result);
		result = result * PRIME + calculator.computeHash(definition.getControlDefinition());
		HashHelper.log(path + "/ControlDefinition", "control", result);

		if (definition.getFilters() != null && !definition.getFilters().isEmpty()) {
			// changed hash code builder to iterate over the fields at the same
			// manner no matter of the order of the fields
			List<String> list = new ArrayList<>(definition.getFilters());
			Collections.sort(list);
			for (String string : list) {
				result = HashHelper.computeHash(result, string, path + "/Filters(" + string + ")");
			}
		} else {
			result = PRIME * result;
		}
		result = HashHelper.computeGenericDefinitionInterfaces(result, definition, calculator);

		return result;
	}

	/**
	 * Compute hash code.
	 *
	 * @param definition
	 *            the definition
	 * @return the int
	 */
	protected int computeHashCode(DataTypeDefinition definition) {
		int result = 1;

		result = HashHelper.computeHash(result, definition.getId(), "Id");
		result = HashHelper.computeHash(result, definition.getName(), NAME);
		result = HashHelper.computeHash(result, definition.getTitle(), "Title");
		result = HashHelper.computeHash(result, definition.getDescription(), "Description");
		result = HashHelper.computeHash(result, definition.getJavaClassName(), "JavaClassName");
		if (definition instanceof DataType) {
			result = HashHelper.computeHash(result, ((DataType) definition).getUri(), URI);
		} else {
			result = HashHelper.computeHash(result, definition.getFirstUri(), URI);
		}
		return result;
	}

	/**
	 * Compute hash code.
	 *
	 * @param definition
	 *            the definition
	 * @return the int
	 */
	protected int computeHashCode(ConditionDefinitionImpl definition) {
		int result = 1;

		String path = PathHelper.getPath(definition);
		result = HashHelper.computeHash(result, definition.getIdentifier(), path + "/Identifier");
		result = HashHelper.computeHash(result, definition.getRenderAs(), path + "/RenderAs");
		result = HashHelper.computeHash(result, definition.getExpression(), path + "/Expression");
		return result;
	}

	/**
	 * Compute hash code.
	 *
	 * @param definition
	 *            the definition
	 * @param calculator
	 *            the calculator
	 * @return the int
	 */
	protected int computeHashCode(RegionDefinition definition, HashCalculator calculator) {
		int result = 1;

		String path = PathHelper.getPath(definition);
		result = HashHelper.computeHash(result, definition.getIdentifier(), path + "/RegionId");
		result = HashHelper.computeHash(result, definition.getLabelId(), path + LABEL_ID);
		result = HashHelper.computeHash(result, definition.getTooltipId(), path + "/Tooltip");
		result = HashHelper.computeHash(result, definition.getDisplayType(), path + DISPLAY_TYPE);
		result = HashHelper.computeHash(result, definition.getOrder(), path + ORDER);

		result = HashHelper.computeGenericDefinitionInterfaces(result, definition, calculator);
		return result;
	}

	/**
	 * Compute hash code.
	 *
	 * @param definition
	 *            the definition
	 * @param calculator
	 *            the calculator
	 * @return the int
	 */
	protected int computeHashCode(TransitionDefinition definition, HashCalculator calculator) {
		int result = 1;

		String path = PathHelper.getPath(definition);
		result = HashHelper.computeHash(result, definition.getEventId(), path + "/EventId");
		result = HashHelper.computeHash(result, definition.getLabelId(), path + LABEL_ID);
		result = HashHelper.computeHash(result, definition.getDisplayType(), path + DISPLAY_TYPE);
		result = HashHelper.computeHash(result, definition.getTooltipId(), path + TOOLTIP_ID);
		result = HashHelper.computeHash(result, definition.getIdentifier(), path + TRANSITION_ID);
		result = HashHelper.computeHash(result, definition.getNextPrimaryState(), path + "/NextPrimaryState");
		result = HashHelper.computeHash(result, definition.getNextSecondaryState(), path + "/NextSecondaryState");
		result = HashHelper.computeHash(result, definition.getDefaultTransition(), path + "/isDefaultTransition");
		result = HashHelper.computeHash(result, definition.isImmediateAction(), path + "/isImmediate");
		result = HashHelper.computeHash(result, definition.getConfirmationMessageId(), path + "/ConfirmationMessageId");
		result = HashHelper.computeHash(result, definition.getDisabledReasonId(), path + "/DisabledReasonId");
		result = HashHelper.computeHash(result, definition.getPurpose(), path + "/Purpose");
		result = HashHelper.computeHash(result, definition.getOrder(), path + ORDER);
		result = HashHelper.computeHash(result, definition.getOwnerPrefix(), path + "/OwnerPrefix");
		result = HashHelper.computeHash(result, definition.getActionPath(), path + "/ActionPath");
		result = HashHelper.computeHash(result, definition.getGroup(), path + "/Group");


		result = HashHelper.computeGenericDefinitionInterfaces(result, definition, calculator);
		return result;
	}

	/**
	 * Compute hash code of {@link TransitionGroupDefinitionImpl}.
	 *
	 * @param definition
	 *            transition group definition
	 * @param calculator
	 *            hash calculator
	 * @return the computed bash
	 */
	protected int computeHashCode(TransitionGroupDefinitionImpl definition) {
		int result = 1;

		result = HashHelper.computeHash(result, definition.getIdentifier(), "id");
		result = HashHelper.computeHash(result, definition.getLabelId(), LABEL_ID);
		result = HashHelper.computeHash(result, definition.getOrder(), ORDER);
		result = HashHelper.computeHash(result, definition.getParent(), "parent");
		result = HashHelper.computeHash(result, definition.getType(), "type");

		return result;
	}

	/**
	 * Compute hash code for {@link AllowedChildDefinition}.
	 *
	 * @param definition
	 *            the definition
	 * @param calculator
	 *            the calculator
	 * @return the int
	 */
	protected int computeHashCode(AllowedChildDefinition definition, HashCalculator calculator) {
		int result = 1;

		DefinitionModel definitionModel = definition.getParentDefinition();
		String path = "";
		if (definition instanceof PathElement) {
			path = PathHelper.getPath((PathElement) definitionModel) + "/allowedChildDef/" + definition.getIdentifier();
		}
		result = HashHelper.computeHash(result, definition.getIdentifier(), path + "/AllowedChildId");
		result = HashHelper.computeHash(result, definition.getType(), path + "/ObjectType");

		result = HashHelper.computeHash(result, definition.getFilters(), calculator, path + "/Filters");
		result = HashHelper.computeHash(result, definition.getPermissions(), calculator, path + "/Permissions");

		return result;
	}

	/**
	 * Compute hash code.
	 *
	 * @param definition
	 *            the definition
	 * @return the int
	 */
	protected int computeHashCode(AllowedChildConfiguration definition) {
		int result = 1;

		result = HashHelper.computeHash(result, definition.getProperty(), "Property");
		result = HashHelper.computeHash(result, definition.getCodelist(), CODELIST);
		result = HashHelper.computeHash(result, definition.getFilterMode(), "FilterMode");

		if (definition.getValues() != null && !definition.getValues().isEmpty()) {
			// changed hash code builder to iterate over the fields at the same
			// manner no matter of the order of the fields
			List<String> list = new ArrayList<>(definition.getValues());
			Collections.sort(list);
			for (String string : list) {
				result = HashHelper.computeHash(result, string, "Values(" + string + ")");
			}
		} else {
			result = PRIME * result;
		}
		return result;
	}

	/**
	 * Compute hash code.
	 *
	 * @param definition
	 *            the definition
	 * @param calculator
	 *            the calculator
	 * @return the int
	 */
	protected int computeHashCode(StateTransition definition, HashCalculator calculator) {
		int result = 1;

		result = HashHelper.computeHash(result, definition.getFromState(), "FromState");
		result = HashHelper.computeHash(result, definition.getToState(), "ToState");
		result = HashHelper.computeHash(result, definition.getTransitionId(), TRANSITION_ID);

		result = HashHelper.computeGenericDefinitionInterfaces(result, definition, calculator);
		return result;
	}

}
