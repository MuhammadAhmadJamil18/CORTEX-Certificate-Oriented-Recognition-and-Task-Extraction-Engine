import React from 'react';
import './NotificationBox.css';

import configuration from '../../config/configuration.json';

const NotificationBox = ({ notifications }) => {
  return (
    <div className="notification-container">
      <div className="notification-header">
        <h4 className="dropdown-title">Notifications</h4>
          <button 
            className="see-all-button"
            onClick={() => window.location.href = `${configuration.documentprocessing.routes.PORTAL.MYNOTIFICATIONS}`}>
            See All
          </button>
      </div>
      <div className="notifications-container">
        {notifications.length ? (
          notifications.map((notification) => (
            <div key={notification.id} className="notification-item">
              <img className="notification-icon" src="/icons/message.png" alt="notification icon" />
              <div>
                <h5 className="notification-subject">{notification.subject}</h5>
                <p className="notification-message">{notification.message}</p>
                <span className="notification-time">
                  {notification.time.split("T")[1].slice(0, 5)}
                </span>
              </div>
            </div>
          ))
        ) : (
          <p className="no-notifications">No new notifications</p>
        )}
      </div>
    </div>
  );
};

export default NotificationBox;
