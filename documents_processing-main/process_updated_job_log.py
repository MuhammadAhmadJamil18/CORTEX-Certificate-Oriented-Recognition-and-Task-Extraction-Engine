#!/usr/bin/env python3
"""
Script to process the updated job log file and cache the results.
Focuses on JOB NO and SALES PERSON columns, handles multiple salespersons per job.
"""

import pandas as pd
import os
import json
import re
from typing import Dict, List, Set, Tuple
from datetime import datetime
from rapidfuzz import fuzz, process

def clean_salesperson_name(name: str) -> str:
    """Clean and standardize salesperson name."""
    if not name or pd.isna(name):
        return ""
    
    name = str(name).strip()
    if name.lower() in ['nan', 'none', 'null', '']:
        return ""
    
    # Remove extra spaces and standardize
    name = re.sub(r'\s+', ' ', name)
    return name

def split_multiple_salespersons(salesperson_text: str) -> List[str]:
    """Split multiple salespersons in one field (e.g., 'ZAHID /MEHAR' -> ['ZAHID', 'MEHAR'])."""
    if not salesperson_text:
        return []
    
    # Split by common separators
    separators = r'[/\\&,;]'
    parts = re.split(separators, salesperson_text)
    
    # Clean each part
    cleaned_parts = []
    for part in parts:
        cleaned = clean_salesperson_name(part)
        if cleaned:
            cleaned_parts.append(cleaned)
    
    return cleaned_parts

def map_salesperson_to_standard(name: str) -> str:
    """Map salesperson names to the updated standard list using fuzzy matching."""
    
    # Updated standard salesperson list
    standard_names = {
        "ALI - ENGINEER",
        "HALEETHA", 
        "IJAZ AHMAD",
        "MS.IQRA JAMIL",
        "ISHAN - ENGINEER",
        "MR.MEHAR EJAZ",
        "M ZAHID",
        "RAHID - ENGINEER",
        "RAMEESHA",
        "SALIK ENGINEER",
        "SALMAA",
        "SHAHBAZ HASSAN",
        "YASAR",
        "ZEESHAN JAVED"
    }
    
    # Direct mapping for common variations
    name_mapping = {
        "ALI": "ALI - ENGINEER",
        "MS.IQRA": "MS.IQRA JAMIL",
        "MS IQRA": "MS.IQRA JAMIL",
        "MS. IQRA": "MS.IQRA JAMIL",
        "MR.MEHAR": "MR.MEHAR EJAZ",
        "MR. MEHAR": "MR.MEHAR EJAZ",
        "MR.MEHER": "MR.MEHAR EJAZ",
        "MEHAR": "MR.MEHAR EJAZ",
        "MAHER": "MR.MEHAR EJAZ",
        "ZAHID": "M ZAHID",
        "SALIK": "SALIK ENGINEER",
        "SALMA": "SALMAA",
        "SHAHBAZ GLOBAL": "SHAHBAZ HASSAN",
        "ZEESHAN": "ZEESHAN JAVED",
        "IJAZ AHMED": "IJAZ AHMAD"
    }
    
    # Try direct mapping first
    if name in name_mapping:
        return name_mapping[name]
    
    # Try fuzzy matching
    best_match = process.extractOne(name, standard_names, scorer=fuzz.ratio)
    if best_match and best_match[1] >= 80:  # 80% similarity threshold
        return best_match[0]
    
    # If no good match found, return original name
    return name

def process_job_log_sheet(file_path: str) -> Dict:
    """Process the updated job log file and extract job-salesperson mappings."""
    
    print("=" * 80)
    print("PROCESSING UPDATED JOB LOG FILE")
    print("=" * 80)
    
    if not os.path.exists(file_path):
        return {"error": f"File not found: {file_path}"}
    
    print(f"📁 File: {file_path}")
    
    # Data structures to store results
    job_to_salespersons = {}  # job_number -> list of salespersons
    salesperson_to_jobs = {}  # salesperson -> list of job numbers
    all_salespersons = set()
    processed_jobs = 0
    skipped_entries = 0
    
    try:
        print("📖 Opening Excel file...")
        excel_file = pd.ExcelFile(file_path, engine='openpyxl')
        sheet_names = excel_file.sheet_names
        print(f"📋 Processing sheets: {sheet_names}")
        
        for sheet_name in sheet_names:
            print(f"\n📄 Processing sheet: {sheet_name}")
            
            # Try different header rows
            for header_row in [0, 1, 2, 3, 4, 5]:
                try:
                    print(f"   🔍 Trying header row {header_row}...")
                    try:
                        df = pd.read_excel(file_path, sheet_name=sheet_name, header=header_row, engine='openpyxl', nrows=1000)
                        print(f"   ✅ Successfully read {len(df)} rows")
                    except Exception as read_error:
                        print(f"   ❌ Error reading with header row {header_row}: {str(read_error)}")
                        continue
                    
                    # Find JOB NO and SALES PERSON columns
                    job_col = None
                    sales_col = None
                    
                    print(f"   🔍 Available columns: {list(df.columns)}")
                    for col in df.columns:
                        col_str = str(col).lower()
                        if 'job no' in col_str or 'job number' in col_str:
                            job_col = col
                            print(f"   ✅ Found job column: {col}")
                        elif 'sales person' in col_str:
                            sales_col = col
                            print(f"   ✅ Found sales column: {col}")
                    
                    if job_col and sales_col:
                        print(f"   ✅ Found columns: {job_col} and {sales_col}")
                        print(f"   📊 Processing {len(df)} rows...")
                        
                        # Process each row
                        for idx, row in df.iterrows():
                            job_number = str(row[job_col]).strip() if not pd.isna(row[job_col]) else ""
                            salesperson_text = str(row[sales_col]).strip() if not pd.isna(row[sales_col]) else ""
                            
                            # Skip if no job number
                            if not job_number or job_number.lower() in ['nan', 'none', 'null', '']:
                                skipped_entries += 1
                                continue
                            
                            # Skip if no salesperson
                            if not salesperson_text or salesperson_text.lower() in ['nan', 'none', 'null', '']:
                                skipped_entries += 1
                                continue
                            
                            # Split multiple salespersons
                            salespersons = split_multiple_salespersons(salesperson_text)
                            
                            if salespersons:
                                # Map to standard names
                                standard_salespersons = []
                                for sp in salespersons:
                                    standard_sp = map_salesperson_to_standard(sp)
                                    standard_salespersons.append(standard_sp)
                                    all_salespersons.add(standard_sp)
                                
                                # Store job to salespersons mapping
                                job_to_salespersons[job_number] = standard_salespersons
                                
                                # Store salesperson to jobs mapping
                                for sp in standard_salespersons:
                                    if sp not in salesperson_to_jobs:
                                        salesperson_to_jobs[sp] = []
                                    if job_number not in salesperson_to_jobs[sp]:
                                        salesperson_to_jobs[sp].append(job_number)
                                
                                processed_jobs += 1
                        
                        print(f"   ✅ Processed {processed_jobs} jobs from this sheet")
                        break  # Found data, no need to try other header rows
                        
                except Exception as e:
                    print(f"   ❌ Error with header row {header_row}: {str(e)}")
                    continue
    
    except Exception as e:
        return {"error": f"Error processing file: {str(e)}"}
    
    # Prepare results
    result = {
        "unique_sales_persons": sorted(list(all_salespersons)),
        "jobs_by_sales_person": {sp: sorted(jobs) for sp, jobs in salesperson_to_jobs.items()},
        "job_to_sales_person": job_to_salespersons,
        "summary": {
            "total_jobs_processed": processed_jobs,
            "total_salespersons": len(all_salespersons),
            "skipped_entries": skipped_entries,
            "sheets_processed": sheet_names
        },
        "metadata": {
            "processed_at": datetime.now().isoformat(),
            "source_file": file_path,
            "source_file_modified": os.path.getmtime(file_path)
        }
    }
    
    return result

