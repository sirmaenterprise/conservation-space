package com.sirma.sep.model;

import java.io.IOException;
import java.io.OutputStream;
import java.io.Writer;
import java.text.Collator;
import java.util.Comparator;
import java.util.Optional;
import java.util.Set;
import java.util.TreeSet;

import com.sirma.itt.semantic.model.vocabulary.EMF;
import org.eclipse.rdf4j.model.BNode;
import org.eclipse.rdf4j.model.IRI;
import org.eclipse.rdf4j.model.Literal;
import org.eclipse.rdf4j.model.Model;
import org.eclipse.rdf4j.model.Resource;
import org.eclipse.rdf4j.model.Statement;
import org.eclipse.rdf4j.model.Value;
import org.eclipse.rdf4j.model.impl.LinkedHashModel;
import org.eclipse.rdf4j.model.util.Literals;
import org.eclipse.rdf4j.model.vocabulary.OWL;
import org.eclipse.rdf4j.model.vocabulary.RDF;
import org.eclipse.rdf4j.model.vocabulary.RDFS;
import org.eclipse.rdf4j.model.vocabulary.SKOS;
import org.eclipse.rdf4j.model.vocabulary.XMLSchema;
import org.eclipse.rdf4j.rio.RDFHandlerException;
import org.eclipse.rdf4j.rio.helpers.BasicWriterSettings;
import org.eclipse.rdf4j.rio.turtle.TurtleUtil;
import org.eclipse.rdf4j.rio.turtle.TurtleWriter;

/**
 * Custom turtle writer with improved pretty print.
 * The changes are: <ul>
 * <li>added comment line with the not shortened subject before subject statements</li>
 * <li>different values for same predicate are now written to new lines instead on a single line with one more indentation</li>
 * <li>added comments in the form of header, which explicitly shows </li>
 * </ul>
 *
 * @author <a href="mailto:borislav.bonev@sirma.bg">Borislav Bonev</a>
 * @author Radoslav Dimitrov
 * @since 26/02/2019
 */
public class SepTurtleWriter extends TurtleWriter {

	private static final String COMMENT_LINE = "#################################################################";
	private static final String COMMENT_START = "#";

	protected boolean indentationIncreasedOnSamePredicate;

	public SepTurtleWriter(OutputStream out) {
		super(out);
	}

	public SepTurtleWriter(Writer writer) {
		super(writer);
	}

	@Override
	public void startRDF() {
		if (isPrettyPrint()) {
			writer.setIndentationString("\t\t");
		} else {
			writer.setIndentationString("\t");
		}
		super.startRDF();
	}

	@Override
	public void endRDF() {
		if (!writingStarted) {
			throw new RDFHandlerException("Document writing has not yet started");
		}
		try {
			if (prettyPrintModel != null) {
				writePrettyPrintModel();
			}
			closePreviousStatement();
			writer.flush();
		} catch (IOException e) {
			throw new RDFHandlerException(e);
		} finally {
			writingStarted = false;
		}
	}

