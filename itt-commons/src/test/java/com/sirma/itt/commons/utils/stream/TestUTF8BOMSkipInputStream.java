/**
 * Copyright (c) 2009 24.04.2009 , Sirma ITT.
/*

/**
 * 
 */
package com.sirma.itt.commons.utils.stream;

import static org.testng.Assert.assertEquals;

import java.io.BufferedInputStream;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import com.sirma.itt.commons.utils.collections.CollectionUtils;

/**
 * Test {@link UTF8BOMSkipInputStream} class.
 * 
 * @author Hristo Iliev
 */
public class TestUTF8BOMSkipInputStream {
    /**
     * Provide streams for testing. Provided stream doesn't support mark/reset
     * operation.
     * 
     * @return {@link Iterable}, the test input streams
     * @throws FileNotFoundException
     *             thrown if some of the input files cannot be found.
     */
    public Iterable<Object[]> provideTests() throws FileNotFoundException {
	List<Object[]> result = new ArrayList<Object[]>();

	result
		.add(new Object[] {
			"Test 1", //$NON-NLS-1$
			new UTF8BOMSkipInputStream(
				new FileInputStream(
					"testResources/com/sirma/itt/commons/utils/stream/TestUTF8BOMSkipInputStream/test01.txt")), //$NON-NLS-1$
			new FileInputStream(
				"testResources/com/sirma/itt/commons/utils/stream/TestUTF8BOMSkipInputStream/expected01.txt") }); //$NON-NLS-1$
	result
		.add(new Object[] {
			"Test 2", //$NON-NLS-1$
			new UTF8BOMSkipInputStream(
				new FileInputStream(
					"testResources/com/sirma/itt/commons/utils/stream/TestUTF8BOMSkipInputStream/test02.txt")), //$NON-NLS-1$
			new FileInputStream(
				"testResources/com/sirma/itt/commons/utils/stream/TestUTF8BOMSkipInputStream/expected02.txt") }); //$NON-NLS-1$
	result
		.add(new Object[] {
			"Test 3", //$NON-NLS-1$
			new UTF8BOMSkipInputStream(
				new FileInputStream(
					"testResources/com/sirma/itt/commons/utils/stream/TestUTF8BOMSkipInputStream/test03.txt")), //$NON-NLS-1$
			new FileInputStream(
				"testResources/com/sirma/itt/commons/utils/stream/TestUTF8BOMSkipInputStream/expected03.txt") }); //$NON-NLS-1$
	result
		.add(new Object[] {
			"Test 4", //$NON-NLS-1$
			new UTF8BOMSkipInputStream(
				new FileInputStream(
					"testResources/com/sirma/itt/commons/utils/stream/TestUTF8BOMSkipInputStream/test04.txt")), //$NON-NLS-1$
			new FileInputStream(
				"testResources/com/sirma/itt/commons/utils/stream/TestUTF8BOMSkipInputStream/expected04.txt") }); //$NON-NLS-1$
	result
		.add(new Object[] {
			"Test 5", //$NON-NLS-1$
			new UTF8BOMSkipInputStream(
				new FileInputStream(
					"testResources/com/sirma/itt/commons/utils/stream/TestUTF8BOMSkipInputStream/test05.txt")), //$NON-NLS-1$
			new FileInputStream(
				"testResources/com/sirma/itt/commons/utils/stream/TestUTF8BOMSkipInputStream/expected05.txt") }); //$NON-NLS-1$
	result
		.add(new Object[] {
			"Test 6", //$NON-NLS-1$
			new UTF8BOMSkipInputStream(
				new FileInputStream(
					"testResources/com/sirma/itt/commons/utils/stream/TestUTF8BOMSkipInputStream/test06.txt")), //$NON-NLS-1$
			new FileInputStream(
				"testResources/com/sirma/itt/commons/utils/stream/TestUTF8BOMSkipInputStream/expected06.txt") }); //$NON-NLS-1$
	result
		.add(new Object[] {
			"Test 7", //$NON-NLS-1$
			new UTF8BOMSkipInputStream(
				new FileInputStream(
					"testResources/com/sirma/itt/commons/utils/stream/TestUTF8BOMSkipInputStream/test07.txt")), //$NON-NLS-1$
			new FileInputStream(
				"testResources/com/sirma/itt/commons/utils/stream/TestUTF8BOMSkipInputStream/expected07.txt") }); //$NON-NLS-1$
	result
		.add(new Object[] {
			"Test 8", //$NON-NLS-1$
			new UTF8BOMSkipInputStream(
				new FileInputStream(
					"testResources/com/sirma/itt/commons/utils/stream/TestUTF8BOMSkipInputStream/test08.txt")), //$NON-NLS-1$
			new FileInputStream(
				"testResources/com/sirma/itt/commons/utils/stream/TestUTF8BOMSkipInputStream/expected08.txt") }); //$NON-NLS-1$
	result
		.add(new Object[] {
			"Test 9", //$NON-NLS-1$
			new UTF8BOMSkipInputStream(
				new FileInputStream(
					"testResources/com/sirma/itt/commons/utils/stream/TestUTF8BOMSkipInputStream/test09.txt")), //$NON-NLS-1$
			new FileInputStream(
				"testResources/com/sirma/itt/commons/utils/stream/TestUTF8BOMSkipInputStream/expected09.txt") }); //$NON-NLS-1$
	result
		.add(new Object[] {
			"Test 10", //$NON-NLS-1$
			new UTF8BOMSkipInputStream(
				new FileInputStream(
					"testResources/com/sirma/itt/commons/utils/stream/TestUTF8BOMSkipInputStream/test10.txt")), //$NON-NLS-1$
			new FileInputStream(
				"testResources/com/sirma/itt/commons/utils/stream/TestUTF8BOMSkipInputStream/expected10.txt") }); //$NON-NLS-1$

	return result;
    }

