package com.sirma.sep.content;

import java.io.IOException;
import java.io.InputStream;
import java.io.Serializable;
import java.nio.charset.StandardCharsets;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

import com.sirma.itt.seip.context.Context;
import com.sirma.itt.seip.io.FileDescriptor;
import com.sirma.sep.content.descriptor.ByteArrayFileDescriptor;

/**
 * Context object used by the {@link InstanceViewPreProcessor}. Provides means for accessing the persisted view and
 * modify it.
 * <p>
 * Typical use of this class are:
 *
 * <pre>
 * <code>
 * FileDescriptor view = context.getView();
 * // do some work with it and if need to update the content
 * context.setView(newViewDescriptor);
 * </code>
 * </pre>
 *
 * or
 *
 * <pre>
 * <code>
 * Document view = context.getParsedView();
 * // do some work with it
 * // if document itself is modified then
 * context.setViewUpdated();
 * // or new document instance was created
 * context.updateView(newDocument);
 * </code>
 * </pre>
 *
 * @author BBonev
 */
public class ViewPreProcessorContext extends Context<String, Object> {

	private static final long serialVersionUID = -964799634604091019L;
	private static final String NEW_VIEW = "newView";
	private static final String OLD_VIEW = "oldView";
	private static final String OLD_VIEW_PARSED = "oldViewParsed";
	private static final String NEW_VIEW_PARSED = "newViewParsed";
	private static final String OWNER = "owner";
	private static final String VIEW_UPDATED = "viewUpdated";

	/**
	 * Instantiates a new view pre processor context for new view save.
	 *
	 * @param owner
	 *            the owner
	 * @param view
	 *            the view
	 */
	public ViewPreProcessorContext(Serializable owner, Content view) {
		this(owner, view, null);
	}

	/**
	 * Instantiates a new view pre processor context for view update.
	 *
	 * @param owner
	 *            the owner
	 * @param view
	 *            the view
	 * @param oldView
	 *            the old view
	 */
	public ViewPreProcessorContext(Serializable owner, Content view, FileDescriptor oldView) {
		put(OWNER, owner);
		put(NEW_VIEW, view);
		put(OLD_VIEW, oldView);
	}

	/**
	 * Checks if the view is present.
	 *
	 * @return true, if is new view present and non <code>null</code> and <code>false</code> otherwise
	 */
	public boolean isViewPresent() {
		return getIfSameType(NEW_VIEW, Content.class) != null;
	}

	/**
	 * Retrieves the {@link FileDescriptor} to the content that is being saved/updated. The method will return the
	 * latest version of the content even if changes were made over the parsed object returned from
	 * {@link #getParsedView()}.
	 * <p>
	 * Note that this method will do extra work if the view is updated and if the caller wants to check for presence of
	 * the view better call {@link #isViewPresent()}.
	 *
	 * @return the newView
	 * @see #isViewPresent()
	 */
	public Content getView() {
		flushViewChanges();
		return getIfSameType(NEW_VIEW, Content.class);
	}

	/**
	 * Sets the new view descriptor and clears the parsed view instance
	 *
	 * @param descriptor
	 *            the descriptor
	 * @return the current instance
	 */
	public ViewPreProcessorContext setView(FileDescriptor descriptor) {
		if (descriptor != null) {
			Content content = getView();
			if (content != null) {
				content.setContent(descriptor);
				remove(NEW_VIEW_PARSED);
			}
		}
		return this;
	}

	/**
	 * Gets the old instance view.
	 *
	 * @return the oldView
	 */
	public FileDescriptor getOldView() {
		return getIfSameType(OLD_VIEW, FileDescriptor.class);
	}

	/**
	 * Returns the instance view parsed as {@link Document}. The instance will be cached in the context. This value will
	 * automatically reset if the {@link #setView(FileDescriptor)} method is called with non <code>null</code>
	 * parameter.
	 *
	 * @return the instance view parsed as {@link Document} or <code>null</code> if no descriptor is set
	 */
	public Document getOldViewParsed() {
		FileDescriptor oldView = getOldView();
		if (oldView == null) {
			return null;
		}
		return (Document) computeIfAbsent(OLD_VIEW_PARSED, key -> parse(oldView));
	}

	/**
	 * Returns the instance old view parsed as {@link Document}. The instance will be cached in the context
	 *
	 * @return the instance old view parsed as {@link Document} or null if no descriptor is set
	 */
	public Document getParsedView() {
		Content content = getIfSameType(NEW_VIEW, Content.class);
		if (content == null) {
			return null;
		}
		FileDescriptor view = content.getContent();
		if (view == null) {
			return null;
		}
		return (Document) computeIfAbsent(NEW_VIEW_PARSED, key -> parse(view));
	}

	/**
	 * Update new view instance and mark the context that view update happened.
	 *
	 * @param document
	 *            the document
	 * @return the current instance
	 * @see #setViewUpdated()
	 */
	public ViewPreProcessorContext updateView(Document document) {
		if (document != null) {
			put(NEW_VIEW_PARSED, document);
			setViewUpdated();
		}
		return this;
	}

	/**
	 * Notifies the context that the parsed view has been changed and changes should be made permanent. This method
	 * should be called when the changes to the model happen to the parsed view directly. Without calling this method
	 * the changes may not be flushed and persisted.
	 *
	 * @return the current instance
	 */
	public ViewPreProcessorContext setViewUpdated() {
		put(VIEW_UPDATED, Boolean.TRUE);
		return this;
	}

	/**
	 * Checks if is view updated.
	 *
	 * @return true, if is view updated
	 */
	boolean isViewUpdated() {
		return getIfSameType(VIEW_UPDATED, Boolean.class, Boolean.FALSE).booleanValue();
	}

	/**
	 * If the parsed view has been updated and the {@link FileDescriptor} has been requested we will flush changes to
	 * the descriptor and remove the update flag. This way multiple updates to the view could happen without constantly
	 * converting the view and then parse again.
	 */
	private void flushViewChanges() {
		if (isViewUpdated()) {
			Document parsed = getParsedView();
			Content content = getIfSameType(NEW_VIEW, Content.class);
			FileDescriptor descriptor = content.getContent();
			byte[] bytes = parsed.toString().getBytes(StandardCharsets.UTF_8);
			content
					.setContent(new ByteArrayFileDescriptor(descriptor.getId(), descriptor.getContainerId(), bytes))
						.setContentLength(Long.valueOf(bytes.length));
			remove(VIEW_UPDATED);
		}
	}

	/**
	 * @return the owning instance
	 */
	public Serializable getOwner() {
		return getIfSameType(OWNER, Serializable.class);
	}

	private static Document parse(FileDescriptor descriptor) {
		if (descriptor == null) {
			return null;
		}
		try (InputStream stream = descriptor.getInputStream()) {
			return Jsoup.parse(stream, StandardCharsets.UTF_8.name(), "");
		} catch (IOException e) {
			throw new ContentValidationException("Invalid view content. Cannot be parsed as html", e);
		}
	}

}