	private void writePrettyPrintModel() throws IOException {
		for (Resource nextContext : prettyPrintModel.contexts()) {

			Set<Resource> classes = getModelByIRI(OWL.CLASS, nextContext);
			Set<Resource> objectProperties = getModelByIRI(OWL.OBJECTPROPERTY, nextContext);
			Set<Resource> dataProperties = getModelByIRI(OWL.DATATYPEPROPERTY, nextContext);
			Set<Resource> primitives = getModelByIRI(EMF.PRIMITIVE, nextContext);
			Set<Resource> concepts = getModelByIRI(SKOS.CONCEPT, nextContext);
			Set<Resource> ontology = getModelByIRI(OWL.ONTOLOGY, nextContext);
			Set<Resource> conceptScheme = getModelByIRI(SKOS.CONCEPT_SCHEME, nextContext);
			Set<Resource> subClasses = prettyPrintModel.filter(null, RDFS.SUBCLASSOF, null, nextContext).subjects();
			Set<? extends Resource> labels = getOntologyLabels(nextContext);

			Model allOther = new LinkedHashModel(prettyPrintModel);

			// clean the set of remaining models, which will be printed at the end as annotation properties
			cleanAnnotationModelSet(allOther, concepts, nextContext);
			cleanAnnotationModelSet(allOther, conceptScheme, nextContext);
			cleanAnnotationModelSet(allOther, classes, nextContext);
			cleanAnnotationModelSet(allOther, subClasses, nextContext);
			cleanAnnotationModelSet(allOther, objectProperties, nextContext);
			cleanAnnotationModelSet(allOther, dataProperties, nextContext);
			cleanAnnotationModelSet(allOther, primitives, nextContext);
			cleanAnnotationModelSet(allOther, ontology, nextContext);
			cleanAnnotationModelSet(allOther, labels, nextContext);

			ontology.forEach(labels::remove);

			// Merge concept and concept scheme models into one set
			Set<Resource> allConcepts = mergeModels(concepts, conceptScheme);

			// Merge classes and subClassOf models into one set
			Set<Resource> allClasses = mergeModels(classes, subClasses);

			printModelByType(nextContext, ontology, "");
			printModelByType(nextContext, labels, "Ontology labels");
			printModelByType(nextContext, primitives, "Data primitives");
			printModelByType(nextContext, objectProperties, "Object Properties");
			printModelByType(nextContext, dataProperties, "Data Properties");
			printModelByType(nextContext, allClasses, "Classes");
			printModelByType(nextContext, allConcepts, "Concepts");
			printModelByType(nextContext, allOther.subjects(), "Annotation properties");
		}
	}

	private Set<? extends Resource> getOntologyLabels(Resource nextContext) {
		Set<Resource> finalLabels = new TreeSet<>(getIriComparator());

		Model labels = prettyPrintModel.filter(null, RDFS.LABEL, null, nextContext);

		labels.subjects().forEach(iri -> {
			if (!(iri instanceof IRI)) {
				// no need to process blank nodes here
				return;
			}
			int splitIdx = TurtleUtil.findURISplitIndex(iri.toString());
			if (splitIdx > 0) {
				String namespace = iri.toString().substring(0, splitIdx);
				String prefix = namespaceTable.get(namespace);
				if (prefix == null) {
					finalLabels.add(iri);
				}
			}
		});
		return finalLabels;
	}

	private void printModelByType(Resource nextContext, Set<? extends Resource> resources, String header) throws IOException {
		if (resources.isEmpty()) {
			// nothing to do
			return;
		}
		writeHeaderComment(header);

		// sort the resources by locale name
		Set<Resource> subjects = new TreeSet<>(getIriComparator());
		subjects.addAll(resources);

		for (Resource nextSubject : subjects) {
			boolean canShortenSubjectBNode = !(nextSubject instanceof BNode && checkPresenceOfResource(nextSubject));

			for (IRI nextPredicate : prettyPrintModel.filter(nextSubject, null, null,
					nextContext).predicates()) {
				Model nextObjects = prettyPrintModel.filter(nextSubject, nextPredicate, null,
						nextContext);
				for (Statement nextSt : nextObjects) {
					Value nextObject = nextSt.getObject();
					boolean canShortenObjectBNode = !(nextObject instanceof BNode && checkPresenceOfValue(nextObject));

					handleStatementInternal(nextSt, true, canShortenSubjectBNode, canShortenObjectBNode);
				}
			}
		}
	}

