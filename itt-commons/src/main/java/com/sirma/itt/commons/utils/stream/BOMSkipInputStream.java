/**
 * Copyright (c) 2009 24.04.2009 , Sirma ITT.
/*

/**
 * 
 */
package com.sirma.itt.commons.utils.stream;

import java.io.FilterInputStream;
import java.io.IOException;
import java.io.InputStream;

/**
 * Input stream which detect if the wrapped stream starts with BOM. If the
 * wrapped stream starts with BOM the BOM is skipped and the first read byte is
 * the byte just after the BOM.
 * 
 * @author Hristo Iliev
 */
public abstract class BOMSkipInputStream extends FilterInputStream {
    /**
     * start of the input if the bomBuffer contains character which are not BOM,
     * this value is the value of the first byte not part of the BOM. If the
     * bomBuffer contains only the BOM, this value is equal to the size of the
     * bomBuffer.
     */
    private int inputStart;
    /**
     * Number of bytes which bomBuffer contains.
     */
    private int readedBomCandidateSize;
    /**
     * Flag showing that the initial read is made.
     */
    private boolean readIsStarted;
    /** Flag showing that the bomBuffer is read, either containing or not BOM. */
    private boolean bomIsRead;
    /** Buffer which contains the BOM. */
    private byte[] bomBuffer;

    /**
     * value of the inputStart at the time {@link #mark(int)} is called for last
     * time.
     */
    private int markedInputStart;
    /**
     * store the state of {@link #bomIsRead} flag at the time {@link #mark(int)}
     * is called for last time.
     */
    private boolean markedBomIsRead;
    /**
     * exception thrown at the time when BOM is tried to be read. If at this
     * time an exception is thrown it will be thrown at next read operation.
     */
    private IOException markException;
    /** read limit of last {@link #mark(int)} operation. */
    private int markedReadlimit = -1;
    /** number of bytes read since last call of {@link #mark(int)}. */
    private int readSinceLastMark;
    /** the last {@link #mark(int)} operation is invalid. */
    private boolean invalidateMark;

    /**
     * Show the size of the BOM. This size is used for creating the bomBuffer
     * which is further passed to {@link #isBom(byte[])} operation.
     * 
     * @return int, number of BOM bytes
     * @see #isBom(byte[])
     */
    protected abstract int bomSizeInBytes();

    /**
     * Check if the specified buffer contains BOM. The buffer will be with
     * length at least equal to the {@link #bomSizeInBytes()} returned value.
     * 
     * @param bom
     *            byte[], the buffer to be checked if contains BOM
     * @return number of bytes to be skipped from the beginning of the buffer.
     *         If no BOM is found this method should return <code>0</code>.
     *         Otherwise it must return as much number of bytes as the size of
     *         the BOM is.
     */
    protected abstract int isBom(byte[] bom);

    /**
     * Initialize the {@link BOMSkipInputStream}.
     * 
     * @param in
     *            {@link InputStream}, the wrapped input stream
     */
    public BOMSkipInputStream(InputStream in) {
	super(in);
    }

    /**
     * Count the number of read bytes. This number is used in {@link #mark(int)}
     * /{@link #reset()} operations. If the number of bytes read overrun the
     * int, then the mark is invalidated.
     * 
     * @param readCount
     *            int, number of read bytes
     * @return int, return the readCount value
     * @see #countNumberOfRead(long)
     */
    private synchronized int countNumberOfRead(int readCount) {
	int saveReaded = readSinceLastMark;
	if (readCount != -1) {
	    readSinceLastMark += readCount;
	}
	if (readSinceLastMark < saveReaded) {
	    invalidateMark = true;
	}
	return readCount;
    }

    /**
     * Count the number of read bytes. This number is used in {@link #mark(int)}
     * /{@link #reset()} operations. If the number of bytes read overrun the int
     * or the long value is greater than {@link Integer#MAX_VALUE} then the mark
     * is invalidated.
     * 
     * @param readCount
     *            long, number of read bytes
     * @return long, return the readCount value
     * @see #countNumberOfRead(int)
     */
    private synchronized long countNumberOfRead(long readCount) {
	int saveReaded = readSinceLastMark;
	if (readCount != -1) {
	    readSinceLastMark += readCount;
	}
	if (((readCount & 0xFFFFFFFF00000000L) != 0)
		|| (readSinceLastMark < saveReaded)) {
	    invalidateMark = true;
	}
	return readCount;
    }

    /**
     * Try to read BOM if it is not read by now. If the {@link IOException}
     * occur while trying to mark, next time when this method is called it will
     * throw the same exception.
     * 
     * @throws IOException
     *             thrown if there is an I/O error while trying to read the BOM,
     *             or the last try to mark encourage an {@link IOException}
     */
    private synchronized void tryBom() throws IOException {
	if (markException != null) {
	    throw markException;
	}
	if (!readIsStarted) {
	    readBOM();
	    readIsStarted = true;
	}
    }