    /**
     * Provide streams for testing. Provided stream support mark/reset
     * operation.
     * 
     * @return {@link Iterable}, the test input streams
     * @throws FileNotFoundException
     *             thrown if some of the input files cannot be found.
     */
    public Iterable<Object[]> provideBufferedTests()
	    throws FileNotFoundException {
	List<Object[]> result = new ArrayList<Object[]>();

	result
		.add(new Object[] {
			"Test 1", //$NON-NLS-1$
			new UTF8BOMSkipInputStream(
				new BufferedInputStream(
					new FileInputStream(
						"testResources/com/sirma/itt/commons/utils/stream/TestUTF8BOMSkipInputStream/test01.txt"))), //$NON-NLS-1$
			new BufferedInputStream(
				new FileInputStream(
					"testResources/com/sirma/itt/commons/utils/stream/TestUTF8BOMSkipInputStream/expected01.txt")) }); //$NON-NLS-1$
	result
		.add(new Object[] {
			"Test 2", //$NON-NLS-1$
			new UTF8BOMSkipInputStream(
				new BufferedInputStream(
					new FileInputStream(
						"testResources/com/sirma/itt/commons/utils/stream/TestUTF8BOMSkipInputStream/test02.txt"))), //$NON-NLS-1$
			new BufferedInputStream(
				new FileInputStream(
					"testResources/com/sirma/itt/commons/utils/stream/TestUTF8BOMSkipInputStream/expected02.txt")) }); //$NON-NLS-1$
	result
		.add(new Object[] {
			"Test 3", //$NON-NLS-1$
			new UTF8BOMSkipInputStream(
				new BufferedInputStream(
					new FileInputStream(
						"testResources/com/sirma/itt/commons/utils/stream/TestUTF8BOMSkipInputStream/test03.txt"))), //$NON-NLS-1$
			new BufferedInputStream(
				new FileInputStream(
					"testResources/com/sirma/itt/commons/utils/stream/TestUTF8BOMSkipInputStream/expected03.txt")) }); //$NON-NLS-1$
	result
		.add(new Object[] {
			"Test 4", //$NON-NLS-1$
			new UTF8BOMSkipInputStream(
				new BufferedInputStream(
					new FileInputStream(
						"testResources/com/sirma/itt/commons/utils/stream/TestUTF8BOMSkipInputStream/test04.txt"))), //$NON-NLS-1$
			new BufferedInputStream(
				new FileInputStream(
					"testResources/com/sirma/itt/commons/utils/stream/TestUTF8BOMSkipInputStream/expected04.txt")) }); //$NON-NLS-1$
	result
		.add(new Object[] {
			"Test 5", //$NON-NLS-1$
			new UTF8BOMSkipInputStream(
				new BufferedInputStream(
					new FileInputStream(
						"testResources/com/sirma/itt/commons/utils/stream/TestUTF8BOMSkipInputStream/test05.txt"))), //$NON-NLS-1$
			new BufferedInputStream(
				new FileInputStream(
					"testResources/com/sirma/itt/commons/utils/stream/TestUTF8BOMSkipInputStream/expected05.txt")) }); //$NON-NLS-1$
	result
		.add(new Object[] {
			"Test 6", //$NON-NLS-1$
			new UTF8BOMSkipInputStream(
				new BufferedInputStream(
					new FileInputStream(
						"testResources/com/sirma/itt/commons/utils/stream/TestUTF8BOMSkipInputStream/test06.txt"))), //$NON-NLS-1$
			new BufferedInputStream(
				new FileInputStream(
					"testResources/com/sirma/itt/commons/utils/stream/TestUTF8BOMSkipInputStream/expected06.txt")) }); //$NON-NLS-1$
	result
		.add(new Object[] {
			"Test 7", //$NON-NLS-1$
			new UTF8BOMSkipInputStream(
				new BufferedInputStream(
					new FileInputStream(
						"testResources/com/sirma/itt/commons/utils/stream/TestUTF8BOMSkipInputStream/test07.txt"))), //$NON-NLS-1$
			new BufferedInputStream(
				new FileInputStream(
					"testResources/com/sirma/itt/commons/utils/stream/TestUTF8BOMSkipInputStream/expected07.txt")) }); //$NON-NLS-1$
	result
		.add(new Object[] {
			"Test 8", //$NON-NLS-1$
			new UTF8BOMSkipInputStream(
				new BufferedInputStream(
					new FileInputStream(
						"testResources/com/sirma/itt/commons/utils/stream/TestUTF8BOMSkipInputStream/test08.txt"))), //$NON-NLS-1$
			new BufferedInputStream(
				new FileInputStream(
					"testResources/com/sirma/itt/commons/utils/stream/TestUTF8BOMSkipInputStream/expected08.txt")) }); //$NON-NLS-1$
	result
		.add(new Object[] {
			"Test 9", //$NON-NLS-1$
			new UTF8BOMSkipInputStream(
				new BufferedInputStream(
					new FileInputStream(
						"testResources/com/sirma/itt/commons/utils/stream/TestUTF8BOMSkipInputStream/test09.txt"))), //$NON-NLS-1$
			new BufferedInputStream(
				new FileInputStream(
					"testResources/com/sirma/itt/commons/utils/stream/TestUTF8BOMSkipInputStream/expected09.txt")) }); //$NON-NLS-1$
	result
		.add(new Object[] {
			"Test 10", //$NON-NLS-1$
			new UTF8BOMSkipInputStream(
				new BufferedInputStream(
					new FileInputStream(
						"testResources/com/sirma/itt/commons/utils/stream/TestUTF8BOMSkipInputStream/test10.txt"))), //$NON-NLS-1$
			new BufferedInputStream(
				new FileInputStream(
					"testResources/com/sirma/itt/commons/utils/stream/TestUTF8BOMSkipInputStream/expected10.txt")) }); //$NON-NLS-1$

	return result;
    }

