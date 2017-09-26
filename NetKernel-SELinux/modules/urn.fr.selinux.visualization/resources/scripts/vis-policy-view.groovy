/**
 * Ce script appelle "/services/policies/<policyname>".
 * Si la politique existe, il affiche la liste des visualisations disponibles.
 * Si elle n'existe pas, l'erreur est remontée.
 */
import org.netkernel.mod.hds.IHDSDocument;
import org.netkernel.mod.hds.HDSFactory;
import org.netkernel.mod.hds.IHDSMutator;

// Parameter 'policyname'
String paramPolicyName = context.source("arg:policyname");

// ----------------------------------------------------------------------------------------------

// We use the "res:/services/policies/<policyname>" service to check if the parameters are ok
IHDSDocument myResource = context.source("res:/services/policies/"+paramPolicyName, IHDSDocument.class);

// If the returned resource does not contain <status>success</status>, we return the same output as the service we called
if (! myResource.getReader().getFirstValue("/output/status").equals("success")) {
    myResponse = context.createResponseFrom(myResource);
    // Propagation of the received HTTP response code
    myResponse.setHeader("httpResponse:/code", myResource.getReader().getFirstValue("/output/code"));
}
else {
    IHDSMutator m = HDSFactory.newDocument();

    // We output the success XML
    m.pushNode("output").addNode("status","success").addNode("code","200");
    m.pushNode("content");
    m.addNode("url","/modules/<modulename>/dependents/");
    m.addNode("url","/modules/<modulename>/dependents/data");
    m.addNode("url","/modules/<modulename>/dependents/data.xml");
    m.addNode("url","/modules/<modulename>/dependencies/");
    m.addNode("url","/modules/<modulename>/dependencies/data");
    m.addNode("url","/modules/<modulename>/dependencies/data.xml");
    m.pushNode("url","/dependencies/<modulename1>+<modulename2>/").addNode("@info", "experimental").addNode("@example","/dependencies/ftp+mysql/").popNode();

    IHDSDocument representation=m.toDocument(false); //don't clone
    myResponse = context.createResponseFrom(representation);
}
