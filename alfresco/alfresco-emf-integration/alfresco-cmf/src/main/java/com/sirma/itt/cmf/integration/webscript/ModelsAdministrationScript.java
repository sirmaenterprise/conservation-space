package com.sirma.itt.cmf.integration.webscript;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Collection;
import java.util.zip.ZipOutputStream;

import org.alfresco.repo.content.MimetypeMap;
import org.alfresco.repo.dictionary.DictionaryDAO;
import org.alfresco.service.cmr.dictionary.ModelDefinition;
import org.alfresco.service.namespace.QName;
import org.apache.commons.io.IOUtils;
import org.apache.tools.zip.ZipEntry;
import org.springframework.extensions.webscripts.AbstractWebScript;
import org.springframework.extensions.webscripts.WebScriptException;
import org.springframework.extensions.webscripts.WebScriptRequest;
import org.springframework.extensions.webscripts.WebScriptResponse;

/**
 * The ModelsAdministrationScript provides access to models currently in the
 * system
 */
public class ModelsAdministrationScript extends AbstractWebScript {

	/** The dictionary dao. */
	private DictionaryDAO dictionaryDAO;

	/*
	 * (non-Javadoc)
	 *
	 * @see org.springframework.extensions.webscripts.WebScript#execute(org.
	 * springframework.extensions.webscripts.WebScriptRequest,
	 * org.springframework.extensions.webscripts.WebScriptResponse)
	 */
	@Override
	public void execute(WebScriptRequest req, WebScriptResponse res) throws IOException {
		if (req.getServicePath().endsWith("dictionarymodel/download")) {
			ModelDefinition.XMLBindingType bindingType = ModelDefinition.XMLBindingType.DEFAULT;
			ZipOutputStream zipFileStream = new ZipOutputStream(res.getOutputStream());
			try {
				Collection<QName> models = getDictionaryDAO().getModels();
				for (QName qName : models) {
					ModelDefinition modelDefinition = getDictionaryDAO().getModel(qName);
					// make it correct fs name
					String name = qName.getPrefixString().replace(":", "_") + ".xml";
					ByteArrayOutputStream xml = new ByteArrayOutputStream();
					modelDefinition.toXML(bindingType, xml);
					zipFileStream.putNextEntry(new ZipEntry(name));
					zipFileStream.write(xml.toByteArray());
				}
				res.setContentType(MimetypeMap.MIMETYPE_ZIP);
				zipFileStream.finish();
			} catch (Exception e) {
				throw new WebScriptException(e.getMessage(), e);
			} finally {
				IOUtils.closeQuietly(zipFileStream);
			}
		}
	}

	/**
	 * Gets the dictionary dao.
	 *
	 * @return the dictionaryDAO
	 */
	public DictionaryDAO getDictionaryDAO() {
		return dictionaryDAO;
	}

	/**
	 * Sets the dictionary dao.
	 *
	 * @param dictionaryDAO
	 *            the dictionaryDAO to set
	 */
	public void setDictionaryDAO(DictionaryDAO dictionaryDAO) {
		this.dictionaryDAO = dictionaryDAO;
	}
}
