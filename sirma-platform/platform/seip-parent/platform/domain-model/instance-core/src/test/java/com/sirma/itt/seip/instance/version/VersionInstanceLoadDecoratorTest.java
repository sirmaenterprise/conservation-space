package com.sirma.itt.seip.instance.version;

import static org.mockito.Matchers.any;
import static org.mockito.Mockito.only;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import java.util.Arrays;

import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import com.sirma.itt.seip.domain.instance.EmfInstance;
import com.sirma.itt.seip.domain.instance.Instance;

/**
 * Test for {@link VersionInstanceLoadDecorator}.
 *
 * @author A. Kunchev
 */
public class VersionInstanceLoadDecoratorTest {

	@InjectMocks
	private VersionInstanceLoadDecorator decorator;

	@Mock
	private InstanceVersionService instanceVersionService;

	@Before
	public void setup() {
		decorator = new VersionInstanceLoadDecorator();
		MockitoAnnotations.initMocks(this);
	}

	@Test
	public void decorateInstance() {
		EmfInstance instance = new EmfInstance();
		decorator.decorateInstance(instance);
		verify(instanceVersionService, only()).populateVersion(instance);
	}

	@Test
	public void decorateResult() {
		decorator.decorateResult(Arrays.asList(new EmfInstance(), new EmfInstance()));
		verify(instanceVersionService, times(2)).populateVersion(any(Instance.class));
	}

}
