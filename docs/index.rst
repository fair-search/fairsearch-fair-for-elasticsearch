.. Elasticsearch Fair search documentation master file, created by
   sphinx-quickstart on Thu Sep 28 14:00:10 2017.
   You can adapt this file completely to your liking, but it should at least
   contain the root `toctree` directive.

Elasticsearch Fair search: the documentation
==========================================================

The Elasticsearch fair search plugin apply innovative fair topk ranking algorithms to retrieve a fair set of candidates from your search. This plugin has been develop in collaboration between the TU Berlin, the Pompeu Fabra University and Pere Urbón.

Get started
-------------------------------

- Brand new? head to :doc:`core-concepts`.
- Otherwise, start with :doc:`fits-in`
- Building an m table, head to :doc:`building_the_mtable`
- Doing fair rescoring, head to :doc:`doing_a_fair_rescoring`

Installing
-----------

Pre-built versions can be found `here <https://fair-search.github.io/>`_. Want a build for an ES version? Follow the instructions in the `README for building <https://github.com/fair-search/fairsearch-elasticsearch-plugin#development>`_ or `create an issue <https://github.com/fair-search/fairsearch-elasticsearch-plugin/issues>`_. Once you've found a version compatible with your Elasticsearch, you'd run a command such as:

    ./bin/elasticsearch-plugin install \
    https://fair-search.github.io/fair-reranker/fairsearch-1.0-es6.1.2-SNAPSHOT.zip

(It's expected you'll confirm some security exceptions, you can pass -b to elasticsearch-plugin to automatically install)


HEEELP!
------------------------------

The plugin and guide was built by the search and data consultant  `Pere Urbon <http://purbon.com>`_ in partnership with the TU Berlin and the Pompeu Fabra University. Please `contact Pere Urbon <mailto:name.surname@acm.org>`_ or `create an issue <https://github.com/fair-search/fairsearch-elasticsearch-plugin/issues>`_ if you have any questions or feedback.



Contents
-------------------------------


.. toctree::
   :maxdepth: 2

   core-concepts
   fits-in
   building_the_mtable
   doing_a_fair_rescoring
   :caption: Contents:


Indices and tables
==================

* :ref:`genindex`
* :ref:`search`
