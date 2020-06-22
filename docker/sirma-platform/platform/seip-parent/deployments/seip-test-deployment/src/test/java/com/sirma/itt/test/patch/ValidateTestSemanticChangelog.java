package com.sirma.itt.test.patch;

import org.junit.Assert;
import org.junit.Test;

import com.sirma.itt.emf.semantic.exception.SemanticPersistenceException;
import com.sirma.itt.emf.semantic.patch.validation.SemanticChangelogValidator;

/**
 * Validate semantic patches in test-semantic-changelog.xml
 * 
 * @author kirq4e
 */
public class ValidateTestSemanticChangelog {

	@Test
	public void validateChangelog() {
		try {
			SemanticChangelogValidator.validateChangelog(new TestSemanticRepositoryUpdate());
		} catch (SemanticPersistenceException e) {
			Assert.fail(e.getMessage());
		}
	}
}
