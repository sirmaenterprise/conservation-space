package com.sirma.itt.seip.instance.save;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.After;
import org.junit.Test;

import com.sirma.itt.seip.configuration.Options;
import com.sirma.itt.seip.domain.instance.EmfInstance;
import com.sirma.itt.seip.instance.InstanceSaveContext;
import com.sirma.itt.seip.instance.state.Operation;
import com.sirma.itt.semantic.model.vocabulary.EMF;

/**
 * Tests the context override in {@link OverrideSemanticContextStep}.
 *
 * @author Mihail Radkov
 */
public class OverrideSemanticContextStepTest {

	@Test
	public void shouldOverrideTheContextIfSupplied() {
		OverrideSemanticContextStep step = new OverrideSemanticContextStep();
		assertFalse(Options.USE_CUSTOM_GRAPH.isSet());

		InstanceSaveContext saveContext = InstanceSaveContext.create(new EmfInstance(), Operation.NO_OPERATION);
		saveContext.put("SEMANTIC_PERSISTENCE_CONTEXT", EMF.DATA_CONTEXT);
		step.beforeSave(saveContext);

		assertTrue(Options.USE_CUSTOM_GRAPH.isSet());
		assertEquals(EMF.DATA_CONTEXT.toString(), Options.USE_CUSTOM_GRAPH.get());

		step.afterSave(saveContext);
		assertFalse(Options.USE_CUSTOM_GRAPH.isSet());
	}

	@Test
	public void shouldClearTheOverrideDuringRollback() {
		OverrideSemanticContextStep step = new OverrideSemanticContextStep();
		Options.USE_CUSTOM_GRAPH.set(EMF.DATA_CONTEXT.toString());

		InstanceSaveContext saveContext = InstanceSaveContext.create(new EmfInstance(), Operation.NO_OPERATION);
		step.rollbackBeforeSave(saveContext);

		assertFalse(Options.USE_CUSTOM_GRAPH.isSet());
	}

	@After
	public void cleanUp() {
		Options.USE_CUSTOM_GRAPH.clear();
	}
}
