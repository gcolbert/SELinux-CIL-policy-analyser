/**
 * Ce script recherche le chemin du système de fichiers, qui est passé en paramètre, dans la politique indiquée.
 * 
 * Il fait une boucle sur res:/services/policies/<policyname>/modules/<modulename>/lookup/<symbol>
 * puis renvoie l'instruction définissant cet identifiant.
 */
import org.netkernel.mod.hds.IHDSDocument;
import org.netkernel.mod.hds.HDSFactory;
import org.netkernel.mod.hds.IHDSMutator;
import org.netkernel.mod.hds.IHDSReader;

// Parameter 'policyname'
String paramPolicyName = context.source("arg:policyname");

// Parameter 'filepath'
String paramFilePath = context.source("arg:filepath");

// ----------------------------------------------------------------------------------------------
// This function computes the Levenshtein's distance between two strings.
// It comes from: https://en.wikibooks.org/wiki/Algorithm_Implementation/Strings/Levenshtein_distance#Groovy
// It is used to sort the "filecon" expressions based on the "s" elements, that contain paths.
private static int distance(String str1, String str2) {
    int[][] dist = new int[str1.size() + 1][str2.size() + 1]
    (0..str1.size()).each { dist[it][0] = it }
    (0..str2.size()).each { dist[0][it] = it }
 
    (1..str1.size()).each { i ->
        (1..str2.size()).each { j ->
            dist[i][j] = [dist[i - 1][j    ] + 1,
                          dist[i    ][j - 1] + 1,
                          dist[i - 1][j - 1] + ((str1[i - 1] == str2[j - 1]) ? 0 : 1)]
                         .min()
        }
    }
    return dist[str1.size()][str2.size()]
}
// ----------------------------------------------------------------------------------------------

// We use the "res:/services/policies/<policyname>/query/" as a source
IHDSDocument myPolicy = context.source("res:/services/policies/"+paramPolicyName+"/query/", IHDSDocument.class);

// If the returned resource does not contain <status>success</status>, we return the same output as the service we called
if (! myPolicy.getReader().getFirstValue("/output/status").equals("success")) {
    myResponse = context.createResponseFrom(myPolicy)
    // Propagation of the received HTTP response code
    myResponse.setHeader("httpResponse:/code", myPolicy.getReader().getFirstValue("/output/code"));
}
else {
    // We query the 'filecon' instructions
	List<IHDSReader> listReaders = myPolicy.getReader().getNodes("key('onInstructionName','filecon')");

    IHDSMutator myUnsortedMutator = HDSFactory.newDocument();
    myUnsortedMutator.pushNode("unsorted");
    Integer matched = 0;
    for (IHDSReader listReader : listReaders) {
        if (paramFilePath =~ listReader.getFirstValue("s")) {
            myUnsortedMutator.append(listReader).popNode();
            matched++;
        }
    }

    List<IHDSMutator> myNodes = myUnsortedMutator.getNodes("/unsorted/l");
    // We sort the instructions based on the "s" elements length
    if (myNodes.size() > 0) {
        myNodes.sort{a,b -> distance(a.getFirstValueOrNull("s"), paramFilePath) <=> distance(b.getFirstValueOrNull("s"), paramFilePath) }
    }

    String myReturnCode = "200";
    IHDSMutator m = HDSFactory.newDocument();
    m.pushNode("output").addNode("status","success").addNode("code",myReturnCode);
    m.pushNode("content");
    for (IHDSMutator node : myNodes) {
        m.append(node).addNode("@distance",distance(node.getFirstValueOrNull("s"),paramFilePath)).popNode();
    }
    m.addNode("@matched", matched).addNode("@total", listReaders.size());

    IHDSDocument representation=m.toDocument(false); //don't clone
    context.createResponseFrom(representation);
}

