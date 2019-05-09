package com.sirma.itt.emf.semantic.model.init;

import static org.mockito.Mockito.spy;

import java.io.IOException;

import org.apache.commons.io.IOUtils;
import org.eclipse.rdf4j.model.IRI;
import org.eclipse.rdf4j.model.Model;
import org.eclipse.rdf4j.model.Resource;
import org.eclipse.rdf4j.model.ValueFactory;
import org.eclipse.rdf4j.model.impl.LinkedHashModel;
import org.eclipse.rdf4j.model.vocabulary.DCTERMS;
import org.eclipse.rdf4j.model.vocabulary.RDF;
import org.eclipse.rdf4j.query.BindingSet;
import org.eclipse.rdf4j.query.QueryEvaluationException;
import org.eclipse.rdf4j.query.TupleQuery;
import org.eclipse.rdf4j.repository.RepositoryConnection;
import org.eclipse.rdf4j.repository.RepositoryException;
import org.mockito.ArgumentCaptor;
import org.mockito.Matchers;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import com.sirma.itt.emf.GeneralSemanticTest;
import com.sirma.itt.emf.semantic.queries.SPARQLQueryHelper;
import com.sirma.itt.emf.semantic.search.TupleQueryResultIterator;
import com.sirma.itt.seip.collections.CollectionUtils;
import com.sirma.itt.semantic.model.vocabulary.EMF;
import com.sirma.itt.semantic.search.SemanticQueries;

/**
 * Tests class description if it generates all the needed properties.
 *
 * @author kirq4e
 */
public class ClassDescriptionGeneratorTest extends GeneralSemanticTest<ClassDescriptionGenerator> {

	/**
	 * Initializes the tested class
	 */
	@BeforeClass
	public void init() {
		MockitoAnnotations.initMocks(this);
	}

	@BeforeMethod
	public void initData() throws IOException {
		beginTransaction();
		RepositoryConnection managedConnection = connectionFactory.produceManagedConnection();
		String query = IOUtils.toString(loadDataFile("ClassDescriptionGeneratorTestData.sparql"));
		managedConnection.prepareUpdate(query).execute();
		commitTransaction();
	}

	/**
	 * Tests class description if it generates all of the needed properties
	 *
	 * @throws RepositoryException
	 *             if an error occurs
	 * @throws QueryEvaluationException
	 *             if an error occurs
	 */
	@Test
	public void testClassDescriptionGeneration() throws RepositoryException, QueryEvaluationException {
		beginTransaction();
		RepositoryConnection connection = spy(connectionFactory.produceManagedConnection());
		service = new ClassDescriptionGenerator(connection);
		ArgumentCaptor<LinkedHashModel> modelCapture = ArgumentCaptor.forClass(LinkedHashModel.class);

		service.initClassAndPropertiesDescription();
		commitTransaction();

		Mockito.verify(connection).add(modelCapture.capture(), Matchers.eq(EMF.CLASS_DESCRIPTION_CONTEXT));
		LinkedHashModel model = modelCapture.getValue();

		connection = connectionFactory.produceReadOnlyConnection();
		TupleQuery tupleQuery = SPARQLQueryHelper.prepareTupleQuery(connection,
				SemanticQueries.QUERY_CLASS_DESCRIPTION.getQuery(), CollectionUtils.emptyMap(), false);
		ValueFactory valueFactory = connection.getValueFactory();
		try (TupleQueryResultIterator resultIterator = new TupleQueryResultIterator(tupleQuery.evaluate())) {
			for (BindingSet bindingSet : resultIterator) {
				IRI classUri = (IRI) bindingSet.getBinding(SPARQLQueryHelper.OBJECT).getValue();
				Assert.assertTrue(model.contains(classUri, RDF.TYPE, EMF.CLASS_DESCRIPTION, (Resource) null));
				Assert.assertTrue(
						model.contains(classUri, EMF.STATUS, valueFactory.createLiteral("APPROVED"), (Resource) null));
				Assert.assertTrue(model.contains(classUri, EMF.DEFAULT_TEMPLATE,
						valueFactory.createLiteral("ontologyClassTemplate"), (Resource) null));
				Assert.assertTrue(model.contains(classUri, EMF.TYPE, valueFactory.createLiteral("classDefinition"),
						(Resource) null));
				Assert.assertTrue(model.contains(classUri, EMF.INSTANCE_TYPE,
						valueFactory.createLiteral("objectinstance"), (Resource) null));
				Assert.assertTrue(
						model.contains(classUri, EMF.IS_DELETED, valueFactory.createLiteral(false), (Resource) null));
			}
		}

		verify(model, "someObjectProperty", DCTERMS.TITLE, "german title", valueFactory);
		verify(model, "someDataProperty", DCTERMS.TITLE, "german title", valueFactory);
		verify(model, "someAnnotationProperty", DCTERMS.TITLE, "german title", valueFactory);
		verify(model, "SomeClass", DCTERMS.TITLE, "german title", valueFactory);
		verify(model, "someAnnotationProperty2", DCTERMS.TITLE, "german title", valueFactory);
		verify(model, "someAnnotationProperty2", DCTERMS.TITLE, "fin title", "fi", valueFactory);
		verify(model, "someAnnotationProperty2", DCTERMS.TITLE, "eng title", "en", valueFactory);

		verify(model, "someObjectProperty", DCTERMS.DESCRIPTION, "german description", valueFactory);
		verify(model, "someDataProperty", DCTERMS.DESCRIPTION, "german description", valueFactory);
		verify(model, "someAnnotationProperty", DCTERMS.DESCRIPTION, "german description", valueFactory);
		verify(model, "SomeClass", DCTERMS.DESCRIPTION, "german description", valueFactory);
	}

	private void verify(Model model, String subjectName, IRI predicate, String value, ValueFactory valueFactory) {
		verify(model, subjectName, predicate, value, "de", valueFactory);
	}

	private void verify(Model model, String subjectName, IRI predicate, String value, String lang, ValueFactory valueFactory) {
		Assert.assertTrue(model.contains(valueFactory.createIRI(EMF.NAMESPACE, subjectName), predicate,
				valueFactory.createLiteral(value, lang)));
	}

	@Override
	protected String getTestDataFile() {
		return null;
	}

}
