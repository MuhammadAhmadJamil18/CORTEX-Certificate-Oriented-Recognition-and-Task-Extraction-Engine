import sys
import os
from datetime import datetime, timedelta
import regex as re
from dateutil.relativedelta import relativedelta
from typing import Dict, List, Union, Tuple

sys.path.append(os.path.dirname(os.path.dirname(os.path.dirname(os.path.abspath(__file__)))))

from extraction.rules.load_extraction_rules import RULES
from extraction.rules.rule_engine import parse_text
from utils import get_unique_candidates, get_results
from extraction.rules.predict_date_of_inspection import predict_date_of_inspection
from extraction.rules.predict_course_validity_duration import predict_course_validity_duration

import docx2txt

def replace_new_line_with_dot(text: str) -> str:
    return re.sub(r"\n+", ".", text, flags=re.M)

def format_date(result: list) -> list:
    for candidate in result:
        candidate["text"] = replace_new_line_with_dot(candidate["text"])
    return result

def parse_validity_duration(duration_text: str) -> Tuple[int, str]:
    """
    Parse validity duration text to extract number and unit
    Returns tuple of (number, unit)
    """
    # Convert text numbers to digits
    number_mapping = {
        'ONE': '1', 'TWO': '2', 'THREE': '3', 'FOUR': '4', 'FIVE': '5',
        'SIX': '6', 'SEVEN': '7', 'EIGHT': '8', 'NINE': '9', 'TEN': '10',
        'ELEVEN': '11', 'TWELVE': '12',
    }
    
    # Clean and standardize the input
    duration_text = duration_text.upper().strip()
    
    # Replace text numbers with digits
    for text_num, digit in number_mapping.items():
        duration_text = duration_text.replace(text_num, digit)
    
    # Extract number and unit using regex
    # The code will handle various duration formats like:
        # "ONE YEAR"
        # "1 YEAR"
        # "TWO MONTHS"
        # "2 MONTHS"
        # "SIX (6) MONTHS"
    pattern = r'(\d+)\s*(?:\([^)]*\))?\s*(YEAR|MONTH|WEEK|DAY)S?'
    match = re.search(pattern, duration_text)
    
    if not match:
        raise ValueError(f"Could not parse duration: {duration_text}")
    
    number = int(match.group(1))
    unit = match.group(2).lower()
    
    return number, unit

def calculate_next_date(start_date_obj: datetime, validity_text: str) -> str:
    """
    Calculate the next inspection date based on start date and validity duration
    Returns date in format DD.MM.YYYY
    """
    # Parse duration
    number, unit = parse_validity_duration(validity_text)
    
    # Calculate end date
    if unit == 'year':
        end_date = start_date_obj + relativedelta(years=number)
    elif unit == 'month':
        end_date = start_date_obj + relativedelta(months=number)
    elif unit == 'week':
        end_date = start_date_obj + timedelta(weeks=number)
    elif unit == 'day':
        end_date = start_date_obj + timedelta(days=number)
    else:
        raise ValueError(f"Invalid duration unit: {unit}")
    
    # Format result
    return end_date.strftime('%d.%m.%Y')

def extract_date_from_text(text: str) -> List[Dict]:
    """
    Extract dates from text using various patterns
    Returns list of dictionaries with date info and confidence
    """
    results = []
    
    # Pattern 1: DD.MM.YYYY format
    pattern1 = r'(?P<date>\d{2}\.\d{2}\.\d{4})'
    matches = re.finditer(pattern1, text)
    for match in matches:
        results.append({
            'text': match.group('date'),
            'confidence': 90,
            'location': match.start(),
            'version': '01'
        })
    
    # Pattern 2: Date after "Next Inspection" or similar phrases
    pattern2 = r'(?:Next Inspection|Due Date|Valid Until|Expiry Date)[:.\s]+(?P<date>\d{2}\.\d{2}\.\d{4})'
    matches = re.finditer(pattern2, text, re.IGNORECASE)
    for match in matches:
        results.append({
            'text': match.group('date'),
            'confidence': 95,
            'location': match.start(),
            'version': '02'
        })
    
    # Pattern 3: Date in table format (after "Result:")
    pattern3 = r'Result:[\s\n]+(?:\d{2}\.\d{2}\.\d{4})[\s\n]+(?P<date>\d{2}\.\d{2}\.\d{4})'
    matches = re.finditer(pattern3, text)
    for match in matches:
        results.append({
            'text': match.group('date'),
            'confidence': 85,
            'location': match.start(),
            'version': '03'
        })
    
    return results

