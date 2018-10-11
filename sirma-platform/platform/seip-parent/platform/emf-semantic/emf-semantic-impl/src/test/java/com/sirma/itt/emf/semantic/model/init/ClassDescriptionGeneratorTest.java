package com.sirma.itt.emf.semantic.model.init;

import static com.sirma.itt.seip.domain.instance.DefaultProperties.DESCRIPTION;
import static com.sirma.itt.seip.domain.instance.DefaultProperties.TITLE;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.when;

import org.eclipse.rdf4j.model.IRI;
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
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import com.sirma.itt.emf.GeneralSemanticTest;
import com.sirma.itt.emf.semantic.queries.SPARQLQueryHelper;
import com.sirma.itt.emf.semantic.search.TupleQueryResultIterator;
import com.sirma.itt.seip.collections.CollectionUtils;
import com.sirma.itt.seip.monitor.Statistics;
import com.sirma.itt.seip.time.TimeTracker;
import com.sirma.itt.seip.util.ReflectionUtils;
import com.sirma.itt.semantic.model.vocabulary.EMF;
import com.sirma.itt.semantic.search.SemanticQueries;

/**
 * Tests class description if it generates all the needed properties.
 *
 * @author kirq4e
 */
public class ClassDescriptionGeneratorTest extends GeneralSemanticTest<ClassDescriptionGenerator> {

	@Mock
	private Statistics statistics;

	/**
	 * Initializes the tested class
	 */
	@BeforeClass
	public void init() {
		MockitoAnnotations.initMocks(this);
		service = new ClassDescriptionGenerator();
		when(statistics.createTimeStatistics(any(), anyString())).thenReturn(new TimeTracker());
		ReflectionUtils.setFieldValue(service, "statistics", statistics);
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
		RepositoryConnection connection = spy(connectionFactory.produceManagedConnection());
		ReflectionUtils.setFieldValue(service, "connection", connection);
		ArgumentCaptor<LinkedHashModel> modelCapture = ArgumentCaptor.forClass(LinkedHashModel.class);

		service.initClassDescription();
		commitTransaction();

		Mockito.verify(connection).add(modelCapture.capture(), Matchers.eq(EMF.CLASS_DESCRIPTION_CONTEXT));
		LinkedHashModel model = modelCapture.getValue();
		Mockito.verify(connection, Mockito.times(1)).clear(Matchers.eq(EMF.CLASS_DESCRIPTION_CONTEXT));

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
				Assert.assertTrue(model.contains(classUri, DCTERMS.TITLE, bindingSet.getBinding(TITLE).getValue(),
						(Resource) null));
				Assert.assertTrue(model.contains(classUri, DCTERMS.DESCRIPTION,
						bindingSet.getBinding(DESCRIPTION).getValue(), (Resource) null));
			}
		}
	}

	@Override
	protected String getTestDataFile() {
		return null;
	}

}
