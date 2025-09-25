import React, { useState } from 'react';
import './FileDrop.css';
import { toast } from 'react-toastify';

import { Process } from '../../api/Process/Process';
import { ProcessAll } from '../../api/Process/ProcessAll';

const FileDrop = ({ allowMultiple = true, onSubmit }) => {
   const [files, setFiles] = useState([]);

   // Handle file drop
   const handleDrop = (event) => {
      event.preventDefault();
      const droppedFiles = Array.from(event.dataTransfer.files);
      if (allowMultiple) {
         setFiles((prevFiles) => [...prevFiles, ...droppedFiles]);
      } else {
         setFiles(droppedFiles.slice(0, 1)); // Take only the first file if single file upload
      }
   };

   // Handle drag over to prevent default browser behavior
   const handleDragOver = (event) => {
      event.preventDefault();
   };

   // Handle file selection via input
   const handleFileSelect = (event) => {
      const selectedFiles = Array.from(event.target.files);
      if (allowMultiple) {
         setFiles((prevFiles) => [...prevFiles, ...selectedFiles]);
      } else {
         setFiles(selectedFiles.slice(0, 1)); // Only keep the first file for single upload
      }
   };

   // Remove a specific file
   const handleRemoveFile = (index) => {
      setFiles((prevFiles) => prevFiles.filter((_, i) => i !== index));
   };

   const handleSubmit = (e) => {
      e.preventDefault();
      const formData = new FormData();
  
      if (files && files.length > 0) {
          formData.append('document', files[0])
      }
  
      if (allowMultiple) {
          files.forEach((file, index) => {
              formData.append(`files`, file);
          });

          ProcessAll(formData, sessionStorage.getItem('token')).then((response) => {
            if (response.status === 200) {
               response.data.forEach((fileResponse) => {
                  if (fileResponse.messageCode === 2000) {
                      toast.success(fileResponse.message + " ID: " + fileResponse.documentId);
                  } else {
                      toast.error("Code:" + fileResponse.messageCode + " " + fileResponse.message);
                  }
              });
            } else{
               toast.error("An error occurred. Please check the console.");
            }
        }).catch((error) => {
            console.log(error);
            toast.error("An error occurred. Please check the console.");
        });

      } else {
          Process(formData, sessionStorage.getItem('token')).then((response) => {
              if (response.status === 200) {
                  if (response.data.messageCode === 2000) {
                      toast.success(response.data.message + " ID: " + response.data.documentId);
                  } else {
                      toast.error("Code:" + response.data.messageCode + " " + response.data.message);
                  }
              }else{
               toast.error("An error occurred. Please check the console.");
            }
          }).catch((error) => {
              console.log(error);
              toast.error("An error occurred. Please check the console.");
          });
      }
  };
  
  
   return (
    <div>
      <div
         className="file-drop-area"
         onDrop={handleDrop}
         onDragOver={handleDragOver}
      >
         <p>Drag & Drop files here, or click to select files</p>
         <input
            type="file"
            multiple={allowMultiple}
            onChange={handleFileSelect}
            style={{ display: 'none' }}
            id="fileInput"
         />
         <label htmlFor="fileInput" className="file-input-label">
            Browse Files
         </label>

         {files.length > 0 && (
            <div className="file-list">
               <h4 style={{textAlign:'left'}}>Selected Files:</h4>
               <ul>
                  {files.map((file, index) => (
                     <li key={index} className="file-item">
                        <span className="file-name">{file.name}</span>
                        <button onClick={() => handleRemoveFile(index)} className="remove-button">
                           Remove
                        </button>
                     </li>
                  ))}
               </ul>
            </div>
         )}
      </div>
         
      <div className="submit-container">
        <button onClick={handleSubmit} className="submit-button">
         Process
        </button>
        </div>
      </div>
   );
};

export default FileDrop;
