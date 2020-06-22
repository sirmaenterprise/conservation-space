package com.sirma.itt.seip.patch.service;

import java.io.File;

import com.sirma.itt.emf.adapter.DMSException;

/**
 * Adapter service for patches in DMS.
 *
 * @author bbanchev
 */
public interface PatchSubsytemAdapterService {

	/**
	 * Backup single patch in DMS with the given name.
	 *
	 * @param zipFile
	 *            the zip file - the file containing the patch
	 * @param name
	 *            the name of patch. If not provided uses the current timestamp
	 * @return true, if successful
	 * @throws DMSException
	 *             on dms related error
	 */
	boolean backupPatch(File zipFile, String name) throws DMSException;
}
