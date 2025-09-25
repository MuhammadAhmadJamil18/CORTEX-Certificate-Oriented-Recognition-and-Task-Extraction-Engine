import React from 'react';
import { Helmet } from 'react-helmet';
import '../../components/BasePage/BasePage.css';
import { Login } from '../../api/Users/Login';
import { toast } from 'react-toastify';

import configuration from '../../config/configuration.json';

const LoginPage = () => {
  const handleSubmit = (e) => {
    e.preventDefault();
    const email = document.getElementById('emailInput').value;
    const password = document.getElementById('passwordInput').value;

    const data = {
      userEmail: email,
      userPassword: password
    };

    Login(data).then((response) => {
      if (response.status === 200) {
        if (response.data.messageCode === 1000) {
          sessionStorage.setItem('token', response.data.token);
          sessionStorage.setItem('userId', response.data.userId)
          
          if (response.data.userRole === 1) {
            sessionStorage.setItem('isAdmin', 'true');
            sessionStorage.setItem('userRole', "admin");
          } else {
            sessionStorage.setItem('isAdmin', 'false');
            sessionStorage.setItem('userRole', "salesman");
          }
          sessionStorage.setItem('user', JSON.stringify(response.data));
          window.location.href = `${configuration.documentprocessing.urls.frontend}${configuration.documentprocessing.routes.PORTAL.DASHBOARD}`;
        } else {
          toast.error("Code:" + response.data.messageCode + " " + response.data.message);
        }
      }
    }).catch((error) => {
      console.log(error);
      toast.error("An error occured. Please check console.");
    });
  }

  return (
    <div className="page-wrapper" id="main-wrapper" data-layout="vertical" data-navbarbg="skin6" data-sidebartype="full"
      data-sidebar-position="fixed" data-header-position="fixed">
      <Helmet>
        <title>Login / Portal</title>
        <link rel="icon" href="/icons/login.png" />
      </Helmet>

      <div className="position-relative overflow-hidden radial-gradient min-vh-100 d-flex align-items-center justify-content-center">
        <div className="d-flex align-items-center justify-content-center w-100">
          <div className="row justify-content-center w-100">
            <div className="col-md-8 col-lg-6 col-xxl-3">
              <div className="card mb-0">
                <div className="card-body">
                  <a href="/" className="text-nowrap logo-img text-center d-block py-3 w-100">
                    <img src="/logo/qualitydxbLogo.png" width="180" alt="" />
                  </a>
                  <p className="text-center">Portal Login</p>
                  <div className="mb-3">
                    <label htmlFor="emailInput" className="form-label">Email</label>
                    <input type="email" className="form-control" id="emailInput" name="emailInput" aria-describedby="emailHelp" required />
                  </div>
                  <div className="mb-4">
                    <label htmlFor="passwordInput" className="form-label">Password</label>
                    <input type="password" className="form-control" id="passwordInput" name="passwordInput" required />
                  </div>
                  <div className="d-flex align-items-center justify-content-between mb-4">
                    <a className="text-primary fw-bold" href="/ForgetPassword">Forgot Password?</a>
                  </div>
                  <button type="submit" className="btn btn-primary w-100 py-8 fs-4 mb-4 rounded-2" onClick={handleSubmit}>Sign In</button>
                </div>
              </div>
            </div>
          </div>
        </div>
      </div>
    </div>
  );
};

export default LoginPage;
