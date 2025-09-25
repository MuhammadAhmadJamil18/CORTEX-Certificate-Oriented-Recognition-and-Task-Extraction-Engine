"# CORTEX-Certificate-Oriented-Recognition-and-Task-Extraction-Engine" 

General Flow
Flask App
1. Create Project Endpoint
Used to create a project for each client.
Each client has a unique ID or name.
The project configuration defines which document types are supported (for example, classification reports or quality certificates).

2. Extract Information Endpoint
Takes a document (or document path) as input.
Uses the project configuration to process it.

3. Other Endpoints
Modify an existing project.
Delete a project.

4. Classification
If the document type is already known, classification is skipped.
If classification is needed, retrieve the possible document types from the project configuration (using the project ID).

5. Extraction
Extract the relevant fields for that project or client.
Currently, "client name" is just a placeholder.
To add a new rule-based field:
Register a predictor in field_predictor_mapping.py.
Create a new file in the extraction/rules folder.
Define the rules for the field in extraction_rules.json.
Write a test for the new field.
You can build and test rules using tools like regex101.com.

6. Return Results
Output is returned in JSON format.


Guidelines
1.	Write unit tests for every new function.
2.	Add clear docstrings and comments.
3.	Always specify input and output types for functions.


Testing
Run tests with:
pytest path_to_test_file
Example:
pytest tests\test_next_date_of_inspection.py

