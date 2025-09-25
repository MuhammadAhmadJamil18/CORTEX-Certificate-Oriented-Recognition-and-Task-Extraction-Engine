
import axios from 'axios';
import configuration from '../../config/configuration.json'

export const ProcessAll = async (request, token) => {
  try {
    const path="/api/processor/processAll";
    const url=`${configuration.documentprocessing.urls.backend}${path}`;

    const response = await axios.post(url, request, {
      headers: {
        'Content-Type': 'multipart/form-data',
        'token': token,
      }
      
    });

    return response; 
  } catch (error) {
    return error; 
  }
};