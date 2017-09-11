/**
 * Ce module lit "res:/services/policies/<policyname>/modules". Si le document ne contient
 * pas <status>success</status>, l'erreur est remontée.
 * Sinon (pas d'erreur), il interroge chaque module avec :
 * "res:/services/policies/<policyname>/modules/<modulename>/query/<queryname>"
 * puis concatène tous les résultats obtenus.
 */
import org.netkernel.mod.hds.IHDSDocument;
import org.netkernel.mod.hds.HDSFactory;
import org.netkernel.mod.hds.IHDSMutator;
import org.netkernel.mod.hds.IHDSReader;

// Parameter 'policyname'
String paramPolicyName = context.source("arg:policyname");

// Parameter 'queryname' (it can be non-existent if we call "/query/")
String paramQueryName = "";
if (context.exists('arg:queryname')) {
    paramQueryName = context.source("arg:queryname");
}

// ----------------------------------------------------------------------------------------------

// We use "res:/services/policies/<policyname>/modules" to find the names of the modules
IHDSDocument myModules = context.source("res:/services/policies/"+paramPolicyName+"/modules", IHDSDocument.class)

// If the returned resource does not contain <status>success</status>, we return the same output as the service we called
if (! myModules.getReader().getFirstValue("/output/status").equals("success")) {
    myResponse = context.createResponseFrom(myModules)
    // Propagation of the received HTTP response code
    myResponse.setHeader("httpResponse:/code", myModules.getReader().getFirstValue("/output/code"));
}
else {
    IHDSMutator m = HDSFactory.newDocument();
    m.pushNode("output").addNode("status","success").addNode("code","200");
    m.pushNode("content");

    Integer matched = 0;
    Integer total = 0;

    // We query each module
    for (String currentModuleName : myModules.getReader().getValues("/output/content/m")) {
        IHDSDocument myModuleQueryResult = context.source("res:/services/policies/"+paramPolicyName+"/modules/"+currentModuleName+"/query/", IHDSDocument.class);
        if (! myModuleQueryResult.getReader().getFirstValue("/output/status").equals("success")) {
            myResponse = context.createResponseFrom(myModuleQueryResult)
            // Propagation of the received HTTP response code
            myResponse.setHeader("httpResponse:/code", myModuleQueryResult.getReader().getFirstValue("/output/code"));
        }
        else {
            List<IHDSReader> listReaders;
            if (paramQueryName!="") {
                // We query the index "onInstructionName" on the current module if a query was made
                listReaders = myModuleQueryResult.getReader().getNodes("key('onInstructionName', '"+paramQueryName+"')");
            }
            else {
                // otherwise, we just retrieve all the nodes
                listReaders = myModuleQueryResult.getReader().getNodes("/output/content");
            }
            // We append the nodes to the output document
            for (IHDSReader listReader : listReaders) {
                m.append(listReader).popNode();
            }
            matched += listReaders.size();
            total += Integer.parseInt(myModuleQueryResult.getReader().getFirstValue("/output/content/@total"));
        }
    }
    m.addNode("@matched", matched).addNode("@total", total);

    // We recreate the various indexes on the policy
    m.declareKey("onInstructionName", "//l",                                        "a[1] | false()");
    m.declareKey("onArg1Value",       "//l",                                        "a[2] | false()");
    m.declareKey("onArg2Value",       "//l",                                        "a[3] | false()");
    m.declareKey("onSymbol",          "//l[count(*)=2]",                            "a[2] | false()");
    m.declareKey("onSymbolRequires",  "//l[a[2]='cil_gen_require']",                "a[3] | false()");
    m.declareKey("onAccessRules",     "//l[a[1]='allow' or a[1]='neverallow']",     "a[2] | a[3] | false()");
    m.declareKey("onAuditRules",      "//l[a[1]='auditallow' or a[1]='dontaudit']", "a[2] | a[3] | false()");

    IHDSDocument representation=m.toDocument(false); //don't clone
    context.createResponseFrom(representation);
}

