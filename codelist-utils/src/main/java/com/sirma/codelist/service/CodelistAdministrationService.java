package com.sirma.codelist.service;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import javax.xml.namespace.QName;
import javax.xml.ws.WebServiceException;

import com.sirma.codelist.exception.CodelistServiceException;
import com.sirma.codelist.ws.stub.Codelist;
import com.sirma.codelist.ws.stub.CodelistsPortType;
import com.sirma.codelist.ws.stub.Codelists_Service;
import com.sirma.codelist.ws.stub.DataServiceFault;

/**
 * Delegate class for communicating with
 * the back-end data service
 * 
 * @author Valeri.Tishev
 *
 */
public class CodelistAdministrationService {

	/** The url of the data service wsdl */
	private URL wsdlUrl;
	
	/** The qualified name of the data service */
	private QName qName;
	
	/**
	 * Instantiates a new codelist administration service.
	 *
	 * @param wsdlUrlLocation the wsdl url location
	 * @param namespaceURI the namespace uri
	 * @param localPart the local part
	 */
	public CodelistAdministrationService(String wsdlUrlLocation, String namespaceURI, String localPart) {
		try {
			wsdlUrl = new URL(wsdlUrlLocation);
			qName = new QName(namespaceURI, localPart);
		} catch (MalformedURLException e) {
			throw new CodelistServiceException(e);
		}
	}
	
	/**
	 * Gets all the code lists returned from the back-end data service 
	 *
	 * @return the code lists
	 */
	public List<Codelist> getCodelists() {
		List<Codelist> codelists = new ArrayList<Codelist>();;

		try {
			CodelistsPortType port = getPort();
			
			if (port.getcodelists() == null) {
				throw new CodelistServiceException("Failed getting codelists from service");
			}
			
			codelists = port.getcodelists().getCodelist();
			
		} catch (DataServiceFault e) {
			throw new CodelistServiceException(e);
		} 
		
		return codelists;
	}
	
	/**
	 * Gets a code list by given id
	 *
	 * @param id the identifier of the code list
	 * @return the code list
	 */
	public List<Codelist> getCodelistById(Integer id) {
		List<Codelist> codelist = new ArrayList<Codelist>();
		
		try {
			CodelistsPortType port = getPort();
			codelist = port.getcodelistCl(id.toString());
		} catch (DataServiceFault e) {
			throw new CodelistServiceException(e);
		}
		
		return codelist;
	}

	/**
	 * Gets the port to the web service
	 *
	 * @return the port
	 */
	private CodelistsPortType getPort() {
		CodelistsPortType port;
		
		try {
			Codelists_Service service = new Codelists_Service(wsdlUrl, qName);
			port = service.getSOAP11Endpoint();
		} catch (WebServiceException e) {
			throw new CodelistServiceException(e);
		}
		
		return port;
	}

}
