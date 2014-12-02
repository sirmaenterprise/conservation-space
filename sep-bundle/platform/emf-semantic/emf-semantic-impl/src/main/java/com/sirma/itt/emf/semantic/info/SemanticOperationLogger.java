package com.sirma.itt.emf.semantic.info;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.util.LinkedList;

/**
 * Operation logger of the operations that are executed on the semantic repository. The operations
 * that are logged are: Add, Delete and Query </p> The operation logger is activated trough the
 * configuration parameter SemanticConfigurationProperties.SEMANTIC_OPERATION_DEBUG_LOG_ENABLED
 *
 * @author kirq4e
 */
public class SemanticOperationLogger {

	private static LinkedList<Object[]> operationList = new LinkedList<>();
	private static String tempDirectory;
	private static Boolean isEnabled = false;

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

		synchronized (operationList) {
			operationList.add(operationArray);

			if (operationList.size() >= 10000) {
				saveLog();
			}
		}
	}

	/**
	 * Saves the log to a file
	 */
	public static void saveLog() {

		if (!isEnabled) {
			return;
		}
		try {
			FileOutputStream out = new FileOutputStream(tempDirectory + "modelList"
					+ System.currentTimeMillis() + "-" + operationList.size() + ".dump");
			ObjectOutputStream output = new ObjectOutputStream(out);

			synchronized (operationList) {

				output.writeObject(operationList);
				output.flush();
				output.close();

				operationList = new LinkedList<Object[]>();
			}

		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	/**
	 * Getter method for operationList.
	 *
	 * @return the operationList
	 */
	public static LinkedList<Object[]> getOperationList() {
		return operationList;
	}

	/**
	 * Setter method for operationList.
	 *
	 * @param operationList
	 *            the operationList to set
	 */
	public static void setOperationList(LinkedList<Object[]> operationList) {
		SemanticOperationLogger.operationList = operationList;
	}

	/**
	 * Getter method for tempDirectory.
	 *
	 * @return the tempDirectory
	 */
	public static String getTempDirectory() {
		return tempDirectory;
	}

	/**
	 * Setter method for tempDirectory.
	 *
	 * @param tempDirectory
	 *            the tempDirectory to set
	 */
	public static void setTempDirectory(String tempDirectory) {
		if (!tempDirectory.endsWith(File.separator)) {
			tempDirectory = tempDirectory + File.separator;
		}
		SemanticOperationLogger.tempDirectory = tempDirectory;
	}

	/**
	 * Getter method for isEnabled.
	 *
	 * @return the isEnabled
	 */
	public static Boolean getIsEnabled() {
		return isEnabled;
	}

	/**
	 * Setter method for isEnabled.
	 *
	 * @param isEnabled
	 *            the isEnabled to set
	 */
	public static void setIsEnabled(Boolean isEnabled) {
		SemanticOperationLogger.isEnabled = isEnabled;
	}

}
