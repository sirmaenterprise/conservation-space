package com.sirma.itt.emf.semantic.persistence;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.Serializable;

import org.eclipse.rdf4j.model.Model;
import org.eclipse.rdf4j.model.Statement;
import org.eclipse.rdf4j.model.ValueFactory;
import org.eclipse.rdf4j.model.impl.SimpleValueFactory;
import org.eclipse.rdf4j.model.vocabulary.RDF;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import com.sirma.itt.seip.convert.DefaultTypeConverter;
import com.sirma.itt.seip.convert.TypeConverter;
import com.sirma.itt.seip.convert.TypeConverterImpl;
import com.sirma.itt.semantic.NamespaceRegistryService;
import com.sirma.itt.semantic.model.vocabulary.EMF;

/**
 * Tests for {@link LocalStatement}
 *
 * @author BBonev
 */
public class LocalStatementTest {

	private ValueFactory valueFactory = SimpleValueFactory.getInstance();
	@Mock
	private NamespaceRegistryService namespaceRegistryService;

	@Before
	public void beforeMethod() {
		MockitoAnnotations.initMocks(this);
		TypeConverter typeConverter = new TypeConverterImpl();
		new DefaultTypeConverter().register(typeConverter);
		new ValueConverter().register(typeConverter);
	}

	@Test
	public void equalsShouldReturnTrue_onSameReference() throws Exception {
		LocalStatement statement = LocalStatement.toAdd(null);
		assertTrue(statement.equals(statement));
	}

	@Test
	public void equalsShouldReturnFalse_onNonLocalStatement() throws Exception {
		assertFalse(LocalStatement.toAdd(null).equals(null));
		assertFalse(LocalStatement.toAdd(null).equals(new Object()));
	}

	@Test
	public void equalsShouldReturnTrue_onSameIsForAdd_AndSameStatement() throws Exception {
		LocalStatement localStatement1 = LocalStatement.toAdd(getStatement(EMF.CASE, RDF.TYPE, EMF.CLASS_DESCRIPTION));
		LocalStatement localStatement2 = LocalStatement.toAdd(getStatement(EMF.CASE, RDF.TYPE, EMF.CLASS_DESCRIPTION));
		assertTrue(localStatement1.equals(localStatement2));

		localStatement1 = LocalStatement.toRemove(getStatement(EMF.CASE, RDF.TYPE, EMF.CLASS_DESCRIPTION));
		localStatement2 = LocalStatement.toRemove(getStatement(EMF.CASE, RDF.TYPE, EMF.CLASS_DESCRIPTION));
		assertTrue(localStatement1.equals(localStatement2));

		localStatement1 = LocalStatement.toRemove(null);
		localStatement2 = LocalStatement.toRemove(null);
		assertTrue(localStatement1.equals(localStatement2));
	}

	@Test
	public void equalsShouldReturnFalse_onDifferentIsForAdd_AndSameStatement() throws Exception {
		LocalStatement localStatement1 = LocalStatement.toAdd(getStatement(EMF.CASE, RDF.TYPE, EMF.CLASS_DESCRIPTION));
		LocalStatement localStatement2 = LocalStatement
				.toRemove(getStatement(EMF.CASE, RDF.TYPE, EMF.CLASS_DESCRIPTION));
		assertFalse(localStatement1.equals(localStatement2));

		localStatement1 = LocalStatement.toRemove(getStatement(EMF.CASE, RDF.TYPE, EMF.CLASS_DESCRIPTION));
		localStatement2 = LocalStatement.toAdd(getStatement(EMF.CASE, RDF.TYPE, EMF.CLASS_DESCRIPTION));
		assertFalse(localStatement1.equals(localStatement2));
	}

	@Test
	public void equalsShouldReturnFalse_onSameIsForAdd_AndDifferentStatement() throws Exception {
		LocalStatement localStatement1 = LocalStatement.toAdd(getStatement(EMF.CASE, RDF.TYPE, EMF.SAVED_SEARCH));
		LocalStatement localStatement2 = LocalStatement
				.toRemove(getStatement(EMF.CASE, RDF.TYPE, EMF.CLASS_DESCRIPTION));
		assertFalse(localStatement1.equals(localStatement2));

		localStatement1 = LocalStatement.toRemove(getStatement(EMF.CASE, RDF.TYPE, EMF.CLASS_DESCRIPTION));
		localStatement2 = LocalStatement.toAdd(getStatement(EMF.CASE, RDF.TYPE, EMF.SAVED_SEARCH));
		assertFalse(localStatement1.equals(localStatement2));
	}

	@Test
	public void hashShouldBeSame_onSameIsForAdd_AndSameStatement() throws Exception {
		LocalStatement localStatement1 = LocalStatement.toAdd(getStatement(EMF.CASE, RDF.TYPE, EMF.CLASS_DESCRIPTION));
		LocalStatement localStatement2 = LocalStatement.toAdd(getStatement(EMF.CASE, RDF.TYPE, EMF.CLASS_DESCRIPTION));
		assertEquals(localStatement1.hashCode(), localStatement2.hashCode());

		localStatement1 = LocalStatement.toRemove(getStatement(EMF.CASE, RDF.TYPE, EMF.CLASS_DESCRIPTION));
		localStatement2 = LocalStatement.toRemove(getStatement(EMF.CASE, RDF.TYPE, EMF.CLASS_DESCRIPTION));
		assertEquals(localStatement1.hashCode(), localStatement2.hashCode());
	}

