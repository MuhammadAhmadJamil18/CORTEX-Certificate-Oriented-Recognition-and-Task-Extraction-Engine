
import axios from 'axios';
import configuration from '../../config/configuration.json'

export const getStatistics = async (token) => {
  try {
    const path="/api/reports/statistics";
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