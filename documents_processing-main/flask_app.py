from flask import Flask, request, jsonify
# from celery_config import make_celery
from field_predictor_mapping import field_to_predictor
from classification.classify_document import classify_document
from mega_sync import MegaSync
import docx2txt
from utils import download_mega_file
import os
import json
from datetime import datetime
from flask_cors import CORS
from process_local import ProcessLocal
from classification.classify_document import classify_document
import pandas as pd
import logging
from job_log_analysis import (
    process_job_log_sheet,
    load_cached_job_log,
    get_salesperson_by_job_number,
    get_unique_salespersons,
    get_jobs_by_salesperson,
    get_job_log_summary
)

#TODO: test the flask app
#TODO: Create a create_project and delete_project endpoint
#TODO: call all the predictors using celery

# Create Flask app
app = Flask(__name__)
CORS(app)

# File to store json data
json_data="data.json"

# Celery configuration with Redis as the broker
# app.config['CELERY_BROKER_URL'] = 'redis://localhost:6379/0'
# app.config['CELERY_RESULT_BACKEND'] = 'redis://localhost:6379/0'

# # Initialize Celery
# celery = make_celery(app)

# Setup logging
logging.basicConfig(level=logging.INFO)

JOB_LOG_PATH = os.path.join('JOB_LOG', 'UPDATED JOB LOG (2).xlsx')
CACHE_PATH = 'updated_job_log_cache.json'

#following route needs to be updated as required
@app.route('/extract', methods=['POST'])
def extract_fields():
    try:
        # Parse input data
        data = request.get_json()
        if not data:
            return jsonify({"error": "No data provided"}), 400
        # Initialize document_text so it's always in scope
        document_text = None
        # Handle document_text directly in the request
        if 'document_text' in data:
            document_text = data['document_text']
        # Handle Mega link file download if provided (and if document_text not supplied)
        elif 'mega_link' in data:
            mega_link = data['mega_link']
            try:
                # If you have code to retrieve the file from MEGA or from your JSON, put it here
                if os.path.exists(json_data):
                    with open(json_data, 'r') as f:
                        json_entries = json.load(f)
                    for entry in json_entries:
                        if entry.get('mega_link') == mega_link:
                            document_text = entry.get('document_text')
                            break
            except Exception as e:
                return jsonify({"error": f"Failed to process file: {str(e)}"}), 400
        # If, after both checks, we still have no document text, return an error
        if not document_text:
            return jsonify({"error": "No document text provided or unable to retrieve from mega_link"}), 400
        # Prepare fields and document type
        fields = data.get('fields', [])
        document_type = data.get("document_type", "")
        possible_document_types = data.get("possible_document_types", [])
        # Classify document if not specified
        if not document_type:
            document_type = classify_document(document_text, possible_document_types)
        # Initialize results dictionary
        results = {}
        # Process each requested field
        for field in fields:
            predictor = field_to_predictor.get(field)
            if not predictor:
                results[field] = "NO PREDICTOR FOUND!!"
                continue
            prediction = predictor(document_text, field, document_type)
            # **Safe‐guard against empty lists** here:
            candidates = prediction.get(field) or []
            if candidates:
                results[field] = candidates[0].get('text', 'NO DATA FOUND')
            else:
                results[field] = 'NO DATA FOUND'
        return jsonify(results)
    except Exception as e:
        # Catch-all error handling
        return jsonify({"error": f"Unexpected error: {str(e)}"}), 500
    
@app.route('/getlinks', methods=['GET'])
def get_all_links():
    try:
        email = "mega@qualitydxb.com"
        password = "^2lc%wT,Soo~"
        syncer = MegaSync(email, password)
            # Get all links from MEGA
        links = syncer.sync_files() 
        return jsonify(links)
    
    except Exception as e:
        # Catch-all error handling
        return jsonify({"error": f"Unexpected error: {str(e)}"}), 500
    
