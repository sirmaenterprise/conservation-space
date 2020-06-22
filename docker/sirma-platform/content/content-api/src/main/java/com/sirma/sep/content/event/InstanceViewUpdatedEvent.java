/**
 *
 */
package com.sirma.sep.content.event;

import java.io.IOException;
import java.io.InputStream;
import java.io.Serializable;
import java.nio.charset.StandardCharsets;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

import com.sirma.sep.content.Content;
import com.sirma.sep.content.ContentInfo;
import com.sirma.sep.content.ContentValidationException;

/**
 * Event fired to notify for new version of an instance view. The event provides access to parsed instance of the view.
 * Note that changes to this view will not be reflected to the actual content. To modify the view before save add an
 * extension implementation of {@link com.sirma.sep.content.InstanceViewPreProcessor}.
 *
 * @author BBonev
 */
public class InstanceViewUpdatedEvent extends InstanceViewEvent {

	private final ContentInfo oldView;
	private Document parsed;

	/**
	 * Instantiates a new instance view updated event.
	 *
	 * @param owner
	 *            the owner
	 * @param newView
	 *            the new view
	 * @param oldView
	 *            the old view
	 */
	public InstanceViewUpdatedEvent(Serializable owner, Content newView, ContentInfo oldView) {
		super(owner, newView);
		this.oldView = oldView;
	}

	/**
	 * Gets the old view of the instance before this view save.
	 *
	 * @return the old view
	 */
	public ContentInfo getOldView() {
		return oldView;
	}

	/**
	 * Gets parsed instance view of the old content
	 *
	 * @return the Jsoup {@link Document} instance containing the view
	 */
	public synchronized Document getOldViewParsed() {
		if (parsed == null) {
			try (InputStream stream = getOldView().getInputStream()) {
				parsed = Jsoup.parse(stream, StandardCharsets.UTF_8.name(), "");
			} catch (IOException e) {
				throw new ContentValidationException("Could not read the old view content", e);
			}
		}
		return parsed;
	}
}
