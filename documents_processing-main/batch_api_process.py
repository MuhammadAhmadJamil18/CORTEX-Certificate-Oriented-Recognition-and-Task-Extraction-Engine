import requests
import os
import json
import time
from tqdm import tqdm
from datetime import datetime
import pandas as pd
import argparse

# --- Authentication ---
LOGIN_URL = "http://217.154.53.135:8080/api/users/login"
CREDENTIALS = {
    "userEmail": "cortex@email.com",
    "userPassword": "12345"
}

def login():
    try:
        login_response = requests.post(LOGIN_URL, json=CREDENTIALS)
        login_response.raise_for_status()
        token = login_response.json().get("token")
        if not token:
            print("Login successful, but token not found in response.")
            exit()
        print("Login successful. Token obtained.")
        return token
    except requests.exceptions.RequestException as e:
        print("Login failed:", e)
        exit()

# --- Config ---
ROOT_DIR = r"C:\Users\HP\Downloads\Re-processing certificate files" #we have to change it to the main directory
IGNORE_FOLDER = "ENAS DATA 2024"
API_URL = "http://217.154.53.135:9090/api/processor/process"
ALLOWED_EXTENSIONS = (".docx", ".xlsx")
FIELDS = [
    "client_name",
    "next_date_of_inspection",
    "job_number",
    "certificate_number",
    "date_of_inspection"
]
LOG_FILE = "docs_process_log.txt"
PROCESSED_XLSX = "processed_files.xlsx"
FAILED_XLSX = "failed_files.xlsx"
TOKEN_REFRESH_INTERVAL = 1000  # Refresh token every 1000 files

# === Utilities ===
def should_ignore(path):
    return IGNORE_FOLDER.lower() in path.lower().split(os.sep)

def log_response(file_path, status, text):
    with open(LOG_FILE, "a", encoding="utf-8") as log:
        timestamp = datetime.now().strftime("%Y-%m-%d %H:%M:%S")
        log.write(f"[{timestamp}] File: {file_path}\nStatus: {status}\nResponse: {text}\n\n")

def append_to_processed(row_dict):
    df = pd.DataFrame([row_dict])
    if not os.path.exists(PROCESSED_XLSX):
        df.to_excel(PROCESSED_XLSX, index=False)
    else:
        with pd.ExcelWriter(PROCESSED_XLSX, mode="a", if_sheet_exists="overlay", engine="openpyxl") as writer:
            existing_df = pd.read_excel(PROCESSED_XLSX)
            df.to_excel(writer, index=False, header=False, startrow=len(existing_df))

def append_to_failed(row_dict):
    df = pd.DataFrame([row_dict])
    if not os.path.exists(FAILED_XLSX):
        df.to_excel(FAILED_XLSX, index=False)
    else:
        with pd.ExcelWriter(FAILED_XLSX, mode="a", if_sheet_exists="overlay", engine="openpyxl") as writer:
            existing_df = pd.read_excel(FAILED_XLSX)
            df.to_excel(writer, index=False, header=False, startrow=len(existing_df))

def get_failed_files():
    if os.path.exists(FAILED_XLSX):
        try:
            df = pd.read_excel(FAILED_XLSX)
            return df.to_dict('records')
        except Exception:
            return []
    return []

def display_failed_files():
    failed_files = get_failed_files()
    if failed_files:
        print(f"\n❌ Failed Files ({len(failed_files)}):")
        print("=" * 50)
        for i, row in enumerate(failed_files, 1):
            print(f"{i:3d}. {row.get('File', 'N/A')} | Error: {row.get('Error', 'N/A')}")
        print("=" * 50)
        print(f"Failed files are also saved in: {FAILED_XLSX}")
    else:
        print("\n✅ No failed files found.")

# === File Processor ===
def process_file(file_path, token):
    retries = 3
    last_error = None
    for attempt in range(1, retries + 1):
        try:
            with open(file_path, "rb") as f:
                files = {
                    "document": (os.path.basename(file_path), f, "application/vnd.openxmlformats-officedocument.wordprocessingml.document")
                }
                data = {
                    "fields": json.dumps(FIELDS),
                    "documentType": "file"
                }
                headers = {"token": token}
                response = requests.post(API_URL, headers=headers, files=files, data=data)
            # Token expired, refresh and retry
            if response.status_code in (401, 403):
                print(f"Token expired or unauthorized. Refreshing token and retrying {file_path}...")
                token = login()
                continue
            log_response(file_path, response.status_code, response.text)
            # Try to parse and save summary to processed Excel
            try:
                resp_json = response.json()
                row = {
                    "File": file_path,
                    "Message": resp_json.get("message", "N/A"),
                    "Message Code": resp_json.get("messageCode", "N/A"),
                    "Document Name": resp_json.get("documentName", "N/A")
                }
                append_to_processed(row)
            except Exception as e:
                append_to_failed({"File": file_path, "Error": f"EXCEL_ERROR: {str(e)}"})
            break  # success
        except Exception as e:
            last_error = str(e)
            log_response(file_path, "ERROR", last_error)
            print(f"[Attempt {attempt}] Error processing {file_path}: {last_error}")
            time.sleep(2 ** attempt)
            if attempt == retries:
                append_to_failed({"File": file_path, "Error": last_error})
    return token

# === Main Runner ===
def main(limit=None):
    all_files = []
    for root, dirs, files in os.walk(ROOT_DIR):
        dirs[:] = [d for d in dirs if IGNORE_FOLDER.lower() not in d.lower()]
        for file in files:
            if file.lower().endswith(ALLOWED_EXTENSIONS):
                file_path = os.path.join(root, file)
                if not should_ignore(file_path):
                    all_files.append(file_path)

    # Apply limit if specified
    if limit:
        all_files = all_files[:limit]
        print(f"🔬 TEST MODE: Processing only first {limit} files")

    token = login()
    milestone = 1000
    for idx, file_path in enumerate(tqdm(all_files, desc="Processing files"), 1):
        token = process_file(file_path, token)
        
        # Proactively refresh token every TOKEN_REFRESH_INTERVAL files
        if idx % TOKEN_REFRESH_INTERVAL == 0:
            print(f" Proactively refreshing token after {idx} files...")
            token = login()
            
        if idx % milestone == 0:
            print(f"📌 Milestone reached: {idx} files processed.")

    # Summary
    print(f"\n Done. Total files processed: {len(all_files)}")
    display_failed_files()

if __name__ == "__main__":
    parser = argparse.ArgumentParser(description="Batch process documents via API")
    parser.add_argument("--limit", type=int, help="Limit processing to first N files (for testing)")
    args = parser.parse_args()
    main(limit=args.limit) 