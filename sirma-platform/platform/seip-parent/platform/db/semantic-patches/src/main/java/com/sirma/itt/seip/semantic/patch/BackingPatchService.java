package com.sirma.itt.seip.semantic.patch;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.invoke.MethodHandles;
import java.nio.file.Files;
import java.util.Collections;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sirma.itt.emf.adapter.DMSException;
import com.sirma.itt.emf.semantic.patch.RuntimeSemanticModelPatch;
import com.sirma.itt.emf.semantic.patch.SemanticPatchService;
import com.sirma.itt.seip.exception.RollbackedException;
import com.sirma.itt.seip.io.TempFileProvider;
import com.sirma.itt.seip.patch.exception.PatchFailureException;
import com.sirma.itt.seip.patch.service.PatchSubsytemAdapterService;
import com.sirma.itt.seip.security.context.SecurityContext;
import com.sirma.itt.seip.util.file.ArchiveUtil;

/**
 * Patch service wrapper that also backup the patches.
 *
 * @author bbanchev
 */
@ApplicationScoped
public class BackingPatchService {

	private static final Logger LOGGER = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

	@Inject
	private TempFileProvider tempFileProvider;
	@Inject
	private SemanticPatchService patchService;
	@Inject
	private PatchSubsytemAdapterService patchAdapter;

	/**
	 * Run patch from stream and backup the executed patch on success.
	 *
	 * @param zipStream
	 *            the zip stream
	 * @param name
	 *            the name to create patch with
	 * @param tenantId
	 *            the tenant id
	 * @throws PatchFailureException
	 *             the patch failure exception on prepare
	 */
	public void runPatchAndBackup(InputStream zipStream, String name, String tenantId) throws PatchFailureException {
		File zipFile = null;
		try {
			String zipName = name == null ? Long.toString(System.currentTimeMillis()) + ".zip" : name;
			// create the store directory
			File systemTempDir = tempFileProvider.getSystemTempDir();
			File directory = new File(systemTempDir, "RuntimePatchSubsystem");
			if (!directory.mkdirs()) {
				LOGGER.warn("Faile to create directories or already existed {}", directory.getAbsolutePath());
			}
			zipFile = new File(directory, zipName);
			try (OutputStream output = new FileOutputStream(zipFile)) {
				IOUtils.copy(zipStream, output);
			}
			patchAndBackupInternal(zipFile, tenantId);
		} catch (PatchFailureException e) {
			throw e;
		} catch (Exception e) {
			throw new PatchFailureException("Failed to extract patch!", e);
		} finally {
			if (zipFile != null) {
				try {
					Files.deleteIfExists(zipFile.toPath());
				} catch (IOException e) {
					LOGGER.warn("Failed to delete file {}", zipFile.getAbsolutePath(), e);
				}
			}
		}
	}

	/**
	 * Compress the provided patch location directory, run the patch and backup the executed patch on success.
	 *
	 * @param patchLocation
	 *            the patch directory location
	 * @param tenantId
	 *            the tenant id
	 * @throws PatchFailureException
	 *             the patch failure exception
	 */
	public void runPatchAndBackup(File patchLocation, String tenantId) throws PatchFailureException {
		File zipFile = new File(patchLocation.getAbsolutePath() + ".zip");
		ArchiveUtil.zipFile(patchLocation, zipFile);
		patchAndBackupInternal(zipFile, tenantId);
	}

	/**
	 * Collect patch from location.
	 *
	 * @param patchLocation
	 *            the patches location - directory or zip file
	 * @return the runtime semantic model patch or null if location is not valid directory or zip file
	 * @throws PatchFailureException
	 *             on failure to construct runtime patch
	 */
	private RuntimeSemanticModelPatch collectRuntimePatch(File patchLocation, String tenantId)
			throws PatchFailureException {
		if (patchLocation.isDirectory()) {
			return new RuntimeSemanticModelPatch(patchLocation);
		} else if (patchLocation.isFile()) {
			File outputDir = createPatchDirectory(patchLocation.getName(), tenantId);
			try {
				String contentType = Files.probeContentType(patchLocation.toPath());
				if (StringUtils.isNotBlank(contentType) && contentType.contains("zip")) {
					// if the file is zip archive - extract it to the output directory
					ArchiveUtil.unZip(patchLocation, outputDir);
				} else {
					// if the file is not an archive - save the file in the output directory
					FileUtils.copyFileToDirectory(patchLocation, outputDir);
				}
			} catch (IOException e) {
				throw new PatchFailureException(e);
			}
			return new RuntimeSemanticModelPatch(outputDir);
		}
		return null;
	}

	private File createPatchDirectory(String patchName, String tenantId) {
		// provide hint if available what patch is executed
		String prefix = patchName == null ? Long.toString(System.currentTimeMillis()) : patchName;
		return tempFileProvider.createTempDir("SemanticDBPatch_" + tenantId + "_" + prefix);
	}

	private void patchAndBackupInternal(File patchLocation, String tenantId) throws PatchFailureException {
		if (patchLocation == null) {
			LOGGER.warn("Provided location is not a valid patch file: null");
			return;
		}
		if (tenantId == null || SecurityContext.isSystemTenant(tenantId)) {
			throw new PatchFailureException("Patch should be executed in tenant context. Provided: " + tenantId);
		}
		RuntimeSemanticModelPatch collectRuntimePatch = collectRuntimePatch(patchLocation, tenantId);
		try {
			patchService.runPatches(Collections.singletonList(collectRuntimePatch));
			patchAdapter.backupPatch(patchLocation, tenantId + "_" + patchLocation.getName());
		} catch (RollbackedException e) {
			throw new PatchFailureException("Failed to execute patch!", e);
		} catch (DMSException e) {
			throw new PatchFailureException("Failed to backup executed patch!", e);
		} catch (Exception e) {
			throw new PatchFailureException(e);
		}
	}
}
