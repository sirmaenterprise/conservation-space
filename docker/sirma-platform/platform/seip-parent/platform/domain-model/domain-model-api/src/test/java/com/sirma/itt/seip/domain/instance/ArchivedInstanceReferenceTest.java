package com.sirma.itt.seip.domain.instance;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import com.sirma.itt.seip.convert.TypeConverter;
import com.sirma.itt.seip.convert.TypeConverterUtil;
import com.sirma.itt.seip.domain.definition.DataTypeDefinition;

/**
 * Unit Tests for the class {@link ArchivedInstanceReference}.
 * <p>
 * Created by Ivo Rusev on 22.11.2016
 */
public class ArchivedInstanceReferenceTest {

	private final String actual = "sourceId";

	@Mock
	private TypeConverter typeConverter;

	@Mock
	private DataTypeDefinition def;

	@Before
	public void beforeMethod() {
		// Those mocks are only needed for test_toInstance.
		MockitoAnnotations.initMocks(this);

		Instance instance = mock(Instance.class);
		InitializedInstance initializedInstance = new InitializedInstance(instance);

		TypeConverterUtil.setTypeConverter(typeConverter);
		when(typeConverter.convert(eq(InitializedInstance.class), any(ArchivedInstanceReference.class)))
				.thenReturn(initializedInstance);
	}

	@Test
	public void test_object_construction() {
		InstanceType type = InstanceType.create("type");
		// Test 3 parameters constructor which calls the two parameters one.
		ArchivedInstanceReference reference = new ArchivedInstanceReference(actual, def, type);

		assertEquals(reference.getReferenceType(), def);
		assertEquals(reference.getId(), actual);
		assertEquals(reference.getClass(), ArchivedInstanceReference.class);
		assertEquals(reference.getType(), type);
	}

	@Test
	public void test_hash_code_and_equals() {
		ArchivedInstanceReference referenceA = new ArchivedInstanceReference(actual, def);
		ArchivedInstanceReference referenceB = new ArchivedInstanceReference(actual, def);
		assertTrue(referenceA.equals(referenceB) && referenceB.equals(referenceA));
		assertEquals(referenceA.hashCode(), referenceB.hashCode());
	}

	// The next 3 tests test the different branches of the equals method. For more information see the tested class'
	// equals method.
	@Test
	public void test_equals_not_equal() {
		ArchivedInstanceReference referenceA = new ArchivedInstanceReference(actual, def);
		ArchivedInstanceReference referenceB = new ArchivedInstanceReference("differentSourceId", def);
		assertFalse(referenceA.equals(referenceB));
		assertFalse(referenceB.equals(referenceA));
		assertNotEquals(referenceA.hashCode(), referenceB.hashCode());
	}

	@Test
	public void test_equals_same_object() {
		ArchivedInstanceReference ref = new ArchivedInstanceReference(actual, def);
		assertTrue(ref.equals(ref));
	}

	@Test
	public void test_equals_parameter_not_instance_reference() {
		ArchivedInstanceReference ref = new ArchivedInstanceReference(actual, def);
		Instance instance = mock(Instance.class);
		assertFalse(ref.equals(instance));
	}

	@Test
	public void test_identifier() {
		ArchivedInstanceReference referenceA = new ArchivedInstanceReference(actual, def);
		assertEquals("sourceId", referenceA.getId());
	}

	@Test
	public void test_toInstance() {
		ArchivedInstanceReference reference = new ArchivedInstanceReference("sourceId", mock(DataTypeDefinition.class));
		assertNotNull(reference.toInstance());
		// Test for the other if branch.
		assertNotNull(reference.toInstance());
	}

}