    /**
     * Provide test of {@link UTF8BOMSkipInputStream#read()} method.
     * 
     * @return {@link Iterator}, tests
     * @throws FileNotFoundException
     *             thrown if some of the test files cannot be found
     */
    @DataProvider(name = "providerUTF8BOMSkipInputStream_read")
    public Iterator<Object[]> providerUTF8BOMSkipInputStream_read()
	    throws FileNotFoundException {
	return provideTests().iterator();
    }

    /**
     * Provide test of {@link UTF8BOMSkipInputStream#read(byte[])} method.
     * 
     * @return {@link Iterator}, tests
     * @throws FileNotFoundException
     *             thrown if some of the test files cannot be found
     */
    @DataProvider(name = "providerUTF8BOMSkipInputStream_read_byteArray")
    public Iterator<Object[]> providerUTF8BOMSkipInputStream_read_byteArray()
	    throws FileNotFoundException {
	final List<Object[]> arraySizes = new LinkedList<Object[]>();
	arraySizes.add(new Object[] { Integer.valueOf(1) });
	arraySizes.add(new Object[] { Integer.valueOf(2) });
	arraySizes.add(new Object[] { Integer.valueOf(3) });
	arraySizes.add(new Object[] { Integer.valueOf(4) });
	arraySizes.add(new Object[] { Integer.valueOf(5) });
	arraySizes.add(new Object[] { Integer.valueOf(8) });
	arraySizes.add(new Object[] { Integer.valueOf(128) });
	return CollectionUtils.combineIterables(provideTests(), arraySizes)
		.iterator();
    }

