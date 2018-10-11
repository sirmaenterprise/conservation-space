package com.sirma.itt.seip.instance.actions;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNotNull;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import org.testng.annotations.BeforeMethod;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import com.sirma.itt.seip.context.Context;
import com.sirma.itt.seip.domain.instance.EmfInstance;
import com.sirma.itt.seip.domain.instance.Instance;
import com.sirma.itt.seip.exception.EmfRuntimeException;
import com.sirma.itt.seip.instance.state.Operation;
import com.sirma.itt.seip.util.ReflectionUtils;

/**
 * The Class OperationInvokerTest.
 *
 * @author BBonev
 */
@Test
public class OperationInvokerTest {

	InstanceOperationImpl operation1 = new InstanceOperationImpl(false, "add");

	InstanceOperationImpl operation2 = new InstanceOperationImpl(false, "delete", "create");

	InstanceOperationImpl operation3 = new InstanceOperationImpl(false, "clone", "copy");

	InstanceOperationImpl operation4 = new InstanceOperationImpl(false, "detach", "attach");

	InstanceOperationImpl operation5 = new InstanceOperationImpl(false, "add");

	OperationInvoker invoker;

	/**
	 * Before method.
	 */
	@BeforeMethod
	public void beforeMethod() {
		invoker = new OperationInvoker();
		ReflectionUtils.setFieldValue(invoker, "operations",
				Arrays.asList(operation1, operation2, operation3, operation4, operation5));
	}

	/**
	 * Test instance invokation.
	 *
	 * @param operation
	 *            the operation
	 */
	@Test(dataProvider = "operationsProvider")
	public void testInstanceInvocation(String operation) {
		invoker.initialize();

		Instance instance = new EmfInstance();
		instance.setId("emf:instance");
		Operation expected = new Operation(operation);
		Context<String, Object> context = invoker.createDefaultContext(instance, expected);
		assertNotNull(context);
		Object invokeOperation = invoker.invokeOperation(context);
		assertNotNull(invokeOperation);
		assertEquals(invokeOperation, expected);
	}

	/**
	 * Test invoke delete.
	 */
	public void testInvokeDelete() {
		invoker.initialize();

		Instance instance = new EmfInstance();
		instance.setId("emf:instance");
		Operation expected = new Operation("delete");
		Object invokeOperation = invoker.invokeOperation(invoker.createDefaultContext(instance, expected));
		assertNotNull(invokeOperation);
		assertEquals(invokeOperation, expected);
	}

	/**
	 * Test not supported operation.
	 */
	@Test(expectedExceptions = EmfRuntimeException.class)
	public void testNotSupportedOperation() {
		invoker.initialize();
		Instance instance = new EmfInstance();
		instance.setId("emf:instance");
		Operation operation = new Operation("notSupportedOperation");
		Context<String, Object> context = invoker.createDefaultContext(instance, operation);
		assertNotNull(context);
		invoker.invokeOperation(context);
	}

	/**
	 * Test operation support.
	 */
	public void testOperationSupport() {
		invoker.initialize();
		Instance instance = new EmfInstance();
		instance.setId("emf:instance");
		Operation operation = new Operation("add");
		Context<String, Object> context = invoker.createDefaultContext(instance, operation);
		assertNotNull(context);
		Object result = invoker.invokeOperation(context);
		assertEquals(result, operation);

		operation2.setAcceptAll(true);
		try {
			synchronized (operation2) {
				operation = new Operation("someMissingOperation");
				context = invoker.createDefaultContext(instance, operation);
				assertNotNull(context);
				result = invoker.invokeOperation(context);
				assertEquals(result, operation);
			}
		} finally {
			operation2.setAcceptAll(false);
		}
	}

	/**
	 * Operations provider.
	 *
	 * @return the object[][]
	 */
	@DataProvider(name = "operationsProvider")
	public Object[][] operationsProvider() {
		return new Object[][] { { "add" }, { "delete" }, { "create" }, { "clone" }, { "copy" }, { "detach" },
				{ "attach" } };
	}

	/**
	 * The Class InstanceOperationImpl.
	 */
	private static class InstanceOperationImpl implements InstanceOperation {

		/** The operations. */
		private final Set<String> operations;
		private boolean acceptAll;

		/**
		 * Instantiates a new instance operation impl.
		 *
		 * @param supportOperations
		 * @param operations
		 *            the operations
		 */
		/**
		 * @param supportOperations
		 * @param operations
		 */
		public InstanceOperationImpl(boolean supportOperations, String... operations) {
			acceptAll = supportOperations;
			this.operations = new HashSet<>(Arrays.asList(operations));
		}

		/**
		 * Checks if is applicable.
		 *
		 * @param instance
		 *            the instance
		 * @param operation
		 *            the operation
		 * @return true, if is applicable
		 */
		@Override
		public boolean isApplicable(Instance instance, Operation operation) {
			return isAcceptAll() || operations.contains(operation.getOperation());
		}

		/**
		 * Gets the supported operations.
		 *
		 * @return the supported operations
		 */
		@Override
		public Set<String> getSupportedOperations() {
			return operations;
		}

		/**
		 * Execute.
		 *
		 * @param executionContext
		 *            the execution context
		 * @return the object
		 */
		@Override
		public Object execute(Context<String, Object> executionContext) {
			assertNotNull(executionContext);
			return executionContext.getIfSameType("operation", Operation.class);
		}

		/**
		 * Getter method for acceptAll.
		 *
		 * @return the acceptAll
		 */
		public boolean isAcceptAll() {
			return acceptAll;
		}

		/**
		 * Setter method for acceptAll.
		 *
		 * @param acceptAll
		 *            the acceptAll to set
		 */
		public void setAcceptAll(boolean acceptAll) {
			this.acceptAll = acceptAll;
		}

	}

}