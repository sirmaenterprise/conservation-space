/**
 * Copyright (c) 2009 27.04.2009 , Sirma ITT.
/*

/**
 * 
 */
package com.sirma.itt.commons.utils.encoding;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.testng.Assert;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import com.sirma.itt.commons.utils.file.FileUtils;

/**
 * 
 * 
 * @author Hristo Iliev
 */
public class TestUTF8Utils {

    /**
     * Provide tests for testing {@link UTF8Utils#startWithBOM(byte[])} and
     * {@link UTF8Utils#startWithBOM(String)} methods.
     * 
     * @return {@link Iterator}, tests
     * @throws IOException
     *             thrown if there is an I/O Exception while accessing the input
     *             data
     */
    @DataProvider(name = "providerUTF8Utils_startWithBOM")
    public Iterator<Object[]> providerUTF8Utils_startWithBOM()
	    throws IOException {
	List<Object[]> result = new ArrayList<Object[]>();

	result
		.add(new Object[] {
			FileUtils
				.getFileAsUTF8String("testResources/com/sirma/itt/commons/utils/encoding/TestUTF8Utils/test01.txt"), //$NON-NLS-1$
			Boolean.TRUE });
	result
		.add(new Object[] {
			FileUtils
				.getFileAsUTF8String("testResources/com/sirma/itt/commons/utils/encoding/TestUTF8Utils/test02.txt"), //$NON-NLS-1$
			Boolean.FALSE });
	result
		.add(new Object[] {
			FileUtils
				.getFileAsUTF8String("testResources/com/sirma/itt/commons/utils/encoding/TestUTF8Utils/test03.txt"), //$NON-NLS-1$
			Boolean.FALSE });
	result
		.add(new Object[] {
			FileUtils
				.getFileAsUTF8String("testResources/com/sirma/itt/commons/utils/encoding/TestUTF8Utils/test04.txt"), //$NON-NLS-1$
			Boolean.FALSE });
	result
		.add(new Object[] {
			FileUtils
				.getFileAsUTF8String("testResources/com/sirma/itt/commons/utils/encoding/TestUTF8Utils/test05.txt"), //$NON-NLS-1$
			Boolean.FALSE });
	result
		.add(new Object[] {
			FileUtils
				.getFileAsUTF8String("testResources/com/sirma/itt/commons/utils/encoding/TestUTF8Utils/test06.txt"), //$NON-NLS-1$
			Boolean.FALSE });
	result
		.add(new Object[] {
			FileUtils
				.getFileAsUTF8String("testResources/com/sirma/itt/commons/utils/encoding/TestUTF8Utils/test07.txt"), //$NON-NLS-1$
			Boolean.FALSE });
	result
		.add(new Object[] {
			FileUtils
				.getFileAsUTF8String("testResources/com/sirma/itt/commons/utils/encoding/TestUTF8Utils/test08.txt"), //$NON-NLS-1$
			Boolean.FALSE });
	result
		.add(new Object[] {
			FileUtils
				.getFileAsUTF8String("testResources/com/sirma/itt/commons/utils/encoding/TestUTF8Utils/test09.txt"), //$NON-NLS-1$
			Boolean.TRUE });
	result
		.add(new Object[] {
			FileUtils
				.getFileAsUTF8String("testResources/com/sirma/itt/commons/utils/encoding/TestUTF8Utils/test10.txt"), //$NON-NLS-1$
			Boolean.TRUE });

	return result.iterator();
    }

    /**
     * Test if the string starts with BOM.
     * 
     * @param testString
     *            {@link String}, checked string
     * @param expected
     *            {@link Boolean}, <code>true</code> if the string contains BOM
     *            or not
     * @throws UnsupportedEncodingException
     *             never thrown
     */
    @Test(dataProvider = "providerUTF8Utils_startWithBOM", groups = { "UTF8Utils" })
    public void testUTF8Utils_startWithBOM(String testString, Boolean expected)
	    throws UnsupportedEncodingException {
	Assert.assertEquals(UTF8Utils.startWithBOM(testString), expected
		.booleanValue());
	Assert.assertEquals(UTF8Utils
		.startWithBOM(testString.getBytes("UTF-8")), expected //$NON-NLS-1$
		.booleanValue());
    }
}
