package fr.tools.sexpr.parser;

import java.io.StreamTokenizer;

public class Token {

	private static final int SYMBOL = StreamTokenizer.TT_WORD;

	private int type;
	private String text;
	private int lineNumber;

	/**
	 * Constructs a new Token
	 * @param lineNumber The line number of the new Token (relatively to the start of the stream)
	 * @param type Determines the type of the Token (see the constants defined in StreamTokenizer class)
	 * @param text If the Token is a word token, this field contains the characters of the word
	 */
	public Token(int lineNumber, int type, String text) {
		this.lineNumber = lineNumber;
		this.type = type;
		this.text = text;
	}
	
	@Override
	public String toString() {
		switch (this.type) {
		case SYMBOL:
		case '"':
			return this.text;
		default:
			return String.valueOf((char) this.type);
		}
	}

	public int getType() {
		return type;
	}

	public String getText() {
		return text;
	}

	public int getLineNumber() {
		return lineNumber;
	}

}