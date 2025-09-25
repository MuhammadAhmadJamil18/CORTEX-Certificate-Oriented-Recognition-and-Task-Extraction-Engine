import React from "react";
import { BrowserRouter as Router, Routes, Route } from "react-router-dom";
import { ToastContainer } from 'react-toastify';
import 'react-toastify/dist/ReactToastify.css'; 

import DashboardPage from "./pages/portal/Dashboard";
import ProfilePage from "./pages/portal/Profile";
import SettingsPage from "./pages/portal/Settings";
import NotificationPage from "./pages/portal/Notification";
import MyNotificationsPage from "./pages/portal/MyNotifications";
import UsersPage from "./pages/portal/Users";
import ProcessPage from "./pages/portal/Process";
import ReportsPage from "./pages/portal/Reports";
import LoginPage from "./pages/users/Login";
import LogoutPage from "./pages/users/Logout";
import ForgetPasswordPage from "./pages/users/ForgetPassword";
import ResetPasswordPage from "./pages/users/ResetPassword";

import ProtectedRoute from './components/ProtectedRoute/ProtectedRoute';
import configuration from './config/configuration.json';

export default function App() {
  return (
    <div>
      <ToastContainer position="top-right" autoClose={5000} hideProgressBar={false} />
    
    <Router>
      <Routes>
 
       <Route path={configuration.documentprocessing.routes.PORTAL.DASHBOARD} element={<ProtectedRoute allowedRoles={['salesman', 'admin']}><DashboardPage /></ProtectedRoute>} />
        <Route path={configuration.documentprocessing.routes.PORTAL.PROFILE} element={<ProtectedRoute allowedRoles={['salesman', 'admin']}><ProfilePage /></ProtectedRoute>} />
        <Route path={configuration.documentprocessing.routes.PORTAL.SETTINGS} element={<ProtectedRoute allowedRoles={['salesman', 'admin']}><SettingsPage /></ProtectedRoute>} />
        <Route path={configuration.documentprocessing.routes.PORTAL.MYNOTIFICATIONS} element={<ProtectedRoute allowedRoles={['salesman', 'admin']}><MyNotificationsPage /></ProtectedRoute>} />
        <Route path={configuration.documentprocessing.routes.PORTAL.PROCESS} element={<ProtectedRoute allowedRoles={['salesman', 'admin']}><ProcessPage /></ProtectedRoute>} />
        <Route path={configuration.documentprocessing.routes.PORTAL.REPORTS} element={<ProtectedRoute allowedRoles={['salesman', 'admin']}><ReportsPage  /></ProtectedRoute>} />
        
        <Route path={configuration.documentprocessing.routes.PORTAL.NOTIFICATION} element={<ProtectedRoute allowedRoles={['admin']}><NotificationPage /></ProtectedRoute>} />
        <Route path={configuration.documentprocessing.routes.PORTAL.USERS} element={<ProtectedRoute allowedRoles={['admin']} ><UsersPage /></ProtectedRoute>} />

        <Route path="/" element={<LoginPage />} />
        <Route path={configuration.documentprocessing.routes.USER.LOGIN} element={<LoginPage />} />
        <Route path={configuration.documentprocessing.routes.USER.FORGETPASSWORD} element={<ForgetPasswordPage />} />
        <Route path={configuration.documentprocessing.routes.USER.RESETPASSWORD} element={<ResetPasswordPage />} />
        <Route path={configuration.documentprocessing.routes.USER.LOGOUT} element={<LogoutPage />} />
      </Routes>
    </Router>
    </div>
  );
}
