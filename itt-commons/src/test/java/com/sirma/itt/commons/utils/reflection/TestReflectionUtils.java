/**
 * Copyright (c) 2009 04.01.2009 , Sirma ITT. /* /**
 */
package com.sirma.itt.commons.utils.reflection;

import static org.testng.Assert.assertEquals;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

/**
 * @author Hristo Iliev
 */
public class TestReflectionUtils {
	/**
	 * Test public property access.
	 * 
	 * @author Hristo Iliev
	 */
	public static class TestClass1 {
		/** test public access. */
		public Integer intValue1;
		/** test protected access. */
		protected Integer intValue2;
		/** test package access. */
		Integer intValue3;
		/** test private access. */
		private Integer intValue4;

		/**
		 * Getter method for intValue1.
		 * 
		 * @return the intValue1
		 */
		public Integer getIntValue1() {
			return intValue1;
		}

		/**
		 * Setter method for intValue1.
		 * 
		 * @param intValue1
		 *            the intValue1 to set
		 */
		public void setIntValue1(Integer intValue1) {
			this.intValue1 = intValue1;
		}

		/**
		 * Getter method for intValue2.
		 * 
		 * @return the intValue2
		 */
		public Integer getIntValue2() {
			return intValue2;
		}

		/**
		 * Setter method for intValue2.
		 * 
		 * @param intValue2
		 *            the intValue2 to set
		 */
		public void setIntValue2(Integer intValue2) {
			this.intValue2 = intValue2;
		}

		/**
		 * Getter method for intValue3.
		 * 
		 * @return the intValue3
		 */
		public Integer getIntValue3() {
			return intValue3;
		}

		/**
		 * Setter method for intValue3.
		 * 
		 * @param intValue3
		 *            the intValue3 to set
		 */
		public void setIntValue3(Integer intValue3) {
			this.intValue3 = intValue3;
		}

		/**
		 * Getter method for intValue4.
		 * 
		 * @return the intValue4
		 */
		public Integer getIntValue4() {
			return intValue4;
		}

		/**
		 * Setter method for intValue4.
		 * 
		 * @param intValue4
		 *            the intValue4 to set
		 */
		public void setIntValue4(Integer intValue4) {
			this.intValue4 = intValue4;
		}
	}

	/**
	 * Test protected property access.
	 * 
	 * @author Hristo Iliev
	 */
	public static class TestClass2 {
		/** test public access. */
		public Integer intValue1;
		/** test protected access. */
		protected Integer intValue2;
		/** test package access. */
		Integer intValue3;
		/** test private access. */
		private Integer intValue4;

		/**
		 * Getter method for intValue1.
		 * 
		 * @return the intValue1
		 */
		protected Integer getIntValue1() {
			return intValue1;
		}

		/**
		 * Setter method for intValue1.
		 * 
		 * @param intValue1
		 *            the intValue1 to set
		 */
		protected void setIntValue1(Integer intValue1) {
			this.intValue1 = intValue1;
		}

		/**
		 * Getter method for intValue2.
		 * 
		 * @return the intValue2
		 */
		protected Integer getIntValue2() {
			return intValue2;
		}

		/**
		 * Setter method for intValue2.
		 * 
		 * @param intValue2
		 *            the intValue2 to set
		 */
		protected void setIntValue2(Integer intValue2) {
			this.intValue2 = intValue2;
		}

		/**
		 * Getter method for intValue3.
		 * 
		 * @return the intValue3
		 */
		protected Integer getIntValue3() {
			return intValue3;
		}

		/**
		 * Setter method for intValue3.
		 * 
		 * @param intValue3
		 *            the intValue3 to set
		 */
		protected void setIntValue3(Integer intValue3) {
			this.intValue3 = intValue3;
		}

		/**
		 * Getter method for intValue4.
		 * 
		 * @return the intValue4
		 */
		protected Integer getIntValue4() {
			return intValue4;
		}

		/**
		 * Setter method for intValue4.
		 * 
		 * @param intValue4
		 *            the intValue4 to set
		 */
		protected void setIntValue4(Integer intValue4) {
			this.intValue4 = intValue4;
		}
	}

	/**
	 * Test package property access.
	 * 
	 * @author Hristo Iliev
	 */
	public static class TestClass3 {
		/** test public access. */
		public Integer intValue1;
		/** test protected access. */
		protected Integer intValue2;
		/** test package access. */
		Integer intValue3;
		/** test private access. */
		private Integer intValue4;

		/**
		 * Getter method for intValue1.
		 * 
		 * @return the intValue1
		 */
		Integer getIntValue1() {
			return intValue1;
		}

		/**
		 * Setter method for intValue1.
		 * 
		 * @param intValue1
		 *            the intValue1 to set
		 */
		void setIntValue1(Integer intValue1) {
			this.intValue1 = intValue1;
		}

		/**
		 * Getter method for intValue2.
		 * 
		 * @return the intValue2
		 */
		Integer getIntValue2() {
			return intValue2;
		}

		/**
		 * Setter method for intValue2.
		 * 
		 * @param intValue2
		 *            the intValue2 to set
		 */
		void setIntValue2(Integer intValue2) {
			this.intValue2 = intValue2;
		}

		/**
		 * Getter method for intValue3.
		 * 
		 * @return the intValue3
		 */
		Integer getIntValue3() {
			return intValue3;
		}

		/**
		 * Setter method for intValue3.
		 * 
		 * @param intValue3
		 *            the intValue3 to set
		 */
		void setIntValue3(Integer intValue3) {
			this.intValue3 = intValue3;
		}

