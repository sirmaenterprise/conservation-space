package com.sirma.itt.cmf.util;

import com.sirma.itt.cmf.beans.definitions.impl.GenericDefinitionImpl;
import com.sirma.itt.seip.domain.definition.Mergeable;
import com.sirma.itt.seip.domain.definition.Mergeable.MergeableInstanceFactory;

/**
 * A factory for creating Mergeable objects.
 *
 * @author BBonev
 */
@SuppressWarnings("rawtypes")
public enum CmfMergeableFactory implements MergeableInstanceFactory {

	/** The workflow definition. */
	GENERIC_DEFINITION {
		@Override
		public Mergeable createInstance() {
			return new GenericDefinitionImpl();
		}
	}
}