package com.sirma.itt.cmf.util;

import com.sirma.itt.cmf.beans.definitions.impl.CaseDefinitionImpl;
import com.sirma.itt.cmf.beans.definitions.impl.DocumentDefinitionImpl;
import com.sirma.itt.cmf.beans.definitions.impl.DocumentDefinitionRefImpl;
import com.sirma.itt.cmf.beans.definitions.impl.GenericDefinitionImpl;
import com.sirma.itt.cmf.beans.definitions.impl.SectionDefinitionImpl;
import com.sirma.itt.cmf.beans.definitions.impl.TaskDefinitionRefImpl;
import com.sirma.itt.cmf.beans.definitions.impl.TaskDefinitionTemplateImpl;
import com.sirma.itt.cmf.beans.definitions.impl.WorkflowDefinitionImpl;
import com.sirma.itt.emf.domain.model.Mergeable;
import com.sirma.itt.emf.domain.model.Mergeable.MergeableInstanceFactory;

/**
 * A factory for creating Mergeable objects.
 *
 * @author BBonev
 */
@SuppressWarnings("rawtypes")
public enum CmfMergeableFactory implements MergeableInstanceFactory {

	/** The case definition. */
	CASE_DEFINITION {
		@Override
		public Mergeable createInstance() {
			return new CaseDefinitionImpl();
		}
	},
	/** The document definition. */
	DOCUMENT_DEFINITION {
		@Override
		public Mergeable createInstance() {
			return new DocumentDefinitionImpl();
		}
	},
	/** The document definition ref. */
	DOCUMENT_DEFINITION_REF {
		@Override
		public Mergeable createInstance() {
			return new DocumentDefinitionRefImpl();
		}
	},
	/** The section definition. */
	SECTION_DEFINITION {
		@Override
		public Mergeable createInstance() {
			return new SectionDefinitionImpl();
		}
	},
	/** The task definition ref. */
	TASK_DEFINITION_REF {
		@Override
		public Mergeable createInstance() {
			return new TaskDefinitionRefImpl();
		}
	},
	/** The task definition template. */
	TASK_DEFINITION_TEMPLATE {
		@Override
		public Mergeable createInstance() {
			return new TaskDefinitionTemplateImpl();
		}
	},
	/** The workflow definition. */
	WORKFLOW_DEFINITION {
		@Override
		public Mergeable createInstance() {
			return new WorkflowDefinitionImpl();
		}
	},
	/** The workflow definition. */
	GENERIC_DEFINITION {
		@Override
		public Mergeable createInstance() {
			return new GenericDefinitionImpl();
		}
	}
}