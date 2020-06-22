package com.sirma.itt.seip.instance.version;

/**
 * Event fired, when new view content should be created for the version instance. Holds the context used to create the
 * version. Its holds required data for the content processing.
 *
 * @author A. Kunchev
 */
public class CreateVersionContentEvent extends AbstractVersionEvent {

	/**
	 * Instantiates a new create version content event.
	 *
	 * @param context
	 *            contains data required for version creation
	 */
	public CreateVersionContentEvent(VersionContext context) {
		super(context);
	}

}
