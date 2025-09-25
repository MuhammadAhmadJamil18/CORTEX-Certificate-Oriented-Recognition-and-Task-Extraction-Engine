import axios from 'axios';
import configuration from '../../config/configuration.json';

export const FetchProfile = async token => {
  const url = `${configuration.documentprocessing.urls.backend}/api/users/me`;
  return axios.get(url, {
    headers: { token }
  });
};
