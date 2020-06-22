package com.sirma.itt.emf.semantic;

import org.eclipse.rdf4j.model.Model;
import org.eclipse.rdf4j.model.ValueFactory;
import org.eclipse.rdf4j.model.impl.LinkedHashModel;
import org.eclipse.rdf4j.repository.RepositoryConnection;
import org.testng.Assert;
import org.testng.annotations.Test;

import com.sirma.itt.emf.GeneralSemanticTest;
import com.sirma.itt.emf.semantic.exception.SemanticPersistenceException;
import com.sirma.itt.emf.semantic.repository.creator.RepositoryUtils;

/**
 * @author kirq4e
 */
public class TestCompareOntologies extends GeneralSemanticTest<Void> {

	public final static String ONTOLOGY_GRPAH_URI = "http://test/ontology/original";
	public final static String EQUAL_ONTOLOGY_GRPAH_URI = "http://test/ontology/equal";
	public final static String DIFFERENT_ONTOLOGY_GRPAH_URI = "http://test/ontology/different";

	/**
	 * Tests comparing of two ontologies
	 */
	@Test
	public void testComarationOfOntoloogies() {
		noTransaction();
		Model originalOntology = loadGraphIntoModel(ONTOLOGY_GRPAH_URI);
		Model comparedOntology = loadGraphIntoModel(EQUAL_ONTOLOGY_GRPAH_URI);

		Model newStatements = new LinkedHashModel();
		Model removedStatements = new LinkedHashModel();
		
		RepositoryUtils.compareOntologies(originalOntology, comparedOntology, newStatements, removedStatements);

		Assert.assertTrue(newStatements.isEmpty());
		Assert.assertTrue(removedStatements.isEmpty());

		comparedOntology = loadGraphIntoModel(DIFFERENT_ONTOLOGY_GRPAH_URI);
		RepositoryUtils.compareOntologies(originalOntology, comparedOntology, newStatements, removedStatements);

		Assert.assertFalse(newStatements.isEmpty());
		Assert.assertFalse(removedStatements.isEmpty());

	}

	private static Model loadGraphIntoModel(String graphUri) {
		ValueFactory valueFactory = connectionFactory.produceValueFactory();
		RepositoryConnection connection = connectionFactory.produceReadOnlyConnection();
		try {
			Model graph = RepositoryUtils.loadGraph(connection, valueFactory.createIRI(graphUri));
			return graph;
		} catch (SemanticPersistenceException e) {
			Assert.fail(e.getMessage(), e);
		}
		return null;
	}

	@Override
	protected String getTestDataFile() {
		return "CompareOntologies.trig";
	}

}
