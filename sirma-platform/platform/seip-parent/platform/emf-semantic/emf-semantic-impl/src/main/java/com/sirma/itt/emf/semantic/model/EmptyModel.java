package com.sirma.itt.emf.semantic.model;

import java.util.Collection;
import java.util.Iterator;
import java.util.Optional;
import java.util.Set;

import org.eclipse.rdf4j.model.BNode;
import org.eclipse.rdf4j.model.IRI;
import org.eclipse.rdf4j.model.Literal;
import org.eclipse.rdf4j.model.Model;
import org.eclipse.rdf4j.model.Namespace;
import org.eclipse.rdf4j.model.Resource;
import org.eclipse.rdf4j.model.Statement;
import org.eclipse.rdf4j.model.Value;
import org.eclipse.rdf4j.model.ValueFactory;

/**
 * Empty implementation of {@link Model} used for operations where model is required but the data added to the model
 * not. (Null pattern)
 *
 * @author BBonev
 */
public class EmptyModel implements Model {

	/**
	 * Comment for serialVersionUID.
	 */
	private static final long serialVersionUID = 1104691774051054393L;

	@Override
	@Deprecated
	public ValueFactory getValueFactory() {
		return null;
	}

	@Override
	@Deprecated
	public Iterator<Statement> match(Resource paramResource, IRI paramURI, Value paramValue,
			Resource... paramArrayOfResource) {
		return java.util.Collections.emptyIterator();
	}

	@Override
	public int size() {
		return 0;
	}

	@Override
	public boolean isEmpty() {
		return true;
	}

	@Override
	public boolean contains(Object o) {
		return false;
	}

	@Override
	public Iterator<Statement> iterator() {
		return java.util.Collections.emptyIterator();
	}

	@Override
	public Object[] toArray() {
		return new Object[0];
	}

	@Override
	public <T> T[] toArray(T[] a) {
		return a;
	}

	@Override
	public boolean add(Statement e) {
		return false;
	}

	@Override
	public boolean remove(Object o) {
		return false;
	}

	@Override
	public boolean containsAll(Collection<?> c) {
		return false;
	}

	@Override
	public boolean addAll(Collection<? extends Statement> c) {
		return false;
	}

	@Override
	public boolean removeAll(Collection<?> c) {
		return false;
	}

	@Override
	public boolean retainAll(Collection<?> c) {
		return false;
	}

	@Override
	public void clear() {
	}

	@Override
	public Model unmodifiable() {
		return this;
	}

	@Override
	public Set<Namespace> getNamespaces() {
		return java.util.Collections.emptySet();
	}

	@Override
	public Optional<Namespace> getNamespace(String paramString) {
		return null;
	}

	@Override
	public Namespace setNamespace(String paramString1, String paramString2) {
		return null;
	}

	@Override
	public void setNamespace(Namespace paramNamespace) {

	}

	@Override
	public Optional<Namespace> removeNamespace(String paramString) {
		return null;
	}

	@Override
	public boolean contains(Resource paramResource, IRI paramURI, Value paramValue, Resource... paramArrayOfResource) {
		return false;
	}

	@Override
	public boolean add(Resource paramResource, IRI paramURI, Value paramValue, Resource... paramArrayOfResource) {
		return false;
	}

	@Override
	public boolean clear(Resource... paramArrayOfResource) {
		return false;
	}

	@Override
	public boolean remove(Resource paramResource, IRI paramURI, Value paramValue, Resource... paramArrayOfResource) {
		return false;
	}

	@Override
	public Model filter(Resource paramResource, IRI paramURI, Value paramValue, Resource... paramArrayOfResource) {
		return this;
	}

	@Override
	public Set<Resource> subjects() {
		return java.util.Collections.emptySet();
	}

	@Override
	public Set<IRI> predicates() {
		return java.util.Collections.emptySet();
	}

	@Override
	public Set<Value> objects() {
		return java.util.Collections.emptySet();
	}

	@Override
	public Set<Resource> contexts() {
		return java.util.Collections.emptySet();
	}

	@Override
	public Optional<Value> objectValue() {
		return null;
	}

	@Override
	public Optional<Literal> objectLiteral() {
		return null;
	}

	@Override
	public Optional<Resource> objectResource() {
		return null;
	}

	@Override
	public Optional<IRI> objectURI() {
		return null;
	}

	@Override
	public Optional<String> objectString() {
		return null;
	}

	@Override
	public Optional<BNode> subjectBNode() {
		return null;
	}

	@Override
	public Optional<Resource> subjectResource() {
		return null;
	}

	@Override
	public Optional<IRI> subjectIRI() {
		return null;
	}

}
