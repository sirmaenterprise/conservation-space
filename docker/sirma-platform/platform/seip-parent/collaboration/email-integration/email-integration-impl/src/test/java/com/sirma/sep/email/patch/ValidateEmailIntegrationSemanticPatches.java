package com.sirma.sep.email.patch;

import org.junit.Assert;
import org.junit.Test;

import com.sirma.itt.emf.semantic.exception.SemanticPersistenceException;
import com.sirma.itt.emf.semantic.patch.validation.SemanticChangelogValidator;

/**
 * Validates email-integration-semantic-changelog.xml
 * 
 * @author kirq4e
 */
public class ValidateEmailIntegrationSemanticPatches {
	
	@Test
	public void validateChangelog() {
		try {
			SemanticChangelogValidator.validateChangelog(new EmailIntegrationSemanticPatch());
		} catch (SemanticPersistenceException e) {
			Assert.fail(e.getMessage());
		}
	}

}
