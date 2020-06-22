package com.sirma.itt.emf.semantic.patch;

import com.sirma.itt.emf.semantic.queries.SPARQLQueryHelper;
import com.sirma.itt.semantic.ConnectionFactory;
import com.sirma.itt.semantic.model.vocabulary.EMF;
import liquibase.database.Database;
import liquibase.exception.CustomChangeException;
import org.eclipse.rdf4j.model.IRI;
import org.eclipse.rdf4j.model.Model;
import org.eclipse.rdf4j.model.Statement;
import org.eclipse.rdf4j.model.impl.LiteralImpl;
import org.eclipse.rdf4j.model.impl.URIImpl;
import org.eclipse.rdf4j.query.BindingSet;
import org.eclipse.rdf4j.query.QueryEvaluationException;
import org.eclipse.rdf4j.query.QueryLanguage;
import org.eclipse.rdf4j.query.TupleQuery;
import org.eclipse.rdf4j.query.TupleQueryResult;
import org.eclipse.rdf4j.query.impl.MapBindingSet;
import org.eclipse.rdf4j.repository.RepositoryConnection;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Matchers;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

/**
 * Test for {@link RemoveDuplicateEmfVersionProperties} patch.
 *
 * @author Boyan Tonchev.
 */
public class RemoveDuplicateEmfVersionPropertiesTest {

    @Mock
    private Database database;

    @Mock
    private RepositoryConnection repositoryConnection;

    @Mock
    private TupleQuery duplicateEmfVersionTupleQueryMock;

    @Mock
    private ConnectionFactory connectionFactory;

    @InjectMocks
    private RemoveDuplicateEmfVersionProperties removeDuplicateEmfVersionProperties;

    @Before
    public void setUp() {
        MockitoAnnotations.initMocks(this);
        Mockito.when(connectionFactory.produceConnection()).thenReturn(repositoryConnection);
        Mockito.when(repositoryConnection.prepareTupleQuery(Matchers.eq(QueryLanguage.SPARQL), Matchers.anyString()))
                .thenReturn(duplicateEmfVersionTupleQueryMock);
    }

    @Test
    public void should_returnConfirmationMessage() {
        Assert.assertEquals(removeDuplicateEmfVersionProperties.getConfirmationMessage(), "Duplicated properties emf:version are removed!");
    }

    @Test
    public void should_deleteEmfVersionPropertiesWithOutLatest_When_ThereMoreThanOne() throws CustomChangeException {
        setUpDuplicateEmfVersionQueryResult();

        removeDuplicateEmfVersionProperties.execute(database);

        //verification
        Mockito.verify(connectionFactory).disposeConnection(repositoryConnection);
        ArgumentCaptor<Model> removeModelCaptor = ArgumentCaptor.forClass(Model.class);
        Mockito.verify(repositoryConnection).remove(removeModelCaptor.capture(), Mockito.any(IRI.class));
        assertRemoveStatements(removeModelCaptor);
    }

    /**
     * Create {@link TupleQueryResult} of {@link RemoveDuplicateEmfVersionProperties#DUPLICATE_EMF_VERSION_QUERY}.
     */
    private void setUpDuplicateEmfVersionQueryResult() {
        TupleQueryResultTest duplicateEmfVersionTupleQueryMockResult = new TupleQueryResultTest();
        Mockito.when(duplicateEmfVersionTupleQueryMock.evaluate()).thenReturn(duplicateEmfVersionTupleQueryMockResult);
    }

