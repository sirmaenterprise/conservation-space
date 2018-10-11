package com.sirma.sep.definition.patches;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.testng.Assert.assertEquals;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.Spy;

import com.sirma.itt.seip.Entity;
import com.sirma.itt.seip.db.DbDao;
import com.sirma.itt.seip.definition.DefintionAdapterService;
import com.sirma.itt.seip.domain.definition.GenericDefinition;
import com.sirma.itt.seip.io.FileDescriptor;
import com.sirma.itt.seip.testutil.fakes.TransactionSupportFake;
import com.sirma.itt.seip.tx.TransactionSupport;
import com.sirma.sep.definition.db.DefinitionContent;

import liquibase.exception.CustomChangeException;

public class DefinitionContentMigrationPatchTest {

	@InjectMocks
	private DefinitionContentMigrationPatch definitionContentMigrationPatch;

	@Mock
	private DefintionAdapterService definitionAdapterService;

	@Mock
	private DbDao dbDao;

	@Spy
	private TransactionSupport transactionSupport = new TransactionSupportFake();

	@Test
	public void should_MigrateDefinitionContentFilesFromDMS() throws CustomChangeException {
		withDefinition("definition.xml");

		definitionContentMigrationPatch.execute(null);

		ArgumentCaptor<Entity<String>> captor = ArgumentCaptor.forClass(Entity.class);
		verify(dbDao, times(1)).saveOrUpdate(captor.capture());

		DefinitionContent content = (DefinitionContent) captor.getValue();
		assertEquals(content.getId(), "testDef");
		assertEquals(content.getFileName(), "definition.xml");
		assertEquals(content.getContent(), "<definition id=\"testDef\">\n\n</definition>");
	}

	@Test(expected=IllegalStateException.class)
	public void should_ReportError_When_ADefinitionFileCannotBeRead() throws CustomChangeException {
		FileDescriptor descriptor = mock(FileDescriptor.class);
		when(descriptor.getInputStream()).thenThrow(IOException.class);

		descriptors.add(descriptor);

		definitionContentMigrationPatch.execute(null);
	}

	@Test(expected=IllegalStateException.class)
	public void should_ReportError_When_ADefinitionFileDoesNotContainARealDefinition() throws CustomChangeException {
		withDefinition("invalid_definition.xml");

		definitionContentMigrationPatch.execute(null);
	}

	private void withDefinition(String fileName) {
		FileDescriptor descriptor = mock(FileDescriptor.class);
		when(descriptor.getFileName()).thenReturn(fileName);
		when(descriptor.getInputStream()).thenReturn(this.getClass().getResourceAsStream(fileName));

		descriptors.add(descriptor);
	}

	private List<FileDescriptor> descriptors = new ArrayList<>();

	@Before
	public void init() {
		MockitoAnnotations.initMocks(this);

		when(definitionAdapterService.getDefinitions(GenericDefinition.class)).thenReturn(descriptors);
	}

}
