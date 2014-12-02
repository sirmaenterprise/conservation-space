package com.sirma.itt.emf.definition.compile;

import com.sirma.itt.emf.definition.model.AllowedChildConfigurationImpl;
import com.sirma.itt.emf.definition.model.AllowedChildDefinitionImpl;
import com.sirma.itt.emf.definition.model.ConditionDefinitionImpl;
import com.sirma.itt.emf.definition.model.ControlDefinitionImpl;
import com.sirma.itt.emf.definition.model.ControlParamImpl;
import com.sirma.itt.emf.definition.model.PropertyDefinitionProxy;
import com.sirma.itt.emf.definition.model.RegionDefinitionImpl;
import com.sirma.itt.emf.definition.model.StateTransitionImpl;
import com.sirma.itt.emf.definition.model.TransitionDefinitionImpl;
import com.sirma.itt.emf.domain.model.Mergeable;
import com.sirma.itt.emf.domain.model.Mergeable.MergeableInstanceFactory;

/**
 * An Emf factory implementation for creating Mergeable objects.
 * 
 * @author BBonev
 */
@SuppressWarnings("rawtypes")
public enum EmfMergeableFactory implements MergeableInstanceFactory {
	/** The field definition. */
	FIELD_DEFINITION {
		@Override
		public Mergeable createInstance() {
			return new PropertyDefinitionProxy();
		}
	},
	/** The control definition. */
	CONTROL_DEFINITION {
		@Override
		public Mergeable createInstance() {
			return new ControlDefinitionImpl();
		}
	},
	/** The control param. */
	CONTROL_PARAM {
		@Override
		public Mergeable createInstance() {
			return new ControlParamImpl();
		}
	},
	/** The region definition. */
	REGION_DEFINITION {
		@Override
		public Mergeable createInstance() {
			return new RegionDefinitionImpl();
		}
	},
	/** The region definition template. */
	REGION_DEFINITION_TEMPLATE {
		@Override
		public Mergeable createInstance() {
			RegionDefinitionImpl definitionImpl = new RegionDefinitionImpl();
			definitionImpl.setTemplate(true);
			return definitionImpl;
		}
	},
	/** The transition definition. */
	TRANSITION_DEFINITION {
		@Override
		public Mergeable createInstance() {
			return new TransitionDefinitionImpl();
		}
	},
	/** The transition definition template. */
	TRANSITION_DEFINITION_TEMPLATE {
		@Override
		public Mergeable createInstance() {
			TransitionDefinitionImpl definitionImpl = new TransitionDefinitionImpl();
			definitionImpl.setTemplate(true);
			return definitionImpl;
		}
	},
	/** The condition definition. */
	CONDITION_DEFINITION {
		@Override
		public Mergeable createInstance() {
			return new ConditionDefinitionImpl();
		}
	},
	ALLOWED_CHILDREN {
		@Override
		public Mergeable createInstance() {
			return new AllowedChildDefinitionImpl();
		}
	},
	ALLOWED_CHILDREN_CONFIGURATION {
		@Override
		public Mergeable createInstance() {
			return new AllowedChildConfigurationImpl();
		}
	},
	STATE_TRANSITION {
		@Override
		public Mergeable createInstance() {
			return new StateTransitionImpl();
		}
	};
}
