package com.sirma.itt.seip.annotations.state;

import static org.junit.Assert.assertEquals;

import java.util.Collections;

import org.junit.Before;
import org.junit.Test;

import com.sirma.itt.seip.annotations.model.Annotation;
import com.sirma.itt.seip.domain.instance.DefaultProperties;

/**
 * AnnotationInstanceStateServiceExtension test.
 *
 * @author tdossev
 */
public class AnnotationInstanceStateServiceExtensionTest {
	private AnnotationInstanceStateServiceExtension annoExtension;

	@Before
	public void setUpBeforeClass() throws Exception {
		annoExtension = new AnnotationInstanceStateServiceExtension();
	}

	@Test
	public void testGetInstanceClass() {
		assertEquals(annoExtension.getInstanceType(), Annotation.class.getSimpleName().toLowerCase());
	}

	@Test
	public void testGetActiveStates() {
		assertEquals(annoExtension.getActiveStates(), Collections.emptySet());
	}

	@Test
	public void testGetPrimaryStateProperty() {
		assertEquals(annoExtension.getPrimaryStateProperty(), DefaultProperties.EMF_STATUS);
	}

}
