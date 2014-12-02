/**
 *
 */
package com.sirma.itt.emf.semantic.query.tools;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sirma.itt.emf.semantic.search.SemanticSearchEngine;

/**
 * Generate code for the lexer and parser from the rules of the Sirma Query grammar
 *
 * @author kirq4e
 */
public class GenerateCode {

	private static final Logger LOGGER = LoggerFactory.getLogger(SemanticSearchEngine.class);

	/**
	 * Hide the default constructor
	 */
	private GenerateCode() {
	}

	/**
	 * Generate the ANTLR code for the Sirma Query grammar.
	 *
	 * @param args
	 *            Arguments
	 */
	public static void main(String[] args) {
		try {

			String grammar = "A:/Development/Workspaces/IDoc/sep-bundle/platform/emf-semantic/emf-semantic-impl/src/main/resources/com/sirma/itt/emf/semantic/query/grammar/Sirma.g4";
			String outputFolder = "A:/Development/Workspaces/IDoc/sep-bundle/platform/emf-semantic/emf-semantic-impl/src/main/java/com/sirma/itt/emf/semantic/query/grammar";

			org.antlr.v4.Tool.main(new String[] { grammar, "-o", outputFolder });
		} catch (Exception e) {
			LOGGER.error(e.getMessage(), e);
		}
	}
}
