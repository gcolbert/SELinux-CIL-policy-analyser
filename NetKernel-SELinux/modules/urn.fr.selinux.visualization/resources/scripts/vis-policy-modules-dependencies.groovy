/**
 * Ce script recherche les dépendances des modules dont les noms sont dans arg:modulenames pour la politique
 * arg:policyname.
 * 
 * Les noms des modules présents dans arg:modulenames doivent être séparés par '+'.
 */
import org.netkernel.layer0.nkf.INKFRequest;
import org.netkernel.mod.hds.IHDSDocument;
import org.netkernel.mod.hds.HDSFactory;
import org.netkernel.mod.hds.IHDSMutator;
import org.netkernel.mod.hds.IHDSReader;

// Parameter 'policyname'
String paramPolicyName = context.source("arg:policyname");

// Parameter 'modulenames'
String paramModuleNames = context.source("arg:modulenames");

// ----------------------------------------------------------------------------------------------

// We split paramModuleNames on the '+' character
String[] moduleNames = paramModuleNames.split('\\+');

Boolean badModuleName = false;
for (String moduleName : moduleNames) {

    // We use the "res:/services/policies/<policyname>/modules/<modulename>/dependencies/data.xml" service to check if the parameters are ok
    IHDSDocument myResource = context.source("res:/services/policies/"+paramPolicyName+"/modules/"+moduleName+"/cil", IHDSDocument.class);
    // If the returned resource does not contain <status>success</status>, we return the same output as the service we called
    if (! myResource.getReader().getFirstValue("/output/status").equals("success")) {
        badModuleName = true;
        myResponse = context.createResponseFrom(myResource);
        // Propagation of the received HTTP response code
        myResponse.setHeader("httpResponse:/code", myResource.getReader().getFirstValue("/output/code"));
    }
}

if (! badModuleName) {

    // We know that all received module names are valid

    // Output
    IHDSMutator m = HDSFactory.newDocument();
    m.pushNode("output")

    for (String moduleName : moduleNames) {

        INKFRequest myRequestToXSLT = context.createRequest("active:xslt");
        myRequestToXSLT.addArgument("operator", "res:/resources/scripts/dependencies-adapt.xsl");
        myRequestToXSLT.addArgument("operand", "res:/services/policies/"+paramPolicyName+"/modules/"+moduleName+"/dependencies");
        myRequestToXSLT.addArgumentByValue("policyname", paramPolicyName);
        myRequestToXSLT.addArgumentByValue("modulename", moduleName);
        myRequestToXSLT.setRepresentationClass(IHDSDocument.class);
        IHDSDocument myResponseFromXSLT = context.issueRequest(myRequestToXSLT);

        List<IHDSReader> myReaders = myResponseFromXSLT.getReader().getNodes("/output/link");
        for (IHDSReader myReader : myReaders) {
            if (myReader != null) {
                m.append(myReader).popNode();
            }
        }
    }
    IHDSDocument representation=m.toDocument(false); //don't clone
    myResponse = context.createResponseFrom(representation);
}

