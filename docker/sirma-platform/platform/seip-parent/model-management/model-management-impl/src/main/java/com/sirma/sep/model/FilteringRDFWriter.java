package com.sirma.sep.model;

import java.util.Collection;
import java.util.function.BiPredicate;
import java.util.function.Predicate;

import org.eclipse.rdf4j.model.Statement;
import org.eclipse.rdf4j.rio.RDFFormat;
import org.eclipse.rdf4j.rio.RDFWriter;
import org.eclipse.rdf4j.rio.RioSetting;
import org.eclipse.rdf4j.rio.WriterConfig;

/**
 * Delegating {@link RDFWriter} that allows filtering of the processed statements, namespaces and comments.<br>
 * The purpose of the writer is no reduce the processed data and not to introduce additional or repace data.
 *
 * @author <a href="mailto:borislav.bonev@sirma.bg">Borislav Bonev</a>
 * @since 20/03/2019
 */
public class FilteringRDFWriter implements RDFWriter {

	private final RDFWriter delegate;
	private Predicate<Statement> statementFilter;
	private Predicate<String> commentFilter;
	private BiPredicate<String, String> namespaceFilter;

	public FilteringRDFWriter(RDFWriter delegate) {
		this.delegate = delegate;
	}

	@Override
	public RDFFormat getRDFFormat() {
		return delegate.getRDFFormat();
	}

	@Override
	public RDFWriter setWriterConfig(WriterConfig config) {
		return delegate.setWriterConfig(config);
	}

	@Override
	public WriterConfig getWriterConfig() {
		return delegate.getWriterConfig();
	}

	@Override
	public Collection<RioSetting<?>> getSupportedSettings() {
		return delegate.getSupportedSettings();
	}

	@Override
	public <T> RDFWriter set(RioSetting<T> setting, T value) {
		return delegate.set(setting, value);
	}

	@Override
	public void startRDF() {
		delegate.startRDF();
	}

	@Override
	public void endRDF() {
		delegate.endRDF();
	}

	@Override
	public void handleNamespace(String prefix, String uri) {
		if (namespaceFilter == null || namespaceFilter.test(prefix, uri)) {
			delegate.handleNamespace(prefix, uri);
		}
	}

	@Override
	public void handleStatement(Statement st) {
		if (statementFilter == null || statementFilter.test(st)) {
			delegate.handleStatement(st);
		}
	}

	@Override
	public void handleComment(String comment) {
		if (commentFilter == null || commentFilter.test(comment)) {
			delegate.handleComment(comment);
		}
	}

	public Predicate<Statement> getStatementFilter() {
		return statementFilter;
	}

	public FilteringRDFWriter setStatementFilter(Predicate<Statement> statementFilter) {
		this.statementFilter = statementFilter;
		return this;
	}

	public Predicate<String> getCommentFilter() {
		return commentFilter;
	}

	public FilteringRDFWriter setCommentFilter(Predicate<String> commentFilter) {
		this.commentFilter = commentFilter;
		return this;
	}

	public BiPredicate<String, String> getNamespaceFilter() {
		return namespaceFilter;
	}

	public FilteringRDFWriter setNamespaceFilter(BiPredicate<String, String> namespaceFilter) {
		this.namespaceFilter = namespaceFilter;
		return this;
	}
}
