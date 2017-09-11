/**
 * Ce module vérifie qu'il existe un dossier "resources/data/<policyname>".
 * Par exemple: "resources/data/targeted"
 * Chaque répertoire doit contenir à son tour des sous-répertoires correspondant aux modules
 * de la politique considérée.
 */
import org.netkernel.layer0.nkf.INKFRequestContext;
import org.netkernel.module.standard.StandardSpace;
import org.netkernel.module.standard.StandardModule;
import org.netkernel.urii.ISpace;
import org.netkernel.request.IRequestScopeLevel;
import java.net.URI;
import org.netkernel.mod.hds.IHDSDocument;
import org.netkernel.mod.hds.HDSFactory;
import org.netkernel.mod.hds.IHDSMutator;

// Parameter 'policyname'
String paramPolicyName = context.source("arg:policyname");

// ----------------------------------------------------------------------------------------------
// Returns the URI of the owning module. It allows us to access resources using a relative path.
private URI getModuleURI(INKFRequestContext aContext)
{
    StandardModule myModule = null;
    StandardModule myGroovyModule = aContext.getKernelContext().getOwningSpace().getOwningModule();
    StandardModule myResultModule = null;
    IRequestScopeLevel myScope = aContext.getKernelContext().getRequestScope();
    while ((myScope!=null) && (myResultModule==null)) {
        ISpace mySpace = myScope.getSpace();
        if (mySpace instanceof StandardSpace) {
            myModule = myScope.getSpace().getOwningModule();
            if (myModule != myGroovyModule) {
                myResultModule = myModule;
            }
        }
        myScope = myScope.getParent();
    }
    return myResultModule.getSource();
}
// ----------------------------------------------------------------------------------------------

// We create the URI of the local directory whose name has been received as a parameter
URI myResourceURI = getModuleURI(context).resolve("resources/data/" + paramPolicyName);

// We convert the URI to File
File myPolicyDirectory = new File(myResourceURI);

IHDSMutator m = HDSFactory.newDocument();

if (! myPolicyDirectory.exists()) {
    // We output the error XML
    String myErrorCode = "404";
    m.pushNode("output").addNode("status","error").addNode("code",myErrorCode).addNode("detail","SELinux policy not found: \""+paramPolicyName+"\"");
    IHDSDocument representation=m.toDocument(false); //don't clone
    myResponse = context.createResponseFrom(representation);
    myResponse.setHeader("httpResponse:/code", myErrorCode);
}
else {
    // We output the success XML
    m.pushNode("output").addNode("status","success").addNode("code","200");
    m.pushNode("content");
    m.addNode("@localpath",myResourceURI);
    m.addNode("url","/modules");
    m.addNode("url","/query/<instruction>");
    m.addNode("url","/lookup/<symbol>");
    m.addNode("url","/filepaths/<regex>");
    m.addNode("url","/accessrules/<domain>");
    m.addNode("url","/auditrules/<domain>");

    IHDSDocument representation=m.toDocument(false); //don't clone
    context.createResponseFrom(representation);
}