    /**
     * Assert are present delete statements for wrong versions (the smallest versions for instances).<br>
     * 1. For {@value TupleQueryResultTest#instanceOneUriString} versions {@value TupleQueryResultTest#VERSION_TWO_ONE} and {@value TupleQueryResultTest#VERSION_TWO_TEN} have to be present.<br>
     * 2. For {@value TupleQueryResultTest#instanceTwoUriString} versions {@value TupleQueryResultTest#VERSION_TWO_NULL} have to be present.
     *
     * <br><br>Assert are not present delete statements for correct versions (the biggest for instances).<br>
     * 1. For {@value TupleQueryResultTest#instanceOneUriString} version {@value TupleQueryResultTest#VERSION_TREE_NULL}<br>
     * 2. For {@value TupleQueryResultTest#instanceTwoUriString} version {@value TupleQueryResultTest#VERSION_TREE_NULL}<br>
     * 3. For {@value TupleQueryResultTest#instanceThreeUriString} version {@value TupleQueryResultTest#VERSION_ONE_NULL}
     *
     * @param removeModelCaptor - captor of parameter {@link Model} of method {@link RepositoryConnection#remove}
     */
    private void assertRemoveStatements(ArgumentCaptor<Model> removeModelCaptor) {
        Model removeModelCaptorValue = removeModelCaptor.getValue();

        //Check if the smallest version are deleted
        assertStatementPresent(removeModelCaptorValue, TupleQueryResultTest.instanceOneUriString,
                               EMF.VERSION.toString(), TupleQueryResultTest.VERSION_TWO_ONE);
        assertStatementPresent(removeModelCaptorValue, TupleQueryResultTest.instanceOneUriString,
                               EMF.VERSION.toString(), TupleQueryResultTest.VERSION_TWO_TEN);
        assertStatementPresent(removeModelCaptorValue, TupleQueryResultTest.instanceTwoUriString,
                               EMF.VERSION.toString(), TupleQueryResultTest.VERSION_TWO_NULL);

        //Check if the biggest version are not deleted
        assertStatementNotPresent(removeModelCaptorValue, TupleQueryResultTest.instanceOneUriString,
                                  EMF.VERSION.toString(), TupleQueryResultTest.VERSION_TREE_NULL);
        assertStatementNotPresent(removeModelCaptorValue, TupleQueryResultTest.instanceTwoUriString,
                                  EMF.VERSION.toString(), TupleQueryResultTest.VERSION_TREE_NULL);
        assertStatementNotPresent(removeModelCaptorValue, TupleQueryResultTest.instanceThreeUriString,
                                  EMF.VERSION.toString(), TupleQueryResultTest.VERSION_ONE_NULL);
    }

    /**
     * Assert that <code>removeModel</code> not contain delete statement with <code>subject</code> <code>predicate</code> <code>object</code>.
     *
     * @param removeModel
     *         - the executed remove model.
     * @param subject
     *         - the subject of statement.
     * @param predicate
     *         - the predicate of statement.
     * @param object
     *         - the object of statement.
     */
    private void assertStatementNotPresent(Model removeModel, String subject, String predicate, String object) {
        String errorMessage = "Found wrong delete statement: \"" + subject + " " + predicate + " " + object + "\"";
        Assert.assertFalse(errorMessage, hasStatement(removeModel.iterator(), subject, predicate, object));
    }

    /**
     * Assert that <code>removeModel</code> contain delete statement with <code>subject</code> <code>predicate</code> <code>object</code>.
     *
     * @param removeModel
     *         - the executed remove model.
     * @param subject
     *         - the subject of statement.
     * @param predicate
     *         - the predicate of statement.
     * @param object
     *         - the object of statement.
     */
    private void assertStatementPresent(Model removeModel, String subject, String predicate, String object) {
        String errorMessage = "Delete statement: \"" + subject + " " + predicate + " " + object + "\" not found";
        Assert.assertTrue(errorMessage, hasStatement(removeModel.iterator(), subject, predicate, object));
    }

    /**
     * Iterate over statements <code>statements</code> and check if there is statement with <code>subject</code> <code>predicate</code> <code>object</code>.
     *
     * @param statements
     *         iterator with statements to be checked.
     * @param subject
     *         - the subject of statement.
     * @param predicate
     *         - the predicate of statement.
     * @param object
     *         - the object of statement.
     * @return true if there is such statement otherwise false.
     */
    private boolean hasStatement(Iterator<Statement> statements, String subject, String predicate, String object) {
        while (statements.hasNext()) {
            Statement deleteStatement = statements.next();
            if (deleteStatement.getSubject().stringValue().equals(subject) && deleteStatement.getPredicate()
                    .stringValue()
                    .equals(predicate) && deleteStatement.getObject().stringValue().equals(object)) {
                return true;
            }
        }
        return false;
    }

