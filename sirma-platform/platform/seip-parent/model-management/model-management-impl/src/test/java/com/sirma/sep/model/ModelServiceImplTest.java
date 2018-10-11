package com.sirma.sep.model;

import static org.junit.Assert.assertEquals;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.when;

import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

import org.eclipse.rdf4j.model.Value;
import org.eclipse.rdf4j.query.BindingSet;
import org.eclipse.rdf4j.query.MalformedQueryException;
import org.eclipse.rdf4j.query.QueryEvaluationException;
import org.eclipse.rdf4j.query.QueryLanguage;
import org.eclipse.rdf4j.query.TupleQuery;
import org.eclipse.rdf4j.query.TupleQueryResult;
import org.eclipse.rdf4j.repository.RepositoryConnection;
import org.eclipse.rdf4j.repository.RepositoryException;
import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.mockito.Spy;

import com.sirma.itt.emf.semantic.queries.SPARQLQueryHelper;
import com.sirma.itt.seip.definition.SemanticDefinitionService;
import com.sirma.itt.seip.domain.instance.ClassInstance;
import com.sirma.itt.seip.monitor.Statistics;
import com.sirma.itt.seip.testutil.mocks.InstanceContextServiceMock;
import com.sirma.itt.seip.testutil.mocks.InstanceReferenceMock;
import com.sirma.itt.seip.time.TimeTracker;

/**
 * Tests the functionality of {@link ModelServiceImpl}.
 * 
 * @author Vilizar Tsonev
 */
public class ModelServiceImplTest {

	@InjectMocks
	private ModelServiceImpl modelService;

	@Mock
	private RepositoryConnection repositoryConnection;

	@Mock
	private SemanticDefinitionService semanticDefinitionService;

	@Mock
	private Statistics statistics;
	
	@Spy
	private InstanceContextServiceMock contextService;
	
	@Before
	public void init() throws RepositoryException, MalformedQueryException, QueryEvaluationException {
		MockitoAnnotations.initMocks(this);
		when(statistics.createTimeStatistics(Mockito.any(), Mockito.anyString()))
				.thenReturn(TimeTracker.createAndStart());
	}

	/**
	 * Tests {@link ModelService#getOntologies()}. Verifies that the attributes of the found ontologies are properly
	 * collected and converted to {@link Ontology}.
	 */
	@Test
	public void testGetOntologies() throws RepositoryException, MalformedQueryException, QueryEvaluationException {
		mockReturnedOntologies(false);

		Ontology emfOntology = new Ontology("http://ittruse.ittbg.com/ontology/enterpriseManagementFramework",
				"Enterprise Management Framework Ontology");
		Ontology chdOntology = new Ontology("http://www.sirma.com/ontologies/2013/10/culturalHeritageDomain",
				"http://www.sirma.com/ontologies/2013/10/culturalHeritageDomain");

		List<Ontology> expectedOntologies = Arrays.asList(emfOntology, chdOntology);
		List<Ontology> actualOntologies = modelService.getOntologies();

		assertEquals(expectedOntologies, actualOntologies);
	}

	/**
	 * Verifies that empty list is returned in case no ontologies have been found.
	 */
	@Test
	public void testGetOntologiesEmptyResult()
			throws RepositoryException, MalformedQueryException, QueryEvaluationException {
		mockReturnedOntologies(true);
		List<Ontology> expectedOntologies = Collections.emptyList();
		List<Ontology> actualOntologies = modelService.getOntologies();

		assertEquals(expectedOntologies, actualOntologies);
	}

	/**
	 * Tests {@link ModelService#getClassesForOntology(String)}. Verifies that the attributes of the returned clases are
	 * properly collected and converted to {@link ClassInfo}.
	 */
	@Test
	public void testGetClassesForOntology()
			throws RepositoryException, MalformedQueryException, QueryEvaluationException {
		mockReturnedClassesForOntology();

		ClassInfo markType = new ClassInfo()
				.setId("http://www.sirma.com/ontologies/2013/10/culturalHeritageDomain#MarkType")
					.setLabel("Mark Type")
					.setOntology("http://www.sirma.com/ontologies/2013/10/culturalHeritageDomain")
					.setSuperClasses(Arrays.asList("http://www.w3.org/2004/02/skos/core#Concept"));
		ClassInfo culturalActivity = new ClassInfo()
				.setId("http://www.sirma.com/ontologies/2013/10/culturalHeritageDomain#CulturalActivity")
					.setLabel("Cultural Activity")
					.setOntology("http://www.sirma.com/ontologies/2013/10/culturalHeritageDomain")
					.setSuperClasses(
							Arrays.asList("http://ittruse.ittbg.com/ontology/enterpriseManagementFramework#Activity"));
		ClassInfo concept = new ClassInfo()
				.setId("http://www.w3.org/2004/02/skos/core#Concept")
					.setLabel("Concept")
					.setOntology("http://www.w3.org/2004/02/skos/core");
		ClassInfo activity = new ClassInfo()
				.setId("http://ittruse.ittbg.com/ontology/enterpriseManagementFramework#Activity")
					.setLabel("Activity")
					.setOntology("http://ittruse.ittbg.com/ontology/enterpriseManagementFramework");

		List<ClassInfo> expectedClasses = Arrays.asList(markType, culturalActivity, concept, activity);

		List<ClassInfo> actualClasses = modelService
				.getClassesForOntology("http://www.sirma.com/ontologies/2013/10/culturalHeritageDomain");

		assertEquals(expectedClasses, actualClasses);
	}

