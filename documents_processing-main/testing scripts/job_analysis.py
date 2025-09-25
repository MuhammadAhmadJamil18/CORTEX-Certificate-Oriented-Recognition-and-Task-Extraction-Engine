import pandas as pd
import os
import json
from typing import Dict, List, Tuple, Any
from datetime import datetime
import hashlib
import glob
from rapidfuzz import process, fuzz

CACHE_FILE = 'job_log_cache.json'

# --- Salesperson Cleaning Logic ---
SALESPERSON_ALIASES = {
    "AHAMD": "AHMED",
    "AHAMED": "AHMED",
    "AHEMAD": "AHMED",
    "AHEMED": "AHMED",
    "AHM,AD": "AHMED",
    "AHMD": "AHMED",
    ",EHAR": "MEHAR",
    "M.EHAR": "MEHAR",
    "MEHAR C": "MEHAR",
    "MEHER": "MEHAR",
    "MERHAR EJAZ": "MEHAR EJAZ",
    "BIQRA": "IQRA",
    "IQAR": "IQRA",
    "IQERA": "IQRA",
    "IQRAQ": "IQRA",
    # ...add more as needed...
}

NON_SALESPERSON_KEYWORDS = [
    "CHAIN SLING", "CLIENT OF UDAY", "CRADLE", "DIESEL TANK AND OPERATOR MEHAR",
    "DUBAI", "DUBAI BRANCH", "FIRST AID WITH AED", "FIRSTAID FIRE", "GENERATOR",
    "ICRQ", "LANDLINE", "LOADING PLATFORM YASAR", "MOBILE CRANE", "OFFICE",
    "RIGGER", "SKID STEER LOADER", "SKILL SAFE", "TRIMAX", "WELDER",
    "-", "…..", "I", ""
    # ...add more as needed...
]

def _clean_salesperson_name(name, canonical_names=None, threshold=90):
    name = name.strip().upper()
    if name in SALESPERSON_ALIASES:
        name = SALESPERSON_ALIASES[name]
    if name in NON_SALESPERSON_KEYWORDS or not name or name in ["-", "…..", "I"]:
        return None
    # Fuzzy match if not an exact match and canonical_names provided
    if canonical_names:
        match = process.extractOne(name, canonical_names, scorer=fuzz.ratio)
        if match and match[1] >= threshold:
            return match[0]
    return name

def compute_file_hash(file_path: str) -> str:
    """Compute a hash of the file's contents for change detection."""
    hasher = hashlib.md5()
    with open(file_path, 'rb') as f:
        for chunk in iter(lambda: f.read(4096), b''):
            hasher.update(chunk)
    return hasher.hexdigest()

def validate_cache(cache: Dict[str, Any], source_file: str) -> bool:
    """Validate if the cache is still fresh compared to the source file."""
    if not cache.get('metadata'):
        return False
    try:
        source_modified = os.path.getmtime(source_file)
        cache_modified = datetime.fromisoformat(cache['metadata']['processed_at'])
        return source_modified <= cache_modified.timestamp()
    except (OSError, ValueError, KeyError):
        return False

def update_cache_incrementally(existing_cache: Dict[str, Any], new_data: Dict[str, Any]) -> Dict[str, Any]:
    """Update existing cache with new data incrementally."""
    return {
        'unique_sales_persons': sorted(set(existing_cache.get('unique_sales_persons', []) + new_data['unique_sales_persons'])),
        'jobs_by_sales_person': {**existing_cache.get('jobs_by_sales_person', {}), **new_data['jobs_by_sales_person']},
        'job_to_sales_person': {**existing_cache.get('job_to_sales_person', {}), **new_data['job_to_sales_person']},
        'summary': {
            'total_rows': existing_cache.get('summary', {}).get('total_rows', 0) + new_data['summary']['total_rows'],
            'unique_sales_person_count': len(set(existing_cache.get('unique_sales_persons', []) + new_data['unique_sales_persons'])),
            'missing_sales_person_count': existing_cache.get('summary', {}).get('missing_sales_person_count', 0) + new_data['summary']['missing_sales_person_count']
        },
        'metadata': new_data['metadata']
    }