	@Test
	public void hashShouldBeDifferent_onDifferentIsForAdd_AndSameStatement() throws Exception {
		LocalStatement localStatement1 = LocalStatement.toAdd(getStatement(EMF.CASE, RDF.TYPE, EMF.CLASS_DESCRIPTION));
		LocalStatement localStatement2 = LocalStatement
				.toRemove(getStatement(EMF.CASE, RDF.TYPE, EMF.CLASS_DESCRIPTION));
		assertNotEquals(localStatement1.hashCode(), localStatement2.hashCode());

		localStatement1 = LocalStatement.toRemove(getStatement(EMF.CASE, RDF.TYPE, EMF.CLASS_DESCRIPTION));
		localStatement2 = LocalStatement.toAdd(getStatement(EMF.CASE, RDF.TYPE, EMF.CLASS_DESCRIPTION));
		assertNotEquals(localStatement1.hashCode(), localStatement2.hashCode());
	}

	@Test
	public void hashShouldBeDifferent_onSameIsForAdd_AndDifferentStatement() throws Exception {
		LocalStatement localStatement1 = LocalStatement.toAdd(getStatement(EMF.CASE, RDF.TYPE, EMF.SAVED_SEARCH));
		LocalStatement localStatement2 = LocalStatement
				.toRemove(getStatement(EMF.CASE, RDF.TYPE, EMF.CLASS_DESCRIPTION));
		assertNotEquals(localStatement1.hashCode(), localStatement2.hashCode());

		localStatement1 = LocalStatement.toRemove(getStatement(EMF.CASE, RDF.TYPE, EMF.CLASS_DESCRIPTION));
		localStatement2 = LocalStatement.toAdd(getStatement(EMF.CASE, RDF.TYPE, EMF.SAVED_SEARCH));
		assertNotEquals(localStatement1.hashCode(), localStatement2.hashCode());
	}

	@Test
	public void copyConstructor_ShouldCopy() throws Exception {
		LocalStatement localStatement = LocalStatement.toAdd(getStatement(EMF.CASE, RDF.TYPE, EMF.SAVED_SEARCH));
		LocalStatement copy = new LocalStatement(localStatement);

		assertTrue(localStatement.equals(copy));
	}

	@Test
	public void isSame_ShouldIgnoreToAddFlag() throws Exception {
		LocalStatement localStatement1 = LocalStatement.toAdd(getStatement(EMF.CASE, RDF.TYPE, EMF.CLASS_DESCRIPTION));
		LocalStatement localStatement2 = LocalStatement
				.toRemove(getStatement(EMF.CASE, RDF.TYPE, EMF.CLASS_DESCRIPTION));

		assertTrue(localStatement1.isSame(localStatement2));

		localStatement1 = LocalStatement.toAdd(null);
		localStatement2 = LocalStatement.toRemove(null);

		assertTrue(localStatement1.isSame(localStatement2));

		assertFalse(localStatement1.isSame(null));

		localStatement1 = LocalStatement.toAdd(getStatement(EMF.CASE, RDF.TYPE, EMF.CLASS_DESCRIPTION));
		localStatement2 = LocalStatement.toRemove(getStatement(EMF.CASE, RDF.TYPE, EMF.SAVED_SEARCH));
		assertFalse(localStatement1.isSame(localStatement2));
	}

	@Test
	public void addToShouldNotAddAddStatementToRemoveModel() throws Exception {
		LocalStatement localStatement = LocalStatement.toAdd(getStatement(EMF.CASE, RDF.TYPE, EMF.CLASS_DESCRIPTION));
		Model addModel = mockModel();
		Model removeModel = mockModel();

		assertTrue(localStatement.addTo(addModel, removeModel));
		verify(addModel).add(any());
		verify(removeModel, never()).add(any());
	}

	@Test
	public void addToShouldNotAddRemoveStatementToAddModel() throws Exception {
		LocalStatement localStatement = LocalStatement
				.toRemove(getStatement(EMF.CASE, RDF.TYPE, EMF.CLASS_DESCRIPTION));
		Model addModel = mockModel();
		Model removeModel = mockModel();

		assertTrue(localStatement.addTo(addModel, removeModel));
		verify(addModel, never()).add(any());
		verify(removeModel).add(any());
	}

	@Test
	public void addToShouldIgnoreNullActualStatement() throws Exception {
		LocalStatement localStatement = LocalStatement.toAdd(null);
		Model addModel = mockModel();
		Model removeModel = mockModel();

		assertFalse(localStatement.addTo(addModel, removeModel));
		verify(addModel, never()).add(any());
		verify(removeModel, never()).add(any());
	}

	@Test
	public void shouldReturnFalseOnNotPresentStatement() throws Exception {
		assertFalse(LocalStatement.toAdd(null).hasStatement());
		assertFalse(LocalStatement.toRemove(null).hasStatement());
	}

	@Test
	public void shouldReturnTrueOnPresentStatement() throws Exception {
		assertTrue(LocalStatement.toAdd(mock(Statement.class)).hasStatement());
		assertTrue(LocalStatement.toRemove(mock(Statement.class)).hasStatement());
	}

	private static Model mockModel() {
		Model model = mock(Model.class);
		when(model.add(any())).thenReturn(Boolean.TRUE);
		return model;
	}

	private Statement getStatement(Object subject, Object predicate, Serializable value) {
		return SemanticPersistenceHelper.createLiteralStatement(subject, predicate, value, namespaceRegistryService,
				valueFactory);
	}
}
