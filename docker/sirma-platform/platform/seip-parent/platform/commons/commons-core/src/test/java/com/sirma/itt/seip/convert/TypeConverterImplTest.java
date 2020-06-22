package com.sirma.itt.seip.convert;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNotNull;
import static org.testng.Assert.assertNull;

import java.util.AbstractCollection;
import java.util.Arrays;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;

import org.testng.annotations.Test;

import com.sirma.itt.seip.convert.TypeConversionException;
import com.sirma.itt.seip.convert.TypeConverter;
import com.sirma.itt.seip.convert.TypeConverterImpl;

/**
 * The Class TypeConverterImplTest.
 *
 * @author BBonev
 */
@Test
public class TypeConverterImplTest {

	/**
	 * Test exact convert.
	 */
	@Test
	public void testExactConvert() {
		TypeConverter converter = createConverter(SourceType.class, DestinationType.class);
		assertNotNull(converter.convert(DestinationType.class, new SourceType()));
	}

	/**
	 * Test inherited convert.
	 */
	@Test
	public void testInheritedConvert() {
		TypeConverter converter = createConverter(Type.class, DestinationType.class);
		assertNotNull(converter.convert(DestinationType.class, new SourceType()));
		assertNotNull(converter.convert(DestinationType.class, new Type() {
			// nothing to do
		}));
	}

	/**
	 * Test inherited destination convert.
	 */
	@Test
	public void testInheritedDestinationConvert() {
		TypeConverter converter = createConverter(Type.class, AbstractDestination.class);
		assertNotNull(converter.convert(DestinationType.class, new SourceType()));
		assertNotNull(converter.convert(AbstractDestination.class, new SourceType()));
		assertNotNull(converter.convert(DestinationType.class, new Type() {
			// nothing to do
		}));
	}

	/**
	 * Test missing converter.
	 */
	@Test(expectedExceptions = TypeConversionException.class)
	public void testMissingConverter() {
		createConverter(Type.class, AbstractDestination.class).convert(Type.class, new DestinationType());
	}

	/**
	 * Test missing converter with try.
	 */
	@Test
	public void testMissingConverterWithTry() {
		assertNull(
				createConverter(Type.class, AbstractDestination.class).tryConvert(Type.class, new DestinationType()));
	}

	@Test
	public void testDeriviedCollections() {
		TypeConverter converter = new TypeConverterImpl();
		new DefaultTypeConverter().register(converter);
		DummyList<String> dummyList = new DummyList<>(Arrays.asList("1", "2"));
		String result = converter.convert(String.class, (Object) dummyList);
		assertEquals(result, "1,2");
	}

	/**
	 * Creates the converter.
	 *
	 * @param <S>
	 *            the generic type
	 * @param <D>
	 *            the generic type
	 * @param source
	 *            the source
	 * @param destination
	 *            the destination
	 * @return the type converter
	 */
	private <S, D> TypeConverter createConverter(Class<S> source, Class<D> destination) {
		TypeConverter converter = new TypeConverterImpl();
		converter.addConverter(source, destination, s -> (D) new DestinationType());
		return converter;
	}

	static class DummyCollection<E, C extends Collection<E>> extends AbstractCollection<E> {

		final C delegate;

		DummyCollection(C delegate) {
			this.delegate = delegate;
		}

		@Override
		public boolean add(E e) {
			return delegate.add(e);
		}

		@Override
		public Iterator<E> iterator() {
			return delegate.iterator();
		}

		@Override
		public int size() {
			return delegate.size();
		}
	}

	static class DummyList<E> extends DummyCollection<E, List<E>> implements List<E> {

		DummyList(List<E> delegate) {
			super(delegate);
		}

		@Override
		public boolean addAll(int index, Collection<? extends E> c) {
			return false;
		}

		@Override
		public E get(int index) {
			return null;
		}

		@Override
		public E set(int index, E element) {
			return null;
		}

		@Override
		public void add(int index, E element) {

		}

		@Override
		public E remove(int index) {
			return null;
		}

		@Override
		public int indexOf(Object o) {
			return 0;
		}

		@Override
		public int lastIndexOf(Object o) {
			return 0;
		}

		@Override
		public ListIterator<E> listIterator() {
			return null;
		}

		@Override
		public ListIterator<E> listIterator(int index) {
			return null;
		}

		@Override
		public List<E> subList(int fromIndex, int toIndex) {
			return null;
		}
	}

	/**
	 * The Interface Type.
	 */
	interface Type {
		// nothing to do
	}

	/**
	 * The Interface SubType.
	 */
	interface SubType extends Type {
		// nothing to do
	}

	/**
	 * The Class SourceType.
	 */
	class SourceType implements Type {
		// nothing to do
	}

	/**
	 * The Class SourceSubType.
	 */
	class SourceSubType implements SubType {
		// nothing to do
	}

	/**
	 * The Class AbstractDestination.
	 */
	abstract class AbstractDestination {
		// nothing to do
	}

	/**
	 * The Class DestinationType.
	 */
	class DestinationType extends AbstractDestination {
		// nothing to do
	}

}
