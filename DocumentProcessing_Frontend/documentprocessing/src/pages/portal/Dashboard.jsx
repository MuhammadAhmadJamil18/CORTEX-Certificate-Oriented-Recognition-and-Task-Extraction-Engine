import React from 'react';
import { Helmet } from 'react-helmet';
import '../../components/BasePage/BasePage.css';



import SideBar from  '../../components/SideBar/SideBar.jsx';
import NavBar from'../../components/NavBar/NavBar.jsx';
import Statistics from '../../components/Statistics/Statistics.jsx';


const DashboardPage = () => {

  const sideBarProperties={
    'isAdmin': sessionStorage.getItem('isAdmin') === 'true' ? true : false
  }

  return (
    <div className="page-wrapper" id="main-wrapper" data-layout="vertical" data-navbarbg="skin6" data-sidebartype="full"
      data-sidebar-position="fixed" data-header-position="fixed">
      <Helmet>
        <title>Dashboard / Portal</title>
        <link rel="icon" href="/icons/dashboard.png" />
      </Helmet>
      <SideBar properties={sideBarProperties} />
      <NavBar />
      <Statistics />
    </div>
  );
};

export default DashboardPage;
