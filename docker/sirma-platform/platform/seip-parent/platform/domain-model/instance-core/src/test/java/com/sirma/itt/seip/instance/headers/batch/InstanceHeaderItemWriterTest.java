package com.sirma.itt.seip.instance.headers.batch;

import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Arrays;

import org.eclipse.rdf4j.model.IRI;
import org.eclipse.rdf4j.model.Resource;
import org.eclipse.rdf4j.model.ValueFactory;
import org.eclipse.rdf4j.model.impl.SimpleValueFactory;
import org.eclipse.rdf4j.repository.RepositoryConnection;
import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.Spy;

import com.sirma.itt.seip.instance.dao.InstanceService;
import com.sirma.itt.semantic.NamespaceRegistryService;
import com.sirma.itt.semantic.model.vocabulary.EMF;

/**
 * Test for {@link InstanceHeaderItemWriter}
 *
 * @author <a href="mailto:borislav.bonev@sirma.bg">Borislav Bonev</a>
 * @since 27/11/2017
 */
public class InstanceHeaderItemWriterTest {

	@InjectMocks
	private InstanceHeaderItemWriter writer;

	@Mock
	private RepositoryConnection repositoryConnection;

	@Mock
	private NamespaceRegistryService namespaceRegistryService;

	@Spy
	private ValueFactory valueFactory = SimpleValueFactory.getInstance();

	@Mock
	private InstanceService instanceService;

	@Before
	public void setUp() throws Exception {
		MockitoAnnotations.initMocks(this);
		when(namespaceRegistryService.getDataGraph()).thenReturn(EMF.DATA_CONTEXT);
		when(namespaceRegistryService.buildUri(anyString())).then(a -> valueFactory.createIRI(a.getArgumentAt(0, String.class)));
	}

	@Test
	public void writeItems() throws Exception {
		GeneratedHeaderData item1 = new GeneratedHeaderData("emf:instance1", "newValue1");
		GeneratedHeaderData item2 = new GeneratedHeaderData("emf:instance3", "newValue2");

		writer.writeItems(Arrays.asList(item1, item2));

		verify(repositoryConnection).add(any(Iterable.class), any());
		verify(repositoryConnection, times(2)).remove(any(Resource.class), any(IRI.class), eq(null));
		verify(instanceService).touchInstance(Arrays.asList("emf:instance1", "emf:instance3"));
	}

}
