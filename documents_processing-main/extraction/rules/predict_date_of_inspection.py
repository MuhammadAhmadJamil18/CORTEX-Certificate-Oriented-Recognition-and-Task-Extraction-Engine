import sys
import os
from datetime import datetime
import regex as re
from typing import Dict, List

from extraction.rules.load_extraction_rules import RULES
from extraction.rules.rule_engine import parse_text
from utils import get_unique_candidates, get_results

def to_iso_date(date_str: str) -> str:
    """Convert mm.dd.yyyy, dd.mm.yyyy, dd/mm/yyyy, dd-mm-yyyy, 'January 02, 2024', 'Jan 02, 2024' to ISO yyyy-mm-dd format. Return original if not matched."""
    from utils import to_iso_date_with_typo_correction
    return to_iso_date_with_typo_correction(date_str)

def predict_date_of_inspection(document_text: str, field_name: str, document_type: str = None) -> Dict:
    """
    Parse date of inspection from document text, with special handling for certificates.
    
    Args:
        document_text (str): The text content of the document
        field_name (str): Name of the field to extract
        document_type (str, optional): Type of document (e.g. "certificate")
        
    Returns:
        Dict: Dictionary containing the extracted date information, or an empty string in case of error.
    """
    # 1. Try rule-based extraction first
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

    # 2. If no results, find all dates and use heuristics
    try:
        # A more generic date pattern
        date_pattern = r'(\d{1,2}[./-]\d{1,2}[./-]\d{2,4})|((?:Jan|Feb|Mar|Apr|May|Jun|Jul|Aug|Sep|Oct|Nov|Dec)[a-z]*\s+\d{1,2},?\s+\d{4})'
        all_dates = re.finditer(date_pattern, document_text, re.IGNORECASE)
        
        formatted_dates = []
        for match in all_dates:
            date_str = match.group(0)
            try:
                # Normalize to DD.MM.YYYY
                dt = datetime.strptime(date_str, '%d.%m.%Y')
            except ValueError:
                try:
                    dt = datetime.strptime(date_str, '%d/%m/%Y')
                except ValueError:
                    try:
                        dt = datetime.strptime(date_str, '%d-%m-%Y')
                    except ValueError:
                        try:
                            dt = datetime.strptime(date_str, '%B %d, %Y')
                        except ValueError:
                            continue # Skip formats we can't parse
            # Convert to ISO format
            iso_date = dt.strftime('%Y-%m-%d')
            formatted_dates.append({
                'text': iso_date,
                'confidence': 75, # Lower confidence as it's a guess
                'location': match.start(),
                'version': 'heuristic-v1'
            })

        # Heuristic: Assume the first found date is the inspection date
        if formatted_dates:
            # Sort by location in document
            sorted_dates = sorted(formatted_dates, key=lambda x: x['location'])
            return {field_name: [sorted_dates[0]]} # Return the earliest date

    except Exception:
        pass # Fallback to empty

    return {field_name: []}