		/**
		 * Getter method for intValue4.
		 * 
		 * @return the intValue4
		 */
		Integer getIntValue4() {
			return intValue4;
		}

		/**
		 * Setter method for intValue4.
		 * 
		 * @param intValue4
		 *            the intValue4 to set
		 */
		void setIntValue4(Integer intValue4) {
			this.intValue4 = intValue4;
		}
	}

	/**
	 * Test private property access.
	 * 
	 * @author Hristo Iliev
	 */
	public static class TestClass4 {
		/** test public access. */
		public Integer intValue1;
		/** test protected access. */
		protected Integer intValue2;
		/** test package access. */
		Integer intValue3;
		/** test private access. */
		private Integer intValue4;

		/**
		 * Getter method for intValue1.
		 * 
		 * @return the intValue1
		 */
		protected Integer getIntValue1() {
			return intValue1;
		}

		/**
		 * Setter method for intValue1.
		 * 
		 * @param intValue1
		 *            the intValue1 to set
		 */
		protected void setIntValue1(Integer intValue1) {
			this.intValue1 = intValue1;
		}

		/**
		 * Getter method for intValue2.
		 * 
		 * @return the intValue2
		 */
		protected Integer getIntValue2() {
			return intValue2;
		}

		/**
		 * Setter method for intValue2.
		 * 
		 * @param intValue2
		 *            the intValue2 to set
		 */
		protected void setIntValue2(Integer intValue2) {
			this.intValue2 = intValue2;
		}

		/**
		 * Getter method for intValue3.
		 * 
		 * @return the intValue3
		 */
		protected Integer getIntValue3() {
			return intValue3;
		}

		/**
		 * Setter method for intValue3.
		 * 
		 * @param intValue3
		 *            the intValue3 to set
		 */
		protected void setIntValue3(Integer intValue3) {
			this.intValue3 = intValue3;
		}

		/**
		 * Getter method for intValue4.
		 * 
		 * @return the intValue4
		 */
		protected Integer getIntValue4() {
			return intValue4;
		}

		/**
		 * Setter method for intValue4.
		 * 
		 * @param intValue4
		 *            the intValue4 to set
		 */
		protected void setIntValue4(Integer intValue4) {
			this.intValue4 = intValue4;
		}
	}

	/**
	 * Provider of test classes.
	 * 
	 * @return {@link Iterator}, tests
	 */
	@DataProvider(name = "providerReflectionUtils_set")
	public Iterator<Object[]> providerReflectionUtils_setField() {
		List<Object[]> result = new ArrayList<Object[]>();

		result.add(new Object[] { new TestClass1() });
		result.add(new Object[] { new TestClass2() });
		result.add(new Object[] { new TestClass3() });
		result.add(new Object[] { new TestClass4() });

		return result.iterator();
	}

	/**
	 * Test setting and getting of field values.
	 * 
	 * @param object
	 *            object on which will be tested.
	 */
	@Test(dataProvider = "providerReflectionUtils_set", groups = { "ReflectionUtils" })
	public void testReflectionUtils_setField(Object object) {
		ReflectionUtils.setField(object, "intValue1", Integer.valueOf(1)); //$NON-NLS-1$
		assertEquals(ReflectionUtils.getField(object, "intValue1"), Integer //$NON-NLS-1$
				.valueOf(1));
		ReflectionUtils.setField(object, "intValue2", Integer.valueOf(2)); //$NON-NLS-1$
		assertEquals(ReflectionUtils.getField(object, "intValue2"), Integer //$NON-NLS-1$
				.valueOf(2));
		ReflectionUtils.setField(object, "intValue3", Integer.valueOf(3)); //$NON-NLS-1$
		assertEquals(ReflectionUtils.getField(object, "intValue3"), Integer //$NON-NLS-1$
				.valueOf(3));
		ReflectionUtils.setField(object, "intValue4", Integer.valueOf(4)); //$NON-NLS-1$
		assertEquals(ReflectionUtils.getField(object, "intValue4"), Integer //$NON-NLS-1$
				.valueOf(4));
	}

	/**
	 * Test setting and getting of properties.
	 * 
	 * @param object
	 *            object on which will be tested.
	 */
	@Test(dataProvider = "providerReflectionUtils_set", groups = { "ReflectionUtils" })
	public void testReflectionUtils_setProperty(Object object) {
		ReflectionUtils.setProperty(object, "intValue1", Integer.valueOf(1)); //$NON-NLS-1$
		assertEquals(ReflectionUtils.getProperty(object, "intValue1"), Integer //$NON-NLS-1$
				.valueOf(1));
		ReflectionUtils.setProperty(object, "intValue2", Integer.valueOf(2)); //$NON-NLS-1$
		assertEquals(ReflectionUtils.getProperty(object, "intValue2"), Integer //$NON-NLS-1$
				.valueOf(2));
		ReflectionUtils.setProperty(object, "intValue3", Integer.valueOf(3)); //$NON-NLS-1$
		assertEquals(ReflectionUtils.getProperty(object, "intValue3"), Integer //$NON-NLS-1$
				.valueOf(3));
		ReflectionUtils.setProperty(object, "intValue4", Integer.valueOf(4)); //$NON-NLS-1$
		assertEquals(ReflectionUtils.getProperty(object, "intValue4"), Integer //$NON-NLS-1$
				.valueOf(4));
	}
}
