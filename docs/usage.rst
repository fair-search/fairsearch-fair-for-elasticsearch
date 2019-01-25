How to use the fair-search plugin
***************************

Now it is time to finally perform a fair re-scoring. 

The usual flow for the fairsearch plug-in is this one:

* a user executes a query in the search engine, and during this process, 
* indicates s/he wants to apply the fairsearch plug-in.

To achive this we are going to use a functionality provided by Elasticsearch named *re-scoring*.

=======================
Assumptions and preconditions for this example
=======================

Lets suppose we have already in our search engine this set of documents:

.. code-block:: json

    Doc1 { body: "hello hello hello hello hello hello hello hello hello hello", gender: "m" }
    Doc3 { body: "hello hello hello hello hello hello hello hello hello bye", gender: "m" }
    Doc5 { body: "hello hello hello hello hello hello hello hello bye bye", gender: "m" }
    Doc7 { body: "hello hello hello hello hello hello hello bye bye bye", gender: "m" }
    Doc9 { body: "hello hello hello hello hello hello bye bye bye bye", gender: "m" }
    Doc2 { body: "hello hello hello hello hello bye bye bye bye bye", gender: "f" }
    Doc4 { body: "hello hello hello hello bye bye bye bye bye bye", gender: "f" }
    Doc6 { body: "hello hello hello bye bye bye bye bye bye bye", gender: "f" }
    Doc8 { body: "hello hello bye bye bye bye bye bye bye bye", gender: "f" }
    Doc10 { body: "hello bye bye bye bye bye bye bye bye bye", gender: "f" }

In this example, women will be our protected category. As we see in the "body" of the documents above, the word "hello" occurs more in the ones having ``gender=m`` (male) than in the ones having ``gender=f`` (female). 

=======================
How does a search looks like
=======================

Lets first imagine we execute a normal search for "hello", one without using the fairsearch plug-in. The results would look like this:

.. code-block:: json

    GET test/_search
    {
        "query": {
            "match": {
                "body": "hello"
            }
        }
    }


This request will return all documents that match the word *hello*, sorted by their relevance scoring. For this particular dataset we would get these results:

    Doc1, Doc3, Doc5, Doc7, Doc9, Doc2, Doc4, Doc6, Doc8, Doc10

and if we take a closer look, these will be:

    m, m, m, m, m, f, f, f, f, f

with all men as first top results, however as we could see in the :doc:`theory` section, there are many situations where we might aim for a fair result. To achieve this we will use the plug-in.

A request with the re-score function will look like this:

.. code-block:: json

    GET test/_search
    {
        "query": {
            "match": {
                "body": "hello"
            }
        },
        "rescore": {
            "fair_rescorer": {
                "protected_key": "gender",
                "protected_value": "f",
                "significance_level": 0.1,
                "min_proportion_protected": 0.6
            }
        }
    }

this request is actually doing an Elasticsearch *match* query (it also could do any other type of query, for example a *bool* or a *multi match*), then after the results are calculated (in every shard) it applies the fair topK algorithm.

This request will give you a response where the target number of protected elements will be scored in relevant places, that for our example will be:

    Doc1, Doc3, Doc2, Doc5, Doc4, Doc7, Doc9, Doc6, Doc8, Doc10
    
or, in terms of gender:

    m, m, f, m, f, m, m, f, f, f

with a much fairer distribution of elements of the protected class (i.e., some women appear in the top positions).

=======================
Details on parameters
=======================

As we saw in the :doc:`theory`, for the fair query to work, we would need an *mtable* against which the re-scoring will be executed. To make the use of the plug-in easier, the re-score function does that in the background. So, the previous re-score call will create an *mtable* with the following parameters:

- ``k=10``, as 10 is the default documents that are returned by Elasticsearch
- ``α=0.1``, as that is the significance level specified in the query
- ``p=0.6``, as that is the minimum proportion of protected elements specified in the top results


Elasticsearch size parameters
-------------------------
Elasticsearch supports the following size parameters:

- ``size``, which is the total number of documents returned by ElasticSearch (defaults to 10)
- ``window_size``, which is the number of documents to be re-ranked by the re-score function and returned to the user (defaults to 10).

For example, if you make a query like this:

.. code-block:: json

    GET test/_search
    {
       "size": 50,
        "query": {
            "match": {
                "body": "hello"
            }
        },
        "rescore": {
            "window_size": 10,
            "fair_rescorer": {
                "protected_key": "gender",
                "protected_value": "f",
                "significance_level": 0.1,
                "min_proportion_protected": 0.6
            }
        }
    }

This will try to find initial 50 documents that match the query and, then, re-rank and *return* only the top 10. So, the plug-in will choose ``window_size`` as the ``k`` parameter or, if smaller, the actual numbers of documents initially returned. 

Ensuring there are protected elements in the topK results
-------------------------

A recommended way of using the plug-in is to specify a higher number for ``size``, so that more elements can be included in the initial list for more fair re-ranking. Then, after the re-scoring phase, only ``window_size`` number of documents will be returned.


