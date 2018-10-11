package com.sirma.itt.seip.definition.compile;

import com.sirma.itt.seip.definition.Mergeable;
import com.sirma.itt.seip.definition.Mergeable.MergeableInstanceFactory;
import com.sirma.itt.seip.definition.model.AllowedChildConfigurationImpl;
import com.sirma.itt.seip.definition.model.AllowedChildDefinitionImpl;
import com.sirma.itt.seip.definition.model.ConditionDefinitionImpl;
import com.sirma.itt.seip.definition.model.ControlParamImpl;
import com.sirma.itt.seip.definition.model.GenericDefinitionImpl;
import com.sirma.itt.seip.definition.model.PropertyDefinitionProxy;
import com.sirma.itt.seip.definition.model.RegionDefinitionImpl;
import com.sirma.itt.seip.definition.model.StateTransitionImpl;
import com.sirma.itt.seip.definition.model.TransitionDefinitionImpl;
import com.sirma.itt.seip.definition.model.TransitionGroupDefinitionImpl;

/**
 * An Emf factory implementation for creating Mergeable objects.
 *
 * @author BBonev
 */
@SuppressWarnings("rawtypes")
public enum EmfMergeableFactory implements MergeableInstanceFactory {

	GENERIC_DEFINITION {
		@Override
		public Mergeable createInstance() {
			return new GenericDefinitionImpl();
		}
	},
	FIELD_DEFINITION {
		@Override
		public Mergeable createInstance() {
			return new PropertyDefinitionProxy();
		}
	},
	CONTROL_PARAM {
		@Override
		public Mergeable createInstance() {
			return new ControlParamImpl();
		}
	},
	REGION_DEFINITION {
		@Override
		public Mergeable createInstance() {
			return new RegionDefinitionImpl();
		}
	},
	TRANSITION_DEFINITION {
		@Override
		public Mergeable createInstance() {
			return new TransitionDefinitionImpl();
		}
	},
	TRANSITION_GROUP_DEFINITION {
		@Override
		public Mergeable createInstance() {
			return new TransitionGroupDefinitionImpl();
		}
	},
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
