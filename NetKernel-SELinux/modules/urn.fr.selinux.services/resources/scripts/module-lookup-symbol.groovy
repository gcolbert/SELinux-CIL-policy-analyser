/**
 * Ce script recherche le symbole indiqué, dans un module de la politique passée en paramètre.
 * 
 * Il appelle active:s-expression-parser-with-filter puis renvoie l'instruction définissant l'identifiant recherché,
 * ou rien si l'identifiant recherché n'est pas défini dans ce module.
 */
import org.netkernel.layer0.nkf.INKFRequest;
import org.netkernel.mod.hds.IHDSDocument;
import org.netkernel.mod.hds.HDSFactory;
import org.netkernel.mod.hds.IHDSMutator;
import org.netkernel.mod.hds.IHDSReader;

// Parameter 'policyname'
String paramPolicyName = context.source("arg:policyname");

// Parameter 'modulename'
String paramModuleName = context.source("arg:modulename");

// Parameter 'symbol'
String paramSymbol = context.source("arg:symbol");

// ----------------------------------------------------------------------------------------------

// We use the "res:/services/policies/<policyname>/modules/<modulename>/cil" service to check if the parameters are ok
IHDSDocument myResource = context.source("res:/services/policies/"+paramPolicyName+"/modules/"+paramModuleName+"/cil", IHDSDocument.class);

// If the returned resource does not contain <status>success</status>, we return the same output as the service we called
if (! myResource.getReader().getFirstValue("/output/status").equals("success")) {
    myResponse = context.createResponseFrom(myResource);
    // Propagation of the received HTTP response code
    myResponse.setHeader("httpResponse:/code", myResource.getReader().getFirstValue("/output/code"));
}
else {
    String myCil = myResource.getReader().getFirstValue("/output/content");

    // We are interested by nodes that have an atom child ("a") with arg1 equal to paramSymbol
    INKFRequest myRequestToParserFilter = context.createRequest("active:s-expression-parser-with-filter");
    myRequestToParserFilter.addArgumentByValue("operand", myCil);
    myRequestToParserFilter.addArgumentByValue("filter-type", "onSymbol");
    myRequestToParserFilter.addArgumentByValue("filter-value", paramSymbol);
    myRequestToParserFilter.addArgumentByValue("module", paramModuleName);
    IHDSDocument myResponseFromParserFilter = context.issueRequest(myRequestToParserFilter);

    if (myResponseFromParserFilter.getReader().getFirstValue("/output/status").equals("success")) {
        // The onSymbol filter-type, that we used, guarantees that the returned "l" nodes
        // have only two children.
        // We know from CIL that symbols are declared in single argument instructions
        // (e.g. "user root", "role object_r", "type abrt_t", etc).
        IHDSReader mySymbolInstruction = myResponseFromParserFilter.getReader().getFirstNodeOrNull("/output/content/l");

        String myReturnCode = "200";
        IHDSMutator m = HDSFactory.newDocument();
        m.pushNode("output").addNode("status","success").addNode("code",myReturnCode);
        m.pushNode("content");
        m.addNode("@total",myResponseFromParserFilter.getReader().getFirstValue("/output/content/@total"));
        if (mySymbolInstruction != null) {
            m.addNode("@matched","1");
            m.append(mySymbolInstruction);
        }
        else {
            m.addNode("@matched","0");
        }

        IHDSDocument representation=m.toDocument(false); //don't clone
        myResponse = context.createResponseFrom(representation);
        myResponse.setHeader("httpResponse:/code", myReturnCode);
    }
    else {
        myResponse = context.createResponseFrom(myResponseFromParserFilter);
        // Propagation of the received HTTP response code
        myResponse.setHeader("httpResponse:/code", myResponseFromParserFilter.getReader().getFirstValue("/output/code"));
    }
}

