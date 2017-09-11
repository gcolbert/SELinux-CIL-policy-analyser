package fr.tools.sexpr.parser.accessor;

import org.netkernel.layer0.nkf.INKFRequestContext;
import org.netkernel.mod.hds.HDSFactory;
import org.netkernel.mod.hds.IHDSDocument;
import org.netkernel.mod.hds.IHDSMutator;
import org.netkernel.module.standard.endpoint.StandardAccessorImpl;

import fr.tools.sexpr.parser.LispParser;
import fr.tools.sexpr.parser.LispTokenizer;

public class SExprParserAccessor extends StandardAccessorImpl {

	public void onSource(INKFRequestContext context) throws Exception {
		// Support one or more operand argument instances
		int numberOfArguments = context.getThisRequest().getArgumentCount();
		String argumentName = "";
		String argumentValue = "";
		String operand = null;
		String module = null;

		for (int i = 0; i < numberOfArguments; i++) {
			argumentName = context.getThisRequest().getArgumentName(i);
			if ("operand".equals(argumentName)) {
				argumentValue = context.getThisRequest().getArgumentValue(i);
				operand = context.source(argumentValue, String.class);
			}
			if ("module".equals(argumentName)) {
				argumentValue = context.getThisRequest().getArgumentValue(i);
				module = context.source(argumentValue, String.class);
			}
		}

		// Set up base of returned representation
		IHDSMutator m = HDSFactory.newDocument();
		m.pushNode("output");
		m.addNode("status", "success");
		m.addNode("code", new Integer(200).toString());
		m.pushNode("content");

		// Create new LispParser instance
		LispParser parser = new LispParser(new LispTokenizer(operand), m);

		// We parse all S-Expressions, which fills the IHDSMutator
		// (the exprCounter is the number of expressions parsed)
		Integer exprCounter = parser.parseAllExprInAList();
		m.addNode("@matched", exprCounter.toString()).addNode("@total", exprCounter.toString());

		for (IHDSMutator mutator : m.getNodes("/output/content/l")) {
			mutator.addNode("@m", module);
		}

		// The elements inside "optional" blocks are tagged with the "o" attribute
		// containing the value of the optional block.
		for (IHDSMutator mutator : m.getNodes("/output/content//l[a='optional']")) {
			Object optionalBlockName = mutator.getFirstValue("a[2]");
			for (IHDSMutator mutator2 : mutator.getNodes("descendant::l")) {
				mutator2.addNode("@m", module).addNode("@o", optionalBlockName);
			}
		}

		// The elements inside "booleanif" blocks are tagged with the "b" attribute
		// containing the name of the boolean.
		for (IHDSMutator mutator : m.getNodes("/output/content//l[a='booleanif']")) {
			Object booleanifArgument = mutator.getFirstValue("l/a[1]");
			String booleanExpression;
			// We found an expression instead of a boolean :(
			if (booleanifArgument.equals("and") || booleanifArgument.equals("or")
					|| booleanifArgument.equals("xor") || booleanifArgument.equals("eq")
					|| booleanifArgument.equals("neq")) {
				// TODO : be able to handle boolean expressions that use more than two booleans
				booleanExpression = mutator.getFirstValue("l/l/a[1]") + " " + booleanifArgument.toString() + " "
						+ mutator.getFirstValue("l/l[2]/a[1]");
			}
			else if (booleanifArgument.equals("not")) {
				booleanExpression = "not ( " + mutator.getFirstValue("l/l/a[1]") + " )";
			}
			else {
				// We found a simple boolean :)
				booleanExpression = booleanifArgument.toString();
			}
			for (IHDSMutator mutator2 : mutator.getNodes("descendant::l[a='true']")) {
				mutator2.addNode("@m", module).addNode("@b", booleanExpression);
				for (IHDSMutator mutator3 : mutator2.getNodes("descendant::l")) {
					mutator3.addNode("@m", module).addNode("@b", booleanExpression);
				}				
			}
			for (IHDSMutator mutator2 : mutator.getNodes("descendant::l[a='false']")) {
				mutator2.addNode("@m", module).addNode("@b", "not ( " + booleanExpression + " )");
				for (IHDSMutator mutator3 : mutator2.getNodes("descendant::l")) {
					mutator3.addNode("@m", module).addNode("@b", "not ( " + booleanExpression + " )");
				}				
			}
		}

		// We need to declare keys to fill some indexes that will allow fast querying.
		// The "| false()" is necessary because for some nodes, the XPath won't match.
		// Without these, the declareKey expressions may fail miserably with e.g. :
		//     org.apache.commons.jxpath.JXPathNotFoundException
		//     No value for xpath: a[position()=2]
		m.declareKey("onInstructionName", "//l",             "a[1] | false()");
		m.declareKey("onArg1Value",       "//l",             "a[2] | false()");
		m.declareKey("onArg2Value",       "//l",             "a[3] | false()");
	    m.declareKey("onSymbol",          "//l[count(*)=2]", "a[2] | false()");
	    m.declareKey("onSymbolRequires",  "//l[a[2]='cil_gen_require']", "a[3] | false()");
	    m.declareKey("onAccessRules",     "//l[a[1]='allow' or a[1]='neverallow']", "a[2] | a[3] | false()");
	    m.declareKey("onAuditRules",      "//l[a[1]='auditallow' or a[1]='dontaudit']", "a[2] | a[3] | false()");

		IHDSDocument representation = m.toDocument(false); // don't clone
		context.createResponseFrom(representation);

	}

}
