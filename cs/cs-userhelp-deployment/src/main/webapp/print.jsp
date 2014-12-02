<%@page import="java.util.*, java.io.*" %>
<%@page import="java.io.BufferedReader,
java.io.File,
java.io.FileOutputStream,
java.io.OutputStream,
com.lowagie.text.pdf.PdfAction,
com.lowagie.text.pdf.PdfReader,
com.lowagie.text.pdf.PdfStamper,
com.sirma.itt.emf.help.util.HtmlToPdfConverter,
com.lowagie.text.pdf.PdfWriter;" %>

<%
	response.setContentType("application/pdf");  
	response.setHeader("Expires", "0");
	response.setHeader("Cache-Control", "must-revalidate, post-check=0, pre-check=0");
	
	
	
	
	String shema = pageContext.getRequest().getScheme();
	int serverPort = pageContext.getRequest().getServerPort();
	String serverName = pageContext.getRequest().getServerName();
	String contextName = request.getContextPath();	
	String navigationState = shema + "://" + serverName + ":" + serverPort + contextName + "/Pages/";
	
		String urlName = navigationState + request.getParameter("fileName");
		
		File tempPdf = File.createTempFile("pdf", null);	
		HtmlToPdfConverter converter = new HtmlToPdfConverter();
		converter.convert(urlName, tempPdf);
		
		PdfReader reader = new PdfReader(new FileInputStream(tempPdf)); 
	    PdfStamper stamper = new PdfStamper(reader, new FileOutputStream(tempPdf)); 
	    
	    
	    
	    //stamper.setPageAction(PdfWriter.PAGE_OPEN, new PdfAction(PdfAction.PRINTDIALOG), 1);
	    
	    //after opening pdf file, it open print dialog. After printing close document
	    StringBuilder script = new StringBuilder(); 
	    script.append("var pp = getPrintParams();"); 
	    script.append("pp.interactive = pp.constants.interactionLevel.full;"); 
	    script.append("print(pp);"); 
	    script.append("this.closeDoc(true);");

	    
	    PdfWriter writer = stamper.getWriter(); 
	    writer.addJavaScript(script.toString());
	    
	    stamper.close(); 
		
						
			FileInputStream pdfInputStream = new FileInputStream(tempPdf); 
			BufferedInputStream buf = new BufferedInputStream(pdfInputStream); 

			int readBytes = 0; 
			while ((readBytes = buf.read()) != -1) { 
				out.write(readBytes); 
			} 
			pdfInputStream.close();
			response.flushBuffer();
			out.flush(); 
		%>	
