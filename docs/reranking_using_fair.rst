Re-ranking using fairsearch
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

Lets first imagine we execute a normal search for "hello", one without using the Fairsearch plugin. The results would look like this:

.. code-block:: json

    GET test/_search
    {
        "query": {
            "match": {
                "body": "hello"
            }
        }
    }


This request will return all documents that match the word hello, sorted by their relevance scoring. For this
particular dataset we would get this results:

    Doc1, Doc3, Doc5, Doc7, Doc9, Doc2, Doc4, Doc6, Doc8, Doc10

that if we take a close look these will be:

    m, m, m, m, m, f, f, f, f, f

with all men as first top results, however as we could see in the :doc:`Motivation` section, there are many situations where we might aim for a fair result. To achieve this we will use the plug-in.

A request with the rescore function will look like this:

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

this request is actually doing an Elasticsearch *match* query, could it by any other type of query, for example a *bool* or a *multi match*. then after the results are calculated (in every shard) it apply the fair topK algorithm.

This request will give you a response where the target number of protected elements will be scored in relevant places, that for our example will be:

    Doc1, Doc3, Doc2, Doc5, Doc4, Doc7, Doc9, Doc6, Doc8, Doc10
    
in terms of gender:

    m, m, f, m, f, m, m, f, f, f

with a much fair distribution of elements of the protected class (i.e., some women appear in the top positions).
