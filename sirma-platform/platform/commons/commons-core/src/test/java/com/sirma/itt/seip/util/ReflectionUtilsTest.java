package com.sirma.itt.seip.util;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;

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
		
		Mockito.when(mock2.getActualTypeArguments()).thenReturn(new Type[]{});
		Assert.assertNull(ReflectionUtils.getTypeArgument(mock2));
		
		Mockito.when(mock2.getActualTypeArguments()).thenReturn(new Type[]{ String.class });
		Assert.assertEquals(String.class, ReflectionUtils.getTypeArgument(mock2));
	}

	@Test
	public void testIsTypeArgument() {
		ParameterizedType mockType = Mockito.mock(ParameterizedType.class);

		Mockito.when(mockType.getActualTypeArguments()).thenReturn(null);
		Assert.assertFalse(ReflectionUtils.isTypeArgument(mockType, String.class));
		
		Mockito.when(mockType.getActualTypeArguments()).thenReturn(new Type[]{ String.class });
		Assert.assertTrue(ReflectionUtils.isTypeArgument(mockType, String.class));
	}
}
