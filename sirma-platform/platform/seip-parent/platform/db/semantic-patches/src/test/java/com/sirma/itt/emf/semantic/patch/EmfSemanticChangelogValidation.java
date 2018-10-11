package com.sirma.itt.emf.semantic.patch;

import org.junit.Assert;
import org.junit.Test;

import com.sirma.itt.emf.semantic.exception.SemanticPersistenceException;
import com.sirma.itt.emf.semantic.patch.validation.SemanticChangelogValidator;

/**
 * Validate all patches in emf-semantic-changelog.xml. If the patches doesn't pass validation it will be shown when the
 * tests are ran and not when the platform is deployed
 * 
 * @author kirq4e
 */
public class EmfSemanticChangelogValidation {

	@Test
	public void validateChangelog() throws Exception {
		try {
			SemanticChangelogValidator.validateChangelog(new SemanticModelPatch());
		} catch (SemanticPersistenceException e) {
			Assert.fail(e.getMessage());
		}
	}
}
