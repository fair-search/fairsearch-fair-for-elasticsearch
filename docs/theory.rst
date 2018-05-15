The Theory
**********

Ranking can be biased
-----------------

Core concepts: ranking bias
~~~~~~~~~~~~~~~~~~~~~~
Search engines today are used to rank many different types of items, including items that represent *people*. Job recruiting search engines, marketplaces for consulting and other services, dating apps, etc. have at its core the idea of ranking/ordering people from most relevant to less relevant, which often means from "best" to "worst".

Traditional scoring methods such as TF-IDF or BM25 (the two most popular ones) can introduce a certain degree of bias; the main motivation of this plugin is to provider methods to search without having this problem.

A computer system is biased [Friedman 1996] if:

    It systematically and unfairly discriminate[s] against certain individuals or groups of individuals in favor of
    others. A system discriminates unfairly if it denies an opportunity or a good or if it assigns an undesirable
    outcome to an individual or a group of individuals on grounds that are unreasonable or inappropriate.

In algorithmic bias, an important concept is that of a *protected group*, which is a category of individuals protected by law, voluntary commitments, or other reasons. Search results are considered *unfair* if members of a protected group are systematically ranked lower than those of a non-protected group.

Examples where a fair search would be required are for instance, the US Equal Employment Opportunity Commission, which sets a
goal of 12% of workers with disabilities in federal agencies in the US, while in Spain, a minimum of 40% of political candidates in voting districts exceeding a certain size must be women.

Real-world example: Job Search
~~~~~~~~~~~~~~~~~~~~~~

Consider the three rankings in the table below, corresponding to searches for an “economist,” “market research analyst,” and “copywriter” in a job search engine, i.e., an online platform for jobs that is used by recruiters and headhunters to find suitable candidates.

Positions 1, 2, ..., 10 are the top-10 ranking positions. A letter ``m`` indicates the candidate is a man, while ``f`` indicates the candidate is a woman.

+-------------+----+----+----+----+----+----+----+----+----+------+-----------------------------+-----------------------------+
| Query       | 1  | 2  | 3  | 4  | 5  | 6  | 7  | 8  | 9  | R10  | Men:Women (top-10)          | Men:Women (top-40)          |
+=============+====+====+====+====+====+====+====+====+====+======+=============================+=============================+
| Econ.       | f  | m  | m  | m  | m  | m  | m  | m  | m  | m    | 90%:10%                     | 73%:27%                     |
+-------------+----+----+----+----+----+----+----+----+----+------+-----------------------------+-----------------------------+
| Analyst     | f  | m  | f  | f  | f  | f  | f  | m  | f  | f    | 20%:80%                     | 43%:57%                     |
+-------------+----+----+----+----+----+----+----+----+----+------+-----------------------------+-----------------------------+
| Copywr.     | m  | m  | m  | m  | m  | m  | f  | m  | m  | m    | 90%:10%                     | 73%:27%                     |
+-------------+----+----+----+----+----+----+----+----+----+------+-----------------------------+-----------------------------+

While analyzing the extent to which candidates of both genders are represented as we go down these lists, we can observe that the proportions keep changing (compare the top-10 against the the top-40).

As a consequence, recruiters examining these lists will see different proportions depending on the point at which they decide to stop. This can cause under represented groups have not a fair outcome, so limiting the visibility.

The FA*IR algorithm
-----------------

What is the fairness criterion applied by FA*IR?
~~~~~~~~~~~~~~~~~~~~~~

A *prefix* of a list are the first elements of the list; for instance, the list *(A, B, C)* has prefixes *(A)*, *(A, B)*, and *(A, B, C)*.

The fairness criterion in FA*IR [Zehlike et al. 2017] requires that the number of protected elements *in every prefix* of the list corresponds to the number of protected elements we would expect if they where picked at random using Bernoulli trials (independent “coin tosses”) with success probability *p*.

This correspondence is not exact, and there is a parameter *α* corresponding to the accepted probability of a Type I error, which means rejecting a fair ranking in this test. A typical value of *α* could be 0.1, or 10%.

Given *p*, *α*, and *k*, which is the total length of the list to be returned, an M-table is computed. This M-table indicates what is the minimum number of protected elements at every prefix.

Example
~~~~~~~~~~~~~~~~~~~~~~

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

Corrections for multiple hypotheses testing
~~~~~~~~~~~~~~~~~~~~~~

The FA*IR plug-in does not use directly the parameter *α*, but computes a *corrected α*, which is in general smaller. For instance, for *p=0.5*, *α=0.1*, *k=100*, the *corrected α=0.0207*.

The *corrected α* accounts for the fact that, in a list of size *k*, there will be *k* tests performed, one for every prefix (for instance, 100). Hence, the probability of failing in at least one prefix is larger than *α* (because there are 100 attempts being made). The correction mechanism is explained in the FA*IR paper [Zehlike et al. 2017].

References
-----------------

[Friedman 1996] Friedman, B., & Nissenbaum, H. (1996). `Bias in computer systems <https://vsdesign.org/publications/pdf/64_friedman.pdf>`_. ACM Transactions on Information Systems (TOIS), 14(3), 330-347.

[Zehlike et al. 2017] Zehlike, M., Bonchi, F., Castillo, C., Hajian, S., Megahed, M., and Baeza-Yates, R. (2017, November). `FA*IR: A fair top-k ranking algorithm <https://arxiv.org/abs/1706.06368>`_. Proc. CIKM 2017 (pp. 1569-1578). ACM Press.