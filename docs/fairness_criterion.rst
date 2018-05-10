Fairness criterion
******************

=======================
What is the fairness criterion applied by FA*IR?
=======================

A *prefix* of a list are the first elements of the list; for instance, the list *(A, B, C)* has prefixes *(A)*, *(A, B)*, and *(A, B, C)*. 

The fairness criterion in FA*IR [Zehlike et al. 2017] requires that the number of protected elements *in every prefix* of the list corresponds to the number of protected elements we would expect if they where picked at random using Bernoulli trials (independent “coin tosses”) with success probability *p*.

This correspondence is not exact, and there is a parameter *α* corresponding to the accepted probability of a Type I error, which means rejecting a fair ranking in this test. A typical value of *α* could be 0.1, or 10%.

Given *p*, *α*, and *k*, which is the total length of the list to be returned, an M-table is computed. This M-table indicates what is the minimum number of protected elements at every prefix.

=======================
Example
=======================

This example illustrates how the re-ranker works, but we will be omitting a correction on *α* that will be explained next.

Suppose *p=0.5*, this means that we would like a list in which the protected candidates are at least 50% of every prefix. Suppose *α=0.1*, meaning we accept a 10% of Type I error.

The M-table in this case is:

+----------+---+---+---+---+---+---+---+---+---+----+
+ Position | 1 | 2 | 3 | 4 | 5 | 6 | 7 | 8 | 9 | 10 | 
+==========+===+===+===+===+===+===+===+===+===+====+
| M        | 0 | 0 | 0 | 1 | 1 | 1 | 2 | 2 | 3 | 3  |
+----------+---+---+---+---+---+---+---+---+---+----+

This means that, among the top 3 elements, even if there is no protected item, we would still consider the list to be fair, because if you toss a fair coin (*p=0.5*) 3 times, the chance of getting "heads" 3 times is above 10% (remember *α=0.1*). However, among the top 4 items, at least one of them has to be protected, because if you toss a fair coin, the chance of getting *heads* 4 times is below 10%, hence, with this *α* it is not believable that the coin was fair in the first place.

The rest of the M table is easy to interpret; for instance: among the top-5 elements there has to be at least 1 protected, among the top-7 there must be 2 at least, and among the top-9 there must be 3 at least.

=======================
Corrections for multiple hypotheses testing
=======================

The FA*IR plug-in does not use directly the parameter *α*, but computes a *corrected α*, which is in general smaller. For instance, for *p=0.5*, *α=0.1*, *k=100*, the *corrected α=0.0207*.

The *corrected α* accounts for the fact that, in a list of size *k*, there will be *k* tests performed, one for every prefix (for instance, 100). Hence, the probability of failing in at least one prefix is larger than *α* (because there are 100 attempts being made). The correction mechanism is explained in the FA*IR paper [Zehlike et al. 2017].

=======================
References
=======================

[Friedman 1996] Friedman, B., & Nissenbaum, H. (1996). `Bias in computer systems <https://vsdesign.org/publications/pdf/64_friedman.pdf>`_. ACM Transactions on Information Systems (TOIS), 14(3), 330-347.

[Zehlike et al. 2017] Zehlike, M., Bonchi, F., Castillo, C., Hajian, S., Megahed, M., and Baeza-Yates, R. (2017, November). `FA*IR: A fair top-k ranking algorithm <https://arxiv.org/abs/1706.06368>`_. Proc. CIKM 2017 (pp. 1569-1578). ACM Press.
