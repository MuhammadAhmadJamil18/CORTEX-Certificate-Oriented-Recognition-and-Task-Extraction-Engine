import axios from "axios";
import configuration from "../../config/configuration.json";

export const FetchSettings = token => {
  const url = `${configuration.documentprocessing.urls.backend}/api/settings/notification`;
  return axios.get(url, { headers:{ token } });
};
