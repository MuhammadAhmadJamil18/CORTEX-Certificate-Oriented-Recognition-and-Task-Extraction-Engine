import React, { useState } from "react";
import "./Process.css";

import Tabs from "../Tabs/Tabs";
import FileDrop from "../FileDrop/FileDrop";

import { toast } from 'react-toastify';

import { ProcessLink } from "../../api/Process/ProcessLink";

const Process = () => {
    const [link, setLink] = useState('');
    const [data,setData] = useState({}); // State to hold the data
    const fields=[
        "client_name",
        "job_number",
        "certificate_number",
        "date_of_inspection",
        "next_date_of_inspection",
        "date_of_inspection"
      ]
    const handleLinkChange = (event) => {
        setLink(event.target.value); // Update the link state
     };

    //  const handleSubmit = (e) => {
    //     e.preventDefault();

    //     ProcessLink(link, sessionStorage.getItem('token')).then((response) => {
    //         if(response.status === 200){
    //            if(response.data.messageCode === 2000){
    //               toast.success(response.data.message+" ID: "+response.data.documentId);
    //            }else{
    //               toast.error("Code:"+response.data.messageCode+" "+response.data.message);
    //            }
    //         }
    //      }).catch((error) => {
    //         console.log(error);
    //         toast.error("An error occured. Please check console.");
    //      });
    //  }

    const handleSubmit = async (e) => {
        e.preventDefault();
        try {
          const token = sessionStorage.getItem('token');
          const response = await ProcessLink(link,fields, token);
          if (response.status === 200) {
            toast.success(`Fields extracted successfully! `);
            // setData(response.data); 
          } else {
            toast.error('Unexpected status: ' + response.status);
          }
        } catch (error) {
          console.error(error);
          toast.error('An error occurred. Check console for details.');
        }
      };

    const tabsData = [
        { name: 'Process', content: <div><FileDrop allowMultiple={false} /></div> },
        { name: 'Process All', content: <div><FileDrop allowMultiple={true} /></div> },
        { name: 'Process Link', content: <div>
            <label htmlFor="linkInput">Enter Link to Shared Drive:</label>
                <input
                    type="text"
                    id="linkInput"
                    value={link}
                    onChange={handleLinkChange}
                    placeholder="https://example.com"
                />
                <div className="submit-container" >
                    <button type="submit" className="submit-button" onClick={handleSubmit}>
                        Process
                    </button>
                </div>
            </div>
         }
     ];

    return (
        <div className="body-wrapper">
            <div className="container-fluid">
                <div className="row">
                    <div className="col-lg-8-process d-flex align-items-stretch">
                        <div className="card w-100">
                            <div className="card-body">
                                <div>
                                {/* className="d-sm-flex d-block align-items-center justify-content-between mb-9" */}
                                    <div className="mb-3 mb-sm-0">               
                                        <Tabs tabs={tabsData} selectedTab={0}/>
                                    </div>
                                    {data && Object.keys(data).length > 0 && (
                    <div className="data-display">
                        {Object.entries(data).map(([key, value]) => (
                            <p key={key}>
                                <strong>{key.replace(/_/g, ' ').replace(/\b\w/g, char => char.toUpperCase())}:</strong> {value}
                            </p>
                        ))}
                    </div>
                )}
                                </div>
                            </div>
                        </div>
                    </div>
                    
                    </div>
            </div>
        </div>
    );
};

export default Process;
