Algorytmy analizy sieci społecznych [PL]
===================================

Do obliczania algorytmów wykorzystana zostanie ich implementacja dostarczana przez bibliotekę `Graph Data Science Library <https://neo4j.com/docs/graph-data-science/current/>`_. Jest to biblioteka stworzona przez twórców silnika Neo4j właśnie dla celów analizy grafów, wykorzystująca możliwości silnika zwłaszcza w kwestii zrównoleglenia wykonania.

Jako dane wejściowe poszczególne procedury przyjmują projekcje grafu. Projekcja grafu jest to coś w rodzaju widoku zmaterializowanego, zawierającego jedynie istotne z punktu widzenia algorytmu dane. Projekcje grafu przechowywane są w pamięci. Projekcje można tworzyć nadając im nazwę oraz listę projekcji wierzchołków i krawędzi. Projekcje wierzchołków i krawędzi to po prostu nazwy typów wierzchołków i krawędzi lub zapytania w Cypher zwracające pewien podzbiór odpowiednio wierzchołków i krawędzi w grafie. Do tworzenia projekcji służy procedura ``gds.graph.project()``. Po utworzeniu projekcji można się do niej odwoływać przy wykonaniu algorytmów podając jej nazwę. 

1. Density
~~~~~~~~~~~~~~~~~~~~~~~~~
Gęstość grafu reprezentowana jako stosunek liczby krawędzi w grafie do maksymalnej liczby krawędzi, jaką ten graf mógłby zawierać. Wartość można uzyskać korzystając z procedury ``gds.graph.list()`` wykonując ją na utworzonej wcześniej projekcji grafu. 

2. Clustering coefficient
~~~~~~~~~~~~~~~~~~~~~~~~~
Jest to miara pozwalająca ocenić, w jakim stopniu wierzchołki grafu skupiają się w gęsto połączonych grupach. Dla sieci społecznych charakterystyczne jest występowanie gęsto połączonych grup, mających stosunkowo nieliczne połączenia między sobą. Liczy się ją dla każdego wierzchołka osobno jako stosunek liczby krawędzi pomiędzy sąsiadami danego wierzchołka do maksymalnej liczby krawędzi pomiędzy nimi. Czyli mierzy się, jak blisko im jest do utworzenia kliki. Można również policzyć średnią wartość tego współczynnika dla całego grafu, jest to znormalizowana suma współczynników dla każdego wierzchołka. Wartość można uzyskać korzystając z procedury ``gds.localClusteringCoefficient.stream()`` wykonując ją na utworzonej wcześniej projekcji grafu, z tym że algorytm jest zdefiniowany dla grafów nieskierowanych, więc tworząc projekcję należy o określeniu orientacji krawędzi jako ``UNDIRECTED``.

3. Degree centrality
~~~~~~~~~~~~~~~~~~~~~~~~~
Jest to liczba krawędzi wchodzących/wychodzących od wierzchołka liczona dla każdego wierzchołka osobno. Pozwala wyznaczyć ważność wierzchołków, np. określoną przez ilość osób obserwujących daną osobę w medium społecznościowym. Wartość można uzyskać korzystając z procedury ``gds.degree.stream()``. Tworząc projekcję grafu można dowolnie określać kierunek krawędzi w zależności od potrzeb. 

4. Closeness centrality
~~~~~~~~~~~~~~~~~~~~~~~~~
Wskaźnik pozwalający na wykrycie wierzchołków zdolnych do sprawnego rozpowszechniania informacji w grafie. Mierzy się go licząc odwrotność sumy odległości do wszystkich innych wierzchołków w grafie od danego wierzchołka. Im wyższa wartość, tym bliżej pozostałych wierzchołków jest dany wierzchołek. Wartość można uzyskać korzystając z procedury ``gds.beta.closeness.stream()``.

5. Betweenness centrality
~~~~~~~~~~~~~~~~~~~~~~~~~
Ilość najkrótszych ścieżek między dwoma wierzchołkami przechodzących przez dany wierzchołek. Pozwala na określenie, jaki wpływ na przepływ informacji w grafie ma dany wierzchołek. Wartość można uzyskać korzystając z procedury ``gds.betweenness.stream()``.

6. PageRank
~~~~~~~~~~~~~~~~~~~~~~~~~
PageRank to algorytm pozwalający wyznaczyć ważność każdego wierzchołka w grafie opierając się na liczbie wchodzących do niego krawędzi i ważności ich wierzchołków źródłowych. W sieciach społecznych pozwala na ocenę statusu osób oraz ich potencjału do wpływania na opinie w sieci. Wartość można uzyskać korzystając z procedury ``gds.pageRank.stream()``. 

7. Degree distribution
~~~~~~~~~~~~~~~~~~~~~~~~~
Rozkład prawdopodobieństwa stopni wierzchołka. Korzystając z procedury ``gds.graph.list()`` można uzyskać histogram stopni. 