def process_dataframe(df: pd.DataFrame, sheet_name: str) -> Tuple[Dict[str, set], Dict[str, str], int, List[str]]:
    """Process a single dataframe and return the extracted data."""
    sales_job_map = {}
    job_to_sales = {}
    missing_sales_person = 0
    debug_info = []
    
    # Look for relevant columns (case-insensitive)
    columns = [str(col).lower() for col in df.columns]
    debug_info.append(f"Columns found in {sheet_name}: {list(df.columns)}")
    
    # Check for different possible column name variations
    job_col = None
    sales_col = None
    
    # Look for exact matches first, then fall back to pattern matching
    known_job_columns = ['JOB #', 'JOB NUMER']
    known_sales_columns = ['SALES PERSON']
    
    # Try exact matches first
    for col in df.columns:
        if col in known_job_columns:
            job_col = col
            debug_info.append(f"Found exact match for job column: {col}")
            break
            
    for col in df.columns:
        if col in known_sales_columns:
            sales_col = col
            debug_info.append(f"Found exact match for sales column: {col}")
            break
    
    # If exact matches weren't found, try pattern matching
    if not job_col:
        for col in df.columns:
            col_lower = str(col).lower()
            if ('job' in col_lower and 
                any(x in col_lower for x in ['#', 'number', 'numer', 'no', 'no.', 'num'])):
                job_col = col
                debug_info.append(f"Found pattern match for job column: {col}")
                break
    
    if not sales_col:
        for col in df.columns:
            col_lower = str(col).lower()
            if ('salesperson' in col_lower.replace(' ', '') or
                ('sales' in col_lower and any(x in col_lower for x in ['person', 'rep', 'representative']))):
                sales_col = col
                debug_info.append(f"Found pattern match for sales column: {col}")
                break
    
    if job_col and sales_col:
        debug_info.append(f"Processing data with columns: {job_col} and {sales_col}")
        canonical_names = set(SALESPERSON_ALIASES.values())
        for _, row in df.iterrows():
            raw_sales_person = str(row[sales_col]).strip() if not pd.isna(row[sales_col]) else ''
            sales_person = _clean_salesperson_name(raw_sales_person, canonical_names=canonical_names)
            job_number = str(row[job_col]).strip() if not pd.isna(row[job_col]) else ''
            if not job_number or job_number == 'nan':
                continue
            if not sales_person:
                missing_sales_person += 1
            else:
                if sales_person not in sales_job_map:
                    sales_job_map[sales_person] = set()
                sales_job_map[sales_person].add(job_number)
                job_to_sales[job_number] = sales_person
    else:
        debug_info.append(f"Could not find required columns")
        if not job_col:
            debug_info.append("Missing job number column")
        if not sales_col:
            debug_info.append("Missing sales person column")
    
    return sales_job_map, job_to_sales, missing_sales_person, debug_info

