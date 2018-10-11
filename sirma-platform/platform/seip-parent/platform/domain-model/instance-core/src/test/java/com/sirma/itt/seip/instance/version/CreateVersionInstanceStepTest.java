package com.sirma.itt.seip.instance.version;

import static org.junit.Assert.assertEquals;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.when;

import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import com.sirma.itt.seip.convert.TypeConverter;
import com.sirma.itt.seip.domain.instance.ArchivedInstance;
import com.sirma.itt.seip.domain.instance.EmfInstance;
import com.sirma.itt.seip.domain.instance.Instance;

/**
 * Test for {@link CreateVersionInstanceStep}.
 *
 * @author A. Kunchev
 */
public class CreateVersionInstanceStepTest {

	@InjectMocks
	private CreateVersionInstanceStep step;

	@Mock
	private TypeConverter typeConverter;

	@Before
	public void setup() {
		MockitoAnnotations.initMocks(this);
	}

	@Test
	public void getName() {
		assertEquals("createVersionInstance", step.getName());
	}

	@Test
	public void execute() {
		Instance targetInstance = new EmfInstance();
		ArchivedInstance version = new ArchivedInstance();
		when(typeConverter.convert(eq(ArchivedInstance.class), eq(targetInstance))).thenReturn(version);

		VersionContext context = VersionContext.create(targetInstance);
		step.execute(context);

		assertEquals(version, context.getVersionInstance().get());
	}
}