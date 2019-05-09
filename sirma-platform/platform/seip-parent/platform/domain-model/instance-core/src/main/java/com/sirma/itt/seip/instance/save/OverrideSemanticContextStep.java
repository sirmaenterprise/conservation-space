package com.sirma.itt.seip.instance.save;

import com.sirma.itt.seip.configuration.Options;
import com.sirma.itt.seip.instance.InstanceSaveContext;
import com.sirma.itt.seip.instance.InstanceSaveStep;
import com.sirma.itt.seip.plugin.Extension;

/**
 * Step for overriding the graph in which semantic statements are persisted.
 * <p>
 * To use it provide {@link org.eclipse.rdf4j.model.IRI} under the {@link #CONTEXT_KEY} key in the context.
 *
 * @author Mihail Radkov
 */
@Extension(target = InstanceSaveStep.NAME, order = 123)
public class OverrideSemanticContextStep implements InstanceSaveStep {

	private static final String CONTEXT_KEY = "SEMANTIC_PERSISTENCE_CONTEXT";

	@Override
	public void beforeSave(InstanceSaveContext saveContext) {
		if (saveContext.containsKey(CONTEXT_KEY)) {
			Options.USE_CUSTOM_GRAPH.set(saveContext.get(CONTEXT_KEY).toString());
		}
	}

	@Override
	public void afterSave(InstanceSaveContext saveContext) {
		Options.USE_CUSTOM_GRAPH.clear();
	}

	@Override
	public void rollbackBeforeSave(InstanceSaveContext saveContext) {
		afterSave(saveContext);
	}

	@Override
	public String getName() {
		return "assignSemanticContextStep";
	}
}
