package com.sirma.itt.seip.instance.version;

import static org.junit.Assert.assertEquals;

import java.util.NoSuchElementException;

import org.junit.Test;

/**
 * Test for {@link VersionMode}.
 *
 * @author A. Kunchev
 */
@SuppressWarnings("static-method")
public class VersionModeTest {

	@Test(expected = NoSuchElementException.class)
	public void getMode_shouldThrowExceptionIfNoMatch() {
		VersionMode.getMode("Batman");
	}

	@Test
	public void getMode_shouldReturnMatchingMode() {
		VersionMode result = VersionMode.getMode("update");
		assertEquals(VersionMode.UPDATE, result);
	}

	@Test
	public void getModeWithDefaultValue_shouldReturnMatchingMode() {
		VersionMode result = VersionMode.getMode("minor");
		assertEquals(VersionMode.MINOR, result);
	}

	@Test
	public void getModeWithDefaultValue_shouldReturnDefaultValueIfNoMatch() {
		VersionMode result = VersionMode.getMode("Joker", VersionMode.MAJOR);
		assertEquals(VersionMode.MAJOR, result);
	}
}