    /**
     * Read the BOM and prepare the input stream for first time use.
     * 
     * @throws IOException
     *             thrown if an {@link IOException} occur while trying to read
     *             the BOM
     */
    private synchronized void readBOM() throws IOException {
	bomBuffer = new byte[bomSizeInBytes()];
	int readed;
	readedBomCandidateSize = 0;
	do {
	    readed = in.read(bomBuffer, readedBomCandidateSize,
		    bomBuffer.length - readedBomCandidateSize);
	    readedBomCandidateSize += readed;
	} while ((readedBomCandidateSize < bomBuffer.length) && (readed != -1));
	if (readed == -1) {
	    readedBomCandidateSize++;
	}
	inputStart = isBom(bomBuffer);
	if (inputStart == readedBomCandidateSize) {
	    bomIsRead = true;
	}
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public synchronized int read() throws IOException {
	int result = read0();
	if (result != -1) {
	    countNumberOfRead(1);
	}
	return result;
    }

    /**
     * Real read operation.
     * 
     * @return int, the read byte or -1 if end-of-stream occur
     * @throws IOException
     *             thrown if there is an I/O error while reading.
     * @see #read()
     */
    private synchronized int read0() throws IOException {
	tryBom();
	if (inputStart < bomBuffer.length) {
	    if (inputStart >= readedBomCandidateSize) {
		// If the stream is smaller than the BOM size
		return -1;
	    }
	    return bomBuffer[inputStart++] & 0xFF;
	}
	return in.read();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public synchronized int read(byte b[]) throws IOException {
	return read(b, 0, b.length);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public synchronized int read(byte b[], int off, int len) throws IOException {
	return countNumberOfRead(read0(b, off, len));
    }

    /**
     * Real read operation with byte array.
     * 
     * @param b
     *            byte[], the byte array to store the read content
     * @param off
     *            int, offset of the array to store the first read byte
     * @param len
     *            int, number of bytes to be read
     * @return int, number of read bytes
     * @throws IOException
     *             thrown if there is an I/O error while reading the input
     *             stream
     * @see #read(byte[], int, int)
     */
    private synchronized int read0(byte b[], int off, int len)
	    throws IOException {
	tryBom();
	if (!bomIsRead) {
	    // Marked as true either if the content of bomBuffer is exactly the
	    // BOM or if all non BOM character in the buffer are read

	    int remainingFromBOMBuffer = readedBomCandidateSize - inputStart;
	    if (remainingFromBOMBuffer > 0) {
		// If the buffer is not (fully) read and contains non BOM
		// characters

		if (remainingFromBOMBuffer >= len) {
		    // If the requested length is smaller than number of
		    // remaining bytes in the buffer

		    System.arraycopy(bomBuffer, inputStart, b, off, len);
		    inputStart += len;
		    if (inputStart == readedBomCandidateSize) {
			// There are not more non BOM bytes in the buffer

			bomIsRead = true;
		    }
		    return len;
		}
		System.arraycopy(bomBuffer, inputStart, b, off,
			remainingFromBOMBuffer);
		inputStart = readedBomCandidateSize;
		bomIsRead = true;
		int currentRead = in.read(b, off + remainingFromBOMBuffer, len
			- remainingFromBOMBuffer);
		if (currentRead == -1) {
		    return remainingFromBOMBuffer;
		}
		return currentRead + remainingFromBOMBuffer;
	    }
	    bomIsRead = true;
	}
	return in.read(b, off, len);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public synchronized long skip(long n) throws IOException {
	return countNumberOfRead(skip0(n));
    }

    /**
     * Real skip operation.
     * 
     * @param n
     *            long, number of bytes to be skipped
     * @return long, number of skipped bytes
     * @throws IOException
     *             thrown if there is an I/O error while reading the stream
     */
    private synchronized long skip0(long n) throws IOException {
	tryBom();
	if (inputStart < readedBomCandidateSize) {
	    inputStart += n;
	    if (inputStart > readedBomCandidateSize) {
		inputStart = readedBomCandidateSize;
		return in.skip(n - (readedBomCandidateSize - inputStart))
			+ readedBomCandidateSize - inputStart;
	    }
	    return n;
	}
	return in.skip(n);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public synchronized int available() throws IOException {
	tryBom();
	return readedBomCandidateSize - inputStart + in.available();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public synchronized void mark(int readlimit) {
	try {
	    tryBom();
	} catch (IOException e) {
	    // If such exception occur next read operation will throw this
	    // exception
	    markException = e;
	}
	markedInputStart = inputStart;
	markedBomIsRead = bomIsRead;
	markedReadlimit = readlimit;
	readSinceLastMark = 0;
	in.mark(readlimit - readedBomCandidateSize + inputStart);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public synchronized void reset() throws IOException {
	if ((readSinceLastMark > markedReadlimit) || invalidateMark) {
	    if (markedReadlimit == -1) {
		throw new IOException("Stream is never marked"); //$NON-NLS-1$
	    }
	    throw new IOException("Mark limit is overrun"); //$NON-NLS-1$
	}
	inputStart = markedInputStart;
	bomIsRead = markedBomIsRead;
	in.reset();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean markSupported() {
	return in.markSupported();
    }
}