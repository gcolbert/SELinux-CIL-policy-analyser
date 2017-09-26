/**
 * Ce module lit "res:/services/policies/<policyname>/modules/<modulename>" pour vérifier la validité des paramètres.
 * Si le statut remonté est "error", l'erreur sous-jacente est remontée.
 * Si le statut est "success", on décompresse en mémoire le contenu du fichier "cil"
 * et on renvoie un IHDSDocument contenant dans "/output/content" toutes les lignes CIL non transformées.
 */
import java.net.URI;
import org.netkernel.mod.hds.IHDSDocument;
import org.netkernel.mod.hds.HDSFactory;
import org.netkernel.mod.hds.IHDSMutator;

// Parameter 'policyname'
String paramPolicyName = context.source("arg:policyname");

// Parameter 'modulename'
String paramModuleName = context.source("arg:modulename");

// We use the "res:/services/policies/<policyname>/modules/<modulename>" service to check if the parameters are ok
IHDSDocument myDocumentModule = context.source("res:/services/policies/"+paramPolicyName+"/modules/"+paramModuleName, IHDSDocument.class)

// If the returned resource does not contain <status>success</status>, we return the same output as the service we called
if (! myDocumentModule.getReader().getFirstValue("/output/status").equals("success")) {
    myResponse = context.createResponseFrom(myDocumentModule)
    myResponse.setNoCache();
    // Propagation of the received HTTP response code
    myResponse.setHeader("httpResponse:/code", myDocumentModule.getReader().getFirstValue("/output/code"));
}
else {
    // Parameters are ok
    // Now we want to read the "cil" file
    // We retrieve the absolute local path to the module, in the @localpath attribute
    URI myPolicyLocalPathURI = myDocumentModule.getReader().getFirstValue("/output/content/@localpath");
    URI myCilURI = myPolicyLocalPathURI.resolve(paramModuleName+"/cil");
    File myCilFile = new File(myCilURI);

    // We need to uncompress the "cil" file with bunzip2
    request = context.createRequest("active:bunzip2");
    request.addArgumentByValue("operand", myCilFile);
    queryResult = context.issueRequest(request);

    // We create our base representation
    String myReturnCode = "200"
    IHDSMutator m = HDSFactory.newDocument();
    m.pushNode("output").addNode("status","success").addNode("code",myReturnCode);
    m.addNode("content",queryResult);

    // We return the representation as a IHDSDocument
    IHDSDocument representation = m.toDocument(false); //don't clone
    myResponse = context.createResponseFrom(representation);
    myResponse.setHeader("httpResponse:/code", myReturnCode);
}

