
import axios from 'axios';
import configuration from '../../config/configuration.json'

export const ResetPassword = async (request) => {
  try {
    const path="/api/users/reset-password";
    const url=`${configuration.documentprocessing.urls.backend}${path}`;
        
    const response = await axios.post(url, request, {
      headers: {
        'Content-Type': 'application/json',
      }
    });
    return response; 
  } catch (error) {
    return error; 
  }
};