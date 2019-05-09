package com.sirma.itt.emf.semantic.persistence;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.Optional;

import javax.xml.datatype.XMLGregorianCalendar;

import org.eclipse.rdf4j.model.IRI;
import org.eclipse.rdf4j.model.Literal;
import org.eclipse.rdf4j.model.Value;

/**
 * Proxy of {@link Value} instance. Used when loading data from semantic database. <br>
 * The proxy guarantees unique identity based on the value returned from {@link Value#stringValue()} for different
 * {@link Value} implementations when returned by semantic query. So that it can be used in maps or sets.
 *
 * @author BBonev
 */
class LiteralProxy extends ValueProxy<Literal>implements Literal {

	private static final long serialVersionUID = -1702242048998633747L;

	/**
	 * Instantiates a new value proxy.
	 *
	 * @param value
	 *            the value
	 */
	public LiteralProxy(Literal value) {
		super(value);
	}

	/**
	 * String value.
	 *
	 * @return the string
	 */
	@Override
	public String stringValue() {
		return getValue().stringValue();
	}

	@Override
	public String toString() {
		return getValue().toString();
	}

	@Override
	public String getLabel() {
		return getValue().getLabel();
	}

	@Override
	public Optional<String> getLanguage() {
		return getValue().getLanguage();
	}

	@Override
	public IRI getDatatype() {
		return getValue().getDatatype();
	}

	@Override
	public byte byteValue() {
		return getValue().byteValue();
	}

	@Override
	public short shortValue() {
		return getValue().shortValue();
	}

	@Override
	public int intValue() {
		return getValue().intValue();
	}

	@Override
	public long longValue() {
		return getValue().longValue();
	}

	@Override
	public BigInteger integerValue() {
		return getValue().integerValue();
	}

	@Override
	public BigDecimal decimalValue() {
		return getValue().decimalValue();
	}

	@Override
	public float floatValue() {
		return getValue().floatValue();
	}

	@Override
	public double doubleValue() {
		return getValue().doubleValue();
	}

	@Override
	public boolean booleanValue() {
		return getValue().booleanValue();
	}

	@Override
	public XMLGregorianCalendar calendarValue() {
		return getValue().calendarValue();
	}

	@Override
	public boolean equals(Object obj) {
		if (!super.equals(obj)) {
			return false;
		}
		if (obj instanceof Literal) {
			Optional<String> language = getLanguage();
			Optional<String> otherLanguage = ((Literal) obj).getLanguage();
			if (language.isPresent() && otherLanguage.isPresent()) {
				return language.get().equalsIgnoreCase(otherLanguage.get());
			}
			// If only one has a language, then return false
			else if (language.isPresent() || otherLanguage.isPresent()) {
				return false;
			}
			return true;
		}
		return false;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = prime * super.hashCode();
		Optional<String> language = getLanguage();
		result = prime * result + (language.map(String::hashCode).orElse(0));
		return result;
	}
}
