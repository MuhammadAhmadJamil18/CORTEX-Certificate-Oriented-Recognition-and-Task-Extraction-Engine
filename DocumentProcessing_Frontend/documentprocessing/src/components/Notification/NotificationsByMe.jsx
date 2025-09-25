import React, { useEffect, useState } from 'react';
import $ from 'jquery';
import 'datatables.net-dt/css/dataTables.dataTables.min.css';
import 'datatables.net';
import './Notification.css';
import RightBar from '../SideBar/RightBar';
import { toast } from 'react-toastify';
import { getNotifications } from '../../api/Notifications/getNotifications';
import { NotificationFrequency } from '../../enums/NotificationFrequency';
import { NotificationStatus } from '../../enums/NotificationStatus';
import { NotificationType } from '../../enums/NotificationType';

const NotificationsByMe = () => {
   const [notifications, setNotifications] = useState([]);

   const [isSidebarOpen, setIsSidebarOpen] = useState(false);
   const [sideBarData, setSideBarData] = useState([]);

   useEffect(() => {
      getNotifications(sessionStorage.getItem('token'))
         .then((response) => {
            if (response.status === 200) {
               // Transform notifications to include auto-incremented ID
               const transformedNotifications = response.data.map((notification, index) => ({
                  ...notification,
                  autoId: index + 1 // Auto-incremented ID
               }));
               setNotifications(transformedNotifications);
            } else {
               toast.error("Unable to fetch notifications.");
            }
         })
         .catch((error) => {
            console.log(error);
            toast.error("An error occurred. Please check console.");
         });
   }, []);

   const viewNotification = (id) => {
      setSideBarData(notifications.find(notification => notification.notificationId === id));
      setIsSidebarOpen(true);
      $('#sidebar').css('right', '0');
   };

   useEffect(() => {
      window.viewNotification = viewNotification;
      const table = $('#notificationsTable').DataTable({
         data: notifications,
         columns: [
            { data: 'autoId' }, // Use the auto-incremented ID for display
            { data: 'notificationSubject' },
            { data: 'notificationId' }, 
            { 
               data: 'scheduleTime',
               render: (data) => data ? new Date(data).toLocaleString() : 'N/A'
            },
            { 
               data: 'notificationType',
               render: (data) => {
                  switch (data) {
                      case NotificationType.BOTH:
                          return 'WEB/EMAIL';
                      case NotificationType.EMAIL:
                          return 'EMAIL';
                      case NotificationType.WEB:
                          return 'WEB';
                      default:
                          return 'UNKNOWN'; // Fallback for any unexpected values
                  }
               }
            },
            { 
               data: 'notifyAll',
               render: (data) => data ? 'ALL' : 'SALESMAN'
            },
            { 
               data: 'notificationFrequency',
               render: (data) => {
                  switch (data) {
                      case NotificationFrequency.DAILY:
                          return 'DAILY';
                      case NotificationFrequency.WEEKLY:
                          return 'WEEKLY';
                      case NotificationFrequency.MONTHLY:
                          return 'MONTHLY';
                      default:
                          return 'UNKNOWN'; // Fallback for any unexpected values
                  }
               }
            },
            {
               data: 'status',
               render: (data) => {
                  const statusText = data === NotificationStatus.DELIVERED ? 'DELIVERED' : 'SCHEDULED';
                  const statusColor = data === NotificationStatus.DELIVERED ? 'forestgreen' : 'indianred';
                  
                  return `<button style="background-color: ${statusColor}; width: 100%; color: white; border: none; padding: 5px 5px; border-radius: 4px;">${statusText}</button>`
               }
            },            
            {
               data: null,
               render: (data) => `
                  <button style="background-color: #007bff; width: 100%, color: white; border: none; padding: 5px 10px; border-radius: 4px; cursor: pointer;" onclick="viewNotification(${data.notificationId})">View</button>
               `
            }
            
         ]
      });

      return () => {
         if ($.fn.DataTable.isDataTable('#notificationsTable')) {
            $('#notificationsTable').DataTable().clear().destroy();
         }
         delete window.viewNotification;
      };
   }, [notifications]);

   return (
      <div className="notify-tab">
         <h4 style={{marginTop: '10px'}}>All Notifications</h4>
         <table id="notificationsTable" className="display" style={{ marginTop: '25px' }}>
            <thead>
               <tr style={{ backgroundColor: '#007bff', color: 'white' }}>
                  <th>S. No.</th> {/* Display the auto-incremented ID */}
                  <th>Subject</th>
                  <th>ID</th>
                  <th>Schedule Time</th>
                  <th>Type</th>
                  <th>Notify Only</th>
                  <th>Frequency</th>
                  <th>Status</th>
                  <th>View</th>
               </tr>
            </thead>
            <tbody>
            </tbody>
         </table>

         <RightBar 
            isOpen={isSidebarOpen} 
            data={sideBarData} 
            onClose={() => setIsSidebarOpen(false)} 
            type="notification"
         />
      </div>
   );
};

export default NotificationsByMe;
