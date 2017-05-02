package com.sirma.itt.emf.semantic;

import static com.sirma.itt.seip.domain.instance.DefaultProperties.DESCRIPTION;
import static com.sirma.itt.seip.domain.instance.DefaultProperties.TITLE;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.when;

import org.mockito.Matchers;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;
import org.openrdf.model.Resource;
import org.openrdf.model.URI;
import org.openrdf.model.ValueFactory;
import org.openrdf.model.impl.LinkedHashModel;
import org.openrdf.model.vocabulary.DCTERMS;
import org.openrdf.model.vocabulary.RDF;
import org.openrdf.query.BindingSet;
import org.openrdf.query.QueryEvaluationException;
import org.openrdf.query.TupleQuery;
import org.openrdf.repository.RepositoryException;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import com.sirma.itt.commons.utils.reflection.ReflectionUtils;
import com.sirma.itt.emf.GeneralSemanticTest;
import com.sirma.itt.emf.mocks.TransactionalRepositoryConnectionMock;
import com.sirma.itt.emf.semantic.model.init.ClassDescriptionGenerator;
import com.sirma.itt.emf.semantic.queries.SPARQLQueryHelper;
import com.sirma.itt.emf.semantic.search.TupleQueryResultIterator;
import com.sirma.itt.seip.collections.CollectionUtils;
import com.sirma.itt.seip.monitor.Statistics;
import com.sirma.itt.seip.testutil.mocks.InstanceProxyMock;
import com.sirma.itt.seip.time.TimeTracker;
import com.sirma.itt.semantic.TransactionalRepositoryConnection;
import com.sirma.itt.semantic.model.vocabulary.EMF;
import com.sirma.itt.semantic.search.SemanticQueries;

/**
 * Tests class description if it generates all the needed properties
 * 
 * @author kirq4e
 */
public class TestClassDescriptionGenerator extends GeneralSemanticTest<ClassDescriptionGenerator> {

	@Mock
	private Statistics statistics;

	/**
	 * Initializes the tested class
	 */
	@BeforeClass
	public void init() {
		MockitoAnnotations.initMocks(this);
		service = new ClassDescriptionGenerator();
		when(statistics.createTimeStatistics(any(), anyString())).then(a -> new TimeTracker());
		ReflectionUtils.setField(service, "statistics", statistics);
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
		TransactionalRepositoryConnection repositoryConnection = new TransactionalRepositoryConnectionMock(context);
		TransactionalRepositoryConnection connection = Mockito.spy(repositoryConnection);
		ReflectionUtils.setField(service, "connection", new InstanceProxyMock<>(connection));
		final LinkedHashModel model = new LinkedHashModel();
		Mockito.doAnswer(new Answer<Void>() {

			@Override
			public Void answer(InvocationOnMock invocation) throws Throwable {
				model.addAll(invocation.getArgumentAt(0, LinkedHashModel.class));
				invocation.callRealMethod();
				return null;
			}
		}).when(connection).add(any(LinkedHashModel.class), Matchers.eq(EMF.CLASS_DESCRIPTION_CONTEXT));

		service.initClassDescription();
		connection = ((InstanceProxyMock<TransactionalRepositoryConnection>) ReflectionUtils.getField(service,
				"connection")).get();
		getConnectionFactory().disposeConnection(connection);
		
		Mockito.verify(connection, Mockito.times(1)).clear(Matchers.eq(EMF.CLASS_DESCRIPTION_CONTEXT));

		connection = new TransactionalRepositoryConnectionMock(context);
		TupleQuery tupleQuery = SPARQLQueryHelper.prepareTupleQuery(connection,
				SemanticQueries.QUERY_CLASS_DESCRIPTION.getQuery(), CollectionUtils.emptyMap(), false);
		ValueFactory valueFactory = connection.getValueFactory();
		try (TupleQueryResultIterator resultIterator = new TupleQueryResultIterator(tupleQuery.evaluate())) {
			for (BindingSet bindingSet : resultIterator) {
				URI classUri = (URI) bindingSet.getBinding(SPARQLQueryHelper.OBJECT).getValue();
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
		getConnectionFactory().disposeConnection(connection);
		
	}

	@Override
	protected String getTestDataFile() {
		return null;
	}

}
