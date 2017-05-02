package com.sirma.itt.seip.convert;

import java.io.Serializable;
import java.util.Collection;
import java.util.Date;

import javax.enterprise.context.ApplicationScoped;

/**
 * Converter implementation proxy to allow injecting the non serializable implementation of the {@link TypeConverter}
 * where it's needed to be serializable.
 *
 * @author BBonev
 */
@ApplicationScoped
@SerializableConverter
public class SerializableTypeConverterProxy implements TypeConverter, Serializable {

	/**
	 * Comment for serialVersionUID.
	 */
	private static final long serialVersionUID = -2036412218974344907L;

	@Override
	public <T> T convert(Class<T> c, Object value) {
		return TypeConverterUtil.getConverter().convert(c, value);
	}

	@Override
	public <T> Collection<T> convert(Class<T> c, Collection values) {
		return TypeConverterUtil.getConverter().convert(c, values);
	}

	@Override
	public boolean booleanValue(Object value) {
		return TypeConverterUtil.getConverter().booleanValue(value);
	}

	@Override
	public char charValue(Object value) {
		return TypeConverterUtil.getConverter().charValue(value);
	}

	@Override
	public byte byteValue(Object value) {
		return TypeConverterUtil.getConverter().byteValue(value);
	}

	@Override
	public short shortValue(Object value) {
		return TypeConverterUtil.getConverter().shortValue(value);
	}

	@Override
	public int intValue(Object value) {
		return TypeConverterUtil.getConverter().intValue(value);
	}

	@Override
	public long longValue(Object value) {
		return TypeConverterUtil.getConverter().longValue(value);
	}

	@Override
	public float floatValue(Object value) {
		return TypeConverterUtil.getConverter().floatValue(value);
	}

	@Override
	public double doubleValue(Object value) {
		return TypeConverterUtil.getConverter().doubleValue(value);
	}

	@Override
	public boolean isMultiValued(Object value) {
		return TypeConverterUtil.getConverter().isMultiValued(value);
	}

	@Override
	public int size(Object value) {
		return TypeConverterUtil.getConverter().size(value);
	}

	@Override
	public <T> Collection<T> getCollection(Class<T> c, Object value) {
		return TypeConverterUtil.getConverter().getCollection(c, value);
	}

	@Override
	public <F, T> void addConverter(Class<F> source, Class<T> destination, Converter<F, T> converter) {
		TypeConverterUtil.getConverter().addConverter(source, destination, converter);
	}

	@Override
	public <F, I, T> Converter<F, T> addDynamicTwoStageConverter(Class<F> source, Class<I> intermediate,
			Class<T> destination) {
		return TypeConverterUtil.getConverter().addDynamicTwoStageConverter(source, intermediate, destination);
	}

	@Override
	public <T> Converter getConverter(Object value, Class<T> dest) {
		return TypeConverterUtil.getConverter().getConverter(value, dest);
	}

	@Override
	public <F, T> Converter getConverter(Class<F> source, Class<T> dest) {
		return TypeConverterUtil.getConverter().getConverter(source, dest);
	}

	@Override
	public <T> T tryConvert(Class<T> c, Object value) {
		return TypeConverterUtil.getConverter().tryConvert(c, value);
	}

	@Override
	public String stringValue(Object value) {
		return TypeConverterUtil.getConverter().stringValue(value);
	}

	@Override
	public Date dateValue(Object value) {
		return TypeConverterUtil.getConverter().dateValue(value);
	}

}
