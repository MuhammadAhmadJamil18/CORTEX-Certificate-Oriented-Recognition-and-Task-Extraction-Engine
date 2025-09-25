import os
import json
import time
import csv
import requests
from datetime import datetime
from tqdm import tqdm

# --- CONFIGURATION ---
MEGA_SYNC_DIR = r"C:\Users\HP\Music\DownloadsMega"
IGNORE_FOLDERS = {"ENAS DATA 2024", "DOCUMENTPROCESSOR"}  # Case-insensitive
ALLOWED_EXTENSIONS = {".docx", ".xlsx", ".csv"}
PROCESSED_JSON = "processed_files.json"
LOG_CSV = "processing_log.csv"
FAILED_CSV = "failed_files.csv"
BATCH_API_URL = "http://217.154.53.135:9090/api/processor/process"
LOGIN_URL = "http://217.154.53.135:9090/api/users/login"
CREDENTIALS = {
    "userEmail": "cortex@email.com",
    "userPassword": "12345"
}
RETRY_LIMIT = 3
RETRY_BACKOFF = 2  # Exponential backoff base (seconds)

# --- UTILITY FUNCTIONS ---
def load_processed():
    if os.path.exists(PROCESSED_JSON):
        with open(PROCESSED_JSON, "r", encoding="utf-8") as f:
            return set(json.load(f))
    return set()

def save_processed(processed):
    with open(PROCESSED_JSON, "w", encoding="utf-8") as f:
        json.dump(sorted(list(processed)), f, indent=2)

def log_action(file_path, status, message, log_file=LOG_CSV):
    is_new = not os.path.exists(log_file)
    with open(log_file, "a", newline='', encoding="utf-8") as f:
        writer = csv.writer(f)
        if is_new:
            writer.writerow(["Timestamp", "File", "Status", "Message"])
        writer.writerow([
            datetime.now().strftime("%Y-%m-%d %H:%M:%S"),
            file_path,
            status,
            message
        ])

def log_failed(file_path, error):
    is_new = not os.path.exists(FAILED_CSV)
    with open(FAILED_CSV, "a", newline='', encoding="utf-8") as f:
        writer = csv.writer(f)
        if is_new:
            writer.writerow(["Timestamp", "File", "Error"])
        writer.writerow([
            datetime.now().strftime("%Y-%m-%d %H:%M:%S"),
            file_path,
            error
        ])

def should_ignore(path):
    # Ignore if any part of the path matches an ignored folder (case-insensitive)
    parts = [p.lower() for p in path.split(os.sep)]
    return any(folder.lower() in parts for folder in IGNORE_FOLDERS)

def get_token():
    try:
        resp = requests.post(LOGIN_URL, json=CREDENTIALS)
        resp.raise_for_status()
        token = resp.json().get("token")
        if not token:
            raise Exception("No token in login response")
        return token
    except Exception as e:
        raise Exception(f"Login failed: {e}")

def process_file(file_path, token):
    for attempt in range(1, RETRY_LIMIT + 1):
        try:
            with open(file_path, "rb") as f:
                files = {
                    "document": (os.path.basename(file_path), f)
                }
                data = {
                    "fields": json.dumps([]),  # Adjust if needed
                    "documentType": "file"
                }
                headers = {"token": token}
                response = requests.post(BATCH_API_URL, headers=headers, files=files, data=data)
            if response.status_code in (401, 403):
                token = get_token()
                continue
            response.raise_for_status()
            return True, response.text, token
        except Exception as e:
            if attempt == RETRY_LIMIT:
                return False, str(e), token
            time.sleep(RETRY_BACKOFF ** attempt)
    return False, "Max retries exceeded", token

def scan_files():
    files_to_process = []
    for root, dirs, files in os.walk(MEGA_SYNC_DIR):
        # Exclude ignored folders
        dirs[:] = [d for d in dirs if d.upper() not in IGNORE_FOLDERS]
        for file in files:
            ext = os.path.splitext(file)[1].lower()
            if ext in ALLOWED_EXTENSIONS:
                file_path = os.path.join(root, file)
                if not should_ignore(file_path):
                    files_to_process.append(file_path)
    return files_to_process

def main():
    processed = load_processed()
    files = scan_files()
    new_files = [f for f in files if f not in processed]
    if not new_files:
        print("No new files to process.")
        return
    print(f"Found {len(new_files)} new files to process.")
    token = get_token()
    for file_path in tqdm(new_files, desc="Processing files"):
        success, message, token = process_file(file_path, token)
        if success:
            log_action(file_path, "SUCCESS", message)
            processed.add(file_path)
            save_processed(processed)
        else:
            log_action(file_path, "FAILED", message)
            log_failed(file_path, message)
    print("Processing complete. See logs for details.")

if __name__ == "__main__":
    main() 