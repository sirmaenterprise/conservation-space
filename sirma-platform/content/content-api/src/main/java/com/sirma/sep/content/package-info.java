/**
 * Classes in this module handle instance content. This includes content upload retrieve, delete, text extraction,
 * thumbnail generation and sanitization. The api supports multiple contents to be assigned to a single instance.
 * <p>
 * One of the major performance improvement for content upload is the asynchronous content upload while still allowing
 * content accessing. This is achieved via local temporary storage used to provide immediate access to the file if
 * needed for processing.
 * <p>
 * Content retrieve happens by first checking if the file is still present locally and so return it otherwise access the
 * remote service. Content is retrieved using {@link com.sirma.sep.content.ContentStore} plugin instance that can
 * handle the proper {@link com.sirma.sep.content.StoreItemInfo}
 * <p>
 * Save instance content config:
 * <ul>
 * <li>create version or not
 * <li>system that should receive the content
 * <li>extract content
 * <li>sanitize - probably not configurable
 * <li>thumbnail
 * </ul>
 *
 * @author BBonev
 */
package com.sirma.sep.content;