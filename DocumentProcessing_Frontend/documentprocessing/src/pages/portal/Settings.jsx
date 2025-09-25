import React from 'react';
import { Helmet } from 'react-helmet'
import '../../components/BasePage/BasePage.css';

import SideBar from  '../../components/SideBar/SideBar.jsx';
import NavBar from'../../components/NavBar/NavBar.jsx';

import Settings from '../../components/Settings/Settings.jsx';

const SettingsPage = () => {

  const sideBarProperties={
    'isAdmin': sessionStorage.getItem('isAdmin') === 'true' ? true : false
  }

  return (
    <div className="page-wrapper" id="main-wrapper" data-layout="vertical" data-navbarbg="skin6" data-sidebartype="full"
      data-sidebar-position="fixed" data-header-position="fixed">
         <Helmet>
        <title>Settings / Portal</title>
        <link rel="icon" href="/icons/settings.png" />
      </Helmet>
      <SideBar properties={sideBarProperties} />
      <NavBar />
      <Settings/>
    </div>
  );
};

export default SettingsPage;
