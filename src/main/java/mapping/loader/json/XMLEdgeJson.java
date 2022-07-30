package mapping.loader.json;

import java.util.Map;

/**
 Zakładamy możliwość łączenia w węzły tagów:
 - rodzic-dziecko, np. /dblp/article z /dblp/article/author
 - dziecko-dziecko, np. /dblp/article/author z /dblp/article/author, jeżeli oba te tagi mają wspólnego rodzica
 W obu przypadkach:
 "mappedAttributes" - mapowania z atrybutów rodzica (w przypadku relacji dziecko-dziecko: z tagu zewnętrznego wobec obu dzieci)
 "mappedChildElements" - mapowania z dzieci rodzica. Mapowany jest tekst między tagiem otwierającym i zamykającym dziecka.
 */
public class XMLEdgeJson {
    private String edgeLabel;
    private boolean directed;

    private String firstNodeMappingId;
    private String secondNodeMappingId;

    private Map<String, String> mappedAttributes;
    private Map<String, String> mappedChildElements;
}