Wymagania funkcjonalne [PL]
===========================

A. Migracja danych
------------------

A1. Plik konfiguracyjny z parametrami połączeń do baz SQL
~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
Kryteria akceptacji:
  * ustalono jednolity format pliku konfiguracyjnego dla połączeń z bazami SQL
  * użytkownik ma możliwość podłączenia się do bazy, podając jedynie nazwę pliku konfiguracyjnego
  * w pliku konfiguracyjnym można podać parametry połączenia takie jak:
        - technologia bazodanowa
        - adres i port serwera
        - nazwa użytkownika
        - hasło
        - nazwa bazy

A2. Zunifikowane schematy bazy grafowej
~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
Kryteria akceptacji:
    * ustalono kilka zunifikowanych schematów bazy grafowej dla heterogenicznych źródeł danych
    * schemat zawiera:
        - typy wierzchołków razem z dostępnymi atrybutami
        - typy krawędzi między wierzchołkami razem z dostępnymi atrybutami

A3. Plik konfiguracyjny z mapowaniem między schematem bazy relacyjnej a schematem docelowym
~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
Kryteria akceptacji:
 * ustalono jednolity format pliku konfiguracyjnego dla mapowania, gdy zbiorem źródłowym jest baza relacyjna SQL
 * użytkonik może wybrać w pliku jeden z dostępnych schematów bazy grafowej
 * użytkownik może ustalić w pliku mapowanie między:
        - tabelą a typem wierzchołka
        - kolumnami tabeli a atrybutami wierzchołka
        - kluczem obcym a typem krawędzi
        - tabelą łącznikową a typem krawędzi
        - kolumnami tabel łącznikowych a atrybutami krawędzi
 * użytkownik może ustalić w pliku, że określone tabele lub kolumny nie będą importowane

A4. Interaktywne przejście przez tworzenie mapowania między schematem bazy relacyjnej a docelowym
~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
Kryteria akceptacji:
 * użytkownik może zdefiniować te same mapowania, co za pomocą plików konfiguracyjnych, przez interakcję z aplikacją konsolową
 * aplikacja umożliwia użytkownikowi ustalenie, że określone tabele lub kolumny nie będą importowane
 * aplikacja podpowiada użytkownikowi i umożliwia wybór dostępnego:
    - schematu docelowej bazy grafowej
    - typu wierzchołka dla danej tabeli
    - atrybutu wierzchołka dla kolumny tabeli
    - typu krawędzi dla klucza obcego
    - typu krawędzi dla tabeli łącznikowej
    - atrybutu krawędzi dla kolumny tabeli łącznikowej

A3. Plik konfiguracyjny z mapowaniem między listą krawędzi w pliku XML a schematem docelowym
~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
Kryteria akceptacji:
 * ustalono jednolity format pliku konfiguracyjnego dla mapowania, gdy zbiorem źródłowym jest plik XML z grafem w postaci listy krawędzi
 * użytkonik może wybrać w pliku jeden z dostępnych schematów bazy grafowej
 * użytkownik może ustalić w pliku mapowanie między tagiem XML a:
        - typem wierzchołka
        - atrybutem wierzchołka
        - typem krawędzi
        - atrybutem krawędzi
 * użytkownik może ustalić w pliku, że określone tagi XML nie będą importowane lub są tagami zewnętrznymi dla właściwych danych

A4. Interaktywne przejście przez tworzenie mapowania między listą krawędzi w pliku XML a schematem docelowym
~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
Kryteria akceptacji:
 * użytkownik może zdefiniować te same mapowania, co za pomocą plików konfiguracyjnych, przez interakcję z aplikacją konsolową
 * aplikacja umożliwia użytkownikowi ustalenie, że określone tagi XML nie będą importowane lub są tagami zewnętrznymi dla właściwych danych
 * aplikacja podpowiada użytkownikowi i umożliwia wybór dostępnego:
    - schematu docelowej bazy grafowej
    - typu wierzchołka dla odpowiedniego tagu XML
    - typu krawędzi dla odpowiedniego tagu XML
    - typu atrybutu krawędzi dla odpowiedniego tagu XML wewnątrz tagu odpowiadającego krawędzi
    - typu atrybutu wierzchołka dla odpowiedniego tagu XML wewnątrz tagu odpowiadającego wierzchołkowi

A7. Zawężenie przedziału czasowego przy imporcie danych
~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
Kryteria akceptacji:
 * aplikacja umożliwia użytkownikowi filtrowanie importowanych danych po jednym lub więcej atrybutach reprezentujących datę i czas
 * aplikacja umożliwia użytkownikowi ustalenie przedziału czasowego dla importowanych danych

A8. Rozszerzenie istniejącego grafu
~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
Kryteria akceptacji:
 * aplikacja pozwala na import nowych danych do już istniejącego grafu

A9. Miary podobieństwa węzłów i krawędzi oraz scalanie grafu wejściowego i docelowego
~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
Kryteria akceptacji:
 * aplikacja rozpoznaje, gdy dane pochodzą z tego samego źródła, co dane w bazie grafowej. Wtedy:
        - aplikacja wyznacza miarę podobieństwa między odpowiednimi węzłami i między odpowiednimi krawędziami
        - aplikacja pozwala użytkownikowi zdecydować, powyżej jakiej wartości miary podobieństwa scalić odpowiednie węzły i krawędzie, a poniżej której uznawać je za osobne węzły i krawędzie
        - w przypadku konfliktu wartości między atrybutami scalanych węzłów i krawędzi aplikacja pozwala użytkownikowi wybrać czy woli zachować dane źródłowe czy docelowe

B. Analiza sieci
----------------

B1. Zawężenie przedziału czasowego przy analizie sieci
~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
Kryteria akceptacji:
 * aplikacja umożliwia użytkownikowi filtrowanie danych wejściowych do danego algorytmu SNA po jednym lub więcej atrybutach reprezentujących datę i czas
 * aplikacja umożliwia użytkownikowi ustalenie przedziału czasowego dla danych wejściowych do danego algorytmu SNA

B2. Wybór i wykonanie algorytmu analizy sieci
~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
Kryteria akceptacji:
 * aplikacja umożliwia użytkownikowi wybór jednego z dostępnych algorytmów analizy sieci
 * aplikacja wykonuje algorytm SNA i zapisuje wyniki w tej samej bazie, co dane wejściowe lub w nowej bazie, w zależności od tego, co ustali użytkownik

B3. Eksport do formatu JSON lub CSV
~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~

Kryteria akceptacji:
  * użytkownik ma możliwość eksportu grafu z wynikami analiz do pliku w formacie JSON lub CSV

