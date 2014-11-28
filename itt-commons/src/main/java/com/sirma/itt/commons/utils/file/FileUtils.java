/**
 * Copyright (c) 2008 08.09.2008 , Sirma ITT.
/*

/**
 * 
 */
package com.sirma.itt.commons.utils.file;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * 
 * 
 * @author Hristo Iliev
 */
public final class FileUtils {

    /**
     * Hide default constructor.
     */
    private FileUtils() {
	// Hide default constructor
    }

    /**
     * Get contents of the file as string. The String is encoded with the
     * default system encoding.
     * 
     * @param fileName
     *            String, file to be read
     * @return String, contents of the file
     * @throws IOException
     *             thrown if I/O error while reading occur
     */
    public static String getFileAsString(String fileName) throws IOException {
	return getFileAsString(fileName, System.getProperty("file.encoding")); //$NON-NLS-1$
    }

    /**
     * Get contents of the file as string. The String is encoded with the
     * specified encoding.
     * 
     * @param file
     *            File to read
     * @param fileEncoding
     *            String, encoding of the file contents
     * @return String, contents of the file
     * @throws IOException
     *             thrown if I/O error while reading occur
     */
    public static String getFileAsString(File file, String fileEncoding)
	    throws IOException {
	FileInputStream input = null;
	try {
	    byte[] buffer = new byte[(int) file.length()];
	    input = new FileInputStream(file);
	    input.read(buffer);
	    return new String(buffer, fileEncoding);
	} finally {
	    if (input != null) {
		input.close();
	    }
	}
    }

    /**
     * Get contents of the file as string. The String is encoded with the
     * specified encoding.
     * 
     * @param fileName
     *            String, file to be read
     * @param fileEncoding
     *            String, encoding of the file contents
     * @return String, contents of the file
     * @throws IOException
     *             thrown if I/O error while reading occur
     */
    public static String getFileAsString(String fileName, String fileEncoding)
	    throws IOException {
	File file = new File(fileName);
	return getFileAsString(file, fileEncoding);
    }

    /**
     * Get contents of the file as string. The String is encoded in UTF-8.
     * 
     * @param file
     *            File to read.
     * @return String, contents of the file
     * @throws IOException
     *             thrown if I/O error while reading occur
     */
    public static String getFileAsUTF8String(File file) throws IOException {
	return getFileAsString(file, "UTF-8"); //$NON-NLS-1$
    }

    /**
     * Get contents of the file as string. The String is encoded in UTF-8.
     * 
     * @param fileName
     *            String, file to be read
     * @return String, contents of the file
     * @throws IOException
     *             thrown if I/O error while reading occur
     */
    public static String getFileAsUTF8String(String fileName)
	    throws IOException {
	return getFileAsString(fileName, "UTF-8"); //$NON-NLS-1$
    }

    /**
     * Get files from directory with specified mask.
     * 
     * @param directory
     *            String, directory to search
     * @param mask
     *            String, mask of files
     * @return File[], files in the directory
     */
    public static File[] getFilesFromMask(final String directory,
	    final String mask) {
	File dir = new File(directory);
	return dir.listFiles(new MaskFilter(mask));
    }

    /**
     * Get all files in the directory.
     * 
     * @param directory
     *            String, directory to search
     * @return File[], files in directory
     */
    public static File[] getFilesFromDirectory(final String directory) {
	File dir = new File(directory);
	return dir.listFiles();
    }

    /**
     * Get all files in directory and all subdirectories.
     * 
     * @param directory
     *            File, directory
     * @return List, list of all files
     */
    public static List<File> getFilesIncludeSubdirectories(final File directory) {
	List<File> result = new ArrayList<File>();
	getFilesIncludeSubdirectories(directory, result);
	return result;
    }

    /**
     * Get all files in directory and all subdirectories.
     * 
     * @param directory
     *            File, directory to search
     * @param list
     *            List, list to store found files
     */
    private static void getFilesIncludeSubdirectories(final File directory,
	    List<File> list) {
	File[] fileList = directory.listFiles();
	for (int i = 0; i < fileList.length; i++) {
	    list.add(fileList[i]);
	    if (fileList[i].isDirectory()) {
		getFilesIncludeSubdirectories(fileList[i], list);
	    }
	}
    }

    /**
     * Read a file as FileInputStream.
     * 
     * @param fileName
     *            the file name to read
     * @return the file as FileInputStream, or null if the file is not found.
     */
    public static FileInputStream readFileAsInputStream(String fileName) {
	try {
	    return new FileInputStream(fileName);
	} catch (FileNotFoundException e) {
	    return null;
	}
    }

    /**
     * Read a file as FileInputStream.
     * 
     * @param file
     *            the file to read
     * @return the file as FileInputStream, or null if the file is not found.
     */
    public static FileInputStream readFileAsInputStream(File file) {
	try {
	    return new FileInputStream(file);
	} catch (FileNotFoundException e) {
	    return null;
	}
    }
}
