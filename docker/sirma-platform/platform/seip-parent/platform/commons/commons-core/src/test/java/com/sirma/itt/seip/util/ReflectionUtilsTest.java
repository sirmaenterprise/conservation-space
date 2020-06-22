package com.sirma.itt.seip.util;

import static org.junit.Assert.assertNotNull;
import static org.testng.Assert.assertEquals;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.ArrayList;

import org.junit.Assert;
import org.junit.Test;
import org.mockito.Mockito;

public class ReflectionUtilsTest {

	@Test
	public void testGetTypeArgument() {
		Type mock1 = Mockito.mock(Type.class);
		Assert.assertNull(ReflectionUtils.getTypeArgument(mock1));

		ParameterizedType mock2 = Mockito.mock(ParameterizedType.class);

		Mockito.when(mock2.getActualTypeArguments()).thenReturn(null);
		Assert.assertNull(ReflectionUtils.getTypeArgument(mock2));

		Mockito.when(mock2.getActualTypeArguments()).thenReturn(new Type[] {});
		Assert.assertNull(ReflectionUtils.getTypeArgument(mock2));

		Mockito.when(mock2.getActualTypeArguments()).thenReturn(new Type[] { String.class });
		Assert.assertEquals(String.class, ReflectionUtils.getTypeArgument(mock2));
	}

	@Test
	public void testIsTypeArgument() {
		ParameterizedType mockType = Mockito.mock(ParameterizedType.class);

		Mockito.when(mockType.getActualTypeArguments()).thenReturn(null);
		Assert.assertFalse(ReflectionUtils.isTypeArgument(mockType, String.class));

		Mockito.when(mockType.getActualTypeArguments()).thenReturn(new Type[] { String.class });
		Assert.assertTrue(ReflectionUtils.isTypeArgument(mockType, String.class));
	}

	@Test
	public void should_CreateNewInstanceUsingDefaultConstructor() {
		ArrayList<?> c = ReflectionUtils.newInstance(ArrayList.class);
		assertNotNull(c);
	}

	@Test(expected = IllegalStateException.class)
	public void should_ThrowError_When_InstantiatingPrivateClass() {
		A a = ReflectionUtils.newInstance(A.class);
		assertNotNull(a);
	}

	@Test
	public void should_SetFieldValueUsingReflection_WhenTheFieldIsInParentClass() {
		B b = new B();
		ReflectionUtils.setFieldValue(b, "a", "test");

		assertEquals("test", ReflectionUtils.getFieldValue(b, "a"));
	}

	@Test(expected = IllegalArgumentException.class)
	public void should_ThrowError_When_TryingToGetNonExistingField() {
		B b = new B();
		ReflectionUtils.getFieldValue(b, "non_existing");
	}

	@Test(expected = IllegalArgumentException.class)
	public void should_ThrowError_When_TryingToSetNonExistingField() {
		B b = new B();
		ReflectionUtils.setFieldValue(b, "non_existing", null);
	}

	@Test(expected = IllegalArgumentException.class)
	public void should_ThrowError_When_TryingToProvideFieldOfNonExistingClass() throws NoSuchFieldException {
		ReflectionUtils.getClassField(null, "b");
	}

	private static class A {
		private String a;
	}

	private static class B extends A {
		private String b;
	}

}
