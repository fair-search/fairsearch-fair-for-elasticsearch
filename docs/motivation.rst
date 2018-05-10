Motivation
**********

=======================
Ranking bias
=======================

Search engines today are used to rank many different types of items, including items that represent *people*. Job recruiting search engines, marketplaces for consulting and other services, dating apps, etc. have at its core the idea of ranking/ordering people from most relevant to less relevant, which often means from "best" to "worst".

Traditional scoring methods such as TF-IDF or BM25 (the two most popular ones) can introduce a certain degree of bias; the main motivation of this plugin is to provider methods to search without having this problem.

A computer system is biased [Friedman 1996] if:

    It systematically and unfairly discriminate[s] against certain individuals or groups of individuals in favor of
    others. A system discriminates unfairly if it denies an opportunity or a good or if it assigns an undesirable
    outcome to an individual or a group of individuals on grounds that are unreasonable or inappropriate.

In algorithmic bias, an important concept is that of a *protected group*, which is a category of individuals protected by law, voluntary commitments, or other reasons. Search results are considered *unfair* if members of a protected group are systematically ranked lower than those of a non-protected group.

Examples where a fair search would be required are for instance, the US Equal Employment Opportunity Commission, which sets a
goal of 12% of workers with disabilities in federal agencies in the US, while in Spain, a minimum of 40% of political candidates in voting districts exceeding a certain size must be women.

=======================
Real-world example
=======================

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

=======================
What the Fairsearch plugin does
=======================

People search engines, as a main example of this plugin application, are not aware of the biases the traditional algorithms for search (aka TF/IDF or BM25) might be introducing in their search results. This will reduce the visibility of already disadvantaged groups corresponding to a legally protected category such as people with disabilities, racial or ethnic minorities, or an under-represented gender in a specific industry).

This plugin uses an efficient algorithm for producing a fair ranking given a protected attribute, i.e., a ranking in which the representation of the minority group does not fall below a minimum proportion *p* at any point in the ranking, while the utility of the ranking is maintained as high as possible.

This method can be used within an anti-discrimination framework such as positive actions. This is certainly, not the only way of achieving fairness, but this plugin provide an algorithm grounded in statistical tests that enables the implementation of a positive action policy in the context of search.

=======================
What the plugin does NOT
=======================

This plugin uses a fairness criterion that requires a couple of input parameters, but it does impose specific parameters for that fairness (e.g., the proportion *p*). Those must be set according to the context of your application.
