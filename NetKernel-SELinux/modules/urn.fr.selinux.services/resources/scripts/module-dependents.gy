/**
 * Ce script recherche les modules qui dépendent du module dont le nom est dans arg:modulename pour la politique
 * arg:policyname.
 * 
 * Il commence par trouver tous les symboles définis dans le module arg:modulename.
 *
 * Puis il fait une boucle sur ces symboles, et appelle à chaque tour active:s-expression-parser-with-filter,
 * avec filter-type="onSymbolRequires" et le nom du symbole courant dans "filter-value".
 * 
 * On renvoie finalement toutes les listes trouvées.
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

// Parameter 'details' (true/false)
String paramDetails = context.source("arg:details");

// ----------------------------------------------------------------------------------------------

// We use the "res:/services/policies/<policyname>/modules/<modulename>/query/" service to check if the parameters are ok
IHDSDocument myModule = context.source("res:/services/policies/"+paramPolicyName+"/modules/"+paramModuleName+"/query/", IHDSDocument.class);

// If the returned resource does not contain <status>success</status>, we return the same output as the service we called
if (! myModule.getReader().getFirstValue("/output/status").equals("success")) {
    myResponse = context.createResponseFrom(myModule);
    // Propagation of the received HTTP response code
    myResponse.setHeader("httpResponse:/code", myModule.getReader().getFirstValue("/output/code"));
}
else {
    // Output
    String myReturnCode = "200"
    IHDSMutator m = HDSFactory.newDocument();
    m.pushNode("output").addNode("status","success").addNode("code",myReturnCode);
    m.pushNode("content");

    // We are interested by all the symbol definitions in the module
    List<IHDSReader> myModuleSymbolReaders = myModule.getReader().getNodes("/output/content/l[count(*)=2]");

    // We will search the instructions of the policy where these symbols have a "cil_gen_require"
    IHDSDocument myPolicy = context.source("res:/services/policies/"+paramPolicyName+"/query/", IHDSDocument.class);

    // If we don't want details, we will simply put the name of the dependent modules in a Map
    Map<String,Boolean> myDependentsMap = new HashMap<String,Boolean>();

    // For each symbol defined in the module
    for (IHDSReader myModuleSymbolReader : myModuleSymbolReaders) {

        String mySymbol = myModuleSymbolReader.getFirstValue("a[2] | false()");

        // We use the policy index "onSymbolRequires" to look for the current symbol in the policy
    	List<IHDSReader> myPolicyDependents = myPolicy.getReader().getNodes("key('onSymbolRequires','"+mySymbol+"')");

        for (IHDSReader myPolicyDependent : myPolicyDependents) {
            String myDependentModuleName = myPolicyDependent.getFirstValueOrNull("@m");

            // If we want details, then we output the "cil_gen_require" instruction with a new attribute "defined-in"
            if (! "false".equals(paramDetails)) {
                m.append(myPolicyDependent).popNode();
            }
            else {
                if (! myDependentsMap.containsKey(myDependentModuleName)) {
                    myDependentsMap.put(myDependentModuleName, myPolicyDependent.getFirstValueOrNull("@o")!=null);
                }
                else {
                    // The Boolean value of the Map is true if the cil_gen_require is ONLY optional
                    // i.e. if the cil_gen_require is either non-optional, either both optional and non-optional,
                    // then the Boolean is false
                    Boolean onlyOptional = myDependentsMap.get(myDependentModuleName);
                    if (onlyOptional) {
                        myDependentsMap.put(myDependentModuleName, myPolicyDependent.getFirstValueOrNull("@o")!=null);
                    }
                    else {
                        myDependentsMap.put(myDependentModuleName, false);
                    }
                }
            }
        }
    }
    if ("false".equals(paramDetails)) {
        // We convert the Set to a TreeSet to get the module names in alphabetical order
        Map<String,Boolean> distinctDependentsModules = new TreeMap(myDependentsMap);
        for (Map.Entry<String,Boolean> aDependentModule : distinctDependentsModules.entrySet()) {
            m.pushNode("m",aDependentModule.getKey());
            // If the associated Boolean is true, then all of the "cil_gen_require"s are inside optional blocks
            // and so we mark this dependent module with the o="true" attribute
            if (aDependentModule.getValue()) {
                m.addNode("@o",aDependentModule.getValue());
            }
            m.popNode();
        }
        m.addNode("@total",distinctDependentsModules.size());
    }

    IHDSDocument representation=m.toDocument(false); //don't clone
    myResponse = context.createResponseFrom(representation);
}

