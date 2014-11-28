/**
 * Copyright (c) 2008 09.09.2008 , Sirma ITT.
/*

/**
 * 
 */
package com.sirma.itt.commons.utils.file;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import org.testng.Assert;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import com.sirma.itt.commons.utils.array.ArraysUtils;

/**
 * 
 * 
 * @author Hristo Iliev
 */
public class TestFileUtils {

	/**
	 * Provider for tests.
	 * 
	 * @return Objects[][], tests
	 */
	@DataProvider(name = "getFilesInDirectories - provider")
	public Object[][] providerGetFilesInDirectories() {
		List<Object[]> result = new ArrayList<Object[]>();
		result.add(new Object[] { "src/main/java/" }); //$NON-NLS-1$
		return ArraysUtils.listOfArraysToDoubleArray(result, Object.class);
	}

	/**
	 * Files to be searched from mask.
	 * 
	 * @param directory
	 *            String, directory where files are stored
	 */
	@Test(dataProvider = "getFilesInDirectories - provider")
	public void testGetFilesInDirectories(String directory) {
		List<File> files = FileUtils.getFilesIncludeSubdirectories(new File(
				directory));
		for (File file : files) {
			System.out.println(file.getAbsolutePath());
		}
	}
}
