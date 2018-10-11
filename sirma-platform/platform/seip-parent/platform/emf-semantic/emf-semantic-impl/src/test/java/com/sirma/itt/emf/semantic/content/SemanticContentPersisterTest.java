package com.sirma.itt.emf.semantic.content;

import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.eclipse.rdf4j.model.IRI;
import org.eclipse.rdf4j.model.Value;
import org.eclipse.rdf4j.model.ValueFactory;
import org.eclipse.rdf4j.model.impl.SimpleValueFactory;
import org.eclipse.rdf4j.repository.RepositoryConnection;
import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Matchers;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.Spy;

import com.sirma.itt.semantic.NamespaceRegistryService;
import com.sirma.itt.semantic.model.vocabulary.EMF;

/**
 * Test for {@link SemanticContentPersister}
 *
 * @author BBonev
 */
public class SemanticContentPersisterTest {

	@InjectMocks
	private SemanticContentPersister contentPersister;

	@Mock
	private RepositoryConnection repositoryConnection;
	@Mock
	private NamespaceRegistryService namespaceRegistryService;
	@Spy
	private ValueFactory valueFactory = SimpleValueFactory.getInstance();

	@Before
	public void beforeMethod() throws Exception {
		MockitoAnnotations.initMocks(this);

		when(namespaceRegistryService.getDataGraph()).thenReturn(EMF.DATA_CONTEXT);
		when(namespaceRegistryService.buildUri(anyString()))
				.then(a -> valueFactory.createIRI(a.getArgumentAt(0, String.class)));
	}

	@Test
	public void addPrimaryContent() throws Exception {
		contentPersister.savePrimaryContent("emf:instanceId", "textToSave");

		IRI instanceIri = valueFactory.createIRI("emf:instanceId");
		Value literal = valueFactory.createLiteral("textToSave");
		verify(repositoryConnection).add(eq(instanceIri), eq(EMF.CONTENT), eq(literal), eq(EMF.DATA_CONTEXT));
		verify(repositoryConnection).remove(eq(instanceIri), eq(EMF.CONTENT), Matchers.isNull(Value.class));
	}

	@Test
	public void addPrimaryContent_noContent() throws Exception {
		contentPersister.savePrimaryContent("emf:instanceId", null);
		contentPersister.savePrimaryContent("emf:instanceId", "   ");
		contentPersister.savePrimaryContent("emf:instanceId", "  \n\n  \t  ");

		IRI instanceIri = valueFactory.createIRI("emf:instanceId");
		verify(repositoryConnection, times(3)).remove(eq(instanceIri), eq(EMF.CONTENT), Matchers.isNull(Value.class));
	}

	@Test
	public void addPrimaryView() throws Exception {
		contentPersister.savePrimaryView("emf:instanceId", "textToSave");

		IRI instanceIri = valueFactory.createIRI("emf:instanceId");
		Value literal = valueFactory.createLiteral("textToSave");
		verify(repositoryConnection).add(eq(instanceIri), eq(EMF.VIEW), eq(literal), eq(EMF.DATA_CONTEXT));
		verify(repositoryConnection).remove(eq(instanceIri), eq(EMF.VIEW), Matchers.isNull(Value.class));

	}

	@Test
	public void saveWidgetsContentTest() throws Exception {
		contentPersister.saveWidgetsContent("emf:instanceId", "widgetTextToSave");

		IRI instanceIri = valueFactory.createIRI("emf:instanceId");
		Value literal = valueFactory.createLiteral("widgetTextToSave");
		verify(repositoryConnection).add(eq(instanceIri), eq(EMF.VIEW_WIDGETS_CONTENT), eq(literal),
				eq(EMF.DATA_CONTEXT));
		verify(repositoryConnection).remove(eq(instanceIri), eq(EMF.VIEW_WIDGETS_CONTENT),
				Matchers.isNull(Value.class));

	}

	@Test
	public void saveOcrContentTest() throws Exception {
		contentPersister.saveOcrContent("emf:instanceId", "widgetTextToSave");

		IRI instanceIri = valueFactory.createIRI("emf:instanceId");
		Value literal = valueFactory.createLiteral("widgetTextToSave");
		verify(repositoryConnection).add(eq(instanceIri), eq(EMF.OCR_CONTENT), eq(literal),
				eq(EMF.DATA_CONTEXT));
		verify(repositoryConnection).remove(eq(instanceIri), eq(EMF.OCR_CONTENT),
				Matchers.isNull(Value.class));

	}

	@Test(expected = NullPointerException.class)
	public void shouldThrowNPEOnNullInstanceId() throws Exception {
		contentPersister.savePrimaryView(null, "textToSave");
	}
}
