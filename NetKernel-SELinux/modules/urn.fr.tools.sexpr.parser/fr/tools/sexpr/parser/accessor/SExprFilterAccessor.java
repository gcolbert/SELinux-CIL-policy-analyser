package fr.tools.sexpr.parser.accessor;

import java.util.List;

import org.netkernel.layer0.nkf.INKFRequest;
import org.netkernel.layer0.nkf.INKFRequestContext;
import org.netkernel.mod.hds.HDSFactory;
import org.netkernel.mod.hds.IHDSDocument;
import org.netkernel.mod.hds.IHDSMutator;
import org.netkernel.mod.hds.IHDSReader;
import org.netkernel.module.standard.endpoint.StandardAccessorImpl;

public class SExprFilterAccessor extends StandardAccessorImpl {

	public void onSource(INKFRequestContext context) throws Exception {
		// Support one or more operand argument instances
		int numberOfArguments = context.getThisRequest().getArgumentCount();
		String argumentName = "";
		String argumentValue = "";
		String operand = null;
		String filterType = null;
		String filterValue = null;
		String module = null;

		for (int i = 0; i < numberOfArguments; i++) {
			argumentName = context.getThisRequest().getArgumentName(i);
			if ("operand".equals(argumentName)) {
				argumentValue = context.getThisRequest().getArgumentValue(i);
				operand = context.source(argumentValue, String.class);
			}
			if ("filter-type".equals(argumentName)) {
				argumentValue = context.getThisRequest().getArgumentValue(i);
				filterType = context.source(argumentValue, String.class);
			}
			if ("filter-value".equals(argumentName)) {
				argumentValue = context.getThisRequest().getArgumentValue(i);
				filterValue = context.source(argumentValue, String.class);
			}
			if ("module".equals(argumentName)) {
				argumentValue = context.getThisRequest().getArgumentValue(i);
				module = context.source(argumentValue, String.class);
			}
		}

		// We call the parser
		INKFRequest request = context.createRequest("active:s-expression-parser");
		request.addArgumentByValue("operand", operand);
		request.addArgumentByValue("module", module);
		IHDSDocument hdsDocument = (IHDSDocument) context.issueRequest(request);

		// If "filterValue" argument is empty, then no filtering is necessary
		if ("".equals(filterValue)) {
			// If there is no value to filter,
			// then we return the whole parsed document.
			// In this case, we don't need to recreate the indexes.
			context.createResponseFrom(hdsDocument);
		} else {
			Integer errorCode;
			IHDSMutator m = HDSFactory.newDocument();
			m.pushNode("output");
			if ((!"onInstructionName".equals(filterType))
					&& (!"onArg1Value".equals(filterType)) && (!"onArg2Value".equals(filterType))
					&& (!"onSymbol".equals(filterType)) && (!"onSymbolRequires".equals(filterType))
					&& (!"onAccessRules".equals(filterType)) && (!"onAuditRules".equals(filterType))
					) {
				errorCode = 400; // "400" is "Bad request" in HTTP
				// Set up base of returned representation
				m.addNode("status", "error");
				m.addNode("code", errorCode.toString());
				m.pushNode("detail",
						"The 'filter-type' argument must be 'onInstructionName', "
						+ "'onArg1Value', 'onArg2Value', "
						+ "'onSymbol', 'onSymbolRequires', "
						+ "'onAccessRules' or 'onAuditRules'. "
						+ "Received value is '" + filterType + "'.");
			} else {
				errorCode = 200;
				// Set up base of returned representation
				m.addNode("status", "success");
				m.addNode("code", errorCode.toString());
				m.pushNode("content");

				// We use the arguments "filter-type" and "filter-value" to
				// filter quickly the returned nodes
				List<IHDSReader> listReaders = hdsDocument.getReader()
						.getNodes("key('" + filterType + "','" + filterValue + "')");
				m.addNode("@matched", String.valueOf(listReaders.size()));
				m.addNode("@total", hdsDocument.getReader().getFirstValue("/output/content/@total").toString());
				for (IHDSReader listReader : listReaders) {
					m.append(listReader).popNode();
				}

			    // We recreate the various indexes on the filtered document
			    m.declareKey("onInstructionName", "//l",                                        "a[1] | false()");
			    m.declareKey("onArg1Value",       "//l",                                        "a[2] | false()");
			    m.declareKey("onArg2Value",       "//l",                                        "a[3] | false()");
			    m.declareKey("onSymbol",          "//l[count(*)=2]",                            "a[2] | false()");
			    m.declareKey("onSymbolRequires",  "//l[a[2]='cil_gen_require']",                "a[3] | false()");
			    m.declareKey("onAccessRules",     "//l[a[1]='allow' or a[1]='neverallow']",     "a[2] | a[3] | false()");
			    m.declareKey("onAuditRules",      "//l[a[1]='auditallow' or a[1]='dontaudit']", "a[2] | a[3] | false()");

			}
			IHDSDocument representation = m.toDocument(false); // don't clone
			context.createResponseFrom(representation);
		}

	}

}
