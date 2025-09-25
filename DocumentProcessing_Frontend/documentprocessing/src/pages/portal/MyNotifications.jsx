import React from 'react';
import { Helmet } from 'react-helmet'
import '../../components/BasePage/BasePage.css';

import SideBar from  '../../components/SideBar/SideBar.jsx';
import NavBar from'../../components/NavBar/NavBar.jsx';
import NotificationsForMe from '../../components/Notification/NotificationsForMe.jsx';


const MyNotificationsPage = () => {

  const sideBarProperties={
    'isAdmin': sessionStorage.getItem('isAdmin') === 'true' ? true : false
  }

  return (
    <div className="page-wrapper" id="main-wrapper" data-layout="vertical" data-navbarbg="skin6" data-sidebartype="full"
      data-sidebar-position="fixed" data-header-position="fixed">
        <Helmet>
        <title>My Notifications / Portal</title>
        <link rel="icon" href="/icons/notification.png" />
      </Helmet>
      <SideBar properties={sideBarProperties} />
      <NavBar />
      <div className="body-wrapper">
            <div className="container-fluid">
                <div className="row">
                    <div className="col-lg-8-process d-flex align-items-stretch">
                        <div className="card w-100">
                            <div className="card-body">
                                <div>
                                    <div className="mb-3 mb-sm-0">               
                                    <NotificationsForMe />
                                    </div>
                                </div>
                            </div>
                        </div>
                    </div>
                    
                    </div>
            </div>
        </div>

    </div>
  );
};

export default MyNotificationsPage;
