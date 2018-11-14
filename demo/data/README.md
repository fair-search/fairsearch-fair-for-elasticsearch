# Data in the Demo-Application
We used a free dataset of anonymized and sample resumes from [Kaggle](https://www.kaggle.com).
You can find the raw dataset [here](https://www.kaggle.com/iammhaseeb/resumes-dataset-with-labels).

## Data Processing
In order to make the demo-application more meaningful, we performed the following actions on the raw data:

1. Removed all labels, all text-formats and not-json-parsable symbols.
2. Removed all incomplete lines without any tolerance, meaning that we took only complete lines of the dataset.
3. Formatted every resume as a JSON object like this:
```
{"index": "test", "type": "test", "id": 3, "body": {"body": "HR Kate Surname Address Mobile ....", "gender": "f"}}
```
4. For all entries, we set the id to be smaller than 1000 if there was a female name in the first few sentences.
5. For all entries, we set the id to be greater or equal to 1000 if there was a male name in the first few sentences.
6. For all entries, where we saw no name in the first few lines, we added a random male or female name by hand with the corresponding id greater than 1000 for females and smaller than 1000 for males.
