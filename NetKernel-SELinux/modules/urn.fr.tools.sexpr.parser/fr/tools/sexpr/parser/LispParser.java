package fr.tools.sexpr.parser;

import java.io.IOException;

import org.netkernel.mod.hds.IHDSMutator;

public class LispParser {

	private LispTokenizer lispTokenizer;
	
	private IHDSMutator hdsMutator = null;
	
	public LispParser(LispTokenizer lispTokenizer, IHDSMutator hdsMutator) {
		this.lispTokenizer = lispTokenizer;
		this.hdsMutator = hdsMutator;
	}

	public class ParseException extends Exception {

		/**
		 * SerialVersionUID
		 */
		private static final long serialVersionUID = -2517041915424066684L;

	}

	/**
	 * Asks the LispTokenizer to parse all the S-Expressions it can find.
	 * Inner S-Expressions are dealt with a recursive function called parseExpr().
	 * The function returns the number of S-Expressions found at the root (the inner
	 * S-Expressions do not increment the counter).
	 * 
	 * @return the number of S-Expressions parsed
	 * @throws ParseException
	 */
	public Integer parseAllExprInAList() throws ParseException {
		Integer exprCounter = 0;
		while (lispTokenizer.hasNext()) {
			exprCounter++;
			parseExpr(0);
			hdsMutator.setCursor("/output/content/l[last()]").addNode("@m-expr",exprCounter).popNode();
		}
		return exprCounter;
	}

	/**
	 * Calls the LispTokenizer and returns the next S-Expression 
	 * @return the next S-Expression of the LispTokenizer
	 * @throws ParseException
	 */
	private Integer parseExpr(Integer currentLineNumber) throws ParseException {
		Token token = lispTokenizer.next();
		switch (token.getType()) {
		case '(':
			hdsMutator.pushNode("l");
			parseExprList(token.getLineNumber());
			break;
		case '"':
			hdsMutator.pushNode("s", token.getText());
			break;
		default:
			hdsMutator.pushNode("a", token.getText());
			break;
		}
		// We create the attribute "module-line" on the current token
		// only if the line has changed
		if (currentLineNumber < token.getLineNumber()) {
			hdsMutator.addNode("@m-line", token.getLineNumber());
		}
		hdsMutator.popNode();
		return token.getLineNumber();
	}

	/**
	 * Recursive function to parse a List
	 * @param lineNumber The expression list will be tagged with this lineNumber
	 * @return a ExprList 
	 * @throws ParseException
	 */
	private void parseExprList(int currentListLineNumber) throws ParseException {
		try {
			Integer currentLineNumber = currentListLineNumber;
			// While there is no closing parenthesis...
			while (lispTokenizer.peekToken().getType() != ')') {
				currentLineNumber = parseExpr(currentLineNumber);
			}
		}
		catch (IOException e) {
			throw new ParseException();
		}
		// We have reached the closing parenthesis, which we ignore
		lispTokenizer.next();
	}

}