    /**
     * Provide test of {@link UTF8BOMSkipInputStream#read(byte[], int, int)}
     * method.
     * 
     * @return {@link Iterator}, tests
     * @throws FileNotFoundException
     *             thrown if some of the test files cannot be found
     */
    @DataProvider(name = "providerUTF8BOMSkipInputStream_read_byteArray_Offset_Length")
    public Iterator<Object[]> providerUTF8BOMSkipInputStream_read_byteArray_Offset_Length()
	    throws FileNotFoundException {
	final List<Object[]> arraySizes = new LinkedList<Object[]>();
	arraySizes.add(new Object[] { Integer.valueOf(2) });
	arraySizes.add(new Object[] { Integer.valueOf(3) });
	arraySizes.add(new Object[] { Integer.valueOf(4) });
	arraySizes.add(new Object[] { Integer.valueOf(5) });
	arraySizes.add(new Object[] { Integer.valueOf(8) });
	arraySizes.add(new Object[] { Integer.valueOf(128) });
	return CollectionUtils.combineIterables(provideTests(), arraySizes)
		.iterator();
    }

    /**
     * Provide test of {@link UTF8BOMSkipInputStream#skip(long)} method.
     * 
     * @return {@link Iterator}, tests
     * @throws FileNotFoundException
     *             thrown if some of the test files cannot be found
     */
    @DataProvider(name = "providerUTF8BOMSkipInputStream_skip")
    public Iterator<Object[]> providerUTF8BOMSkipInputStream_skip()
	    throws FileNotFoundException {
	final List<Object[]> arraySizes = new LinkedList<Object[]>();
	arraySizes.add(new Object[] { Integer.valueOf(2) });
	arraySizes.add(new Object[] { Integer.valueOf(3) });
	arraySizes.add(new Object[] { Integer.valueOf(4) });
	arraySizes.add(new Object[] { Integer.valueOf(5) });
	arraySizes.add(new Object[] { Integer.valueOf(8) });
	arraySizes.add(new Object[] { Integer.valueOf(128) });
	final List<Object[]> skipSizes = new LinkedList<Object[]>();
	skipSizes.add(new Object[] { Integer.valueOf(1) });
	skipSizes.add(new Object[] { Integer.valueOf(2) });
	skipSizes.add(new Object[] { Integer.valueOf(3) });
	skipSizes.add(new Object[] { Integer.valueOf(4) });
	skipSizes.add(new Object[] { Integer.valueOf(5) });
	skipSizes.add(new Object[] { Integer.valueOf(8) });
	skipSizes.add(new Object[] { Integer.valueOf(128) });
	return CollectionUtils.combineIterables(provideTests(),
		CollectionUtils.combineIterables(arraySizes, skipSizes))
		.iterator();
    }

