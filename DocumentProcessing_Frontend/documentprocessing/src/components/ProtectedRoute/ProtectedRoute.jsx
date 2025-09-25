import React from 'react';
import { Navigate } from 'react-router-dom';
import configuration from '../../config/configuration.json';

const ProtectedRoute = ({ children, allowedRoles }) => {
  const token = sessionStorage.getItem('token');
  const userRole = sessionStorage.getItem('userRole'); // Store actual role like "admin" or "salesman"

  if (!token) {
    return <Navigate to={configuration.documentprocessing.routes.USER.LOGIN} replace />;
  }

  // Check if the user's role is allowed to access this route
  if (!allowedRoles.includes(userRole) && userRole !== 'admin') {
    return <Navigate to={configuration.documentprocessing.routes.USER.LOGOUT} replace />;
  }

  return children;
};

export default ProtectedRoute;
