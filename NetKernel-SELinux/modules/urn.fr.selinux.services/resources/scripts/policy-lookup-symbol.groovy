/**
 * Ce script recherche le symbole indiqué, dans la politique dont l'identifiant est passé en paramètre.
 * 
 * Il utilise l'index "onSymbol" sur res:/services/policies/<policyname>/query/
 * puis renvoie l'instruction définissant cet identifiant.
 */
import org.netkernel.mod.hds.IHDSDocument;
import org.netkernel.mod.hds.HDSFactory;
import org.netkernel.mod.hds.IHDSMutator;
import org.netkernel.mod.hds.IHDSReader;

// Parameter 'policyname'
String paramPolicyName = context.source("arg:policyname");

// Parameter 'symbol'
String paramSymbol = context.source("arg:symbol");

// ----------------------------------------------------------------------------------------------

// We use the "res:/services/policies/<policyname>/query/" as a source
IHDSDocument myPolicy = context.source("res:/services/policies/"+paramPolicyName+"/query/", IHDSDocument.class);

// If the returned resource does not contain <status>success</status>, we return the same output as the service we called
if (! myPolicy.getReader().getFirstValue("/output/status").equals("success")) {
    myResponse = context.createResponseFrom(myPolicy)
    // Propagation of the received HTTP response code
    myResponse.setHeader("httpResponse:/code", myPolicy.getReader().getFirstValue("/output/code"));
}
else {
    String myReturnCode = "200";
    IHDSMutator m = HDSFactory.newDocument();
    m.pushNode("output").addNode("status","success").addNode("code",myReturnCode);
    m.pushNode("content");
    // We query the symbol
	List<IHDSReader> listReaders = myPolicy.getReader().getNodes("key('onSymbol','"+paramSymbol+"')");
    m.addNode("@matched", listReaders.size()).addNode("@total", myPolicy.getReader().getFirstValue("/output/content/@total").toString());
    for (IHDSReader listReader : listReaders) {
        m.append(listReader).popNode();
    }

    IHDSDocument representation=m.toDocument(false); //don't clone
    context.createResponseFrom(representation);
}

