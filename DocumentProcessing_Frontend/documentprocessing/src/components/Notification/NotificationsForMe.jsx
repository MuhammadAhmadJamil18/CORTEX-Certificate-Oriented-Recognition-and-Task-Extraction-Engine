import React, { useEffect, useState } from 'react';
import $ from 'jquery';
import 'datatables.net-dt/css/dataTables.dataTables.min.css';
import 'datatables.net';
import './Notification.css';
import { toast } from 'react-toastify';
import RightBar from '../SideBar/RightBar';
import { getNotificationsForMe } from '../../api/Notifications/getNotificationsForMe';
import { NotificationStatus } from '../../enums/NotificationStatus';
import { NotificationType } from '../../enums/NotificationType';

const NotificationsForMe = () => {
  const [notifications, setNotifications] = useState([]);
  const [filteredNotifications, setFilteredNotifications] = useState([]);
  const [isSidebarOpen, setIsSidebarOpen] = useState(false);
  const [sideBarData, setSideBarData] = useState([]);

  // Filter states, now including time range
  const [filters, setFilters] = useState({
    recipientType: 'all',
    status: 'all',
    notificationType: 'all',
    timeFrom: '',
    timeTo: ''
  });

  const getUniqueRecipients = () => {
    const recipients = [...new Set(notifications.map(n => n.recipientName).filter(Boolean))];
    return recipients.sort();
  };

  useEffect(() => {
    getNotificationsForMe(sessionStorage.getItem('token'))
      .then((response) => {
        if (response.status === 200) {
          const transformedNotifications = response.data.map((notification, index) => ({
            ...notification,
            autoId: index + 1,
            recipientName: notification.recipientName || 'N/A'
          }));
          setNotifications(transformedNotifications);
          setFilteredNotifications(transformedNotifications);
        } else {
          toast.error("Unable to fetch notifications.");
        }
      })
      .catch((error) => {
        console.error(error);
        toast.error("An error occurred. Please check console.");
      });
  }, []);

  // Apply filters including time range
  useEffect(() => {
    let filtered = [...notifications];

    if (filters.recipientType !== 'all') {
      filtered = filtered.filter(n => n.recipientName === filters.recipientType);
    }
    if (filters.status !== 'all') {
      const statusValue = filters.status === 'delivered'
        ? NotificationStatus.DELIVERED
        : NotificationStatus.SCHEDULED;
      filtered = filtered.filter(n => n.status === statusValue);
    }
    if (filters.notificationType !== 'all') {
      let typeValue = null;
      switch (filters.notificationType) {
        case 'web':   typeValue = NotificationType.WEB;   break;
        case 'email': typeValue = NotificationType.EMAIL; break;
        case 'both':  typeValue = NotificationType.BOTH;  break;
      }
      if (typeValue !== null) {
        filtered = filtered.filter(n => n.notificationType === typeValue);
      }
    }
    if (filters.timeFrom) {
      const from = new Date(filters.timeFrom).getTime();
      filtered = filtered.filter(n => {
        const t = new Date(n.scheduleTime).getTime();
        return t >= from;
      });
    }
    if (filters.timeTo) {
      const to = new Date(filters.timeTo).getTime();
      filtered = filtered.filter(n => {
        const t = new Date(n.scheduleTime).getTime();
        return t <= to;
      });
    }

    setFilteredNotifications(filtered);
  }, [filters, notifications]);

  const handleFilterChange = (filterType, value) => {
    setFilters(prev => ({ ...prev, [filterType]: value }));
  };

  const clearAllFilters = () => {
    setFilters({
      recipientType: 'all',
      status: 'all',
      notificationType: 'all',
      timeFrom: '',
      timeTo: ''
    });
  };

  const viewNotification = (id) => {
    setSideBarData(notifications.find(n => n.notificationId === id));
    setIsSidebarOpen(true);
    $('#sidebar').css('right', '0');
  };

  useEffect(() => {
    window.viewNotification = viewNotification;
    if ($.fn.DataTable.isDataTable('#notificationsTable')) {
      $('#notificationsTable').DataTable().clear().destroy();
    }

    $('#notificationsTable').DataTable({
      data: filteredNotifications,
      columns: [
        { data: 'autoId' },
        { data: 'notificationSubject' },
        { data: 'notificationMessage' },
        {
          data: 'scheduleTime',
          render: data => data ? new Date(data).toLocaleString() : 'N/A'
        },
        { data: 'notifiedBy' },
        { data: 'recipientName' },
        {
          data: 'notificationType',
          render: data => {
            switch (data) {
              case NotificationType.BOTH:  return 'WEB/EMAIL';
              case NotificationType.EMAIL: return 'EMAIL';
              case NotificationType.WEB:   return 'WEB';
              default:                     return 'UNKNOWN';
            }
          }
        },
        {
          data: 'status',
          render: data => {
            const text = data === NotificationStatus.DELIVERED ? 'DELIVERED' : 'SCHEDULED';
            const color = data === NotificationStatus.DELIVERED ? 'forestgreen' : 'indianred';
            return `<button style="
              background-color: ${color};
              width:100%; color:white; border:none;
              padding:5px; border-radius:4px;
            ">${text}</button>`;
          }
        },
        {
          data: null,
          render: d => `<button style="
            background-color:#007bff; width:100%; color:white;
            border:none; padding:5px; border-radius:4px;
            cursor:pointer;
          " onclick="viewNotification(${d.notificationId})">View</button>`
        }
      ],
      destroy: true,
      responsive: true,
      pageLength: 10,
      lengthMenu: [5,10,25,50,100],
      language: { emptyTable: "No notifications match the current filters" }
    });

    return () => {
      if ($.fn.DataTable.isDataTable('#notificationsTable')) {
        $('#notificationsTable').DataTable().clear().destroy();
      }
      delete window.viewNotification;
    };
  }, [filteredNotifications]);

  const getFilterCount = () =>
    Object.values(filters).filter(v => v && v !== 'all').length;

  return (
    <div className="notify-tab">
      <div className="filter-container" style={{
        backgroundColor:'#f8f9fa', padding:'20px',
        borderRadius:'8px', marginBottom:'20px',
        border:'1px solid #e9ecef'
      }}>
        <div style={{
          display:'flex', justifyContent:'space-between',
          alignItems:'center', marginBottom:'15px'
        }}>
          <h4 style={{
            margin:0, color:'#495057',
            display:'flex', alignItems:'center', gap:'10px'
          }}>
            🔍 Filters
            {getFilterCount()>0 && (
              <span style={{
                backgroundColor:'#007bff', color:'white',
                borderRadius:'50%', width:'24px', height:'24px',
                display:'flex', alignItems:'center',
                justifyContent:'center', fontSize:'12px', fontWeight:'bold'
              }}>
                {getFilterCount()}
              </span>
            )}
          </h4>
          {getFilterCount()>0 && (
            <button onClick={clearAllFilters}
              style={{
                backgroundColor:'#6c757d', color:'white',
                border:'none', padding:'8px 16px', borderRadius:'4px',
                cursor:'pointer', fontSize:'14px'
              }}
            >
              Clear All Filters
            </button>
          )}
        </div>

        <div style={{
          display:'grid',
          gridTemplateColumns:'repeat(auto-fit, minmax(200px,1fr))',
          gap:'15px'
        }}>
          {/* Recipient */}
          <div>
            <label style={{ display:'block', marginBottom:'5px', color:'#495057' }}>
              Recipient Type:
            </label>
            <select
              value={filters.recipientType}
              onChange={e=>handleFilterChange('recipientType', e.target.value)}
              style={{
                width:'100%', padding:'8px', border:'1px solid #ced4da',
                borderRadius:'4px', fontSize:'14px', cursor:'pointer'
              }}
            >
              <option value="all">All Recipients</option>
              {getUniqueRecipients().map(r=>(
                <option key={r} value={r}>{r}</option>
              ))}
            </select>
          </div>

          {/* Status */}
          <div>
            <label style={{ display:'block', marginBottom:'5px', color:'#495057' }}>
              Status:
            </label>
            <select
              value={filters.status}
              onChange={e=>handleFilterChange('status', e.target.value)}
              style={{
                width:'100%', padding:'8px', border:'1px solid #ced4da',
                borderRadius:'4px', fontSize:'14px', cursor:'pointer'
              }}
            >
              <option value="all">All Status</option>
              <option value="delivered">Delivered</option>
              <option value="scheduled">Scheduled</option>
            </select>
          </div>

          {/* Type */}
          <div>
            <label style={{ display:'block', marginBottom:'5px', color:'#495057' }}>
              Notification Type:
            </label>
            <select
              value={filters.notificationType}
              onChange={e=>handleFilterChange('notificationType', e.target.value)}
              style={{
                width:'100%', padding:'8px', border:'1px solid #ced4da',
                borderRadius:'4px', fontSize:'14px', cursor:'pointer'
              }}
            >
              <option value="all">All Types</option>
              <option value="web">Web</option>
              <option value="email">Email</option>
              <option value="both">Web/Email</option>
            </select>
          </div>

          {/* Time From */}
          <div>
            <label style={{ display:'block', marginBottom:'5px', color:'#495057' }}>
              From:
            </label>
            <input
              type="datetime-local"
              value={filters.timeFrom}
              onChange={e=>handleFilterChange('timeFrom', e.target.value)}
              style={{
                width:'100%', padding:'8px', border:'1px solid #ced4da',
                borderRadius:'4px', fontSize:'14px'
              }}
            />
          </div>

          {/* Time To */}
          <div>
            <label style={{ display:'block', marginBottom:'5px', color:'#495057' }}>
              To:
            </label>
            <input
              type="datetime-local"
              value={filters.timeTo}
              onChange={e=>handleFilterChange('timeTo', e.target.value)}
              style={{
                width:'100%', padding:'8px', border:'1px solid #ced4da',
                borderRadius:'4px', fontSize:'14px'
              }}
            />
          </div>
        </div>

        <div style={{
          marginTop:'15px', padding:'10px',
          backgroundColor:'#e7f3ff', borderRadius:'4px',
          border:'1px solid #b3d9ff'
        }}>
          <span style={{ fontSize:'14px', color:'#0066cc' }}>
            📊 Showing {filteredNotifications.length} of {notifications.length} notifications
            {getFilterCount()>0 && ` (${getFilterCount()} filter${getFilterCount()>1?'s':''} applied)`}
          </span>
        </div>
      </div>

      <div style={{ overflowX:'auto' }}>
        <table id="notificationsTable" className="display" style={{ width:'100%', marginTop:'20px' }}>
          <thead>
            <tr style={{ backgroundColor:'#007bff', color:'white' }}>
              <th>S. No.</th>
              <th>Subject</th>
              <th>Message</th>
              <th>Time</th>
              <th>Notifier</th>
              <th>Recipient</th>
              <th>Type</th>
              <th>Status</th>
              <th>View</th>
            </tr>
          </thead>
          <tbody />
        </table>
      </div>

      <RightBar
        isOpen={isSidebarOpen}
        data={sideBarData}
        onClose={() => setIsSidebarOpen(false)}
        type="notification"
      />
    </div>
  );
};

export default NotificationsForMe;
