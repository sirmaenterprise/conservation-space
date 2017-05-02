package com.sirma.itt.seip.permissions.rest;

import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.when;

import java.io.ByteArrayOutputStream;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Collection;

import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import com.sirma.itt.seip.domain.definition.label.LabelProvider;
import com.sirma.itt.seip.permissions.model.RoleId;
import com.sirma.itt.seip.permissions.role.RoleIdentifier;

import net.javacrumbs.jsonunit.JsonAssert;

/**
 * Test of {@link RoleIdentifierCollectionWriter}
 *
 * @author BBonev
 */
public class RoleIdentifierCollectionWriterTest {

	@InjectMocks
	private RoleIdentifierCollectionWriter writer;

	@Mock
	private LabelProvider labelProvider;

	@Before
	public void beforeMethod() {
		MockitoAnnotations.initMocks(this);
	}

	@Test
	public void testWrite() throws Exception {
		when(labelProvider.getLabel(anyString()))
				.then(answer -> answer.getArgumentAt(0, String.class).toLowerCase());

		Collection<RoleIdentifier> roles = Arrays.asList(new RoleId("CONSUMER", 0), new RoleId("MANAGER", 0));
		ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
		writer.writeTo(roles, null, null, null, null, null, outputStream);

		String output = new String(outputStream.toByteArray(), StandardCharsets.UTF_8);

		JsonAssert.assertJsonEquals(
				"[{ \"value\":\"CONSUMER\",\"label\":\"consumer.label\" }, {\"value\":\"MANAGER\",\"label\":\"manager.label\" } ]",
				output);
	}

}
