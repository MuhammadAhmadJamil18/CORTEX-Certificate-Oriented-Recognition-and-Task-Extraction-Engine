import React, { useEffect } from 'react';
import '../../components/BasePage/BasePage.css';

import configuration from '../../config/configuration.json';


const LogoutPage = () => {

    useEffect(() => {
        sessionStorage.removeItem('token');
        sessionStorage.removeItem('userId');
        sessionStorage.removeItem('user');
        sessionStorage.removeItem('notifications');
        window.location.href = `${configuration.documentprocessing.urls.frontend}${configuration.documentprocessing.routes.USER.LOGIN}`;
    }, []);

  return (
    <div>
   </div>
  );
};

export default LogoutPage;
