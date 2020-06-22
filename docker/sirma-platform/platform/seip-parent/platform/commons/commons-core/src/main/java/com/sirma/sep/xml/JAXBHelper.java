package com.sirma.sep.xml;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.StringWriter;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.Collections;
import java.util.List;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sirma.itt.seip.Message;
import com.sirma.itt.seip.MessageType;

/**
 * Helper class for working with JaxB library
 *
 * @author BBonev
 */
public class JAXBHelper {

	private static final Logger LOGGER = LoggerFactory.getLogger(JAXBHelper.class);

	private JAXBHelper() {
	}

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
			List<String> errors = XmlValidator.resolveErrors(xml, xmlType);

			if (errors.isEmpty()) {
				return true;
			}
			StringBuilder msg = new StringBuilder();
			StringBuilder err = new StringBuilder();
			String lineEnd = System.getProperty("line.separator");

			msg
					.append(lineEnd)
						.append("=======================================================================")
						.append(lineEnd);
			msg.append("Found the following errors in file [").append(file.getName()).append("]");

			for (String xmlError : errors) {
				err.append(lineEnd).append(xmlError);
				messages.add(new Message(MessageType.ERROR,
						"Error found in file '" + file.getName() + "' " + xmlError));
			}
			msg.append(err);

			msg.append(lineEnd).append("=======================================================================");
			LOGGER.error("{}", msg);
		} catch (XmlValidatorError e) {
			String message = "Error validating XML type " + xmlType;
			LOGGER.error(message, e);
		} catch (IOException e) {
			String message = "Failed to read the sourse XML file";
			LOGGER.error(message, e);
		}
		return false;
	}

	/**
	 * Validates xml file against XSD
	 *
	 * @param file path to the file to validate
	 * @param schemaProider provider of the schema used for validation
	 * @return list of validation errors
	 */
	public static List<String> validateFile(Path file, XmlSchemaProvider schemaProider) {
		try (InputStream fileStream = Files.newInputStream(file, StandardOpenOption.READ)) {
			List<String> errors = XmlValidator.resolveErrors(fileStream, schemaProider);

			if (LOGGER.isDebugEnabled() && !errors.isEmpty()) {
				LOGGER.debug("Found the following errors in file {}:", file.getFileName().toAbsolutePath());
				errors.forEach(LOGGER::debug);
			}

			return errors;
		} catch (IOException e) {
			LOGGER.error("Failed to process " + file.getFileName().toAbsolutePath(), e);
			return Collections.emptyList();
		}
	}

	/**
	 * Converts a file from a given path to a java object
	 *
	 * @param file path to the file that will be converted
	 * @param objectType type of the object
	 * @return converted object
	 */
	public static <S> S load(Path file, Class<S> objectType) {
		try {
			InputStream fileStream = Files.newInputStream(file, StandardOpenOption.READ);
			String fileName = file.getFileName().toString();

			return load(fileName, fileStream, objectType);
		} catch (IOException e) {
			throw new IllegalStateException(e);
		}
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
	public static <S> S load(File file, Class<S> src) {
		try {
			InputStream fileStream = new FileInputStream(file);
			String fileName = file.getName();

			return load(fileName, fileStream, src);
		} catch (IOException e) {
			throw new IllegalStateException(e);
		}
	}

	@SuppressWarnings("unchecked")
	private static <S> S load(String fileName, InputStream fileStream, Class<S> src) throws IOException {
		try (InputStreamReader streamReader = new InputStreamReader(fileStream, StandardCharsets.UTF_8)) {
			JAXBContext context = JAXBContext.newInstance(src);
			Unmarshaller um = context.createUnmarshaller();
			return (S) um.unmarshal(streamReader);
		} catch (JAXBException e) {
			LOGGER.error("Error while converting file {} to {}", fileName, src, e);
			throw new XmlValidatorError("Error while converting file " + fileName + " to " + src, e);
		}
	}

	/**
	 * Converts java object to xml file.
	 *
	 * @param object object to convert
	 * @return object as xml
	 */
	public static <S> String toXml(S object) {
		try {
			StringWriter writer = new StringWriter();
			JAXBContext context = JAXBContext.newInstance(object.getClass());
			Marshaller m = context.createMarshaller();
			m.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, Boolean.TRUE);
			m.marshal(object, writer);
			return writer.toString();
		} catch (JAXBException e) {
			throw new IllegalArgumentException("Cannot convert object to XML file", e);
		}
	}
}
