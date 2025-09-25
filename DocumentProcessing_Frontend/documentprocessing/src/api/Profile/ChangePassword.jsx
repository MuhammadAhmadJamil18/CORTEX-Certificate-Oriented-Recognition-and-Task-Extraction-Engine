import axios from 'axios';
import configuration from '../../config/configuration.json';

export const ChangePassword = async (payload, token) => {
  const url = `${configuration.documentprocessing.urls.backend}/api/users/change-password`;
  return axios.post(url, payload, {
    headers: {
      'Content-Type': 'application/json',
      token
    }
  });
};
