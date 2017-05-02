package com.sirma.itt.seip.instance.actions;

import java.util.HashMap;
import java.util.Map;

import javax.annotation.PostConstruct;
import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sirma.itt.seip.instance.actions.ExecutableOperation;
import com.sirma.itt.seip.instance.actions.ExecutableOperationFactory;
import com.sirma.itt.seip.instance.actions.ExecutableOperationProperties;
import com.sirma.itt.seip.plugin.ExtensionPoint;

/**
 * Default implementation of a {@link ExecutableOperationFactory}. The implementation injects all operations and maps
 * them by operation id. If one operation is defined more then once the one with higher priority will be used.
 *
 * @author BBonev
 */
@ApplicationScoped
public class ExecutableOperationFactoryImpl implements ExecutableOperationFactory {

	/** The Constant LOGGER. */
	private static final Logger LOGGER = LoggerFactory.getLogger("ExecutableOperationFactory");

	/** The operations extension point. */
	@Inject
	@ExtensionPoint(value = ExecutableOperation.TARGET_NAME)
	private Iterable<ExecutableOperation> operationsExtensionPoint;

	/** The operations mapping. */
	private Map<String, ExecutableOperation> operationsMapping;

	/**
	 * Initializes the operations mapping
	 */
	@PostConstruct
	public void initializeMappings() {
		operationsMapping = new HashMap<>(100);
		for (ExecutableOperation operation : operationsExtensionPoint) {
			ExecutableOperation current = operationsMapping.get(operation.getOperation());
			if (current != null) {
				LOGGER.warn("Overriding executor for operation {} from {} to {}", operation.getOperation(), current,
						operation);
			}
			operationsMapping.put(operation.getOperation(), operation);
		}
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public ExecutableOperation getExecutor(String operation) {
		return operationsMapping.get(operation);
	}

	@Override
	public ExecutableOperation getImmediateExecutor() {
		return getExecutor(ExecutableOperationProperties.IMMEDIATE_OPERATION_ID);
	}

}
