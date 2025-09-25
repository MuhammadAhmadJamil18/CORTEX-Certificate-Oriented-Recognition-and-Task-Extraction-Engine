
import axios from 'axios';
import configuration from '../../config/configuration.json'

export const Filters = async (token) => {
  try {
    const path="/api/reports/filters";
    const url=`${configuration.documentprocessing.urls.backend}${path}`;

    const response = await axios.get(url, {
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