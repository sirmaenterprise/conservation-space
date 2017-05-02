package com.sirma.itt.seip.rest.security;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.util.Optional;

import org.junit.Before;
import org.junit.Test;

import com.sirma.itt.seip.rest.secirity.SecurityTokensHolder;

/**
 * Tests for {@link SecurityTokensHolder}.
 *
 * @author smustafov
 */
public class SecurityTokensHolderTest {

	private SecurityTokensHolder holder;

	@Before
	public void setup() {
		holder = new SecurityTokensHolder();
	}

	@Test
	public void testGetSamlToken_notExistingKey() {
		holder.addToken("otherToken", "samlToken", "index");
		assertNull(holder.getSamlToken("token"));
	}

	@Test
	public void testGetJwtTokenBySaml() {
		holder.addToken("otherToken", "samlToken", "index");
		Optional<String> token = holder.getJwtToken("samlToken");
		assertNotNull(token);
		assertTrue(token.isPresent());
		assertEquals("otherToken", token.get());
	}

	@Test
	public void testGetSamlToken_existingKey() {
		holder.addToken("token", "samlToken", "index");
		assertEquals("samlToken", holder.getSamlToken("token"));
	}

	@Test
	public void testGetSessionIndex_notExistingKey() {
		holder.addToken("token1", "samlToken", "index");
		assertNull(holder.getSessionIndex("token"));
	}

	@Test
	public void testGetSessionIndex_existingKey() {
		holder.addToken("token", "samlToken", "index");
		assertEquals("index", holder.getSessionIndex("token"));
	}

	@Test
	public void testRemoveBySamlToken_nullKey() {
		holder.addToken("token", "samlToken", "index");
		holder.removeBySamlToken(null);

		assertEquals("samlToken", holder.getSamlToken("token"));
	}

	@Test
	public void testRemoveBySamlToken_notExistingKey() {
		holder.addToken("token", "samlToken", "index");
		holder.removeBySamlToken("samlToken1");

		assertEquals("samlToken", holder.getSamlToken("token"));
	}

	@Test
	public void testRemoveBySamlToken_existingKey() {
		holder.addToken("token", "samlToken", "index");
		holder.removeBySamlToken("samlToken");

		assertNull(holder.getSamlToken("token"));
	}

}
