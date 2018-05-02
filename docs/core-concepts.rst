Core Concepts
*******************************

Welcome! You’re here if you’re interested in adding fair ranking capabilities to your Elasticsearch system.
This guidebook is intended for Search engineers and data scientists.


=======================
Introduction
=======================

Having a search engines over people is are very common thing for tasks such as job recruiting, building a representative
group, etc. However traditional search score method such as TF-IDF or BM25 (the two most popular ones) can introduce
a certain degree of bias.

As we already stated, the main motivation of this plugin is to provider methods to search without having this problem.

According to the research, a computer system is biased if:

    It systematically and unfairly discriminate[s] against certain individuals or groups of individuals in favor of
    others. A system discriminates unfairly if it denies an opportunity or a good or if it assigns an undesirable
    outcome to an individual or a group of individuals on grounds that are unreasonable or inappropriate.


The outcome of a search will be unfair if members of a protected group are systematically ranked lower than those
of a privileged group.

Examples where a fair search would be required are for instance,the US Equal Employment Opportunity Commission sets a
goal of 12% of workers with disabilities in federal agencies in the US,2 while in Spain, a minimum of 40% of
political candidates in voting districts exceeding a certain size must be women.

The fairness criterion compare the number of protected elements in every subset with the expected number of protected
elements if they where picked at random using Bernoulli trials (independent “coin tosses”) with success probability
p. Given that we use a statistical test for this comparison, we include a signicance parameter α corresponding to
the probability of a Type I error, which means rejecting a fair ranking in this test.

The parameters alpha and p will be core to control the behaviour of this algorithm.

=======================
Real world examples from XING (recruiting portal)
=======================

Consider the three rankings in Table 1 corresponding to searches for an “economist” “market research analyst”
and “copywriter” in XING, an online platform for jobs that is used by recruiters and headhunters to find
suitable candidates.

+-------------+----------------------+-----------------------------+
| Search term | Position             | Top 10 (m/f) Top 40 (m/f)   |
+=============+======================+=============================+
| Econ        | f m m m m m m m m m  | 90% 10% 73% 27%             |
+-------------+----------------------+-----------------------------+
| Analyst     | f m f f f f f m f f  | 20% 80% 43% 57%             |
+-------------+----------------------+-----------------------------+
| Copywr      | m m m m m m f m m m  | 90% 10% 73% 27%             |
+-------------+----------------------+-----------------------------+
Table 1: Example of non-uniformity of the top-10 vs. the top-40 results for dierent queries in XING (Jan 2017).


While analyzing the extent to which candidates of both genders are represented as we go down these lists,
we can observe that the proportions keep changing and is not uniform (see, for instance, the top-10 vs. the top-40).
As a consequence, recruiters examining these lists will see dierent proportions depending on the point at which they
decide to stop. This can cause under represented groups have not a fair outcome, so limiting the visibility.

=======================
Summary
=======================

This plugin has an efficient algorithm, named FA*IR, for producing a top-k ranking that maximizes utility while
satisfying ranked group fairness, as long as there are “enough” protected candidates to achieve the desired minimum
proportion.

This method can be used within an anti-discrimination framework such as positive actions. This is certainly, not the
only way of achieving fairness, but this plugin provide an algorithm grounded in statistical tests that enables
the implementation of a positive action policy in the context of search.