The Plugin
**********

What the Fairsearch plugin does
-----------------

People search engines, as a main example of this plugin application, are not aware of the biases the traditional algorithms for search (aka TF/IDF or BM25) might be introducing in their search results. This will reduce the visibility of already disadvantaged groups corresponding to a legally protected category such as people with disabilities, racial or ethnic minorities, or an under-represented gender in a specific industry).

This plugin uses an efficient algorithm for producing a fair ranking given a protected attribute, i.e., a ranking in which the representation of the minority group does not fall below a minimum proportion *p* at any point in the ranking, while the utility of the ranking is maintained as high as possible.

This method can be used within an anti-discrimination framework such as positive actions. This is certainly, not the only way of achieving fairness, but this plugin provide an algorithm grounded in statistical tests that enables the implementation of a positive action policy in the context of search.

What the plugin does NOT
-----------------

This plugin uses a fairness criterion that requires a couple of input parameters, but it does impose specific parameters for that fairness (e.g., the proportion *p*). Those must be set according to the context of your application.
