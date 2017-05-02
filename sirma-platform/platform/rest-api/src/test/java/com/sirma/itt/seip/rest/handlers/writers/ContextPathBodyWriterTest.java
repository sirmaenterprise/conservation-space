package com.sirma.itt.seip.rest.handlers.writers;

import static com.sirma.itt.seip.domain.instance.DefaultProperties.HEADER_BREADCRUMB;

import java.io.ByteArrayOutputStream;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;

import org.junit.Test;

import com.sirma.itt.seip.domain.instance.ClassInstance;
import com.sirma.itt.seip.domain.instance.EmfInstance;
import com.sirma.itt.seip.io.ResourceLoadUtil;
import com.sirma.itt.seip.rest.resources.instances.ContextPath;

import net.javacrumbs.jsonunit.JsonAssert;

/**
 * Test for {@link ContextPathBodyWriter}
 *
 * @author BBonev
 */
public class ContextPathBodyWriterTest {

	@Test
	public void writeValidOutput() throws Exception {
		ContextPathBodyWriter writer = new ContextPathBodyWriter();
		EmfInstance parent = createInstance("emf:parent", "case");
		EmfInstance instance = createInstance("emf:instance", null);
		ContextPath path = new ContextPath(Arrays.asList(parent, instance));
		try (ByteArrayOutputStream entityStream = new ByteArrayOutputStream()) {
			writer.writeTo(path, null, null, null, null, null, entityStream);
			byte[] byteArray = entityStream.toByteArray();
			String json = new String(byteArray, StandardCharsets.UTF_8);
			JsonAssert.assertJsonEquals(ResourceLoadUtil.loadResource(getClass(), "context-path.json"), json);
		}
	}

	private static EmfInstance createInstance(String id, String type) {
		EmfInstance instance = new EmfInstance();
		instance.setId(id);
		ClassInstance classInstance = new ClassInstance();
		classInstance.setCategory(type);
		instance.setType(classInstance);
		instance.add(HEADER_BREADCRUMB, "The instance breadcrumb header");
		return instance;
	}
}