	@Override
	protected void writeLiteral(Literal lit)
			throws IOException {
		String label = lit.getLabel();
		IRI datatype = lit.getDatatype();

		if (label.indexOf('\n') != -1 || label.indexOf('\r') != -1 || label.indexOf('\t') != -1) {
			// Write label as long string
			writer.write("\"\"\"");
			writer.write(TurtleUtil.encodeLongString(label));
			writer.write("\"\"\"");
		} else {
			// Write label as normal string
			writer.write("\"");
			writer.write(TurtleUtil.encodeString(label));
			writer.write("\"");
		}

		if (Literals.isLanguageLiteral(lit)) {
			// Append the literal's language
			writer.write("@");
			writeLanguageLiteralIfPresent(lit);
		} else if (!XMLSchema.STRING.equals(datatype) || !isXsdStringToPlainLiteral()) {
			// Append the literal's datatype (possibly written as an abbreviated
			// URI)
			writer.write("^^");
			writeURI(datatype);
		}
	}

	private void writeLanguageLiteralIfPresent(Literal lit) throws IOException {
		Optional<String> literalOptional = lit.getLanguage();
		if (literalOptional.isPresent()) {
			writer.write(literalOptional.get());
		}
	}

	/**
	 * Internal method that differentiates between the pretty-print and streaming writer cases.
	 *
	 * @param st                     The next statement to write
	 * @param endRDFCalled           True if endRDF has been called before this method is called. This is used to buffer statements
	 *                               for pretty-printing before dumping them when all statements have been delivered to us.
	 * @param canShortenSubjectBNode True if, in the current context, we may be able to shorten the subject of this statement iff it
	 *                               is an instance of {@link BNode}.
	 * @param canShortenObjectBNode  True if, in the current context, we may be able to shorten the object of this statement iff it
	 *                               is an instance of {@link BNode}.
	 */
	@SuppressWarnings("squid:S3776")
	@Override
	protected void handleStatementInternal(Statement st, boolean endRDFCalled, boolean canShortenSubjectBNode,
										   boolean canShortenObjectBNode) {

		// Avoid accidentally writing statements early, but don't lose track of
		// them if they are sent here
		if (prettyPrintModel != null && !endRDFCalled) {
			prettyPrintModel.add(st);
			return;
		}

		Resource subj = st.getSubject();
		IRI pred = st.getPredicate();
		Value obj = st.getObject();

		try {
			if (subj.equals(lastWrittenSubject)) {
				if (pred.equals(lastWrittenPredicate)) {
					// Identical subject and predicate
					if (isPrettyPrint()) {
						writer.write(" ,");
						writer.writeEOL();
						if (!indentationIncreasedOnSamePredicate) {
							writer.increaseIndentation();
							indentationIncreasedOnSamePredicate = true;
						}
					} else {
						writer.write(" , ");
					}
				} else {
					// Identical subject, new predicate
					writer.write(" ;");
					writer.writeEOL();

					if (isPrettyPrint() && indentationIncreasedOnSamePredicate) {
						writer.decreaseIndentation();
						indentationIncreasedOnSamePredicate = false;
					}

					// Write new predicate
					writePredicate(pred);
					writer.write(" ");
					lastWrittenPredicate = pred;
				}
			} else {
				// New subject
				closePreviousStatement();

				// mark a beginning of a new subject in pretty print
				if (isPrettyPrint()) {
					writer.writeEOL();
					writeCommentLine(subj.toString());
				}

				// Write new subject:
				writer.writeEOL();
				writeResource(subj, canShortenSubjectBNode);
				writer.write(" ");
				lastWrittenSubject = subj;

				// Write new predicate
				writePredicate(pred);
				writer.write(" ");
				lastWrittenPredicate = pred;

				statementClosed = false;
				writer.increaseIndentation();
			}

			writeValue(obj, canShortenObjectBNode);

			// Don't close the line just yet. Maybe the next
			// statement has the same subject and/or predicate.
		} catch (IOException e) {
			throw new RDFHandlerException(e);
		}
	}

