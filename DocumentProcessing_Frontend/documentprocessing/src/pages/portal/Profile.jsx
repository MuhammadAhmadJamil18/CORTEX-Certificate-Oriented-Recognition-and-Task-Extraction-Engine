import React from 'react';
import { Helmet } from 'react-helmet'
import '../../components/BasePage/BasePage.css';

import SideBar from  '../../components/SideBar/SideBar.jsx';
import NavBar from'../../components/NavBar/NavBar.jsx';

import Profile from '../../components/Profile/Profile.jsx';

const ProfilePage = () => {

  const sideBarProperties={
    'isAdmin': sessionStorage.getItem('isAdmin') === 'true' ? true : false
  }

  return (
    <div className="page-wrapper" id="main-wrapper" data-layout="vertical" data-navbarbg="skin6" data-sidebartype="full"
      data-sidebar-position="fixed" data-header-position="fixed">
         <Helmet>
        <title>Profile / Portal</title>
        <link rel="icon" href="/icons/profile.png" />
      </Helmet>
      <SideBar properties={sideBarProperties} />
      <NavBar />
      <Profile/>
    </div>
  );
};

export default ProfilePage;