def to_iso_date(date_str: str) -> str:
    """Convert mm.dd.yyyy, dd.mm.yyyy, dd/mm/yyyy, dd-mm-yyyy, 'January 02, 2024', 'Jan 02, 2024' to ISO yyyy-mm-dd format. Return original if not matched."""
    from utils import to_iso_date_with_typo_correction
    return to_iso_date_with_typo_correction(date_str)

def predict_next_date_of_inspection(document_text: str, field_name: str, document_type: str = None) -> dict:
    """
    Predict next date of inspection using a multi-step strategy.
    """
    # Strategy 1: Calculate from start date and validity period
    try:
        start_date_res = predict_date_of_inspection(document_text, "date_of_inspection")
        validity_res = predict_course_validity_duration(document_text, "course_validity_duration")
        
        start_date_text = start_date_res["date_of_inspection"][0]["text"]
        validity_text = validity_res["course_validity_duration"][0]["text"]
        
        # Normalize start_date
        dt_start = None
        for fmt in ('%B %d, %Y', '%d.%m.%Y', '%d/%m/%Y', '%d-%m-%Y'):
            try:
                dt_start = datetime.strptime(start_date_text, fmt)
                break
            except ValueError:
                continue
        
        if dt_start:
            next_date = calculate_next_date(dt_start, validity_text)
            # Convert to ISO format
            next_date_iso = to_iso_date(next_date)
            return {field_name: [{'text': next_date_iso, 'confidence': 100, 'location': 0, 'version': 'calculated-v1'}]}
    except (KeyError, IndexError, ValueError):
        pass

    # Strategy 2: Rule-based extraction
    content = parse_text(document_text, RULES.get(field_name, {}))
    results = get_results(content, field_name)
    if results:
        # Convert all found dates to ISO format
        for candidate in results:
            if isinstance(candidate, dict) and 'text' in candidate:
                candidate['text'] = to_iso_date(candidate['text'])
            elif isinstance(candidate, str):
                candidate = to_iso_date(candidate)
        return {field_name: get_unique_candidates(results)}

    # Strategy 3: Heuristic fallback (find last date in doc)
    try:
        # A more generic date pattern
        date_pattern = r'(\d{1,2}[./-]\d{1,2}[./-]\d{2,4})|((?:Jan|Feb|Mar|Apr|May|Jun|Jul|Aug|Sep|Oct|Nov|Dec)[a-z]*\s+\d{1,2},?\s+\d{4})'
        all_dates = re.finditer(date_pattern, document_text, re.IGNORECASE)
        
        formatted_dates = []
        for match in all_dates:
            date_str = match.group(0)
            # Attempt to parse various date formats
            dt = None
            for fmt in ('%d.%m.%Y', '%d/%m/%Y', '%d-%m-%Y', '%B %d, %Y', '%b %d, %Y'):
                try:
                    dt = datetime.strptime(date_str, fmt)
                    break
                except ValueError:
                    continue
            
            if dt:
                iso_date = dt.strftime('%Y-%m-%d')
                formatted_dates.append({
                    'text': iso_date,
                    'confidence': 75, # Lower confidence as it's a guess
                    'location': match.start(),
                    'version': 'heuristic-v1'
                })

        # Heuristic: Assume the last found date is the next inspection date
        if formatted_dates:
            # Sort by location in document
            sorted_dates = sorted(formatted_dates, key=lambda x: x['location'])
            return {field_name: [sorted_dates[-1]]} # Return the last date

    except Exception:
        pass # Fallback to empty

    return {field_name: []}

# filepath='./test.docx'
# text = docx2txt.process(filepath)
# print(text)


# print(predict_next_date_of_inspection(text, 'date_of_inspection'))