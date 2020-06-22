package com.sirma.sep.model.management;

import java.lang.invoke.MethodHandles;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.function.Consumer;
import java.util.function.Supplier;
import java.util.stream.Stream;

import javax.enterprise.inject.Instance;
import javax.inject.Inject;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sirma.itt.seip.CachingSupplier;
import com.sirma.sep.model.management.operation.ChangeSetValidationFailed;
import com.sirma.sep.model.management.operation.ModelChangeSet;
import com.sirma.sep.model.management.operation.ModelChangeSetInfo;
import com.sirma.sep.model.management.operation.ModelChangeSetOperation;

/**
 * Manager for {@link ModelChangeSetOperation}s. Provides means for executing change sets over given model.
 *
 * @author <a href="mailto:borislav.bonev@sirma.bg">Borislav Bonev</a>
 * @since 27/07/2018
 */
public class ModelChangeSetOperationManager {

	private static final Logger LOGGER = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

	private Map<String, List<ModelChangeSetOperation<Object>>> operationMap = new HashMap<>();

	@Inject
	ModelChangeSetOperationManager(Instance<ModelChangeSetOperation<?>> operations) {
		for (ModelChangeSetOperation operation : operations) {
			registerOperationHandler(operation.getName(), operation);
		}
	}

	public void execute(Models models, Collection<ModelChangeSetInfo> changeSets,
			OnSuccessfulChangeSetLister onSuccessfulChange, OnFailedChangeSetListener onFailedChange) {

		// function that encapsulates the logic needed for executing a single change set by applicable operation
		OperationInvoker executor = createExecutor(models, onSuccessfulChange, onFailedChange);

		changeSets.forEach(callChangeSetProcessor(() -> executor, onFailedChange));
	}

	private Consumer<ModelChangeSetInfo> callChangeSetProcessor(Supplier<OperationInvoker> executor,
			OnFailedChangeSetListener onFailedChange) {
		return changeSetInfo -> {
			for (ModelChangeSetOperation<Object> operation : resolveOperations(changeSetInfo, onFailedChange)) {
				if (executor.get().invoke(operation, changeSetInfo)) {
					// we are done, the change set was handled successfully. go process the next change
					return;
				}
			}
		};
	}

	private OperationInvoker createExecutor(Models models, OnSuccessfulChangeSetLister onSuccessfulChange,
			OnFailedChangeSetListener onFailedChange) {

		Consumer<ModelChangeSetInfo> modelChangeProcessor = createLazyRecursiveExecutor(models, onSuccessfulChange,
				onFailedChange);

		return (operation, changeSetInfo) -> {
			logBeginningOfOperationProcessing(operation, changeSetInfo);

			Object node = resolveNode(models, changeSetInfo, onFailedChange);
			ModelChangeSet changeSet = changeSetInfo.getChangeSet();
			// should we call all validations of all changes before applying the changes or should be one by one?
			if (operation.isAccepted(node)) {
				boolean isValueChanged = false;
				boolean forceChangeApply = false;
				try {
					isValueChanged = operation.validate(models, node, changeSet);
				} catch (ChangeSetValidationFailed e) {
					// we can recover only if there is onFailedChange listener and that listener decides that the
					// exception is not fatal and we can continue to apply the change
					forceChangeApply = notifyOrFail(e, changeSetInfo, onFailedChange);
				}

				if (isValueChanged || forceChangeApply) {
					// first apply the change, if there any intermediate changes apply them as well and then the
					// main change should be considered for completed and the model version increased
					Stream<ModelChangeSetInfo> subChanges = operation.applyChange(models, node, changeSet);
					subChanges.forEachOrdered(modelChangeProcessor);
					onSuccessfulChange.changeApplied(models, changeSetInfo);
				}
				return true;
			} else {
				logNoOperationForNodeFound(operation, changeSetInfo, node);
			}
			return false;
		};
	}

