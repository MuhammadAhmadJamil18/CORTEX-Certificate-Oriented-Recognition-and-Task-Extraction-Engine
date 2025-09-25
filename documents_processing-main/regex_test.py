import os
import json
import pandas as pd
from datetime import datetime
from typing import Dict, List, Tuple
import logging
import docx2txt
from extraction.rules.load_extraction_rules import RULES
from extraction.rules.rule_engine import parse_text
from utils import get_unique_candidates, get_results
from openpyxl.styles import PatternFill, Font, Alignment, Border, Side
from openpyxl.utils import get_column_letter

# Configure logging
logging.basicConfig(
    level=logging.INFO,
    format='%(asctime)s - %(levelname)s - %(message)s',
    handlers=[
        logging.FileHandler('regex_accuracy_test.log'),
        logging.StreamHandler()
    ]
)

class RegexAccuracyTester:
    def __init__(self, test_files_dir: str):
        self.test_files_dir = test_files_dir
        self.results = []
        
    def process_file(self, file_path: str) -> Dict:
        """Process a single file and return extracted data"""
        try:
            # Extract text from document
            document_text = docx2txt.process(file_path)
            
            # Initialize results dictionary
            extracted_data = {}
            
            # Process each field using regex rules
            for field_name in RULES.keys():
                content = parse_text(document_text, RULES[field_name])
                results = get_results(content, field_name)
                results = get_unique_candidates(results, confidence_boost=5)
                
                # Store the best match for each field
                if results:
                    extracted_data[field_name] = results[0]['text']
                else:
                    extracted_data[field_name] = ""
            
            return {
                'file_path': file_path,
                'extracted_data': extracted_data
            }
            
        except Exception as e:
            logging.error(f"Error processing file {file_path}: {str(e)}")
            return None

    def compare_results(self, old_result: Dict, new_result: Dict) -> Dict:
        """Compare old and new results and return accuracy metrics"""
        comparison = {
            'file_name': os.path.basename(old_result.get('file_path', '')),
            'fields_compared': 0,
            'matching_fields': 0,
            'accuracy': 0.0,
            'improved_fields': [],
            'degraded_fields': [],
            'unchanged_fields': [],
            'field_details': {}  # Store detailed comparison for each field
        }
        
        # Compare each field
        for field in old_result.get('extracted_data', {}):
            old_value = old_result['extracted_data'][field]
            new_value = new_result.get('extracted_data', {}).get(field)
            
            comparison['fields_compared'] += 1
            
            # Store field details
            field_detail = {
                'old_value': old_value,
                'new_value': new_value,
                'status': ''
            }
            
            if old_value == new_value:
                comparison['matching_fields'] += 1
                comparison['unchanged_fields'].append(field)
                field_detail['status'] = 'Unchanged'
            elif new_value and not old_value:
                comparison['improved_fields'].append(field)
                field_detail['status'] = 'Improved'
            elif old_value and not new_value:
                comparison['degraded_fields'].append(field)
                field_detail['status'] = 'Degraded'
            else:
                field_detail['status'] = 'Different'
            
            comparison['field_details'][field] = field_detail
        
        # Calculate accuracy
        if comparison['fields_compared'] > 0:
            comparison['accuracy'] = (comparison['matching_fields'] / comparison['fields_compared']) * 100
            
        return comparison

    def run_tests(self) -> List[Dict]:
        """Run tests on all files in the test directory"""
        for filename in os.listdir(self.test_files_dir):
            if filename.endswith(('.docx', '.doc', '.pdf')):
                file_path = os.path.join(self.test_files_dir, filename)
                logging.info(f"Processing file: {filename}")
                
                # Process with old rules
                old_result = self.process_file(file_path)
                
                # Process with new rules
                new_result = self.process_file(file_path)
                
                if old_result and new_result:
                    comparison = self.compare_results(old_result, new_result)
                    self.results.append(comparison)
                    
        return self.results

    def generate_report(self, output_path: str = None):
        """Generate Excel report with test results"""
        if not output_path:
            timestamp = datetime.now().strftime('%Y%m%d_%H%M%S')
            output_path = f'regex_accuracy_report_{timestamp}.xlsx'
            
        # Create Excel writer
        with pd.ExcelWriter(output_path) as writer:
            # 1. Summary Sheet
            summary_data = {
                'Metric': [
                    'Total Files Tested',
                    'Average Accuracy',
                    'Total Fields Compared',
                    'Total Matching Fields',
                    'Total Improved Fields',
                    'Total Degraded Fields',
                    'Total Unchanged Fields'
                ],
                'Value': [
                    len(self.results),
                    f"{sum(r['accuracy'] for r in self.results) / len(self.results):.2f}%" if self.results else "0%",
                    sum(r['fields_compared'] for r in self.results),
                    sum(r['matching_fields'] for r in self.results),
                    sum(len(r['improved_fields']) for r in self.results),
                    sum(len(r['degraded_fields']) for r in self.results),
                    sum(len(r['unchanged_fields']) for r in self.results)
                ]
            }
            summary_df = pd.DataFrame(summary_data)
            summary_df.to_excel(writer, sheet_name='Summary', index=False)
            
            # 2. File-wise Results Sheet
            file_results = []
            for result in self.results:
                file_result = {
                    'File Name': result['file_name'],
                    'Accuracy': f"{result['accuracy']:.2f}%",
                    'Fields Compared': result['fields_compared'],
                    'Matching Fields': result['matching_fields'],
                    'Improved Fields': len(result['improved_fields']),
                    'Degraded Fields': len(result['degraded_fields']),
                    'Unchanged Fields': len(result['unchanged_fields'])
                }
                file_results.append(file_result)
            
            file_df = pd.DataFrame(file_results)
            file_df.to_excel(writer, sheet_name='File Results', index=False)
            
            # 3. Detailed Field Comparison Sheet
            field_comparisons = []
            for result in self.results:
                for field, details in result['field_details'].items():
                    field_comparison = {
                        'File Name': result['file_name'],
                        'Field Name': field,
                        'Old Value': details['old_value'],
                        'New Value': details['new_value'],
                        'Status': details['status']
                    }
                    field_comparisons.append(field_comparison)
            
            field_df = pd.DataFrame(field_comparisons)
            field_df.to_excel(writer, sheet_name='Field Details', index=False)
            
        logging.info(f"Report generated: {output_path}")
        return output_path

def main():
    # Get test files directory from user
    test_files_dir = input("Enter the path to your test files directory: ")
    
    if not os.path.exists(test_files_dir):
        print(f"Error: Directory {test_files_dir} does not exist")
        return
        
    # Initialize tester
    tester = RegexAccuracyTester(test_files_dir)
    
    # Run tests
    print("Running tests...")
    tester.run_tests()
    
    # Generate report
    print("Generating report...")
    report_path = tester.generate_report()
    
    print(f"\nTesting complete! Report generated at: {report_path}")
    print("Please check the log file 'regex_accuracy_test.log' for detailed information.")

if __name__ == "__main__":
    main() 