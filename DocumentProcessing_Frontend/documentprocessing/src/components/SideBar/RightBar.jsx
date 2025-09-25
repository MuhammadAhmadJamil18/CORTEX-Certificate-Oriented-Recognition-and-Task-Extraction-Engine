import React, { useEffect, useState } from 'react';
import './RightBar.css';
import { NotificationType } from '../../enums/NotificationType';
import { NotificationFrequency } from '../../enums/NotificationFrequency';
import { NotificationStatus } from '../../enums/NotificationStatus';

const RightBar = ({ isOpen, data, onClose, type }) => {

   const [sideBarContent, setSideBarContent] = useState([]);
   const [sideBarTitle, setSideBarTitle] = useState([]);
   useEffect(() => {
      if (type === "report") {
         setSideBarTitle('Document Report');
         if (data && data.searchResultList) {
            const searchResults = data.searchResultList.map(result => ({
               searchKey: result.searchKey,
               searchResult: result.searchResult
            }));

            setSideBarContent(searchResults);
         }
      } else if (type === "notification") {
         setSideBarTitle('Notification');
         if (data) {
            console.log(data);
         }
      }
   }, [data]);

   return (
      <div id="sidebar" className={isOpen ? 'open' : ''}>
         <div className="sidebar-header">
            <h4>{sideBarTitle}</h4>
            <button className="close-btn" onClick={onClose}>
               ✖
            </button>
         </div>
         {type === "report" && (
            <div className="sidebar-content">
               <div className="form-group">
                  <label htmlFor="documentName"><b>Document Name:</b> {data.documentName}</label>
                  <label htmlFor="documentId"><b>Document ID: </b>{data.documentId}</label>
                  <label htmlFor='processingDate'><b>Processing Date: </b>{new Date(data.parseDate).toLocaleString()}</label>
                  <label htmlFor='processBy'><b>Processed By:</b> {data.userName}</label>
                  <label htmlFor='processBy'><b>Sales Person:</b> {data.salesPersonName}</label>
               </div>

               <table className="search-table">
                  <thead>
                     <tr>
                        <th>Key</th>
                        <th>Value</th>
                     </tr>
                  </thead>
                  <tbody>
                     {sideBarContent.map((item, index) => (
                        <tr key={index}>
                           <td><b>{item.searchKey}</b></td>
                           <td>{item.searchResult}</td>
                        </tr>
                     ))}
                  </tbody>
               </table>

               <div className="button-container">
                  <button className="button" onClick={onClose}>Close</button>
                  {/* <button className="button">Notify</button> */}
               </div>
            </div>
         )}

         {type === "notification" && (
            <div className="sidebar-content">
               <div className="form-group">
                  <label htmlFor='notificationSubject'><b>Subject:</b> {data.notificationSubject}</label>
                  <label htmlFor='notificationMessage'><b>Message:</b> {data.notificationMessage}</label>
                  <label htmlFor='notificationId'><b>ID:</b> {data.notificationId}</label>
                  <label htmlFor='notifier'><b>Notifier:</b> {data.notifiedBy}</label>
                  <label htmlFor='scheduleTime'><b>Date & Time:</b> {new Date(data.scheduleTime).toLocaleString()}</label>
                  <label htmlFor='type'><b>Type:</b> {
                     (() => {
                        switch (data.notificationType) {
                           case NotificationType.BOTH:
                              return 'WEB/EMAIL';
                           case NotificationType.EMAIL:
                              return 'EMAIL';
                           case NotificationType.WEB:
                              return 'WEB';
                           default:
                              return 'UNKNOWN'; // Fallback for any unexpected values
                        }
                     })()
                  }</label>
                  <label htmlFor='frequency'><b>Frequency:</b> {
                     (() => {
                        switch (data.notificationType) {
                           case NotificationFrequency.DAILY:
                              return 'DAILY';
                           case NotificationFrequency.WEEKLY:
                              return 'WEEKLY';
                           case NotificationFrequency.MONTHLY:
                              return 'MONTHLY';
                           default:
                              return 'UNKNOWN';
                        }
                     })()
                  }</label>
                  <label htmlFor='notifyOnly'><b>Notify Only:</b> {data.notifyAll ? 'ALL' : 'SALESMAN'}</label>
                  <label htmlFor='status'><b>Status:</b> {data.status === NotificationStatus.DELIVERED ? 'DELIVERED' : 'SCHEDULED'}</label>
               </div>

               <div className="button-container">
                  <button className="button" onClick={onClose}>Close</button>
                  <button className="button">Delete</button>
               </div>
            </div>
         )}

      </div>
   );
};

export default RightBar;
