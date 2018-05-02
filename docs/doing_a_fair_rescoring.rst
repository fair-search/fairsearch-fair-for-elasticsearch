Having a fair rescoring
*******************************

You got to the end of the documentation, now is time to finally run a fair top-k rescoring.

The usual flow for a fair search is this one:

* a user execute a query in the search engine, however during this process s/he also
* select the UI s/he wants to retrieve a fair search.

To achive this we are going to use the rescoring functionality provided by elasticsearch.

=======================
Assumptions and preconditions for this example
=======================

Lets suppose we have already in our search engine this set of documents:

.. code-block:: json

    Doc1 { body: "hello hello hello hello hello hello hello hello hello hello", gender: "m" }
    Doc2 { body: "hello hello hello hello hello bye bye bye bye bye", gender: "f" }
    Doc3 { body: "hello hello hello hello hello hello hello hello hello bye", gender: "m" }
    Doc4 { body: "hello hello hello hello bye bye bye bye bye bye", gender: "f" }
    Doc5 { body: "hello hello hello hello hello hello hello hello bye bye", gender: "m" }
    Doc6 { body: "hello hello hello bye bye bye bye bye bye bye", gender: "f" }
    Doc7 { body: "hello hello hello hello hello hello hello bye bye bye", gender: "m" }
    Doc8 { body: "hello hello bye bye bye bye bye bye bye bye", gender: "f" }
    Doc9 { body: "hello hello hello hello hello hello bye bye bye bye", gender: "m" }
    Doc10 { body: "hello bye bye bye bye bye bye bye bye bye", gender: "f" }

we can see that they all have a body and gender as fields.

For this example gender will be our protected category.

=======================
How does a search looks like
=======================


Lets first imagine we execute a normal search, one without using the fair rescorer, this would looks like this:

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

that if we take a close look will be:

    m, m, m, m, m, f, f, f, f, f

with all men as first top results, however as we could see in the :doc:`core-concepts` section, there are lots of
situations where we might aim for a fair result. To achieve this we will use the fair rescorer provided with this plugin.

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

this request is actually doing a match query, could it by any other type of elasticsearch query, for example a bool
or a multi match. then after the results are calculated (in every shard) it apply the fair topK algorithm.

This request will give you a response where the target number of protected elements will be scored in relevant places,
that for our example will be:

    Doc1, Doc3, Doc2, Doc5, Doc4, Doc7, Doc9, Doc6, Doc8, Doc10

with a much fair distribution of elements of the protected class.