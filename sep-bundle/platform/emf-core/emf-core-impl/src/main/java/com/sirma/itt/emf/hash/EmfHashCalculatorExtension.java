package com.sirma.itt.emf.hash;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import com.sirma.itt.emf.definition.model.AllowedChildConfiguration;
import com.sirma.itt.emf.definition.model.AllowedChildConfigurationImpl;
import com.sirma.itt.emf.definition.model.AllowedChildDefinition;
import com.sirma.itt.emf.definition.model.AllowedChildDefinitionImpl;
import com.sirma.itt.emf.definition.model.ConditionDefinitionImpl;
import com.sirma.itt.emf.definition.model.ControlDefinitionImpl;
import com.sirma.itt.emf.definition.model.ControlParamImpl;
import com.sirma.itt.emf.definition.model.DataType;
import com.sirma.itt.emf.definition.model.DataTypeDefinition;
import com.sirma.itt.emf.definition.model.FieldDefinitionImpl;
import com.sirma.itt.emf.definition.model.PropertyDefinitionProxy;
import com.sirma.itt.emf.definition.model.RegionDefinition;
import com.sirma.itt.emf.definition.model.RegionDefinitionImpl;
import com.sirma.itt.emf.definition.model.StateTransitionImpl;
import com.sirma.itt.emf.definition.model.TransitionDefinition;
import com.sirma.itt.emf.definition.model.TransitionDefinitionImpl;
import com.sirma.itt.emf.definition.model.WritablePropertyDefinition;
import com.sirma.itt.emf.instance.model.CMInstance;
import com.sirma.itt.emf.instance.model.DMSInstance;
import com.sirma.itt.emf.instance.model.Instance;
import com.sirma.itt.emf.instance.model.TenantAware;
import com.sirma.itt.emf.plugin.Extension;
import com.sirma.itt.emf.state.transition.StateTransition;

/**
 * Hash calculator extension to provide hash computation for the base Emf classes.
 *
 * @author BBonev
 */
@Extension(target = HashCalculatorExtension.TARGET_NAME, order = 10)
public class EmfHashCalculatorExtension implements HashCalculatorExtension {

	/** The Constant NAME. */
	private static final String NAME = "Name";
	/** The Constant URI. */
	private static final String URI = "Uri";
	/** The Constant FIELDS. */
	private static final String FIELDS = "Fields";
	/** The Constant CONDITIONS. */
	private static final String CONDITIONS = "Conditions";
	/** The Constant TRANSITION_ID. */
	private static final String TRANSITION_ID = "TransitionId";
	/** The Constant TOOLTIP_ID. */
	private static final String TOOLTIP_ID = "TooltipId";
	/** The Constant ORDER. */
	private static final String ORDER = "Order";
	/** The Constant CODELIST. */
	private static final String CODELIST = "Codelist";
	/** The Constant LABEL_ID. */
	private static final String LABEL_ID = "LabelId";
	/** The Constant DISPLAY_TYPE. */
	private static final String DISPLAY_TYPE = "DisplayType";

	/** The Constant PRIME. */
	private static final int PRIME = HashHelper.PRIME;

	/** The Constant SUPPORTED_OBJECTS. */
	private static final List<Class<?>> SUPPORTED_OBJECTS = new ArrayList<Class<?>>(Arrays.asList(
			ControlDefinitionImpl.class, ConditionDefinitionImpl.class, DataType.class,
			FieldDefinitionImpl.class, PropertyDefinitionProxy.class,
			TransitionDefinitionImpl.class, RegionDefinitionImpl.class,
			AllowedChildDefinitionImpl.class, AllowedChildConfigurationImpl.class,
			StateTransitionImpl.class, Instance.class));

