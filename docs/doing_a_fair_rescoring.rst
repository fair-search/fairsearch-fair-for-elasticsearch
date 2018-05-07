Having a fair rescoring
*******************************

You got to the end of the documentation, now is time to finally run a fair top-k rescoring.

The usual flow for a fair search is this one:

* a user execute a query in the search engine, however during this process s/he also
* select the UI s/he wants to retrieve a fair search.

To achive this we are going to use the rescoring functionality provided by elasticsearch.

Your request will look something like:

.. code-block:: json

    GET test/_search
    {
        "query": {
            "match_all": {}
        },
        "rescore": {
            "fair_rescorer": {
                "protected_key": "gender",
                "protected_value": "Female",
                "significance_level": 0.1,
                "min_proportion_protected": 0.5
            }
        }
    }

this request is actually doing a match_all query, could it by any other type of elasticsearch query, for example a bool
or a multi match. then after the results are calculated (in every shard) it apply the fair rescoring algorithm. This
request will give you a response where the target number of protected elements will be scored in relevant places.
