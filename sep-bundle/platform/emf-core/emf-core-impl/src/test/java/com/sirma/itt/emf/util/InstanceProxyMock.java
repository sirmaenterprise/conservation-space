package com.sirma.itt.emf.util;

import java.lang.annotation.Annotation;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

import javax.enterprise.inject.AmbiguousResolutionException;
import javax.enterprise.inject.Instance;
import javax.enterprise.inject.UnsatisfiedResolutionException;
import javax.enterprise.util.TypeLiteral;

/**
 * Proxy class for {@link Instance} interface
 * 
 * @param <T>
 *            the generic type
 * @author BBonev
 */
public class InstanceProxyMock<T> implements Instance<T> {

	/** The target. */
	private List<T> target;

	/**
	 * Instantiates a new instance proxy mock from collection of instances.
	 * 
	 * @param instances
	 *            the list of instance to use
	 */
	public InstanceProxyMock(Collection<T> instances) {
		if (instances != null) {
			this.target = new ArrayList<>(instances);
		}
	}

	/**
	 * Instantiates a new instance proxy mock.
	 * 
	 * @param target
	 *            the target
	 * @param others
	 *            the others
	 */
	@SafeVarargs
	public InstanceProxyMock(T target, T... others) {
		int size = 1;
		if (others != null) {
			size += others.length;
		}
		this.target = new ArrayList<>(size);
		if (target != null) {
			this.target.add(target);
		}
		if (others != null) {
			for (T t : others) {
				if (t != null) {
					this.target.add(t);
				}
			}
		}
	}
	/**
	 * {@inheritDoc}
	 */
	@Override
	public Iterator<T> iterator() {
		return this.target.iterator();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public T get() {
		if (isUnsatisfied()) {
			throw new UnsatisfiedResolutionException();
		}
		if (isAmbiguous()) {
			throw new AmbiguousResolutionException();
		}
		return target.get(0);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public Instance<T> select(Annotation... qualifiers) {
		return this;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	@SuppressWarnings("unchecked")
	public <U extends T> Instance<U> select(Class<U> subtype, Annotation... qualifiers) {
		return (Instance<U>) this;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	@SuppressWarnings("unchecked")
	public <U extends T> Instance<U> select(TypeLiteral<U> subtype, Annotation... qualifiers) {
		return (Instance<U>) this;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public boolean isUnsatisfied() {
		return target.isEmpty();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public boolean isAmbiguous() {
		return target.size() > 1;
	}

}
