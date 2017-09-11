/**
 * Ce module interroge "res:/services/policies/<policyname>".
 * Si cette ressource contient une erreur, l'erreur est remontée.
 * Sinon, on utilise l'attribute "localpath" dans "/output/content" qui doit pointer vers
 * le répertoire local contenant la politique demandée.
 * Le répertoire doit contenir à son tour des sous-répertoires correspondant aux modules
 * de la politique considérée.
 *
 * Structure XML en sortie (si succès) :
 * <output>
 *   <status>success|error<status>
 *   <code>200</code>
 *   <content count="3">
 *     <m>module1</m>
 *     <m>module2</m>
 *     <m>module3</m>
 *   </content>
 * </output>
 */
import java.net.URI;
import org.netkernel.mod.hds.IHDSDocument;
import org.netkernel.mod.hds.HDSFactory;
import org.netkernel.mod.hds.IHDSMutator;

// Parameter 'policyname'
String paramPolicyName = context.source("arg:policyname");

// We call "res:/services/policies/<policyname>" to check for errors
IHDSDocument myResource = context.source("res:/services/policies/"+paramPolicyName, IHDSDocument.class);

// If the returned resource does not contain <status>success</status>, we return the same output as the service we called
if (! myResource.getReader().getFirstValue("/output/status").equals("success")) {
    myResponse = context.createResponseFrom(myResource);
    // Propagation of the received HTTP response code
    myResponse.setHeader("httpResponse:/code", myResource.getReader().getFirstValue("/output/code"));
}
else {
    // We retrieve the absolute local path to the policy, in the @localpath attribute
    URI myResourceURI = myResource.getReader().getFirstValue("/output/content/@localpath");

    // We convert the URI to File
    File myPolicyDirectory = new File(myResourceURI);

    List<String> myPolicyDirectoryList = new ArrayList<String>(Arrays.asList(myPolicyDirectory.list()));

    // We output the success XML
    IHDSMutator m = HDSFactory.newDocument();
    m.pushNode("output").addNode("status","success").addNode("code","200");
    m.pushNode("content");
    for (String moduleName : myPolicyDirectoryList) {
        m.addNode("m", moduleName);
    }
    m.addNode("@count",myPolicyDirectoryList.size);
    IHDSDocument representation=m.toDocument(false); //don't clone
    myResponse = context.createResponseFrom(representation);
}

