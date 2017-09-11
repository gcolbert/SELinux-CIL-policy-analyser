package fr.tools.sexpr.parser;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.Reader;
import java.io.StreamTokenizer;
import java.io.StringReader;
import java.util.Iterator;
import java.util.NoSuchElementException;

public class LispTokenizer implements Iterator<Token> {

	private StreamTokenizer streamTokenizer;

	/**
	 * Constructs a tokenizer that scans input from the given string.
	 * 
	 * @param src
	 *            A string containing S-expressions.
	 */
	public LispTokenizer(String src) {
		this(new StringReader(src));
	}

	/**
	 * Constructs a tokenizer that scans input from the given Reader.
	 * 
	 * @param r
	 *            Reader for the character input source
	 */
	private LispTokenizer(Reader r) {
		if (r == null) {
			r = new StringReader("");
		}
		BufferedReader buffrdr = new BufferedReader(r);
		streamTokenizer = new StreamTokenizer(buffrdr);
		streamTokenizer.resetSyntax(); // We don't like the default settings

		streamTokenizer.whitespaceChars(0, ' ');
		streamTokenizer.wordChars(' ' + 1, 255);
		streamTokenizer.ordinaryChar('(');
		streamTokenizer.ordinaryChar(')');
		streamTokenizer.ordinaryChar('\'');
		streamTokenizer.commentChar(';');
		streamTokenizer.quoteChar('"');
	}

	/**
	 * Returns the Token that will be read by the next call to next(),
	 * or null if the end of the stream has been read.
	 * @return the next Token that will be returned by next().
	 * @throws IOException
	 */
	public Token peekToken() throws IOException {
		streamTokenizer.nextToken();
		if (streamTokenizer.ttype == StreamTokenizer.TT_EOF) {
			return null;
		}
		else {
			Token token = new Token(streamTokenizer.lineno(), streamTokenizer.ttype, streamTokenizer.sval);
			streamTokenizer.pushBack();
			return token;
		}
	}

	/**
	 * Returns true if there is a next Token, otherwise false.
	 */
	@Override
	public boolean hasNext() {
		try {
			streamTokenizer.nextToken();
		}
		catch(IOException e) {
			return false;
		}
		if (streamTokenizer.ttype == StreamTokenizer.TT_EOF) {
			return false;
		}
		else {
			streamTokenizer.pushBack();
			return true;
		}
	}

	/**
	 * Returns the next Token parsed by the inner StreamTokenizer
	 */
	@Override
	public Token next() throws NoSuchElementException {
		try {
			streamTokenizer.nextToken();
		} catch (IOException e) {
			throw new NoSuchElementException();
		}

		return new Token(streamTokenizer.lineno(), streamTokenizer.ttype, streamTokenizer.sval);
	}

	/**
	 * Not implemented.
	 */
	@Override
	public void remove() {
	}
}
