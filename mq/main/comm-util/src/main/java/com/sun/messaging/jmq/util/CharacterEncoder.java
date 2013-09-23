/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright (c) 2000-2012 Oracle and/or its affiliates. All rights reserved.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common Development
 * and Distribution License("CDDL") (collectively, the "License").  You
 * may not use this file except in compliance with the License.  You can
 * obtain a copy of the License at
 * https://glassfish.dev.java.net/public/CDDL+GPL_1_1.html
 * or packager/legal/LICENSE.txt.  See the License for the specific
 * language governing permissions and limitations under the License.
 *
 * When distributing the software, include this License Header Notice in each
 * file and include the License file at packager/legal/LICENSE.txt.
 *
 * GPL Classpath Exception:
 * Oracle designates this particular file as subject to the "Classpath"
 * exception as provided by Oracle in the GPL Version 2 section of the License
 * file that accompanied this code.
 *
 * Modifications:
 * If applicable, add the following below the License Header, with the fields
 * enclosed by brackets [] replaced by your own identifying information:
 * "Portions Copyright [year] [name of copyright owner]"
 *
 * Contributor(s):
 * If you wish your version of this file to be governed by only the CDDL or
 * only the GPL Version 2, indicate your decision by adding "[Contributor]
 * elects to include this software in this distribution under the [CDDL or GPL
 * Version 2] license."  If you don't indicate a single choice of license, a
 * recipient has the option to distribute your version of this file under
 * either the CDDL, the GPL Version 2 or to extend the choice of license to
 * its licensees as provided above.  However, if you add GPL Version 2 code
 * and therefore, elected the GPL Version 2 license, then the option applies
 * only if the new code is made subject to such option by the copyright
 * holder.
 */

/*
 * @(#)CharacterEncoder.java	1.3 06/29/07
 */ 

package com.sun.messaging.jmq.util;

import java.io.InputStream;
import java.io.ByteArrayInputStream;
import java.io.OutputStream;
import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.io.IOException;


/**
 * This class defines the encoding half of character encoders.
 * A character encoder is an algorithim for transforming 8 bit binary
 * data into text (generally 7 bit ASCII or 8 bit ISO-Latin-1 text)
 * for transmition over text channels such as e-mail and network news.
 * 
 * The character encoders have been structured around a central theme
 * that, in general, the encoded text has the form:
 *
 * <pre>
 *	[Buffer Prefix]
 *	[Line Prefix][encoded data atoms][Line Suffix]
 *	[Buffer Suffix]
 * </pre>
 *
 * In the CharacterEncoder and CharacterDecoder classes, one complete
 * chunk of data is referred to as a <i>buffer</i>. Encoded buffers 
 * are all text, and decoded buffers (sometimes just referred to as 
 * buffers) are binary octets.
 *
 * To create a custom encoder, you must, at a minimum,  overide three
 * abstract methods in this class.
 * <DL>
 * <DD>bytesPerAtom which tells the encoder how many bytes to 
 * send to encodeAtom
 * <DD>encodeAtom which encodes the bytes sent to it as text.
 * <DD>bytesPerLine which tells the encoder the maximum number of
 * bytes per line.
 * </DL>
 *
 * Several useful encoders have already been written and are 
 * referenced in the See Also list below.
 *
 * @see		CharacterDecoder;
 * @see		UCEncoder
 * @see		UUEncoder
 * @see		BASE64Encoder
 */
public abstract class CharacterEncoder {

    /** Stream that understands "printing" */
    protected PrintStream pStream;

    /** Return the number of bytes per atom of encoding */
    abstract protected int bytesPerAtom();

    /** Return the number of bytes that can be encoded per line */
    abstract protected int bytesPerLine();

    /**
     * Encode the prefix for the entire buffer. By default is simply
     * opens the PrintStream for use by the other functions.
     */
    protected void encodeBufferPrefix(OutputStream aStream) throws IOException {
	pStream = new PrintStream(aStream);
    }

    /**
     * Encode the suffix for the entire buffer.
     */
    protected void encodeBufferSuffix(OutputStream aStream) throws IOException { }

    /**
     * Encode the prefix that starts every output line.
     */
    protected void encodeLinePrefix(OutputStream aStream, int aLength)
    throws IOException {
    }

    /**
     * Encode the suffix that ends every output line. By default
     * this method just prints a <newline> into the output stream.
     */
    protected void encodeLineSuffix(OutputStream aStream) throws IOException {
	pStream.println();
    }

    /** Encode one "atom" of information into characters. */
    abstract protected void encodeAtom(OutputStream aStream, byte someBytes[],
		int anOffset, int aLength) throws IOException;