def save_cache(data: Dict, cache_path: str = "updated_job_log_cache.json"):
    """Save the processed data to cache file."""
    try:
        with open(cache_path, 'w', encoding='utf-8') as f:
            json.dump(data, f, indent=2, ensure_ascii=False)
        print(f"✅ Cache saved to: {cache_path}")
        return True
    except Exception as e:
        print(f"❌ Error saving cache: {str(e)}")
        return False

def print_summary(data: Dict):
    """Print a summary of the processed data."""
    print("\n" + "=" * 80)
    print("PROCESSING SUMMARY")
    print("=" * 80)
    
    summary = data.get("summary", {})
    print(f"📊 Total jobs processed: {summary.get('total_jobs_processed', 0)}")
    print(f"👥 Total salespersons: {summary.get('total_salespersons', 0)}")
    print(f"⏭️  Skipped entries: {summary.get('skipped_entries', 0)}")
    
    print(f"\n📋 Salespersons found:")
    for sp in data.get("unique_sales_persons", []):
        job_count = len(data.get("jobs_by_sales_person", {}).get(sp, []))
        print(f"   • {sp}: {job_count} jobs")
    
    print(f"\n🔗 Sample job mappings:")
    job_to_sp = data.get("job_to_sales_person", {})
    for i, (job, salespersons) in enumerate(list(job_to_sp.items())[:10]):
        print(f"   • {job}: {', '.join(salespersons)}")
    
    if len(job_to_sp) > 10:
        print(f"   ... and {len(job_to_sp) - 10} more jobs")

def check_missing_salespersons(data: Dict):
    """Check which salespersons from the updated list are missing in the job log."""
    print("\n" + "=" * 80)
    print("MISSING SALESPERSONS ANALYSIS")
    print("=" * 80)
    
    # Updated standard salesperson list
    standard_names = {
        "ALI - ENGINEER",
        "HALEETHA", 
        "IJAZ AHMAD",
        "MS.IQRA JAMIL",
        "ISHAN - ENGINEER",
        "MR.MEHAR EJAZ",
        "M ZAHID",
        "RAHID - ENGINEER",
        "RAMEESHA",
        "SALIK ENGINEER",
        "SALMAA",
        "SHAHBAZ HASSAN",
        "YASAR",
        "ZEESHAN JAVED"
    }
    
    found_salespersons = set(data.get("unique_sales_persons", []))
    
    missing = standard_names - found_salespersons
    found = standard_names.intersection(found_salespersons)
    
    print(f"✅ Found in job log ({len(found)}):")
    for sp in sorted(found):
        job_count = len(data.get("jobs_by_sales_person", {}).get(sp, []))
        print(f"   • {sp}: {job_count} jobs")
    
    print(f"\n❌ Missing from job log ({len(missing)}):")
    for sp in sorted(missing):
        print(f"   • {sp}")

if __name__ == "__main__":
    file_path = r"C:\Users\HP\Videos\documents_processing-main\JOB_LOG\UPDATED JOB LOG (2).xlsx"
    cache_path = "updated_job_log_cache.json"
    
    # Process the job log file
    result = process_job_log_sheet(file_path)
    
    if "error" in result:
        print(f"❌ Error: {result['error']}")
    else:
        # Print summary
        print_summary(result)
        
        # Check missing salespersons
        check_missing_salespersons(result)
        
        # Save to cache
        save_cache(result, cache_path)
        
        print(f"\n{'='*80}")
        print("PROCESSING COMPLETE")
        print(f"{'='*80}")