@app.route('/processlocal', methods=['POST'])
def process_local_route():
    """
    New endpoint to process local folders only.
    Expects JSON body with a list of folder paths, e.g.:
    {
        "folder_paths": ["./local_folder1", "./local_folder2"]
    }
    """
    try:
        data = request.get_json()
        folder_paths = data.get('folder_paths', [])

        # Instantiate the local processor and run
        local_processor = ProcessLocal(folder_paths=folder_paths)
        processed_result = local_processor.process_local_files()

        return jsonify({
            "status": "success",
            "processed_files_count": len(processed_result),
        })
    except Exception as e:
        return jsonify({"error": f"Error processing local folders: {str(e)}"}), 500

# NEW THINGHelper function to efficiently search for job number and sales person in a large xlsx file

def get_salesperson_by_job_number(job_number, file_path):
    try:
        for chunk in pd.read_excel(file_path, sheet_name=0, chunksize=10000, engine='openpyxl'):
            # Try to find the relevant columns (case-insensitive)
            columns = [col.lower() for col in chunk.columns]
            if 'job number' in columns:
                job_col = chunk.columns[columns.index('job number')]
            else:
                continue
            if 'sales person' in columns:
                sales_col = chunk.columns[columns.index('sales person')]
            else:
                continue
            # Filter for the job number
            match = chunk[chunk[job_col] == job_number]
            if not match.empty:
                sales_person = match.iloc[0][sales_col]
                if pd.isna(sales_person) or str(sales_person).strip() == '':
                    return 'no data found'
                return str(sales_person)
        return 'no data found'
    except Exception as e:
        return f'error: {str(e)}'

def get_unique_salespersons_and_jobs(file_path):
    sales_job_map = {}
    try:
        for chunk in pd.read_excel(file_path, sheet_name=0, chunksize=10000, engine='openpyxl'):
            columns = [col.lower() for col in chunk.columns]
            if 'job number' in columns and 'sales person' in columns:
                job_col = chunk.columns[columns.index('job number')]
                sales_col = chunk.columns[columns.index('sales person')]
                for _, row in chunk.iterrows():
                    sales_person = str(row[sales_col]).strip() if not pd.isna(row[sales_col]) else ''
                    job_number = row[job_col]
                    if sales_person:
                        if sales_person not in sales_job_map:
                            sales_job_map[sales_person] = set()
                        sales_job_map[sales_person].add(job_number)
        # Convert sets to sorted lists
        for k in sales_job_map:
            sales_job_map[k] = sorted(list(sales_job_map[k]))
        return list(sales_job_map.keys()), sales_job_map
    except Exception as e:
        return [], {}, f'error: {str(e)}'

@app.route('/process_job_log', methods=['POST'])
def process_job_log():
    """
    Process the Job Log Sheet and cache the results. Returns summary statistics.
    """
    result = process_job_log_sheet(JOB_LOG_PATH, CACHE_PATH)
    if 'error' in result:
        logging.error(f"Processing error: {result['error']}")
        return jsonify({'error': result['error']}), 500
    logging.info("Job log processed and cached.")
    return jsonify({'summary': result.get('summary', {})})

@app.route('/salesperson_by_job', methods=['POST'])
def salesperson_by_job():
    """
    Get the sales person for a given job number from the cached data.
    """
    data = request.get_json()
    job_number = data.get('job_number')
    if not job_number:
        return jsonify({'error': 'job_number is required'}), 400

    cache = load_cached_job_log(CACHE_PATH)
    raw = cache.get('job_to_sales_person', {}).get(str(job_number), None)

    if raw is None:
        # Job number not in cache at all
        return jsonify({
            'error': f'No data found for job_number {job_number}'
        }), 404

    # Trim and check for empty-ish values
    salesperson = str(raw).strip()
    if salesperson.lower() in ('', 'nan', 'none', 'null'):
        # Job in cache but no salesperson assigned
        return jsonify({
            'job_number': job_number,
            'salesperson': 'Not Assigned'
        }), 200

    # Normalize: lowercase + spaces → underscores
    normalized = salesperson.lower().replace(' ', '_')
    return jsonify({
        'job_number': job_number,
        'salesperson': normalized
    }), 200


