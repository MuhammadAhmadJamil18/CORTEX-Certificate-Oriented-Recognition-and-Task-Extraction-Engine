
import axios from 'axios';
import configuration from '../../config/configuration.json'

export const UpdateUser = async (request, token) => {
  try {
    const path="/api/users/update";
    const url=`${configuration.documentprocessing.urls.backend}${path}`;

    const response = await axios.post(url, request, {
      headers: {
        'Content-Type': 'application/json',
        'token': token,
      }
      
    });

    return response; 
  } catch (error) {
    return error; 
  }
};