package com.sirma.cmf.web.workflow.task;

import static org.testng.Assert.assertNull;

import org.mockito.Mockito;
import org.testng.annotations.Test;

import com.sirma.cmf.CMFTest;
import com.sirma.cmf.web.DocumentContext;
import com.sirma.cmf.web.entity.dispatcher.EntityOpenDispatcher;
import com.sirma.itt.commons.utils.reflection.ReflectionUtils;

/**
 * Test for WorkflowTransitionExecutor.
 */
@Test
public class WorkflowTransitionExecutorTest extends CMFTest {

	/** The executor. */
	private final WorkflowTransitionExecutor executor;

	/** The entity open dispatcher. */
	private final EntityOpenDispatcher entityOpenDispatcher;

	/**
	 * Instantiates a new workflow transition executor test.
	 */
	public WorkflowTransitionExecutorTest() {
		executor = new WorkflowTransitionExecutor() {

			private DocumentContext documentContext = new DocumentContext();

			@Override
			public DocumentContext getDocumentContext() {
				return documentContext;
			}

			@Override
			public void setDocumentContext(DocumentContext documentContext) {
				this.documentContext = documentContext;
			}

		};

		entityOpenDispatcher = Mockito.mock(EntityOpenDispatcher.class);

		ReflectionUtils.setField(executor, "log", LOG);
		ReflectionUtils.setField(executor, "entityOpenDispatcher", entityOpenDispatcher);
	}

	/**
	 * Execute transition test.
	 */
	public void executeTransitionTest() {
		String navigation = executor.executeTransition(null);
		assertNull(navigation);

		// TODO: implement other tests
		// navigation = executor.executeTransition("startworkflow");

		// navigation = executor.executeTransition(ActionTypeConstants.STOP);

		// navigation = executor.executeTransition("cancel");

		// navigation = executor.executeTransition("cancelstart");

		// navigation = executor.executeTransition("?othertransition?");

	}
}