@app.route('/unique_salespersons', methods=['GET'])
def unique_salespersons():
    """
    Get the list of unique sales persons from the cached data, excluding empty/null values.
    """
    cache = load_cached_job_log(CACHE_PATH)
    salespersons = [s for s in get_unique_salespersons(cache) if s and str(s).strip().lower() not in ('', 'nan', 'none', 'null')]
    return jsonify({'unique_sales_persons': salespersons})

@app.route('/jobs_by_salesperson', methods=['GET'])
def jobs_by_salesperson():
    """
    Get an analytical mapping of sales persons to their job numbers and job counts from the cached data.
    Jobs with missing/empty salesperson are grouped under 'Not Assigned'.
    """
    cache = load_cached_job_log(CACHE_PATH)
    jobs_by_sales = cache.get('jobs_by_sales_person', {})
    job_to_sales = cache.get('job_to_sales_person', {})
    # Build the analytical structure
    result = {}
    # Track jobs with missing/empty salesperson
    unassigned_jobs = []
    for job_number, salesperson in job_to_sales.items():
        if not salesperson or str(salesperson).strip().lower() in ('', 'nan', 'none', 'null'):
            unassigned_jobs.append(job_number)
    # Add regular salespersons
    for salesperson, job_numbers in jobs_by_sales.items():
        if salesperson and str(salesperson).strip().lower() not in ('', 'nan', 'none', 'null'):
            result[salesperson] = {
                'job_count': len(job_numbers),
                'job_numbers': job_numbers
            }
    # Add unassigned jobs under 'Not Assigned' if any
    if unassigned_jobs:
        result['Not Assigned'] = {
            'job_count': len(unassigned_jobs),
            'job_numbers': unassigned_jobs
        }
    return jsonify({'data': result})

@app.route('/job_log_summary', methods=['GET'])
def job_log_summary():
    """
    Get summary statistics from the cached data.
    """
    cache = load_cached_job_log(CACHE_PATH)
    return jsonify({'summary': get_job_log_summary(cache)})


@app.route('/classify_document', methods=['POST'])
def classify_document_route():
    """
    Expects JSON:
    {
      "document_text": "…",                   # required
      "possible_document_types": ["A", "B"]   # optional
    }
    Returns JSON:
    {
      "document_type": "A"
    }
    """
    try:
        data = request.get_json(force=True)
    except Exception:
        return jsonify({"error": "Request body must be valid JSON"}), 400

    document_text = data.get('document_text')
    if not document_text:
        return jsonify({"error": "Missing required field: document_text"}), 400

    possible_types = data.get('possible_document_types')  # can be None

    try:
        doc_type = classify_document(document_text, possible_types)
    except Exception as e:
        return jsonify({"error": f"Classification failed: {str(e)}"}), 500

    if not doc_type:
        return jsonify({"document_type": None}), 200

    return jsonify({"document_type": doc_type}), 200


# This is a rough structure of the flask endpoint which is using celery. Disabled for now
# @app.route('/extract', methods=['POST'])
# def extract_fields():
#     data = request.get_json()

#     # Validate input data
#     if not data or 'document_text' not in data or 'fields' not in data:
#         return jsonify({"error": "Invalid request. 'document_text' and 'fields' are required."}), 400

#     document_text = data['document_text']
#     fields = data['fields']
#     document_type = data.get("document_type", "")
#     possible_document_types = data.get("possible_document_types", [])

#     # Step 1: Classify the document
#     if not document_type:
#         document_type_task = classify_document.delay(document_text, possible_document_types)
#         document_type = document_type_task.get(timeout=5)  # Blocking until classification completes


#     # Dictionary to hold tasks and field mappings
#     tasks = {}

#     # Map fields to the corresponding Celery task
#     for field in fields:
#         predictor = field_to_predictor.get(field, None)
#         if predictor:
#             tasks[field] = predictor.delay(field, document_text)
#         else:
#             return jsonify({"error": f"No predictor available for field '{field}'"}), 400

#     # Gather results
#     results = {}
#     for field, task in tasks.items():
#         results[field] = task.get(timeout=10)  # Blocks until the task is complete (with a timeout)

#     return jsonify(results)

if __name__ == '__main__':
    app.run(debug=True)



