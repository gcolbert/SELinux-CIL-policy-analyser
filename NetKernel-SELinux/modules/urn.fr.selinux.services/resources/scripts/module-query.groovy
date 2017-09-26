/**
 * Ce module lit "res:/services/policies/<policyname>/modules/<modulename>/cil".
 * Si le document ne contient pas <status>success</status>, l'erreur est remontée.
 * Sinon (pas d'erreur), on appelle "active:s-expression-parser-with-filter"
 * qui filtre les instructions, pour renvoyer seulement celles dont le type
 * est égal à la valeur du paramètre <queryname>.
 * Remarque: Si "queryname" est vide, toutes les instructions sont renvoyées.
 */
import org.netkernel.mod.hds.IHDSDocument;
import org.netkernel.mod.hds.IHDSMutator;

// Parameter 'policyname'
String paramPolicyName = context.source("arg:policyname");

// Parameter 'modulename'
String paramModuleName = context.source("arg:modulename");

// Parameter 'queryname' (it can be non-existent if we call "/query/")
String paramQueryName = "";
if (context.exists('arg:queryname')) {
    paramQueryName = context.source("arg:queryname");
}

// ----------------------------------------------------------------------------------------------

// We use the "res:/services/policies/<policyname>/modules/<modulename>/cil" service to check if the parameters are ok
IHDSDocument myModuleCil = context.source("res:/services/policies/"+paramPolicyName+"/modules/"+paramModuleName+"/cil", IHDSDocument.class);

// If the returned resource does not contain <status>success</status>, we return the same output as the service we called
if (! myModuleCil.getReader().getFirstValue("/output/status").equals("success")) {
    myResponse = context.createResponseFrom(myModuleCil);
    // Propagation of the received HTTP response code
    myResponse.setHeader("httpResponse:/code", myModuleCil.getReader().getFirstValue("/output/code"));
}
else {
    String myCil = myModuleCil.getReader().getFirstValue("/output/content")

    // We call the S-Expression Parser with Filter
    request = context.createRequest("active:s-expression-parser-with-filter");
    request.addArgumentByValue("operand", myCil);
    request.addArgumentByValue("filter-type", "onInstructionName");
    request.addArgumentByValue("filter-value", paramQueryName);
    request.addArgumentByValue("module", paramModuleName);
    IHDSDocument queryResult = context.issueRequest(request);

    myResponse = context.createResponseFrom( queryResult );

}