	@Override
	protected void closePreviousStatement()
			throws IOException {
		if (!statementClosed) {
			// The previous statement still needs to be closed:
			writer.write(" .");
			writer.writeEOL();
			writer.decreaseIndentation();
			if (indentationIncreasedOnSamePredicate) {
				writer.decreaseIndentation();
			}

			statementClosed = true;
			lastWrittenSubject = null;
			lastWrittenPredicate = null;
			indentationIncreasedOnSamePredicate = false;
		}
	}

	@Override
	protected void writeCommentLine(String line) throws IOException {
		this.writer.write("### ");
		this.writer.write(line);
		this.writer.writeEOL();
	}

	private void writeHeaderComment(String title) throws IOException {
		// if there is no title specified, skip writing
		if (title.isEmpty()) {
			return;
		}
		closePreviousStatement();

		writer.writeEOL();
		writer.write(COMMENT_LINE);
		writer.writeEOL();

		writer.write(COMMENT_START);
		writer.writeEOL();

		writer.write(COMMENT_START + "\t" + title);
		writer.writeEOL();

		writer.write(COMMENT_START);
		writer.writeEOL();

		writer.write(COMMENT_LINE);
		writer.writeEOL();
	}

	private boolean checkPresenceOfResource(Resource resource) {
		// Cannot shorten this blank node as it is used as the object of a statement somewhere so must be written in a
		// non-anonymous form
		return prettyPrintModel.contains(null, null, resource) ||
				// TriG section 2.3.1 specifies that we cannot shorten blank nodes shared across contexts, and this code
				// is shared with TriG.
				prettyPrintModel.filter(resource, null, null).contexts().size() > 1 ||
				// Cannot anonymize if this blank node has been used as a context also
				prettyPrintModel.contains(null, null, null, resource);
	}

	private boolean checkPresenceOfValue(Value value) {
		// Cannot shorten this blank node as it is used as the subject of a statement somewhere
		// so must be written in a non-anonymous form
		// NOTE: that this is only a restriction in this implementation because we write in CSPO order,
		// if we followed the linked chain we could be able to shorten here in some cases
		return prettyPrintModel.contains((BNode) value, null, null) ||
				// Cannot shorten BNode if any other statements reference it as an object
				prettyPrintModel.filter(null, null, value).size() > 1 ||
				// Cannot anonymize if this blank node has been used as a context also
				!prettyPrintModel.filter(null, null, null, (BNode) value).isEmpty();
	}

	private Comparator<Resource> getIriComparator() {
		Collator collator = Collator.getInstance();
		return (firstIri, secondIri) -> {
			// Blank nodes are sorted by their full names
			String first = firstIri instanceof IRI ? ((IRI) firstIri).getLocalName() : firstIri.toString();
			String second = secondIri instanceof IRI ? ((IRI) secondIri).getLocalName() : secondIri.toString();
			return collator.compare(first, second);
		};
	}

	private void cleanAnnotationModelSet(Model annotations, Set<? extends Resource> modelToRemove, Resource context) {
		modelToRemove.forEach(subj -> annotations.remove(subj, null, null, context));
	}

	private Set<Resource> getModelByIRI(IRI iri, Resource context) {
		return prettyPrintModel.filter(null, RDF.TYPE, iri, context).subjects();
	}

	private Set<Resource> mergeModels(Set<Resource> first, Set<Resource> second) {
		Set<Resource> mergeSet = new TreeSet<>(getIriComparator());
		mergeSet.addAll(first);
		mergeSet.addAll(second);
		return mergeSet;
	}

	private boolean isXsdStringToPlainLiteral() {
		return getWriterConfig().get(BasicWriterSettings.XSD_STRING_TO_PLAIN_LITERAL);
	}

	public SepTurtleWriter setPrettyPrintEnabled() {
		getWriterConfig().set(BasicWriterSettings.PRETTY_PRINT, true);
		return this;
	}

	private Boolean isPrettyPrint() {
		return getWriterConfig().get(BasicWriterSettings.PRETTY_PRINT);
	}
}