    /**
     * This method works around the bizarre semantics of BufferedInputStream's
     * read method.
     */
    protected int readFully(InputStream in, byte buffer[]) 
	throws java.io.IOException {
	for (int i = 0; i < buffer.length; i++) {
	    int q = in.read();
	    if (q == -1)
		return i;
	    buffer[i] = (byte)q;
	}
	return buffer.length;
    }

    /**
     * Encode bytes from the input stream, and write them as text characters
     * to the output stream. This method will run until it exhausts the
     * input stream.
     */
    public void encode(InputStream inStream, OutputStream outStream) 
	throws IOException {
	int	j;
	int	numBytes;
	byte	tmpbuffer[] = new byte[bytesPerLine()];

	encodeBufferPrefix(outStream);
	
	while (true) {
	    numBytes = readFully(inStream, tmpbuffer);
	    if (numBytes == -1) {
		break;
	    }
	    encodeLinePrefix(outStream, numBytes);
	    for (j = 0; j < numBytes; j += bytesPerAtom()) {

		if ((j + bytesPerAtom()) <= numBytes) {
		    encodeAtom(outStream, tmpbuffer, j, bytesPerAtom());
		} else {
		    encodeAtom(outStream, tmpbuffer, j, (numBytes)- j);
		}
	    }
	    if (numBytes <= bytesPerLine()) {
		break;
	    } else {
		encodeLineSuffix(outStream);
	    }
	}
	encodeBufferSuffix(outStream);
    }

    /**
     * Encode the buffer in <i>aBuffer</i> and write the encoded
     * result to the OutputStream <i>aStream</i>.
     */
    public void encode(byte aBuffer[], OutputStream aStream) 
    throws IOException {
	ByteArrayInputStream inStream = new ByteArrayInputStream(aBuffer);
	encode(inStream, aStream);
    }

    /**
     * A 'streamless' version of encode that simply takes a buffer of
     * bytes and returns a string containing the encoded buffer.
     */
    public String encode(byte aBuffer[]) {
	ByteArrayOutputStream	outStream = new ByteArrayOutputStream();
	ByteArrayInputStream	inStream = new ByteArrayInputStream(aBuffer);
	try {
	    encode(inStream, outStream);
	} catch (Exception IOException) {
	    // This should never happen.
	    return null;
	}
	return (outStream.toString());
    }

    /**
     * Encode bytes from the input stream, and write them as text characters
     * to the output stream. This method will run until it exhausts the
     * input stream. It differs from encode in that it will add a newline
     * at the end of any buffer it encodes, even if that buffer is 
     * shorter than bytesPerLine.
     */
    public void encodeBuffer(InputStream inStream, OutputStream outStream) 
	throws IOException {
	int	j;
	int	numBytes;
	byte	tmpbuffer[] = new byte[bytesPerLine()];

	encodeBufferPrefix(outStream);
	
	while (true) {
	    numBytes = readFully(inStream, tmpbuffer);
	    if (numBytes == -1) {
		break;
	    }
	    encodeLinePrefix(outStream, numBytes);
	    for (j = 0; j < numBytes; j += bytesPerAtom()) {
		if ((j + bytesPerAtom()) <= numBytes) {
		    encodeAtom(outStream, tmpbuffer, j, bytesPerAtom());
		} else {
		    encodeAtom(outStream, tmpbuffer, j, (numBytes)- j);
		}
	    }
	    encodeLineSuffix(outStream);
	    if (numBytes < bytesPerLine()) {
		break;	
	    }
	}
	encodeBufferSuffix(outStream);
    }

    /**
     * Encode the buffer in <i>aBuffer</i> and write the encoded
     * result to the OutputStream <i>aStream</i>.
     */
    public void encodeBuffer(byte aBuffer[], OutputStream aStream) 
    throws IOException {
	ByteArrayInputStream inStream = new ByteArrayInputStream(aBuffer);
	encodeBuffer(inStream, aStream);
    }

    /**
     * A 'streamless' version of encode that simply takes a buffer of
     * bytes and returns a string containing the encoded buffer.
     */
    public String encodeBuffer(byte aBuffer[]) {
	ByteArrayOutputStream	outStream = new ByteArrayOutputStream();
	ByteArrayInputStream	inStream = new ByteArrayInputStream(aBuffer);
	try {
	    encodeBuffer(inStream, outStream);
	} catch (Exception IOException) {
	    // This should never happen.
	    return null;
	}
	return (outStream.toString());
    }

}