    /**
     * Provide test of {@link UTF8BOMSkipInputStream#mark(int)}/
     * {@link UTF8BOMSkipInputStream#skip(long)} methods.
     * 
     * @return {@link Iterator}, tests
     * @throws FileNotFoundException
     *             thrown if some of the test files cannot be found
     */
    @DataProvider(name = "providerUTF8BOMSkipInputStream_mark_reset")
    public Iterator<Object[]> providerUTF8BOMSkipInputStream_mark_reset()
	    throws FileNotFoundException {
	final List<Object[]> arraySizes = new LinkedList<Object[]>();
	arraySizes.add(new Object[] { Integer.valueOf(2), Integer.valueOf(2) });
	arraySizes.add(new Object[] { Integer.valueOf(2), Integer.valueOf(3) });
	arraySizes.add(new Object[] { Integer.valueOf(2), Integer.valueOf(4) });
	arraySizes.add(new Object[] { Integer.valueOf(2), Integer.valueOf(5) });
	arraySizes.add(new Object[] { Integer.valueOf(2), Integer.valueOf(8) });
	arraySizes
		.add(new Object[] { Integer.valueOf(2), Integer.valueOf(128) });
	arraySizes.add(new Object[] { Integer.valueOf(3), Integer.valueOf(3) });
	arraySizes.add(new Object[] { Integer.valueOf(3), Integer.valueOf(4) });
	arraySizes.add(new Object[] { Integer.valueOf(3), Integer.valueOf(5) });
	arraySizes.add(new Object[] { Integer.valueOf(3), Integer.valueOf(8) });
	arraySizes
		.add(new Object[] { Integer.valueOf(3), Integer.valueOf(128) });
	arraySizes.add(new Object[] { Integer.valueOf(4), Integer.valueOf(4) });
	arraySizes.add(new Object[] { Integer.valueOf(4), Integer.valueOf(5) });
	arraySizes.add(new Object[] { Integer.valueOf(4), Integer.valueOf(8) });
	arraySizes
		.add(new Object[] { Integer.valueOf(4), Integer.valueOf(128) });
	arraySizes.add(new Object[] { Integer.valueOf(5), Integer.valueOf(5) });
	arraySizes.add(new Object[] { Integer.valueOf(5), Integer.valueOf(8) });
	arraySizes
		.add(new Object[] { Integer.valueOf(5), Integer.valueOf(128) });
	arraySizes.add(new Object[] { Integer.valueOf(8), Integer.valueOf(8) });
	arraySizes
		.add(new Object[] { Integer.valueOf(8), Integer.valueOf(128) });
	arraySizes.add(new Object[] { Integer.valueOf(128),
		Integer.valueOf(128) });
	return CollectionUtils.combineIterables(provideBufferedTests(),
		arraySizes).iterator();
    }

    /**
     * Test the {@link UTF8BOMSkipInputStream#read()} method.
     * 
     * @param testName
     *            {@link String}, name of the method. Used for fancy debug
     *            information.
     * @param inputStream
     *            {@link InputStream}, {@link UTF8BOMSkipInputStream} wrapper
     * @param expectedStream
     *            {@link InputStream}, expected stream
     * @throws IOException
     *             thrown if there is an I/O error while testing the streams
     */
    @Test(dataProvider = "providerUTF8BOMSkipInputStream_read", groups = {
	    "BOMSkipInputStream", "UTF8BOMSkipInputStream" }, enabled = true)
    public void testUTF8BOMSkipInputStream_read(String testName,
	    InputStream inputStream, InputStream expectedStream)
	    throws IOException {
	int readedInputStream;
	do {
	    readedInputStream = inputStream.read();
	    assertEquals(readedInputStream, expectedStream.read(), testName);
	} while (readedInputStream != -1);
    }

