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

    POST /_fs/_mtable/test/0.5/0.1/5


=======================
List all stored M tables
=======================

To list all stored M tables you can use this command:

    GET _fs/_mtable



=======================
Delete stored M tables
=======================