def process_job_log_sheet(file_path: str, cache_path: str = CACHE_FILE) -> Dict[str, Any]:
    """
    Process the Job Log Sheet (Excel or CSV) and cache the results in a JSON file.
    Returns a dictionary with:
        - unique_sales_persons: List[str]
        - jobs_by_sales_person: Dict[str, List[Any]]
        - job_to_sales_person: Dict[str, str]
        - summary: Dict[str, Any]
        - metadata: Dict[str, Any]
    """
    temp_cache_path = f"{cache_path}.tmp"
    sales_job_map = {}
    job_to_sales = {}
    total_rows = 0
    missing_sales_person = 0
    processed_sheets = []
    all_debug_info = []
    
    try:
        if file_path.lower().endswith('.xlsx'):
            # Process Excel file
            excel_file = pd.ExcelFile(file_path, engine='openpyxl')
            sheet_names = excel_file.sheet_names
            all_debug_info.append(f"Processing Excel file with sheets: {sheet_names}")
            
            for sheet_name in sheet_names:
                # Try reading with different header rows
                for header_row in [0, 1, 2, 3, 4, 5]:
                    try:
                        df = pd.read_excel(file_path, sheet_name=sheet_name, header=header_row, engine='openpyxl')
                        total_rows += len(df)
                        
                        sheet_sales_job_map, sheet_job_to_sales, sheet_missing, debug_info = process_dataframe(df, f"{sheet_name} (header row {header_row})")
                        
                        if sheet_sales_job_map:
                            processed_sheets.append(f"{sheet_name} (header row {header_row})")
                            sales_job_map.update(sheet_sales_job_map)
                            job_to_sales.update(sheet_job_to_sales)
                            missing_sales_person += sheet_missing
                            all_debug_info.extend(debug_info)
                            break  # Found data, no need to try other header rows
                        else:
                            all_debug_info.extend(debug_info)
                    except Exception as e:
                        all_debug_info.append(f"Error reading {sheet_name} with header row {header_row}: {str(e)}")
                        continue
        
        else:
            # Process CSV file
            df = pd.read_csv(file_path)
            total_rows = len(df)
            file_name = os.path.basename(file_path)
            all_debug_info.append(f"Processing CSV file: {file_name}")
            
            sheet_sales_job_map, sheet_job_to_sales, sheet_missing, debug_info = process_dataframe(df, file_name)
            
            if sheet_sales_job_map:
                processed_sheets.append(file_name)
                sales_job_map.update(sheet_sales_job_map)
                job_to_sales.update(sheet_job_to_sales)
                missing_sales_person += sheet_missing
            
            all_debug_info.extend(debug_info)
        
        if not sales_job_map:
            debug_str = "\n".join(all_debug_info)
            return {
                'error': 'No data found with job numbers and sales persons in any sheet',
                'debug_info': debug_str
            }
        
        # Convert sets to sorted lists
        for k in sales_job_map:
            sales_job_map[k] = sorted(list(sales_job_map[k]))
        
        unique_sales = sorted(list(sales_job_map.keys()))
        summary = {
            'total_rows': total_rows,
            'unique_sales_person_count': len(unique_sales),
            'missing_sales_person_count': missing_sales_person,
            'sheets_processed': processed_sheets,
            'debug_info': all_debug_info
        }
        
        result = {
            'unique_sales_persons': unique_sales,
            'jobs_by_sales_person': sales_job_map,
            'job_to_sales_person': job_to_sales,
            'summary': summary,
            'metadata': {
                'processed_at': datetime.now().isoformat(),
                'source_file': file_path,
                'source_file_hash': compute_file_hash(file_path),
                'source_file_modified': os.path.getmtime(file_path)
            }
        }

        # Try to update incrementally if possible
        try:
            with open(cache_path, 'r', encoding='utf-8') as f:
                existing_cache = json.load(f)
            if validate_cache(existing_cache, file_path):
                result = update_cache_incrementally(existing_cache, result)
        except (FileNotFoundError, json.JSONDecodeError):
            pass  # Proceed with full cache write

        # Atomic write using temporary file
        with open(temp_cache_path, 'w', encoding='utf-8') as f:
            json.dump(result, f, indent=2)
        os.replace(temp_cache_path, cache_path)
        
        return result
    except Exception as e:
        if os.path.exists(temp_cache_path):
            os.remove(temp_cache_path)
        return {
            'error': str(e),
            'debug_info': "\n".join(all_debug_info)
        }

def load_cached_job_log(cache_path: str = CACHE_FILE) -> Dict[str, Any]:
    """Load and validate the cached job log data."""
    try:
        with open(cache_path, 'r', encoding='utf-8') as f:
            cache = json.load(f)
        
        # Validate cache structure
        required_keys = ['unique_sales_persons', 'jobs_by_sales_person', 'job_to_sales_person', 'summary', 'metadata']
        if not all(key in cache for key in required_keys):
            return None
        
        return cache
    except (FileNotFoundError, json.JSONDecodeError):
        return None

def get_salesperson_by_job_number(job_number: str, cache: Dict[str, Any]) -> str:
    """Get the sales person for a given job number from the cached data."""
    return cache.get('job_to_sales_person', {}).get(str(job_number), 'no data found')

def get_unique_salespersons(cache: Dict[str, Any]) -> List[str]:
    """Get the list of unique sales persons from the cached data."""
    return cache.get('unique_sales_persons', [])

def get_jobs_by_salesperson(cache: Dict[str, Any]) -> Dict[str, List[str]]:
    """Get the mapping of sales persons to their job numbers from the cached data."""
    return cache.get('jobs_by_sales_person', {})

def get_job_log_summary(cache: Dict[str, Any]) -> Dict[str, Any]:
    """Get summary statistics from the cached data."""
    return {
        **cache.get('summary', {}),
        'cache_metadata': cache.get('metadata', {})
    } 