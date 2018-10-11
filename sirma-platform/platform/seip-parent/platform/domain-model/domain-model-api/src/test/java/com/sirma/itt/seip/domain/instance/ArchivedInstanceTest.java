package com.sirma.itt.seip.domain.instance;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertSame;
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

/**
 * Test for {@link ArchivedInstance}.
 *
 * @author A. Kunchev
 */
public class ArchivedInstanceTest {

	@Mock
	private TypeConverter typeConverter;

	@Before
	public void setup() {
		MockitoAnnotations.initMocks(this);

		TypeConverterUtil.setTypeConverter(typeConverter);
		when(typeConverter.convert(eq(ArchivedInstanceReference.class), any(ArchivedInstance.class)))
				.thenReturn(mock(ArchivedInstanceReference.class));
	}

	@Test
	public void setVersion() {
		ArchivedInstance instance = new ArchivedInstance();
		instance.setVersion("12.4");
		assertEquals(12, instance.getMajorVersion());
		assertEquals(4, instance.getMinorVersion());
	}

	@Test
	public void getVersion() {
		ArchivedInstance instance = new ArchivedInstance();
		instance.setMajorVersion(5);
		instance.setMinorVersion(21);
		assertEquals("5.21", instance.getVersion());
	}

	@Test
	public void toReference() {
		ArchivedInstance instance = new ArchivedInstance();
		InstanceReference reference = instance.toReference();
		assertNotNull(reference);
		// Test for the other if branch.
		assertSame(reference, instance.toReference());
	}

}
