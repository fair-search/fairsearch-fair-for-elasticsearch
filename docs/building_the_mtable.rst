Building M tables
*******************************

The m tables are a core component of this plugin setup, as described in the original paper it describe the fairness
representation conditions for each use case.

    Specifically, the ranked group fairness criterion compares the
    number of protected elements in every prex of the ranking, with
    the expected number of protected elements if they were picked at
    random using Bernoulli trials. The criterion is based on a statistical
    test, and we include a signicance parameter (α) corresponding to
    the probability of rejecting a fair ranking (i.e., a Type I error).


    Definition 3.1 (Fair representation condition). Let F (x;n,p) be
    the cumulative distribution function for a binomial distribution of
    parameters n and p. A set τ ⊆ Tk,n, having τp protected candidates
    fairly represents the protected group with minimal proportion p
    and signicance α, if F (τp ; k,p) > α

from the original paper.

In the plugin we operationalize this process by creating them inside elasticsearch as documents in their own internal store,
otherwise the process of calculating them on every request would it be very costly.


=======================
Create a new M table
=======================

To create a new M table you can issue the next command:

    POST _fs/_mtable/{name}/{proportion}/{alpha}/{k}

where the parameters are:

* name: A name to attach to this given collection of fairness tables.
* proportion: The proportion of protected elements.
* alpha: The significance parameter (α) corresponding to the probability of rejecting a fair ranking.
* k: The expected size of returned documents in the search.


for example:

    POST /_fs/_mtable/test/0.5/0.1/5

.. code-block:: json

    {
        "_index": ".fs_store",
        "_type": "store",
        "_id": "name(0.5,0.1,5)",
        "_version": 1,
        "result": "created",
        "forced_refresh": true,
        "_shards": {
            "total": 2,
            "successful": 1,
            "failed": 0
        },
        "_seq_no": 0,
        "_primary_term": 1
    }

this will store a document in elasticsearch that will look like:

.. code-block:: json

      {
        "_index": ".fs_store",
        "_type": "store",
        "_id": "name(0.5,0.1,5)",
        "_score": 1,
        "_source": {
          "name": "test",
          "type": "mtable",
          "proportion": 0.5,
          "alpha": 0.1,
          "k": 5,
          "mtable": [
            0,
            0,
            0,
            0,
            1,
            1
          ]
        }
      }

=======================
List all stored M tables
=======================

To list all stored M tables you can use this command:

    GET _fs/_mtable

this will give you an answer like:

.. code-block:: json

    {
        "took": 7,
        "timed_out": false,
        "_shards": {
        "total": 5,
        "successful": 5,
        "skipped": 0,
        "failed": 0
    },
    "hits": {
        "total": 3,
        "max_score": 1,
        "hits": [
        {
            "_index": ".fs_store",
            "_type": "store",
            "_id": "name(0.5,0.1,5)",
            "_score": 1,
            "_source": {
                "name": "test",
                "type": "mtable",
                "proportion": 0.5,
                "alpha": 0.1,
                "k": 5,
                "mtable": [
                 0,
                 0,
                 0,
                 0,
                 1,
                 1
                 ]
            }
        },
    ....
        ]
        }
    }


=======================
Delete stored M tables
=======================

Currently there is no functionality offered to delete an specific mtable, you should probably also never do that yourself.
However if you want to delete documents, use the standard document api from elastic and refer to the specific table
document id.