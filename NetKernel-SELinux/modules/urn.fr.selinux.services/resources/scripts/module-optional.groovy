/**
 * Ce script affiche le bloc optional dont l'identifiant est fourni, ou ne renvoie rien s'il n'existe pas dans le module indiqué.
 */
import org.netkernel.mod.hds.IHDSDocument;
import org.netkernel.mod.hds.HDSFactory;
import org.netkernel.mod.hds.IHDSMutator;
import org.netkernel.mod.hds.IHDSReader;

// Parameter 'policyname'
String paramPolicyName = context.source("arg:policyname");

// Parameter 'modulename'
String paramModuleName = context.source("arg:modulename");

// Parameter 'optionalname'
String paramOptionalName = context.source("arg:optionalname");

// ----------------------------------------------------------------------------------------------

// We use the "res:/services/policies/<policyname>/modules/<modulename>/optionals" service to check if the parameters are ok
IHDSDocument myResource = context.source("res:/services/policies/"+paramPolicyName+"/modules/"+paramModuleName+"/optionals", IHDSDocument.class);

// If the returned resource does not contain <status>success</status>, we return the same output as the service we called
if (! myResource.getReader().getFirstValue("/output/status").equals("success")) {
    myResponse = context.createResponseFrom(myResource);
    // Propagation of the received HTTP response code
    myResponse.setHeader("httpResponse:/code", myResource.getReader().getFirstValue("/output/code"));
}
else {
    // We output the success XML
    IHDSMutator m = HDSFactory.newDocument();
    m.pushNode("output").addNode("status","success").addNode("code","200");
    m.pushNode("content");

    IHDSReader myOptional = myResource.getReader().getFirstNodeOrNull("key('onArg1Value', '"+paramOptionalName+"')");

    if (myOptional != null) {
        m.append(myOptional).popNode();
        m.addNode("@matched","1");
    }
    else {
        m.addNode("@matched","0");
    }

    IHDSDocument representation=m.toDocument(false); //don't clone
    myResponse = context.createResponseFrom(representation);
}

