import React from 'react';
import { Helmet } from 'react-helmet'
import '../../components/BasePage/BasePage.css';

import SideBar from  '../../components/SideBar/SideBar.jsx';
import NavBar from'../../components/NavBar/NavBar.jsx';
import Notification from '../../components/Notification/Notification.jsx';

const NotificationPage = () => {

  const sideBarProperties={
    'isAdmin': sessionStorage.getItem('isAdmin') === 'true' ? true : false
  }

  return (
    <div className="page-wrapper" id="main-wrapper" data-layout="vertical" data-navbarbg="skin6" data-sidebartype="full"
      data-sidebar-position="fixed" data-header-position="fixed">
        <Helmet>
        <title>Notifications / Portal</title>
        <link rel="icon" href="/icons/notification.png" />
      </Helmet>
      <SideBar properties={sideBarProperties} />
      <NavBar />
      <Notification />
    </div>
  );
};

export default NotificationPage;
