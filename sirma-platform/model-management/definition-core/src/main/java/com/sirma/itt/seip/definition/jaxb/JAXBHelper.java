package com.sirma.itt.seip.definition.jaxb;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.util.List;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sirma.itt.seip.Message;
import com.sirma.itt.seip.MessageType;
import com.sirma.itt.seip.definition.util.ValidationLoggingUtil;
import com.sirma.itt.seip.definition.xml.XmlError;
import com.sirma.itt.seip.definition.xml.XmlValidator;
import com.sirma.itt.seip.domain.exceptions.DefinitionValidationException;
import com.sirma.itt.seip.domain.xml.XmlSchemaProvider;
import com.sirma.itt.seip.domain.xml.XmlValidatorError;

/**
 * Helper class for working with JaxB library
 *
 * @author BBonev
 */
public class JAXBHelper {
	private static final Logger LOGGER = LoggerFactory.getLogger(JAXBHelper.class);

	/**
	 * Validate definition.
	 *
	 * @param file
	 *            the file
	 * @param xmlType
	 *            the xml type
	 * @param messages
	 *            the messages
	 * @return true, if successful
	 */
	public static boolean validateFile(File file, XmlSchemaProvider xmlType, List<Message> messages) {
		try (FileInputStream xml = new FileInputStream(file)) {
			List<XmlError> errors = XmlValidator.resolveErrors(xml, xmlType);
			if (errors.isEmpty()) {
				return true;
			}
			StringBuilder msg = new StringBuilder();
			StringBuilder err = new StringBuilder();
			msg.append("\n=======================================================================\n");
			err.append("\tFound errors while validating XML type ").append(xmlType);
			for (XmlError xmlError : errors) {
				err.append("\n").append(xmlError.getFormattedMessage());
			}
			msg.append(err);
			msg.append("\n=======================================================================");
			ValidationLoggingUtil.addMessage(MessageType.ERROR, err.toString(), messages);
			LOGGER.error(msg.toString());
		} catch (XmlValidatorError e) {
			String message = "Error validating XML type " + xmlType;
			LOGGER.error(message, e);
			ValidationLoggingUtil.addMessage(MessageType.ERROR, message, messages);
		} catch (FileNotFoundException e) {
			String message = "Failed to read the sourse XML file";
			LOGGER.error(message, e);
			ValidationLoggingUtil.addMessage(MessageType.ERROR, message, messages);
		} catch (IOException e) {
			String message = "Failed to read the sourse XML file";
			LOGGER.error(message, e);
			ValidationLoggingUtil.addMessage(MessageType.ERROR, message, messages);
		}
		return false;
	}

	/**
	 * The content of the given file and convert the content using JAXB unmarshaller.
	 *
	 * @param <S>
	 *            the expected type
	 * @param file
	 *            the file to load
	 * @param src
	 *            the class type to convert to.
	 * @return the created instance or <code>null</code> if failed to read the file.
	 */
	@SuppressWarnings("unchecked")
	public static <S> S load(File file, Class<S> src) {
		JAXBContext context;
		try (InputStreamReader streamReader = new InputStreamReader(new FileInputStream(file), "UTF-8")) {
			context = JAXBContext.newInstance(src);
			Unmarshaller um = context.createUnmarshaller();
			return (S) um.unmarshal(streamReader);
		} catch (JAXBException e) {
			LOGGER.error("Error while converting file {} to {}", file.getAbsolutePath(), src, e);
			throw new DefinitionValidationException(
					"Error while converting file " + file.getAbsolutePath() + " to " + src, e);
		} catch (FileNotFoundException e) {
			LOGGER.warn("File not found " + file.getAbsolutePath(), e);
		} catch (UnsupportedEncodingException e) {
			LOGGER.warn("Problem with encoding", e);
		} catch (IOException e) {
			LOGGER.warn("Problem with encoding", e);
		}
		return null;
	}
}
