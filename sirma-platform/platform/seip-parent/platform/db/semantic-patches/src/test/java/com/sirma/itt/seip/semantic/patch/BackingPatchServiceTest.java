package com.sirma.itt.seip.semantic.patch;

import static java.util.stream.Collectors.toList;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyCollection;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyZeroInteractions;
import static org.mockito.Mockito.when;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.util.stream.Stream;
import java.util.zip.ZipInputStream;

import org.apache.commons.io.FileUtils;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import com.sirma.itt.emf.semantic.patch.SemanticPatchService;
import com.sirma.itt.seip.definition.SemanticDefinitionService;
import com.sirma.itt.seip.io.TempFileProvider;
import com.sirma.itt.seip.patch.exception.PatchFailureException;
import com.sirma.itt.seip.patch.service.PatchSubsytemAdapterService;

/**
 * Test for {@link BackingPatchService}.
 *
 * @author A. Kunchev
 */
@RunWith(MockitoJUnitRunner.class)
public class BackingPatchServiceTest {

	@InjectMocks
	private BackingPatchService service;

	@Mock
	private TempFileProvider tempFileProvider;

	@Mock
	private SemanticPatchService patchService;

	@Mock
	private PatchSubsytemAdapterService patchAdapter;

	@Mock
	private SemanticDefinitionService semanticDefinitionService;

	private static File systemTempDir = new File("src/test/resources/systemTempDir");
	private static File tempDir = new File("src/test/resources/tempDir");

	@BeforeClass
	public static void setup() {
		systemTempDir.mkdir();
		tempDir.mkdir();
	}

	@AfterClass
	public static void clean() {
		FileUtils.deleteQuietly(systemTempDir);
		FileUtils.deleteQuietly(tempDir);
	}

	@Before
	public void beforeTest() {
		when(tempFileProvider.getSystemTempDir()).thenReturn(systemTempDir);
		when(tempFileProvider.createTempDir(anyString())).thenAnswer(a -> {
			File dir = new File(tempDir.getPath(), a.getArgumentAt(0, String.class));
			dir.mkdir();
			return dir;
		});
	}

	@Test
	public void runPatchAndBackup_nullTenantId_nameWithZipExtension() throws Exception {
		try (ZipInputStream zipStream = new ZipInputStream(new ByteArrayInputStream("testStream".getBytes()))) {
			service.runPatchAndBackup(zipStream, "name.zip", null);
		} catch (Exception e) {
			assertTrue(e instanceof PatchFailureException);
		} finally {
			verifyZeroInteractions(patchService, patchService);
		}
	}

	@Test
	public void runPatchAndBackup_systemTenantId_nameWithoutZipExtension() throws Exception {
		try (ZipInputStream zipStream = new ZipInputStream(new ByteArrayInputStream("testStream".getBytes()))) {
			service.runPatchAndBackup(zipStream, "name", "system.tenant");
		} catch (Exception e) {
			assertTrue(e instanceof PatchFailureException);
		} finally {
			verifyZeroInteractions(patchService, patchService);
		}
	}

	@Test
	public void runPatchAndBackup_blankZipName() throws Exception {
		try (ZipInputStream zipStream = new ZipInputStream(new ByteArrayInputStream("testStream".getBytes()))) {
			service.runPatchAndBackup(zipStream, "", "test.tenant");
			assertEquals(2, Stream
					.of(tempDir.listFiles())
						.filter(File::isDirectory)
						.flatMap(dir -> Stream.of(dir.list()))
						.filter(name -> name.endsWith(".zip") || name.endsWith("-changelog.xml"))
						.collect(toList())
						.size());

			verify(patchService).runPatches(anyCollection());
			verify(patchAdapter).backupPatch(any(), anyString());
		}
	}

	@Test
	public void runPatchAndBackup_directoryName() throws Exception {
		File patchesDir = new File(systemTempDir, "SDB_Patches_directoty");
		patchesDir.mkdir();
		File.createTempFile("semantic", "-changelog.xml", patchesDir);
		try (ZipInputStream zipStream = new ZipInputStream(new ByteArrayInputStream("testStream".getBytes()))) {
			service.runPatchAndBackup(zipStream, "SDB_Patches_directoty", "test.tenant");
			verify(patchService).runPatches(anyCollection());
			verify(patchAdapter).backupPatch(any(), anyString());
		} finally {
			FileUtils.deleteQuietly(patchesDir);
		}
	}

	@Test
	public void runPatchAndBackupPatchDir() throws Exception {
		File patchesDir = new File("src/test/resources/SDB_Patches");
		try {
			patchesDir.mkdir();
			File.createTempFile("sparql-patch", ".sparql", patchesDir);

			service.runPatchAndBackup(patchesDir, "test.tenant");

			verify(patchService).runPatches(anyCollection());
			verify(patchAdapter).backupPatch(any(), anyString());
			verify(semanticDefinitionService).modelUpdated();
		} finally {
			FileUtils.deleteQuietly(patchesDir);
			// delete generated zip file
			FileUtils.deleteQuietly(new File("src/test/resources/SDB_Patches.zip"));
		}
	}
}