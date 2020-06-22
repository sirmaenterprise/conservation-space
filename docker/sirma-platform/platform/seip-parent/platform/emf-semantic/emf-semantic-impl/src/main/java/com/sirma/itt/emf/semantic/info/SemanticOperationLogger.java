package com.sirma.itt.emf.semantic.info;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.function.Consumer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sirma.itt.seip.DestroyObservable;
import com.sirma.itt.seip.Destroyable;
import com.sirma.itt.seip.collections.CollectionUtils;
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
	private static Contextual<OperationState> operationStore;
	private static ExecutorService executorService = Executors.newSingleThreadExecutor(r -> {
		Thread thread = new Thread(r);
		thread.setName("SemanticLogDumper");
		thread.setDaemon(true);
		return thread;
	});

	public static final String TUPLE_QUERY_OPERATION = "TQ";
	public static final String BOOLEAN_QUERY_OPERATION = "BQ";
	public static final String GRAPH_QUERY_OPERATION = "GQ";
	public static final String UPDATE_QUERY_OPERATION = "UQ";
	public static final String COMMIT_OPERATION = "C";
	public static final String ROLLBACK_OPERATION = "R";
	public static final String BEGIN_OPERATION = "B";
	public static final String FAIL_OPERATION = "F";

	/**
	 * Initialize.
	 *
	 * @param operationStore
	 *            the operation store
	 */
	@Startup(phase = StartupPhase.DEPLOYMENT, order = 0)
	static void initialize(Contextual<OperationState> operationStore) {
		SemanticOperationLogger.operationStore = operationStore;
		operationStore.initializeWith(OperationState::new);
		DestroyObservable.addObserver(operationStore, (Consumer<OperationState>) OperationState::saveLog);
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
		operationStore.getContextValue().addLogOperation(operation, value, bindings);
	}

	/**
	 * Saves the log to a file
	 */
	public static void saveLog() {
		operationStore.getContextValue().saveLog();
	}

	/**
	 * Getter method for operationList.
	 *
	 * @return the operationList
	 */
	public static List<Object[]> getOperationList() {
		return operationStore.getContextValue().getOperationList();
	}

	/**
	 * Getter method for tempDirectory.
	 *
	 * @return the tempDirectory
	 */
	public static String getTempDirectory() {
		return operationStore.getContextValue().getTempDirectory();
	}

	/**
	 * Setter method for tempDirectory.
	 *
	 * @param directoryName
	 *            the path to the temporary directory
	 */
	public static void setTempDirectory(String directoryName) {
		operationStore.getContextValue().setTempDirectory(directoryName);
	}

	/**
	 * Getter method for isEnabled.
	 *
	 * @return the isEnabled
	 */
	public static boolean isEnabled() {
		return operationStore.getContextValue().isEnabled();
	}

	/**
	 * Setter method for isEnabled.
	 *
	 * @param isEnabled
	 *            the isEnabled to set
	 */
	public static void setIsEnabled(boolean isEnabled) {
		operationStore.getContextValue().setIsEnabled(isEnabled);
	}

	/**
	 * Saves and clears all contexts
	 */
	public static void shutdown() {
		if (!isEnabled()) {
			return;
		}
		operationStore.getContextValue().shutdown();
		Destroyable.destroy(operationStore);
	}

	/**
	 * @return the flushCount
	 */
	public static int getFlushCount() {
		return operationStore.getContextValue().getFlushCount();
	}

	/**
	 * @param flushCount the flushCount to set
	 */
	public static void setFlushCount(int flushCount) {
		operationStore.getContextValue().setFlushCount(flushCount);
	}

	/**
	 * @return the context
	 */
	public static String getContext() {
		return operationStore.getContextValue().getContext();
	}

	/**
	 * @param context the context to set
	 */
	public static void setContext(String context) {
		operationStore.getContextValue().setContext(context);
	}


	private static class OperationState {
		private String outputDir;
		private List<Object[]> operationList = new LinkedList<>();
		private boolean isEnabled;
		private Lock lock = new ReentrantLock();
		private int flushCount = 1000;
		private String context;

		void addLogOperation(String operation, Object value, Object bindings) {
			if (!isEnabled()) {
				return;
			}
			Object[] operationArray = new Object[4];
			operationArray[0] = operation;
			operationArray[1] = value;
			operationArray[2] = bindings;
			operationArray[3] = System.currentTimeMillis();

			lock.lock();
			try {
				operationList.add(operationArray);

				if (operationList.size() >= flushCount) {
					saveLog();
				}
			} finally {
				lock.unlock();
			}
		}

		void saveLog() {
			if (!isEnabled()) {
				return;
			}

			LinkedList<Object[]> stateCopy;
			lock.lock();
			try {
				// copy store contents so it could be unlocked
				stateCopy = new LinkedList<>(operationList);
				// trigger GC on the list contents
				operationList.clear();
				operationList = new LinkedList<>();
			} finally {
				lock.unlock();
			}
			final String output = outputDir;
			final String ctx = context;
			executorService.execute(() -> saveLog(output, ctx, stateCopy));
		}

		private static void saveLog(String outputDir, String context, LinkedList<Object[]> ops) {
			if (ops == null || CollectionUtils.isEmpty(ops)) {
				return;
			}

			try (ObjectOutputStream output = new ObjectOutputStream(new FileOutputStream(
					outputDir + createDumpFileName(context, ops)))) {
				output.writeObject(ops);
				output.flush();
			} catch (IOException e) {
				LOGGER.error("Failed while saving the log.", e);
			}
		}

		private static String createDumpFileName(String context, LinkedList<Object[]> ops) {
			return "semantic-debug-" + context + "-" + System.currentTimeMillis() + "-" + ops.size() + ".dump";
		}

		List<Object[]> getOperationList() {
			return operationList;
		}

		String getTempDirectory() {
			return outputDir;
		}

		void setTempDirectory(String directoryName) {
			String tempDir = directoryName;
			if (!tempDir.endsWith(File.separator)) {
				tempDir = tempDir + File.separator;
			}
			outputDir = tempDir;
		}

		boolean isEnabled() {
			return isEnabled;
		}

		void setIsEnabled(boolean isEnabled) {
			this.isEnabled = isEnabled;
		}

		void shutdown() {
			if (!isEnabled()) {
				return;
			}
			operationList.clear();
		}

		int getFlushCount() {
			return flushCount;
		}

		void setFlushCount(int flushCount) {
			this.flushCount = flushCount;
		}

		String getContext() {
			return context;
		}

		void setContext(String context) {
			this.context = context;
		}
	}
}