    /**
     * Test the {@link UTF8BOMSkipInputStream#read(byte[])} method.
     * 
     * @param testName
     *            {@link String}, name of the method. Used for fancy debug
     *            information.
     * @param inputStream
     *            {@link InputStream}, {@link UTF8BOMSkipInputStream} wrapper
     * @param expectedStream
     *            {@link InputStream}, expected stream
     * @param arraySize
     *            {@link Integer}, length of the simultaneously read bytes
     * @throws IOException
     *             thrown if there is an I/O error while testing the streams
     */
    @Test(dataProvider = "providerUTF8BOMSkipInputStream_read_byteArray", groups = {
	    "BOMSkipInputStream", "UTF8BOMSkipInputStream" }, enabled = true)
    public void testUTF8BOMSkipInputStream_read_byteArray(String testName,
	    InputStream inputStream, InputStream expectedStream,
	    Integer arraySize) throws IOException {
	int readedInputStream;
	byte[] readedInputStreamByteArray = new byte[arraySize.intValue()];
	byte[] readedExpectedByteArray = new byte[arraySize.intValue()];
	String numberOfReadedBytesDifference = "Differences in number of readed bytes: " //$NON-NLS-1$
		+ testName;
	String readedBytesDifference = "Readed bytes differences: " + testName; //$NON-NLS-1$
	do {
	    readedInputStream = inputStream.read(readedInputStreamByteArray);
	    assertEquals(readedInputStream, expectedStream
		    .read(readedExpectedByteArray),
		    numberOfReadedBytesDifference);
	    assertEquals(readedInputStreamByteArray, readedExpectedByteArray,
		    readedBytesDifference);
	} while (readedInputStream != -1);
    }

    /**
     * Test the {@link UTF8BOMSkipInputStream#read(byte[], int, int)} method.
     * 
     * @param testName
     *            {@link String}, name of the method. Used for fancy debug
     *            information.
     * @param inputStream
     *            {@link InputStream}, {@link UTF8BOMSkipInputStream} wrapper
     * @param expectedStream
     *            {@link InputStream}, expected stream
     * @param arraySize
     *            {@link Integer}, length of the simultaneously read bytes
     * @throws IOException
     *             thrown if there is an I/O error while testing the streams
     */
    @Test(dataProvider = "providerUTF8BOMSkipInputStream_read_byteArray_Offset_Length", groups = {
	    "BOMSkipInputStream", "UTF8BOMSkipInputStream" }, enabled = true)
    public void testUTF8BOMSkipInputStream_read_byteArray_Offset_Length(
	    String testName, InputStream inputStream,
	    InputStream expectedStream, Integer arraySize) throws IOException {
	int readedInputStream;
	byte[] readedInputStreamByteArray = new byte[arraySize.intValue()];
	byte[] readedExpectedByteArray = new byte[arraySize.intValue()];
	String numberOfReadedBytesDifference = "Differences in number of readed bytes: " //$NON-NLS-1$
		+ testName;
	String readedBytesDifference = "Readed bytes differences: " + testName; //$NON-NLS-1$
	do {
	    readedInputStream = inputStream.read(readedInputStreamByteArray, 1,
		    arraySize.intValue() - 1);
	    assertEquals(readedInputStream, expectedStream.read(
		    readedExpectedByteArray, 1, arraySize.intValue() - 1),
		    numberOfReadedBytesDifference);
	    assertEquals(readedInputStreamByteArray, readedExpectedByteArray,
		    readedBytesDifference);
	} while (readedInputStream != -1);
    }

