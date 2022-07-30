package mapping.loader.json;

import java.util.Map;


public class XMLNodeJson {
    private String nodeMappingId;
    private String nodeLabel;
    private String xpath; // np. "/dblp/article/author"
    private String innerTextMappedTo; // jeśli chcemy zmapować np. <author>Jan Kowalski</author> na jakiś atrybut węzła w grafie

    private Map<String, String> mappedAttributes; // mapowanie atrybutów tagu
    private Map<String, String> mappedChildElements; // mapowany jest tekst między tagiem otwierającym i zamykającym
}