    /**
     * {@link TupleQueryResult} implementation used from test to represent result of query for duplicated emf:version
     * instances. Returned bindings will represent tree versions for instance one,
     * two for instance two and one for instance tree. See table below:
     * <table border="1">
     * <tr>
     * <th>instance</th>
     * <th>version</th>
     * </tr>
     * <tr>
     * <td rowspan="3">{@value #instanceOneUriString}</td>
     * <td>{@value #VERSION_TWO_ONE}</td>
     * </tr>
     * <tr>
     * <td>{@value #VERSION_TWO_TEN}</td>
     * </tr>
     * <tr>
     * <td>{@value #VERSION_TREE_NULL}</td>
     * </tr>
     * <tr>
     * <td rowspan="2">{@value #instanceTwoUriString}</td>
     * <td>{@value #VERSION_TREE_NULL}</td>
     * </tr>
     * <tr>
     * <td>{@value #VERSION_TWO_NULL}</td>
     * </tr>
     * <tr>
     * <td>{@value #instanceThreeUriString}</td>
     * <td>{@value #VERSION_ONE_NULL}</td>
     * </tr>
     * </table>
     */
    private class TupleQueryResultTest implements TupleQueryResult {

        private static final String instanceOneUriString = "http://ittruse.ittbg.com/ontology/enterpriseManagementFramework#instance-one-id";
        private static final String instanceTwoUriString = "http://ittruse.ittbg.com/ontology/enterpriseManagementFramework#instance-two-id";
        private static final String instanceThreeUriString = "http://ittruse.ittbg.com/ontology/enterpriseManagementFramework#instance-tree-id";
        private static final String VERSION_ONE_NULL = "1.0";
        private static final String VERSION_TWO_NULL = "2.0";
        private static final String VERSION_TWO_ONE = "2.1";
        private static final String VERSION_TWO_TEN = "2.10";
        private static final String VERSION_TREE_NULL = "3.0";

        private int index = 0;
        private List<BindingSet> bindings = new LinkedList<>();

        private TupleQueryResultTest() {
            bindings.add(createEmfVersionBindingSet(instanceOneUriString, VERSION_TWO_ONE));
            bindings.add(createEmfVersionBindingSet(instanceOneUriString, VERSION_TWO_TEN));
            bindings.add(createEmfVersionBindingSet(instanceOneUriString, VERSION_TREE_NULL));
            bindings.add(createEmfVersionBindingSet(instanceTwoUriString, VERSION_TREE_NULL));
            bindings.add(createEmfVersionBindingSet(instanceTwoUriString, VERSION_TWO_NULL));
            bindings.add(createEmfVersionBindingSet(instanceThreeUriString, VERSION_ONE_NULL));
        }

        @Override
        public List<String> getBindingNames() throws QueryEvaluationException {
            return null;
        }

        @Override
        public void close() throws QueryEvaluationException {
        }

        @Override
        public boolean hasNext() throws QueryEvaluationException {
            return index < bindings.size();
        }

        @Override
        public BindingSet next() throws QueryEvaluationException {
            return bindings.get(index++);
        }

        @Override
        public void remove() throws QueryEvaluationException {
        }

        private BindingSet createEmfVersionBindingSet(String instanceUriString, String versionLabel) {
            MapBindingSet bindingSet = new MapBindingSet();
            bindingSet.addBinding(SPARQLQueryHelper.OBJECT, new URIImpl(instanceUriString));
            bindingSet.addBinding(EMF.VERSION.getLocalName(), new LiteralImpl(versionLabel));
            return bindingSet;
        }
    }
}