package com.sirma.itt.emf.sequence;

import java.util.Collection;

/**
 * Service for sequence generating unique sequence numbers
 *
 * @author BBonev
 */
public interface SequenceGeneratorService {

	/**
	 * Gets the next id for the given sequence identifier. The identifier is used to distinguish the different sequences
	 *
	 * @param sequenceId
	 *            the sequence id
	 * @return the next id
	 */
	Long getNextId(String sequenceId);

	/**
	 * Increment the sequence identified by the given name. If the sequence does not exists new will be created.
	 *
	 * @param sequenceId
	 *            the sequence id
	 * @return the sequence
	 */
	Sequence incrementSequence(String sequenceId);

	/**
	 * Gets the current state of the sequence.
	 *
	 * @param sequenceId
	 *            the sequence id
	 * @return the current id
	 */
	Long getCurrentId(String sequenceId);

	/**
	 * Gets the next sequence by template. The template may define a multiple sequence generation on one call or partial
	 * incrementing.
	 * <p>
	 * The supported elements are
	 * <ul>
	 * <li><code>{sequenceID}</code> - if present the generator will place the current value of the given sequence if
	 * exits or will create new one. If current sequence is 2 the result will be 2.
	 * <li><code>{sequenceID+}</code> - if present the generator will place the current value of the given sequence
	 * identified by the given 'sequenceID' if exists and will increment the sequence. If the current value is 2, the
	 * method will return 2 and will store 3 in DB.
	 * <li><code>{+sequenceID}</code> - if present the generator will place the next value of the sequence identified by
	 * the given 'sequenceID'. If the current value is 0 (does not exist) the method will return 2 and will store 2 into
	 * the DB.
	 * </ul>
	 * Examples:
	 * <ol>
	 * <li>The template <code>test-{seq1}-{+seq2}</code> if seq1=2 and seq2=4 will produce <code>test-2-5</code>
	 * <li>The template <code>test-{seq1+}-{seq2}</code> if seq1=2 and seq2=does not exist will produce
	 * <code>test-2-1</code> and DB state will be seq1=3, seq2=1
	 * </ol>
	 *
	 * @param template
	 *            the template
	 * @return the next sequence by template
	 */
	String getNextSequenceByTemplate(String template);

	/**
	 * Resets the given sequence to the given value. If the index does not exists it will be created and initialized
	 * with the given value. After this method call calling {@link #getCurrentId(String)} will result in returning the
	 * same value as passed here. If the given value is null or less then a zero then the method does nothing.
	 *
	 * @param sequenceId
	 *            the sequence id
	 * @param value
	 *            the value to set that is non <code>null</code> or greater or equal to zero.
	 */
	void resetSequenceTo(String sequenceId, Long value);

	/**
	 * List all sequences
	 *
	 * @param <S>
	 *            the Sequence type
	 * @return the collection containing all sequences or empty collection if there are no sequences.
	 */
	<S extends Sequence> Collection<S> listAll();

	/**
	 * Gets a sequence by name if exists or <code>null</code> if not.
	 *
	 * @param <S>
	 *            the sequence type
	 * @param name
	 *            the name of the sequence
	 * @return the sequence object if any
	 */
	<S extends Sequence> S getSequence(String name);
}
