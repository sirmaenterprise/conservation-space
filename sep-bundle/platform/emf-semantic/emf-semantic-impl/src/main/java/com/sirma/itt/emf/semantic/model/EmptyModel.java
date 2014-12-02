package com.sirma.itt.emf.semantic.model;

import java.util.Collection;
import java.util.Iterator;
import java.util.Set;

import org.openrdf.model.Literal;
import org.openrdf.model.Model;
import org.openrdf.model.Namespace;
import org.openrdf.model.Resource;
import org.openrdf.model.Statement;
import org.openrdf.model.URI;
import org.openrdf.model.Value;
import org.openrdf.model.ValueFactory;
import org.openrdf.model.util.ModelException;

/**
 * Empty implementation of {@link Model} used for operations where model is required but the data
 * added to the model not. (Null pattern)
 * 
 * @author BBonev
 */
public class EmptyModel implements Model {

	/**
	 * Comment for serialVersionUID.
	 */
	private static final long serialVersionUID = 1104691774051054393L;

	/**
	 * {@inheritDoc}
	 */
	@Override
	@Deprecated
	public ValueFactory getValueFactory() {
		return null;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	@Deprecated
	public Iterator<Statement> match(Resource paramResource, URI paramURI, Value paramValue,
			Resource... paramArrayOfResource) {
		return java.util.Collections.emptyIterator();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public int size() {
		return 0;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public boolean isEmpty() {
		return true;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public boolean contains(Object o) {
		return false;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public Iterator<Statement> iterator() {
		return java.util.Collections.emptyIterator();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public Object[] toArray() {
		return new Object[0];
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public <T> T[] toArray(T[] a) {
		return a;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public boolean add(Statement e) {
		return false;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public boolean remove(Object o) {
		return false;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public boolean containsAll(Collection<?> c) {
		return false;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public boolean addAll(Collection<? extends Statement> c) {
		return false;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public boolean removeAll(Collection<?> c) {
		return false;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public boolean retainAll(Collection<?> c) {
		return false;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void clear() {
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public Model unmodifiable() {
		return this;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public Set<Namespace> getNamespaces() {
		return java.util.Collections.emptySet();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public Namespace getNamespace(String paramString) {
		return null;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public Namespace setNamespace(String paramString1, String paramString2) {
		return null;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void setNamespace(Namespace paramNamespace) {

	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public Namespace removeNamespace(String paramString) {
		return null;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public boolean contains(Resource paramResource, URI paramURI, Value paramValue,
			Resource... paramArrayOfResource) {
		return false;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public boolean add(Resource paramResource, URI paramURI, Value paramValue,
			Resource... paramArrayOfResource) {
		return false;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public boolean clear(Resource... paramArrayOfResource) {
		return false;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public boolean remove(Resource paramResource, URI paramURI, Value paramValue,
			Resource... paramArrayOfResource) {
		return false;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public Model filter(Resource paramResource, URI paramURI, Value paramValue,
			Resource... paramArrayOfResource) {
		return this;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public Set<Resource> subjects() {
		return java.util.Collections.emptySet();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public Set<URI> predicates() {
		return java.util.Collections.emptySet();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public Set<Value> objects() {
		return java.util.Collections.emptySet();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public Set<Resource> contexts() {
		return java.util.Collections.emptySet();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public Value objectValue() throws ModelException {
		return null;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public Literal objectLiteral() throws ModelException {
		return null;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public Resource objectResource() throws ModelException {
		return null;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public URI objectURI() throws ModelException {
		return null;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public String objectString() throws ModelException {
		return null;
	}

}