	private void mockReturnedClassesForOntology() {
		// mock skos:Concept
		ClassInstance concept = Mockito.mock(ClassInstance.class);
		when(concept.getId()).thenReturn("http://www.w3.org/2004/02/skos/core#Concept");
		when(concept.getLabel()).thenReturn("Concept");
		when(concept.getString(eq("ontology"))).thenReturn("http://www.w3.org/2004/02/skos/core");

		// mock chd:MarkType
		ClassInstance markType = Mockito.mock(ClassInstance.class);
		when(markType.getId()).thenReturn("http://www.sirma.com/ontologies/2013/10/culturalHeritageDomain#MarkType");
		when(markType.getLabel()).thenReturn("Mark Type");
		when(markType.getString(eq("ontology")))
				.thenReturn("http://www.sirma.com/ontologies/2013/10/culturalHeritageDomain");
		contextService.bindContext(markType, InstanceReferenceMock.createGeneric(concept));
		when(markType.getSuperClasses()).thenReturn(Collections.singletonList(concept));
		// mock emf:Activity
		ClassInstance activity = Mockito.mock(ClassInstance.class);
		when(activity.getId()).thenReturn("http://ittruse.ittbg.com/ontology/enterpriseManagementFramework#Activity");
		when(activity.getLabel()).thenReturn("Activity");
		when(activity.getString(eq("ontology")))
				.thenReturn("http://ittruse.ittbg.com/ontology/enterpriseManagementFramework");

		// mock chd:CulturalActivity
		ClassInstance culturalActivity = Mockito.mock(ClassInstance.class);
		when(culturalActivity.getId())
				.thenReturn("http://www.sirma.com/ontologies/2013/10/culturalHeritageDomain#CulturalActivity");
		when(culturalActivity.getLabel()).thenReturn("Cultural Activity");
		when(culturalActivity.getString(eq("ontology")))
				.thenReturn("http://www.sirma.com/ontologies/2013/10/culturalHeritageDomain");
		contextService.bindContext(culturalActivity, InstanceReferenceMock.createGeneric(activity));
		when(culturalActivity.getSuperClasses()).thenReturn(Collections.singletonList(activity));

		List<ClassInstance> classesToReturn = new LinkedList<>();
		classesToReturn.add(markType);
		classesToReturn.add(culturalActivity);

		when(semanticDefinitionService
				.getClassesForOntology(eq("http://www.sirma.com/ontologies/2013/10/culturalHeritageDomain")))
						.thenReturn(classesToReturn);
	}

	private void mockReturnedOntologies(boolean returnEmptyResult)
			throws RepositoryException, MalformedQueryException, QueryEvaluationException {
		// Mock the RepositoryConnection
		TupleQuery query = Mockito.mock(TupleQuery.class);
		when(repositoryConnection.prepareTupleQuery(Mockito.any(QueryLanguage.class), Mockito.anyString()))
				.thenReturn(query);

		// Mock the TupleQueryResult that will be returned after query evaluation
		TupleQueryResult result = Mockito.mock(TupleQueryResult.class);
		if (returnEmptyResult) {
			when(result.hasNext()).thenReturn(Boolean.FALSE);
		} else {
			// Mock the EMF ontology row
			BindingSet emfRow = Mockito.mock(BindingSet.class);
			Value emfIdValue = Mockito.mock(Value.class);
			Value emfTitleValue = Mockito.mock(Value.class);
			when(emfIdValue.stringValue())
					.thenReturn("http://ittruse.ittbg.com/ontology/enterpriseManagementFramework");
			when(emfTitleValue.stringValue()).thenReturn("Enterprise Management Framework Ontology");
			when(emfRow.getValue(Mockito.eq(SPARQLQueryHelper.OBJECT))).thenReturn(emfIdValue);
			when(emfRow.getValue(Mockito.eq("title"))).thenReturn(emfTitleValue);

			// Mock a second EMF ontology (with just a different label) to ensure that duplications get skipped
			BindingSet emfSecondRow = Mockito.mock(BindingSet.class);
			Value emfSecondIdValue = Mockito.mock(Value.class);
			Value emfSecondTitleValue = Mockito.mock(Value.class);
			when(emfSecondIdValue.stringValue())
					.thenReturn("http://ittruse.ittbg.com/ontology/enterpriseManagementFramework");
			when(emfSecondTitleValue.stringValue()).thenReturn("Enterprise Management Framework Ontology Duplicate");
			when(emfSecondRow.getValue(Mockito.eq(SPARQLQueryHelper.OBJECT))).thenReturn(emfSecondIdValue);
			when(emfSecondRow.getValue(Mockito.eq("title"))).thenReturn(emfSecondTitleValue);

			// Mock the CHD ontology row (don't set it a title in order to test the second logic branch)
			BindingSet chdRow = Mockito.mock(BindingSet.class);
			Value chdIdValue = Mockito.mock(Value.class);
			when(chdIdValue.stringValue()).thenReturn("http://www.sirma.com/ontologies/2013/10/culturalHeritageDomain");
			when(chdRow.getValue(Mockito.eq(SPARQLQueryHelper.OBJECT))).thenReturn(chdIdValue);

			// Mock thye TupleQueryResult that will be returned after query evaluation
			when(result.hasNext()).thenReturn(Boolean.TRUE, Boolean.TRUE, Boolean.TRUE, Boolean.TRUE, Boolean.FALSE);
			when(result.next()).thenReturn(emfRow, emfSecondRow, chdRow);
		}

		when(query.evaluate()).thenReturn(result);
	}

}
