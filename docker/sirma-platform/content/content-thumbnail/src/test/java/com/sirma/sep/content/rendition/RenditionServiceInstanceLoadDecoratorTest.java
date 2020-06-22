package com.sirma.sep.content.rendition;

import static org.mockito.Matchers.anyCollection;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.when;
import static org.junit.Assert.*;

import java.io.Serializable;
import java.util.Collection;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import com.sirma.itt.seip.domain.instance.DefaultProperties;
import com.sirma.itt.seip.domain.instance.EmfInstance;
import com.sirma.itt.seip.domain.instance.Instance;

/**
 * Test for RenditionServiceInstanceLoadDecorator.
 *
 * @author A. Kunchev
 */
public class RenditionServiceInstanceLoadDecoratorTest {

	public static final String A_THUMBNAIL = "a thumbnail";
	@InjectMocks
	private RenditionServiceInstanceLoadDecorator serviceDecorator = new RenditionServiceInstanceLoadDecorator();

	@Mock
	private RenditionService renditionService;

	@Before
	@SuppressWarnings("unchecked")
	public void setup() {
		MockitoAnnotations.initMocks(this);
		when(renditionService.getThumbnail(anyString())).thenReturn(A_THUMBNAIL);
		when(renditionService.getThumbnails(anyCollection())).then(a -> {
			Collection<Serializable> ids = a.getArgumentAt(0, Collection.class);
			return ids.stream().collect(Collectors.toMap(Function.identity(), id -> A_THUMBNAIL));
		});
	}

	@Test
	public void shouldLoadInstanceThumbnail() {
		EmfInstance instance = new EmfInstance("emf:instance");
		serviceDecorator.decorateInstance(instance);
		verifyInstanceHasThumbnail(instance);
	}

	private void verifyInstanceHasThumbnail(Instance instance) {
		assertEquals(A_THUMBNAIL, instance.get(DefaultProperties.THUMBNAIL_IMAGE));
	}

	@Test
	public void shouldLoadThumbnailOfMultipleInstances() {
		Collection<Instance> instances = IntStream.range(1, 5)
				.boxed()
				.map(id -> "emf:instance-" + id)
				.map(EmfInstance::new)
				.collect(Collectors.toList());
		serviceDecorator.decorateResult(instances);
		instances.forEach(this::verifyInstanceHasThumbnail);
	}
}
