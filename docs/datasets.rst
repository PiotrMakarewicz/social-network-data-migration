Datasets
==============

Salon24 (PostgreSQL)
----------------------------------------------------

.. image:: https://github.com/PiotrMakarewicz/social-network-data-migration/blob/main/README_files/salon24_schema.png?raw=true

The dataset comes from a popular Polish blogging portal `Salon24 <https://www.salon24.pl/>`_. It contains data fetched from the website and results of NLP analysis. The source data consists of 31 750 authors, 380 700 blog posts, 5 703 140 comments to the posts and 176 777 tags. It is bounded by the time interval between January 1st, 2008 and June 6th, 2013.

NLP analysis was performed in two directions. First of them was evaluating the sentiment of written posts and comments. The results are stored in the `sentiment` and `sentiment2` columns of tables `comments` and `posts`. The second goal was to assess the probability of posts and comments being linked to specific topics. The calculated values are stored in the `probability` columns in tables `comment_topic` and `post_topic`. There are 350 topics characterised by certain keywords stored in the `keywords` column.

The dataset containing both the source data and NLP analysis results makes for a great resource for testing the use case in which the user decides which tables and columns they choose for the migration. We may assume that the user wishes to keep only the source data, therefore discarding the analysis results.

Huffington Post (PostgreSQL)
----------------------------------------------------
.. image:: https://github.com/PiotrMakarewicz/social-network-data-migration/blob/main/README_files/huffington_schema.png?raw=true

WOSN Facebook Wall Posts (TSV)
-------------------------------------------------
The dataset is a directed network of a small subset of posts to other user's wall on Facebook. The nodes are Facebook users, and each directed edge represents a post, linking a post author to the wall owner. Because users may write multiple posts on a wall, the network is a multigraph. There are also some self-loops in the network as users may post on their own walls. Data comes from a paper *B. Viswanath, A. Mislove, Meeyoung Cha and K. P. Gummadi "On the Evolution of User Interaction in Facebook"* presented at the WOSN 2009 conference. It contains information about 	46 952 users and 876 993 wall posts. Source: `https://socialnetworks.mpi-sws.org/data-wosn2009.html`

DBLP (XML)
-------------------------------------------------

The dataset contains the collaboration graph of authors from DBLP computer science bibliography. An edge between two authors represents a common publication and is annotated with its publishment date. The source files can be downloaded in the XML format from the `KONECT Project <http://konect.cc/networks/dblp_coauthor/>`_ website.

There are 12 497 authors and 49 760 publications (edges) in the dataset. The underlying graph representation is an edge list, contrary to the other datasets we use. This introduces another use case to be adressed by our application. 