    /**
     * Test the {@link UTF8BOMSkipInputStream#skip(long)} method.
     * 
     * @param testName
     *            {@link String}, name of the method. Used for fancy debug
     *            information.
     * @param inputStream
     *            {@link InputStream}, {@link UTF8BOMSkipInputStream} wrapper
     * @param expectedStream
     *            {@link InputStream}, expected stream
     * @param arraySize
     *            {@link Integer}, length of the simultaneously read bytes
     * @param skipSize
     *            {@link Integer}, number of skipped bytes
     * @throws IOException
     *             thrown if there is an I/O error while testing the streams
     */
    @Test(dataProvider = "providerUTF8BOMSkipInputStream_skip", groups = {
	    "BOMSkipInputStream", "UTF8BOMSkipInputStream" }, enabled = true)
    public void testUTF8BOMSkipInputStream_skip(String testName,
	    InputStream inputStream, InputStream expectedStream,
	    Integer arraySize, Integer skipSize) throws IOException {
	int readedInputStream;
	byte[] readedInputStreamByteArray = new byte[arraySize.intValue()];
	byte[] readedExpectedByteArray = new byte[arraySize.intValue()];
	String numberOfSkippedBytesDifference = "Differences in number of readed bytes: " //$NON-NLS-1$
		+ testName;
	String numberOfReadedBytesDifference = "Differences in number of readed bytes: " //$NON-NLS-1$
		+ testName;
	String readedBytesDifference = "Readed bytes differences: " + testName; //$NON-NLS-1$
	do {
	    assertEquals(inputStream.skip(skipSize.intValue()), expectedStream
		    .skip(skipSize.intValue()), numberOfSkippedBytesDifference);
	    readedInputStream = inputStream.read(readedInputStreamByteArray, 1,
		    arraySize.intValue() - 1);
	    assertEquals(readedInputStream, expectedStream.read(
		    readedExpectedByteArray, 1, arraySize.intValue() - 1),
		    numberOfReadedBytesDifference);
	    assertEquals(readedInputStreamByteArray, readedExpectedByteArray,
		    readedBytesDifference);
	} while (readedInputStream != -1);
    }

    /**
     * Test the {@link UTF8BOMSkipInputStream#mark(int)}/
     * {@link UTF8BOMSkipInputStream#reset()} method.
     * 
     * @param testName
     *            {@link String}, name of the method. Used for fancy debug
     *            information.
     * @param inputStream
     *            {@link InputStream}, {@link UTF8BOMSkipInputStream} wrapper
     * @param expectedStream
     *            {@link InputStream}, expected stream
     * @param arraySize
     *            {@link Integer}, length of the simultaneously read bytes
     * @param markLimit
     *            {@link Integer}, limit of mark
     * @throws IOException
     *             thrown if there is an I/O error while testing the streams
     */
    @Test(dataProvider = "providerUTF8BOMSkipInputStream_mark_reset", groups = {
	    "BOMSkipInputStream", "UTF8BOMSkipInputStream" }, enabled = true)
    public void testUTF8BOMSkipInputStream_mark_reset(String testName,
	    InputStream inputStream, InputStream expectedStream,
	    Integer arraySize, Integer markLimit) throws IOException {
	int readedInputStream;
	int expectedInputStream;
	byte[] readedInputStreamByteArray = new byte[arraySize.intValue()];
	byte[] readedExpectedByteArray = new byte[arraySize.intValue()];
	String markPositionDifference = "Differences in mark position: " //$NON-NLS-1$
		+ testName;
	String numberOfReadedBytesDifference = "Differences in number of readed bytes: " //$NON-NLS-1$
		+ testName;
	String readedBytesDifference = "Readed bytes differences: " + testName; //$NON-NLS-1$
	do {
	    readedInputStream = inputStream.read();
	    expectedInputStream = expectedStream.read();
	    assertEquals(readedInputStream, expectedInputStream,
		    markPositionDifference);
	    inputStream.mark(markLimit.intValue());
	    expectedStream.mark(markLimit.intValue());
	    readedInputStream = inputStream.read(readedInputStreamByteArray, 1,
		    arraySize.intValue() - 1);
	    assertEquals(readedInputStream, expectedStream.read(
		    readedExpectedByteArray, 1, arraySize.intValue() - 1),
		    numberOfReadedBytesDifference);
	    assertEquals(readedInputStreamByteArray, readedExpectedByteArray,
		    readedBytesDifference);
	    inputStream.reset();
	    expectedStream.reset();
	} while (readedInputStream != -1);
    }
}