	/**
	 * {@inheritDoc}
	 */
	@Override
	public List<Class<?>> getSupportedObjects() {
		return SUPPORTED_OBJECTS;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public Integer computeHash(HashCalculator calculator, Object object) {
		if (object instanceof Instance) {
			return computeHashCode((Instance) object, calculator);
		} else if (object instanceof ControlDefinitionImpl) {
			return computeHashCode((ControlDefinitionImpl) object, calculator);
		} else if (object instanceof ConditionDefinitionImpl) {
			return computeHashCode((ConditionDefinitionImpl) object, calculator);
		} else if (object instanceof DataType) {
			return computeHashCode((DataType) object, calculator);
		} else if (object instanceof TransitionDefinitionImpl) {
			return computeHashCode((TransitionDefinitionImpl) object, calculator);
		} else if (object instanceof RegionDefinitionImpl) {
			return computeHashCode((RegionDefinitionImpl) object, calculator);
		} else if (object instanceof WritablePropertyDefinition) {
			return computeHashCode((WritablePropertyDefinition) object, calculator);
		} else if (object instanceof AllowedChildConfigurationImpl) {
			return computeHashCode((AllowedChildConfigurationImpl) object, calculator);
		} else if (object instanceof AllowedChildDefinitionImpl) {
			return computeHashCode((AllowedChildDefinitionImpl) object, calculator);
		} else if (object instanceof StateTransitionImpl) {
			return computeHashCode((StateTransition) object, calculator);
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
		result = HashHelper.computeHash(result, instance.getPath(), "Path(" + instance.getPath()
				+ ")");

		result = HashHelper.computeHash(result, instance.getProperties(), calculator, "Properties");

		if (instance instanceof DMSInstance) {
			result = HashHelper.computeHash(result, ((DMSInstance) instance).getDmsId(), "DmsId");
		}
		if (instance instanceof TenantAware) {
			result = HashHelper.computeHash(result, ((TenantAware) instance).getContainer(),
					"Container");
		}
		if (instance instanceof CMInstance) {
			result = HashHelper.computeHash(result,
					((CMInstance) instance).getContentManagementId(), "ContentManagementId");
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
	protected int computeHashCode(ControlDefinitionImpl definition, HashCalculator calculator) {
		int result = 1;

		result = HashHelper.computeHash(result, definition.getIdentifier(), "ControlId");
		result = HashHelper.computeHash(result, definition.getPath(), "Path(" + definition.getPath() + ")");

		result = HashHelper.computeHash(result, definition.getControlParams(), calculator, "ControlParams");
		result = HashHelper.computeHash(result, definition.getUiParams(), calculator, "UiParams");
		result = HashHelper.computeHash(result, definition.getFields(), calculator, FIELDS);

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
	protected int computeHashCode(ControlParamImpl definition, HashCalculator calculator) {
		int result = 1;

		result = HashHelper.computeHash(result, definition.getIdentifier(), "ParamId");
		result = HashHelper.computeHash(result, definition.getName(), NAME);
		result = HashHelper.computeHash(result, definition.getValue(), "Value");
		result = HashHelper.computeHash(result, definition.getPath(), "Path");

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

		/*
		 * codelist, container, displaytype, dmstype, filters, label, mandatory, mandatoryenforced,
		 * maxlength, multivalued, fieldname, indexorder, override, previewempty, rnc, tooltip,
		 * fieldtype, value, datatype_id
		 */
		result = HashHelper.computeHash(result, definition.getName(), NAME);
		result = HashHelper.computeHash(result, definition.getDmsType(), "DmsType");
		result = HashHelper.computeHash(result, definition.getRnc(), "Rnc");
		result = HashHelper.computeHash(result, definition.getType(), "Type");
		result = HashHelper.computeHash(result, definition.getDefaultValue(), "DefaultValue");
		result = HashHelper.computeHash(result, definition.getDisplayType(), DISPLAY_TYPE);
		result = HashHelper.computeHash(result, definition.getLabelId(), LABEL_ID);
		result = HashHelper.computeHash(result, definition.isPreviewEnabled(), "PreviewEnabled");
		result = HashHelper.computeHash(result, definition.getTooltipId(), TOOLTIP_ID);
		result = HashHelper.computeHash(result, definition.isMandatory(), "isMandatory");
		result = HashHelper.computeHash(result, definition.isMandatoryEnforced(),
				"isMandatoryEnforced");
		result = HashHelper.computeHash(result, definition.getMaxLength(), "MaxLength");
		result = HashHelper.computeHash(result, definition.getCodelist(), CODELIST);
		result = HashHelper.computeHash(result, definition.getOrder(), ORDER);
		result = HashHelper.computeHash(result, definition.isMultiValued(), "isMultiValued");
		result = HashHelper.computeHash(result, definition.isOverride(), "isOverride");
		result = HashHelper.computeHash(result, definition.getContainer(), "Container");
		result = HashHelper.computeHash(result, definition.getUri(), URI);
		// CMF-1547: forgot to add result * prime
		result = (result * PRIME) + computeHashCode(definition.getDataType(), calculator);

		if (definition.getControlDefinition() != null) {
			result = (PRIME * result)
					+ computeHashCode((ControlDefinitionImpl) definition.getControlDefinition(), calculator);
		} else {
			result = PRIME * result;
		}
		if ((definition.getFilters() != null) && !definition.getFilters().isEmpty()) {
			// changed hash code builder to iterate over the fields at the same
			// manner no matter of the order of the fields
			List<String> list = new ArrayList<String>(definition.getFilters());
			Collections.sort(list);
			for (String string : list) {
				result = HashHelper.computeHash(result, string, "Filters(" + string + ")");
			}
		} else {
			result = PRIME * result;
		}
		result = HashHelper.computeHash(result, definition.getConditions(), calculator, CONDITIONS);
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
	protected int computeHashCode(DataTypeDefinition definition, HashCalculator calculator) {
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
	 * @param calculator
	 *            the calculator
	 * @return the int
	 */
	protected int computeHashCode(ConditionDefinitionImpl definition, HashCalculator calculator) {
		int result = 1;
		result = HashHelper.computeHash(result, definition.getIdentifier(), "Identifier");
		result = HashHelper.computeHash(result, definition.getRenderAs(), "RenderAs");
		result = HashHelper.computeHash(result, definition.getExpression(), "Expression");
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

		result = HashHelper.computeHash(result, definition.getIdentifier(), "RegionId");
		result = HashHelper.computeHash(result, definition.getLabelId(), LABEL_ID);
		result = HashHelper.computeHash(result, definition.getTooltipId(), "Tooltip");
		result = HashHelper.computeHash(result, definition.getDisplayType(), DISPLAY_TYPE);
		result = HashHelper.computeHash(result, definition.getOrder(), ORDER);

		result = HashHelper.computeHash(result, definition.getFields(), calculator, FIELDS);
		result = HashHelper.computeHash(result, definition.getConditions(), calculator, CONDITIONS);

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
		result = HashHelper.computeHash(result, definition.getEventId(), "EventId");
		result = HashHelper.computeHash(result, definition.getLabelId(), LABEL_ID);
		result = HashHelper.computeHash(result, definition.getTooltipId(), TOOLTIP_ID);
		result = HashHelper.computeHash(result, definition.getIdentifier(), TRANSITION_ID);
		result = HashHelper.computeHash(result, definition.getNextPrimaryState(),
				"NextPrimaryState");
		result = HashHelper.computeHash(result, definition.getNextSecondaryState(),
				"NextSecondaryState");
		result = HashHelper.computeHash(result, definition.getDefaultTransition(),
				"isDefaultTransition");
		result = HashHelper.computeHash(result, definition.isImmediateAction(), "isImmediate");
		result = HashHelper.computeHash(result, definition.getConfirmationMessageId(), "ConfirmationMessageId");
		result = HashHelper.computeHash(result, definition.getDisabledReasonId(), "DisabledReasonId");
		result = HashHelper.computeHash(result, definition.getPurpose(), "Purpose");
		result = HashHelper.computeHash(result, definition.getOrder(), ORDER);
		result = HashHelper.computeHash(result, definition.getOwnerPrefix(), "OwnerPrefix");

		result = HashHelper.computeHash(result, definition.getFields(), calculator, FIELDS);
		result = HashHelper.computeHash(result, definition.getConditions(), calculator, CONDITIONS);

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
		result = HashHelper.computeHash(result, definition.getIdentifier(), "AllowedChildId");
		result = HashHelper.computeHash(result, definition.getType(), "ObjectType");

		result = HashHelper.computeHash(result, definition.getFilters(), calculator, "Filters");
		result = HashHelper.computeHash(result, definition.getPermissions(), calculator, "Permissions");

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
	protected int computeHashCode(AllowedChildConfiguration definition, HashCalculator calculator) {
		int result = 1;
		result = HashHelper.computeHash(result, definition.getProperty(), "Property");
		result = HashHelper.computeHash(result, definition.getCodelist(), CODELIST);

		if ((definition.getValues() != null) && !definition.getValues().isEmpty()) {
			// changed hash code builder to iterate over the fields at the same
			// manner no matter of the order of the fields
			List<String> list = new ArrayList<String>(definition.getValues());
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

		result = HashHelper.computeHash(result, definition.getConditions(), calculator, CONDITIONS);
		return result;
	}

}
