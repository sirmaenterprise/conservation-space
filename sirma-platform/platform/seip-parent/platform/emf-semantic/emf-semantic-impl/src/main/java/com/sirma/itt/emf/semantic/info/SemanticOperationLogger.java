package com.sirma.itt.emf.semantic.info;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.LinkedList;
import java.util.List;
import java.util.function.Consumer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sirma.itt.seip.DestroyObservable;
import com.sirma.itt.seip.Destroyable;
import com.sirma.itt.seip.Pair;
import com.sirma.itt.seip.collections.CollectionUtils;
import com.sirma.itt.seip.concurrent.locks.ContextualLock;
import com.sirma.itt.seip.context.Contextual;
import com.sirma.itt.seip.runtime.boot.Startup;
import com.sirma.itt.seip.runtime.boot.StartupPhase;

/**
 * Operation logger of the operations that are executed on the semantic repository. The operations that are logged are:
 * Add, Delete and Query
 * </p>
 * The operation logger is activated trough the configuration parameter
 * SemanticConfigurationProperties.SEMANTIC_OPERATION_DEBUG_LOG_ENABLED
 *
 * @author kirq4e
 */
// the class has configuration injections
@SuppressWarnings("squid:S1118")
public class SemanticOperationLogger {

	private static final Logger LOGGER = LoggerFactory.getLogger(SemanticOperationLogger.class);
	/**
	 * Stores the temporary directory where the data is saved and all the current operations
	 */
	private static Contextual<Pair<String, List<Object[]>>> operationList;
	private static boolean isEnabled = false;
	private static ContextualLock lock;
	private static int flushCount;

	public static final String TUPLE_QUERY_OPERATION = "TQ";
	public static final String UPDATE_QUERY_OPERATION = "UQ";
	public static final String COMMIT_OPERATION = "C";
	public static final String BEGIN_OPERATION = "B";

	/**
	 * Initialize.
	 *
	 * @param operationStore
	 *            the operation store
	 * @param contextLock
	 *            the context lock
	 */
	@Startup(phase = StartupPhase.DEPLOYMENT, order = 0)
	static void initialize(Contextual<Pair<String, List<Object[]>>> operationStore, ContextualLock contextLock) {
		operationList = operationStore;
		operationList.initializeWith(() -> new Pair<>(null, new LinkedList<>()));
		DestroyObservable.addObserver(operationList,
				(Consumer<Pair<String, List<Object[]>>>) SemanticOperationLogger::saveLog);

		SemanticOperationLogger.lock = contextLock;
	}

	/**
	 * Adds log operation to internal cache
	 *
	 * @param operation
	 *            Operation type - A = add, D = delete, Q = query
	 * @param value
	 *            Value of the operation - query string or Model with statements
	 * @param bindings
	 *            Map with bindings if the operation is Query
	 */
	public static void addLogOperation(String operation, Object value, Object bindings) {
		if (!isEnabled) {
			return;
		}
		Object[] operationArray = new Object[3];
		operationArray[0] = operation;
		operationArray[1] = value;
		operationArray[2] = bindings;

		lock.lock();
		try {
			List<Object[]> ops = operationList.getContextValue().getSecond();
			ops.add(operationArray);

			if (ops.size() >= flushCount) {
				saveLog();
			}
		} finally {
			lock.unlock();
		}
	}

	/**
	 * Saves the log to a file
	 */
	public static void saveLog() {
		if (!isEnabled) {
			return;
		}

		lock.lock();
		try {
			// do not clear context value, because it will clear the temporary folder name
			Pair<String,List<Object[]>> contextValue = operationList.getContextValue();
			saveLog(contextValue);
			if (contextValue != null) {
				contextValue.setSecond(new LinkedList<>());
			}
		} finally {
			lock.unlock();
		}
	}

	/**
	 * Saves the log to a file
	 */
	private static void saveLog(Pair<String, List<Object[]>> op) {
		if (!isEnabled || op == null || CollectionUtils.isEmpty(op.getSecond())) {
			return;
		}

		List<Object[]> ops = op.getSecond();
		try (ObjectOutputStream output = new ObjectOutputStream(new FileOutputStream(
				op.getFirst() + "modelList" + System.currentTimeMillis() + "-" + ops.size() + ".dump"))) {

			Serializable serialized = new LinkedList<>(ops);
			output.writeObject(serialized);
			output.flush();

			ops.clear();
		} catch (IOException e) {
			LOGGER.error("Failed while saving the log.", e);
		}
	}

	/**
	 * Getter method for operationList.
	 *
	 * @return the operationList
	 */
	public static List<Object[]> getOperationList() {
		return operationList.getContextValue().getSecond();
	}

	/**
	 * Getter method for tempDirectory.
	 *
	 * @return the tempDirectory
	 */
	public static String getTempDirectory() {
		return operationList.getContextValue().getFirst();
	}

	/**
	 * Setter method for tempDirectory.
	 *
	 * @param directoryName
	 *            the path to the temporary directory
	 */
	public static void setTempDirectory(String directoryName) {
		String tempDir = directoryName;
		if (!tempDir.endsWith(File.separator)) {
			tempDir = tempDir + File.separator;
		}
		SemanticOperationLogger.operationList.getContextValue().setFirst(tempDir);
	}

	/**
	 * Getter method for isEnabled.
	 *
	 * @return the isEnabled
	 */
	public static boolean isEnabled() {
		return isEnabled;
	}

	/**
	 * Setter method for isEnabled.
	 *
	 * @param isEnabled
	 *            the isEnabled to set
	 */
	public static void setIsEnabled(boolean isEnabled) {
		SemanticOperationLogger.isEnabled = isEnabled;
	}

	/**
	 * Saves and clears all contexts
	 */
	public static void shutdown() {
		if (!isEnabled) {
			return;
		}
		Destroyable.destroy(operationList);
	}

	/**
	 * @return the flushCount
	 */
	public static int getFlushCount() {
		return flushCount;
	}

	/**
	 * @param flushCount the flushCount to set
	 */
	public static void setFlushCount(int flushCount) {
		SemanticOperationLogger.flushCount = flushCount;
	}

}
