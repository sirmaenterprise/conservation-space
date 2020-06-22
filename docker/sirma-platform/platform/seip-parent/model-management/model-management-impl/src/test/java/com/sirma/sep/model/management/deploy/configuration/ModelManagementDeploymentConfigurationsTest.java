package com.sirma.sep.model.management.deploy.configuration;

import static org.junit.Assert.*;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyBoolean;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Optional;

import org.eclipse.rdf4j.model.IRI;
import org.eclipse.rdf4j.model.Literal;
import org.eclipse.rdf4j.model.Statement;
import org.eclipse.rdf4j.model.Value;
import org.eclipse.rdf4j.model.impl.SimpleValueFactory;
import org.eclipse.rdf4j.repository.RepositoryConnection;
import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.Spy;

import com.sirma.itt.seip.configuration.convert.ConverterContext;
import com.sirma.itt.seip.testutil.fakes.TransactionSupportFake;
import com.sirma.itt.seip.tx.TransactionSupport;

/**
 * @author <a href="mailto:borislav.bonev@sirma.bg">Borislav Bonev</a>
 * @since 20/03/2019
 */
public class ModelManagementDeploymentConfigurationsTest {

	private static final String TEST_ONTOLOGY = "http://ittruse.ittbg.com/model/enterpriseManagementFramework";

	@Spy
	private TransactionSupport transactionSupport = new TransactionSupportFake();
	@Mock
	private RepositoryConnection repositoryConnection;
	@Mock
	private ConverterContext converterContext;

	@Before
	public void init() {
		MockitoAnnotations.initMocks(this);
	}

	@Test
	public void registerOntology() throws Exception {
		when(converterContext.getRawValue()).thenReturn(TEST_ONTOLOGY);
		when(repositoryConnection.hasStatement(any(), any(), any(), anyBoolean())).thenReturn(Boolean.FALSE);

		IRI iri = ModelManagementDeploymentConfigurationsImpl.registerOntology(converterContext, transactionSupport,
				repositoryConnection);

		assertEquals(SimpleValueFactory.getInstance().createIRI(TEST_ONTOLOGY), iri);
		ArgumentCaptor<Statement> statements = ArgumentCaptor.forClass(Statement.class);
		verify(repositoryConnection, times(2)).add(statements.capture(), eq(iri));
		Optional<Value> title = statements.getAllValues()
				.stream()
				.map(Statement::getObject)
				.filter(Literal.class::isInstance)
				.findFirst();
		assertTrue(title.isPresent());
		assertEquals("Model Enterprise Management Framework", title.get().stringValue());
	}

}