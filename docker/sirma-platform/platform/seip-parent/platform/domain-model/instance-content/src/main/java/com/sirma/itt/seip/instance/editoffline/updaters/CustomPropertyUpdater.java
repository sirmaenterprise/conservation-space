package com.sirma.itt.seip.instance.editoffline.updaters;

import java.io.File;
import java.io.Serializable;

import com.sirma.itt.seip.plugin.Plugin;

/**
 * Interface for all custom properties of uploaded content updaters.
 *
 * @author T. Dossev
 */
public interface CustomPropertyUpdater extends Plugin {

	/**
	 * Checks if current implementation can update <code>mimeType</code>.
	 *
	 * @param mimeType
	 *            mimeType of the requested instance
	 * @return true if can
	 */
	public boolean canUpdate(String mimeType);

	/**
	 * Adds custom properties to a copy of the requested instance and returns it.
	 *
	 * @param instanceId
	 *            id of the instance containing uploaded file
	 * @return file with custom properties set
	 */
	public File update(Serializable instanceId);
}
