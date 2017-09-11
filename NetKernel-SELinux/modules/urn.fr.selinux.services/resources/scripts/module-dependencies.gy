/**
 * Ce script recherche les dépendances du module dont le nom est dans arg:modulename pour la politique
 * arg:policyname.
 * 
 * Il appelle active:s-expression-parser-with-filter avec filter-type="onArg1Value" et filter-value="cil_gen_require"
 * pour obtenir la liste des instructions du module qui contiennent "cil_gen_require",
 * ce qui indique une dépendance externe sur le symbole qui suit.
 * On recherche la déclaration de ce symbole à l'aide du service res:/services/policies/<policyname>/lookup/<symbole>.
 * Finalement on récupère l'élément liste ("l") où est défini le symbole recherché, ce qui permet
 * d'obtenir le nom du module dans l'attribut "module".
 * On répète ces opérations pour tous les symboles appelant "cil_gen_require" de ce module, puis on renvoie finalement
 * toutes les listes trouvées avec le nom du module externe dans un nouvel attribut "defined-in".
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

// We use the "res:/services/policies/<policyname>/modules/<modulename>/cil" service to check if the parameters are ok
IHDSDocument myResource = context.source("res:/services/policies/"+paramPolicyName+"/modules/"+paramModuleName+"/cil", IHDSDocument.class);

// If the returned resource does not contain <status>success</status>, we return the same output as the service we called
if (! myResource.getReader().getFirstValue("/output/status").equals("success")) {
    myResponse = context.createResponseFrom(myResource);
    // Propagation of the received HTTP response code
    myResponse.setHeader("httpResponse:/code", myResource.getReader().getFirstValue("/output/code"));
}
else {
    String myCil = myResource.getReader().getFirstValue("/output/content");

    // We are interested by instructions that contain an atom ("a") with value "cil_gen_require" as their first argument
    INKFRequest myRequestToParserFilter = context.createRequest("active:s-expression-parser-with-filter");
    myRequestToParserFilter.addArgumentByValue("operand", myCil);
    myRequestToParserFilter.addArgumentByValue("filter-type", "onArg1Value");
    myRequestToParserFilter.addArgumentByValue("filter-value", "cil_gen_require");
    myRequestToParserFilter.addArgumentByValue("module", paramModuleName);
    IHDSDocument myResponseFromParserFilter = context.issueRequest(myRequestToParserFilter);
    List<IHDSReader> myCilInstructions = myResponseFromParserFilter.getReader().getNodes("/output/content/l");

    // Output
    String myReturnCode = "200"
    IHDSMutator m = HDSFactory.newDocument();
    m.pushNode("output").addNode("status","success").addNode("code",myReturnCode);
    m.pushNode("content");

    Map<String,Boolean> requiredModulesMap = new HashMap<String,Boolean>();
    for (IHDSReader myCilInstruction : myCilInstructions) {
        // The required symbol is the next atom after the one containing "cil_gen_require", so the 3rd
        String mySymbol = myCilInstruction.getFirstValueOrNull("a[3]");

        // We look up the symbol that follows the cil_gen_require keyword
        IHDSDocument myLookupResult = context.source("res:/services/policies/"+paramPolicyName+"/lookup/"+mySymbol, IHDSDocument.class);

        // We extract the module name from the lookup response
        String myRequiredModule = myLookupResult.getReader().getFirstValueOrNull("/output/content/l/@m");

        // If we want details, then we output the "cil_gen_require" instruction with a new attribute "defined-in"
        if (! "false".equals(paramDetails)) {
            m.append(myCilInstruction).addNode("@defined-in",myRequiredModule).popNode();
        }
        else {
            // If we don't want details but rather a synthetic list of dependencies,
            // then we simply put the module's name in a Map.
            // We need to test for the null of the myRequiredModule variable, because
            // there are some "cil_gen_require" which are not defined in any module.
            // e.g. for "targeted" policy, in module "base", symbols "rolekit_t" and "hotplug_etc_t"
            if (myRequiredModule != null) {
                if (! requiredModulesMap.containsKey(myRequiredModule)) {
                    requiredModulesMap.put(myRequiredModule, myCilInstruction.getFirstValueOrNull("@o")!=null);
                }
                else {
                    // The Boolean value of the Map is true if the requirement is ONLY optional
                    // i.e. if the requirement is either non-optional, either both optional and non-optional,
                    // then the Boolean is false
                    Boolean onlyOptional = requiredModulesMap.get(myRequiredModule);
                    if (onlyOptional) {
                        requiredModulesMap.put(myRequiredModule, myCilInstruction.getFirstValueOrNull("@o")!=null);
                    }
                    else {
                        requiredModulesMap.put(myRequiredModule, false);
                    }
                }
            }
        }

    }
    if ("false".equals(paramDetails)) {
        // We convert the Set to a TreeSet to get the module names in alphabetical order
        Map<String,Boolean> distinctRequiredModules = new TreeMap(requiredModulesMap);
        for (Map.Entry<String,Boolean> aRequiredModule : distinctRequiredModules.entrySet()) {
            m.pushNode("m",aRequiredModule.getKey());
            // If the associated Boolean is true, then all of the "cil_gen_require"s are inside optional blocks
            // and so we mark this required module with the o="true" attribute
            if (aRequiredModule.getValue()) {
                m.addNode("@o",aRequiredModule.getValue());
            }
            m.popNode();
        }
        m.addNode("@total",distinctRequiredModules.size());
    }

    IHDSDocument representation=m.toDocument(false); //don't clone
    myResponse = context.createResponseFrom(representation);
}

