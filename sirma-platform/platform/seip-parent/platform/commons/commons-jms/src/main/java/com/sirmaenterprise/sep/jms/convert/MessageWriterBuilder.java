package com.sirmaenterprise.sep.jms.convert;

import com.sirma.itt.seip.plugin.Plugin;

/**
 * Defines a extension point that can provide different means of {@link MessageWriter} bean instantiation. <br>
 * The extension will be used to instantiate {@link MessageWriter} registred via one of the methods
 * {@link com.sirmaenterprise.sep.jms.api.SenderService#registerWriter(Class, Class)} or
 * {@link com.sirmaenterprise.sep.jms.api.MessageSender#registerWriter(Class, Class)}.
 *
 * @author <a href="mailto:borislav.bonev@sirma.bg">Borislav Bonev</a>
 * @since 23/05/2017
 */
public interface MessageWriterBuilder extends Plugin {

	/**
	 * Resolve and instantiate a {@link MessageWriter} instance from the given class. If instantiation is impossible
	 * the method should return null. Any runtime exceptions thrown will be ignored.
	 *
	 * @param writerClass the writer class to resolve and instantiate
	 * @return instantiated writer or null
	 */
	MessageWriter build(Class<? extends MessageWriter> writerClass);

}
