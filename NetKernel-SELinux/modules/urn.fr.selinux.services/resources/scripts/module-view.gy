/**
 * Ce module appelle res:/services/policies/<policyname>. Si le status est "error", l'erreur est remontée.
 * Sinon (pas d'erreur), on essaye d'accéder sur le disque au répertoire resources/data/<policyname>/<modulename>
 * Ce répertoire doit contenir un module SELinux (sur CentOS, voir /etc/selinux/targeted/active/modules/100/*).
 *
 * Structure XML en sortie :
 * <output>
 *   <status>success|error<status>
 *   <code>200|404|500</code>
 *   <content>
 *     <url>/cil</url>
 *     <url>/query/</url>
 *     <url>/query/[instruction]</url>
 *     <url>/lookup/[symbol]</url>
 *     <url>/dependencies</url>
 *     <url>/dependencies/details</url>
 *     <url>/optionals</url>
 *     <url>/optionals/[optional_id]</url>
 *   </content>
 * </output>
 */
import java.net.URI;
import org.netkernel.mod.hds.IHDSDocument;
import org.netkernel.mod.hds.HDSFactory;
import org.netkernel.mod.hds.IHDSMutator;

// Parameter 'policyname'
String paramPolicyName = context.source("arg:policyname");

// Parameter 'modulename'
String paramModuleName = context.source("arg:modulename");

// ----------------------------------------------------------------------------------------------
// Returns the XML output for the various errors.
private void returnError(String aErrorCode, String aDetail) {
    IHDSMutator m = HDSFactory.newDocument();
    m.pushNode("output").addNode("status","error").addNode("code",aErrorCode).addNode("detail",aDetail);
    IHDSDocument representation=m.toDocument(false); //don't clone
    myResponse = context.createResponseFrom(representation);
    myResponse.setHeader("httpResponse:/code", aErrorCode);
}
// ----------------------------------------------------------------------------------------------

// We use the "res:/services/policies/<policyname>" service to check if the policy name parameter is ok
IHDSDocument myDocumentPolicy = context.source("res:/services/policies/"+paramPolicyName, IHDSDocument.class);

// If the returned policy does not contain <status>success</status>, we return the same output as the service we called
if (! myDocumentPolicy.getReader().getFirstValue("/output/status").equals("success")) {
    myResponse = context.createResponseFrom(myDocumentPolicy);
    // Propagation of the received HTTP response code
    myResponse.setHeader("httpResponse:/code", myDocumentPolicy.getReader().getFirstValue("/output/code"));
}
else {
    // The policyname parameter is ok

    // We retrieve the absolute local path to the policy, in the @localpath attribute
    URI myPolicyLocalPathURI = myDocumentPolicy.getReader().getFirstValue("/output/content/@localpath");

    // We create the path to the module
    URI mySELinuxModuleDirectoryURI = myPolicyLocalPathURI.resolve(paramPolicyName+"/"+paramModuleName);

    // We convert the URI to File
    File mySELinuxModuleDirectory = new File(mySELinuxModuleDirectoryURI);

    if (! mySELinuxModuleDirectory.exists()) {
        returnError("404", "SELinux module not found: \""+paramModuleName+"\"");
    }
    else {
        // We want to read the "<moduleName>/cil" file
        URI myCilURI = mySELinuxModuleDirectoryURI.resolve(paramModuleName+"/cil");
        // We convert the URI to File
        File myCilFile = new File(myCilURI);

        // Very strange case: the <moduleName> directory exists, but the "cil" file doesn't...
        if (! myCilFile.exists()){
            returnError("500", "File not found \"" + paramModuleName + "/cil\"");
        }
        else {
            String myReturnCode = "200"
            IHDSMutator m = HDSFactory.newDocument();
            m.pushNode("output").addNode("status","success").addNode("code",myReturnCode);
            m.pushNode("content");
            m.addNode("@localpath",mySELinuxModuleDirectoryURI);
            m.addNode("url","/cil");
            m.addNode("url","/query/");
            m.addNode("url","/query/<instruction>");
            m.addNode("url","/lookup/<symbol>");
            m.addNode("url","/dependencies");
            m.addNode("url","/dependencies/details");
            m.addNode("url","/dependents");
            m.addNode("url","/dependents/details");
            m.addNode("url","/optionals");
            m.addNode("url","/optionals/<optional_id>");
            m.popNode();
            IHDSDocument representation=m.toDocument(false); //don't clone
            myResponse = context.createResponseFrom(representation);
            myResponse.setHeader("httpResponse:/code", myReturnCode);
        }
    }
}

