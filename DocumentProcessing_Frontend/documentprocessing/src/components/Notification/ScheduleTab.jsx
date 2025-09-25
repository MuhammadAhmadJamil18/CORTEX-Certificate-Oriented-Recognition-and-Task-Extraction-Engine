import React, { useState } from 'react';
import './Notification.css'; // CSS file for styling
import { toast } from 'react-toastify';

import { Schedule } from '../../api/Notifications/Schedule';

const ScheduleTab = () => {
   const [formData, setFormData] = useState({
      notificationType: 2,
      notificationMessage: 'This is a scheduled test notification message. Kindly ignore.',
      notificationSubject: 'Scheduled Notification',
      notifyAll: 'false',
      notificationFrequency: 1,
      scheduleTime: '',
      scheduleTimezone: 'PST',
   });

   const handleChange = (e) => {
      const { name, value } = e.target;
      setFormData((prevData) => ({
         ...prevData,
         [name]: value
      }));
   };

   const handleSubmit = (e) => {
      e.preventDefault();

      Schedule(formData, sessionStorage.getItem('token')).then((response) => {
         if(response.status === 200){
            if(response.data.messageCode === 2001){
               toast.success(response.data.message+" ID: "+response.data.notificationId);
            }else{
               toast.error("Code:"+response.data.messageCode+" "+response.data.message);
            }
         } else {
            toast.error("An error occured. Please check console.");
         }
      }).catch((error) => {
         console.log(error);
         toast.error("An error occured. Please check console.");
      });
  }

   return (
      <div className="notify-tab">
         <h4 style={{ marginTop: '10px' }}>Schedule a Notification</h4>
         <form onSubmit={handleSubmit}>
            <div className="notification-fields">
               <div className="form-group">
                  <label htmlFor="notificationType">Notification Type:</label>
                  <select
                     id="notificationType"
                     name="notificationType"
                     value={formData.notificationType}
                     onChange={handleChange}
                     required
                  >
                      <option value="2">Email Notification</option>
                     <option value="3">Web Notification</option>
                     <option value="1">Email & Web Notification</option>
                  </select>
               </div>

               <div className="form-group">
                  <label htmlFor="notificationFrequency">Notification Frequency:</label>
                  <select
                     id="notificationFrequency"
                     name="notificationFrequency"
                     value={formData.notificationFrequency}
                     onChange={handleChange}
                     required
                  >
                     <option value="1">Daily</option>
                     <option value="2">Weekly</option>
                     <option value="3">Monthly</option>
                  </select>
               </div>

               <div className="form-group">
                  <label htmlFor="notifyAll">Notify All:</label>
                  <select
                     id="notifyAll"
                     name="notifyAll"
                     value={formData.notifyAll}
                     onChange={handleChange}
                     required
                  >
                       <option value="true">Notify All Users</option>
                       <option value="false">Notify Salesman Only</option>
                  </select>
               </div>
            </div>

            <div className="form-group">
               <label htmlFor="notificationSubject">Notification Subject:</label>
               <input
                  type="text"
                  id="notificationSubject"
                  name="notificationSubject"
                  value={formData.notificationSubject}
                  onChange={handleChange}
                  required
               />
            </div>

            <div className="form-group">
               <label htmlFor="notificationMessage">Notification Message:</label>
               <textarea
                  id="notificationMessage"
                  name="notificationMessage"
                  value={formData.notificationMessage}
                  onChange={handleChange}
                  required
               />
            </div>

            <div className="form-group">
               <label htmlFor="scheduleTime">Schedule Time:</label>
               <input
                  type="datetime-local"
                  id="scheduleTime"
                  name="scheduleTime"
                  value={formData.scheduleTime}
                  onChange={handleChange}
                  required
               />
            </div>

            <div className="form-group">
               <label htmlFor="scheduleTimezone">Schedule Timezone:</label>
               <input
                  type="text"
                  id="scheduleTimezone"
                  name="scheduleTimezone"
                  value={formData.scheduleTimezone}
                  onChange={handleChange}
                  placeholder="e.g., GMT+1"
                  required
               />
            </div>

            <div className="submit-container">
               <button type="submit" className="submit-button">
                  Schedule
               </button>
            </div>
         </form>
      </div>
   );
};

export default ScheduleTab;
