package com.sirma.itt.emf.cls.rest;

import static org.junit.Assert.assertNotNull;

import org.junit.Test;

import com.sirma.itt.seip.rest.annotations.security.AdminResource;

/**
 * Tests for {@link EditService}.
 *
 * @author smustafov
 */
public class EditServiceTest {

	/**
	 * Tests if the class has the {@link AdminResource} annotation.
	 */
	@Test
	public void testMarkedAsAdminResource() {
		AdminResource annotation = EditService.class.getAnnotation(AdminResource.class);
		assertNotNull(annotation);
	}

}
