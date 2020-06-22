package com.sirma.itt.seip.eai.content.tool.service.io;

import java.io.File;
import java.util.UUID;

import com.sirma.itt.seip.eai.content.tool.exception.EAIRuntimeException;
import com.sirma.itt.seip.eai.content.tool.params.ParametersProvider;

/**
 * Service class to work with file content on user machine.
 * 
 * @author bbanchev
 */
public class LocalFileService {
	/** Initialized {@link LocalFileService}. */
	public static final LocalFileService INSTANCE = new LocalFileService();
	static {
		init(null);
	}
	/** current working directory. */
	private File workingDirectory = null;
	private File tempDirectory = null;

	private LocalFileService() {
		// default empty constructor
	}

	/**
	 * Reinitialize the singleton {@link LocalFileService} with new root directory. By default is the current directory.
	 * 
	 * @param root
	 *            is the root to initialize {@link LocalFileService} with. All provided directories would be sub
	 *            directories of this
	 * @return the initialized {@link LocalFileService}
	 */
	public static LocalFileService init(File root) {
		INSTANCE.workingDirectory = createDirectory(root, ".sirma-eai-tool");
		INSTANCE.tempDirectory = createDirectory(root, ".temp");
		return INSTANCE;
	}

	/**
	 * Gets the working directory for the tool - sub directory with name <code>.sirma-eai-tool</code> of root
	 *
	 * @return the working directory
	 */
	public File getWorkingDirectory() {
		return workingDirectory;
	}

	/**
	 * Instance location directory
	 * 
	 * @return the initialize instance directory
	 */
	public File getInstanceDirectory() {
		String location = ParametersProvider.get(ParametersProvider.PARAM_CONTENT_URI);
		if (location == null) {
			throw new EAIRuntimeException("Instance id not set as runtime parameter.");
		}
		return createDirectory(getWorkingDirectory(), "." + location.replace(":", ""));
	}

	/**
	 * Creates a temporary file with {@link UUID} name
	 *
	 * @param suffix
	 *            the file suffix
	 * @return the created temporary file or throws {@link EAIRuntimeException} on failure
	 */
	public File createFile(String suffix) {
		return createFile(tempDirectory, UUID.randomUUID().toString(), suffix);
	}

	/**
	 * Creates the temporary file in the temporary directory
	 *
	 * @param prefix
	 *            the prefix for the file
	 * @param suffix
	 *            the file suffix
	 * @param directory
	 *            the directory to store under
	 * @return the created temporary file or throws {@link EAIRuntimeException} on failure
	 */
	public static File createFile(File directory, String prefix, String suffix) {
		File file = new File(directory, prefix + suffix);
		if (file.getParentFile() == null || !file.getParentFile().exists()) {
			file.getParentFile().mkdirs();
		}
		return file;
	}

	/**
	 * Creates a directory in the working directory
	 *
	 * @param dirName
	 *            the dir name
	 * @return the created file
	 */
	public File createDirectory(String dirName) {
		return createDirectory(workingDirectory, dirName);
	}

	/**
	 * Creates a directory in the provided directory
	 * 
	 * @param root
	 *            is the root storage dir
	 * @param dirName
	 *            the dir name
	 * @return the created file
	 */
	public static File createDirectory(File root, String dirName) {
		File dir = new File(root, dirName);
		if (!dir.exists()) {
			dir.mkdirs();
		}
		return dir;
	}

	/**
	 * Deletes a file and attempts delete on exit if initial delete fails.
	 * 
	 * @param file
	 *            is the file to delete
	 */
	public static void deleteFile(File file) {
		if (file != null && !file.delete()) {
			file.deleteOnExit();
		}
	}
}
