package com.sirma.itt.emf.jaxb;

import java.io.StringReader;
import java.io.StringWriter;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlRootElement;

import org.eclipse.persistence.oxm.annotations.XmlCDATA;
import org.testng.Assert;
import org.testng.annotations.Test;

import com.sirma.itt.seip.testutil.EmfTest;

/**
 * The Class CdataTest.
 *
 * @author BBonev
 */
@Test
public class CdataTest extends EmfTest {

	/** The xml. */
	String XML = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>" + "<root>" + "<name><![CDATA[<h1>kshitij</h1>]]></name>"
			+ "<surname><![CDATA[<h1>solanki</h1>]]></surname>" + "<id><![CDATA[0]]></id>" + "</root>";

	/**
	 * Test template cdata.
	 *
	 * @throws JAXBException
	 *             the JAXB exception
	 */
	public void testTemplateCdata() throws JAXBException {
		JAXBContext jc = JAXBContext.newInstance(Root.class);

		Unmarshaller unmarshaller = jc.createUnmarshaller();
		Root root = (Root) unmarshaller.unmarshal(new StringReader(XML));

		Marshaller marshaller = jc.createMarshaller();
		marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true);
		StringWriter writer = new StringWriter();
		marshaller.marshal(root, writer);
		Assert.assertTrue(writer.toString().contains("CDATA"));
	}

	/**
	 * The Class Root.
	 */
	@XmlRootElement
	@XmlAccessorType(XmlAccessType.FIELD)
	public static class Root {

		/** The name. */
		@XmlCDATA
		private String name;

		/** The surname. */
		@XmlCDATA
		private String surname;

		/** The id. */
		@XmlCDATA
		private String id;

	}
}
