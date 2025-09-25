import axios from "axios";
import configuration from "../../config/configuration.json";

export const SaveSettings = (payload, token) => {
  const url = `${configuration.documentprocessing.urls.backend}/api/settings/notification`;
  return axios.post(url, payload, {
    headers:{
      "Content-Type":"application/json",
      token
    }
  });
};
