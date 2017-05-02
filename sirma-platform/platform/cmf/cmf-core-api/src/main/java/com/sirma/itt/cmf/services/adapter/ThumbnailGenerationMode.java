package com.sirma.itt.cmf.services.adapter;

/**
 * Enum for allowed types for thubmnail generation of documents
 *
 * @author bbanchev
 */
public enum ThumbnailGenerationMode {
	/** asynch after time. */
	ASYNCH, /** synch during upload. */
	SYNCH, /** no generation of thumbnail. */
	NONE;
}