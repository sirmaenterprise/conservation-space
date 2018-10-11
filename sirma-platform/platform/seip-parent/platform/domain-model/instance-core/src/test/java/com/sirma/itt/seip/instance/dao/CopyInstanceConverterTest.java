package com.sirma.itt.seip.instance.dao;

import static org.junit.Assert.*;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.MockitoAnnotations;
import org.mockito.Spy;

import com.sirma.itt.seip.Entity;
import com.sirma.itt.seip.domain.instance.Instance;
import com.sirma.itt.seip.instance.ObjectInstance;
import com.sirma.itt.seip.instance.properties.SemanticNonPersistentPropertiesExtension;
import com.sirma.itt.seip.serialization.SerializationHelper;
import com.sirma.itt.seip.serialization.kryo.KryoHelper;

/**
 * Test for {@link CopyInstanceConverter}
 *
 * @author <a href="mailto:borislav.bonev@sirma.bg">Borislav Bonev</a>
 * @since 25/07/2018
 */
public class CopyInstanceConverterTest {
	@InjectMocks
	private CopyInstanceConverter converter;

	@Spy
	private List<SemanticNonPersistentPropertiesExtension> nonPersistentPropertiesExtension = new ArrayList<>();
	@Spy
	private SerializationHelper serializationHelper = new SerializationHelper(new KryoHelper());

	@Before
	public void setUp() throws Exception {
		MockitoAnnotations.initMocks(this);

		nonPersistentPropertiesExtension.clear();
		nonPersistentPropertiesExtension.add(() -> Collections.singleton("nonPersistentProperty"));
	}

	@Test
	public void convertToEntity_shouldRemoveTracking() throws Exception {
		ObjectInstance instance = new ObjectInstance();
		instance.setId("emf:instance-id");
		instance.enableChangesTracking();
		instance.add("someProperty", "someValue");

		Entity<Serializable> entity = converter.convertToEntity(instance);
		assertTrue(entity instanceof ObjectInstance);
		assertFalse(((ObjectInstance) entity).isTracked());
	}

	@Test
	public void convertToEntity_shouldRemoveNonPersistableProperties() throws Exception {
		ObjectInstance instance = new ObjectInstance();
		instance.setId("emf:instance-id");
		instance.add("someProperty", "someValue");
		instance.add("nonPersistentProperty", "nonPersistentValue");

		Entity<Serializable> entity = converter.convertToEntity(instance);
		assertTrue(entity instanceof ObjectInstance);
		assertFalse(((ObjectInstance) entity).isPropertyPresent("nonPersistentProperty"));
	}

	@Test
	public void convertToInstance_shouldRemoveTracking() throws Exception {
		ObjectInstance instance = new ObjectInstance();
		instance.setId("emf:instance-id");
		instance.enableChangesTracking();
		instance.add("someProperty", "someValue");

		Instance converted = converter.convertToInstance(instance);
		assertTrue(converted instanceof ObjectInstance);
		assertFalse(((ObjectInstance) converted).isTracked());
		assertTrue(converted.isPropertyPresent("someProperty"));
	}

}
