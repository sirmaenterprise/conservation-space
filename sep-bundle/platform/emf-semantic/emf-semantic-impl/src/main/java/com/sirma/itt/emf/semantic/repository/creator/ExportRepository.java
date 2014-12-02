package com.sirma.itt.emf.semantic.repository.creator;

import java.io.FileOutputStream;

import org.openrdf.model.Statement;
import org.openrdf.repository.RepositoryConnection;
import org.openrdf.repository.RepositoryResult;
import org.openrdf.repository.http.HTTPRepository;
import org.openrdf.rio.RDFFormat;
import org.openrdf.rio.RDFWriter;
import org.openrdf.rio.Rio;

import com.sirma.itt.emf.exceptions.EmfRuntimeException;

/**
 * Export semantic repository to a local file
 *
 * @author kirq4e
 */
public class ExportRepository {

	/**
	 * Export repository data to file
	 *
	 * @param args
	 *            main arguments
	 */
	public static void main(String[] args) {
		try {
			HTTPRepository repository = new HTTPRepository(
					"http://31.13.228.149:8080/owlim-workbench", "cs-perf");
			repository.setUsernameAndPassword("admin", "root");
			repository.initialize();

			RepositoryConnection repositoryConnection = repository.getConnection();

			FileOutputStream out = new FileOutputStream("a:/Development/export.trig");

			RDFWriter writer = Rio.createWriter(RDFFormat.TRIG, out);
			writer.startRDF();
			RepositoryResult<Statement> statements = repositoryConnection.getStatements(null, null,
					null, false);
			while (statements.hasNext()) {
				writer.handleStatement(statements.next());
			}
			statements.close();
			writer.endRDF();
			out.flush();

			out.close();

		} catch (Exception e) {
			throw new EmfRuntimeException(e);
		}
	}

}