	private Consumer<ModelChangeSetInfo> createLazyRecursiveExecutor(Models models,
			OnSuccessfulChangeSetLister onSuccessfulChange, OnFailedChangeSetListener onFailedChange) {
		// predefining the executors not to create them for each processed change
		// the singleOpInvoker is a supplier otherwise we will have infinite recursion on the first line
		Supplier<OperationInvoker> singleOpInvoker = () -> createExecutor(models, onSuccessfulChange, onFailedChange);
		// cache the operation to limit the lambda instances as this is called for each new change
		CachingSupplier<OperationInvoker> cachedOpInvoker = new CachingSupplier<>(singleOpInvoker);
		// create a single consumer for all processed operations
		return callChangeSetProcessor(cachedOpInvoker, onFailedChange);
	}

	private Object resolveNode(Models models, ModelChangeSetInfo changeSetInfo,
			OnFailedChangeSetListener onFailedChange) {
		ModelChangeSet changeSet = changeSetInfo.getChangeSet();
		Object node = models.select(changeSet.getSelector());

		if (node == null && onFailedChange != null) {
			onFailedChange.changeFailed(
					new ChangeSetValidationFailed("Not supported node " + changeSet.getPath().prettyPrint()),
					changeSetInfo);
		}
		return node;
	}

	private List<ModelChangeSetOperation<Object>> resolveOperations(ModelChangeSetInfo changeSetInfo,
			OnFailedChangeSetListener onFailedChange) {
		String operationId = getOperationId(changeSetInfo, onFailedChange);
		List<ModelChangeSetOperation<Object>> operations = Collections.emptyList();
		if (operationId != null) {
			operations = operationMap.getOrDefault(operationId, Collections.emptyList());
			if (operations.isEmpty()) {
				notifyOrFail("Unknown operation " + operationId, changeSetInfo, onFailedChange);
			}
		}
		return operations;
	}

	private String getOperationId(ModelChangeSetInfo changeSetInfo, OnFailedChangeSetListener onFailedChange) {
		String operation = changeSetInfo.getChangeSet().getOperation();
		if (operation == null) {
			notifyOrFail("Missing operation identifier", changeSetInfo, onFailedChange);
		}
		return operation;
	}

	private void notifyOrFail(String message, ModelChangeSetInfo change, OnFailedChangeSetListener onFailedChange) {
		ChangeSetValidationFailed validationFailed = new ChangeSetValidationFailed(message);
		notifyOrFail(validationFailed, change, onFailedChange);
	}

	private boolean notifyOrFail(ChangeSetValidationFailed validationFailed, ModelChangeSetInfo change,
			OnFailedChangeSetListener onFailedChange) {
		if (onFailedChange != null) {
			return onFailedChange.changeFailed(validationFailed, change);
		}
		throw validationFailed;
	}

	@SuppressWarnings("unchecked")
	private void registerOperationHandler(String operationName, ModelChangeSetOperation operation) {
		operationMap.computeIfAbsent(operationName, k -> new CopyOnWriteArrayList<>()).add(operation);
	}

	private static void logNoOperationForNodeFound(ModelChangeSetOperation<Object> operation, ModelChangeSetInfo changeSetInfo,
			Object node) {
		String nodeType = node == null ? "null" : node.getClass().getName();
		LOGGER.trace("The operation handler {} rejected node with path {} and type {}",
				operation.getClass().getName(), changeSetInfo.getChangeSet().getSelector(), nodeType);
	}

	private static void logBeginningOfOperationProcessing(ModelChangeSetOperation<Object> operation,
			ModelChangeSetInfo changeSetInfo) {
		String intermediate = changeSetInfo.isIntermediate() ? "intermediate" : "";
		LOGGER.trace("Processing {} operation {} on node {} by {}", intermediate,
				changeSetInfo.getChangeSet().getOperation(), changeSetInfo.getChangeSet().getSelector(),
				operation.getClass().getName());
	}

	/**
	 * This is used to replace the use of {@code BiPredicate<ModelChangeSetOperation<Object>, ModelChangeSetInfo>}
	 *
	 * @author BBonev
	 */
	@FunctionalInterface
	private interface OperationInvoker {

		boolean invoke(ModelChangeSetOperation<Object> operation, ModelChangeSetInfo changeSetInfo);
	}
}
