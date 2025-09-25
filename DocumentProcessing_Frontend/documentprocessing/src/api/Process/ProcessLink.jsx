
import axios from 'axios';
import configuration from '../../config/configuration.json'

// export const ProcessLink = async (request, token) => {
//   try {
//     const path="/api/processor/processLink?documentsLink="+request;
//     const url=`${configuration.documentprocessing.urls.backend}${path}`;

//     console.log(url);

//     const response = await axios.post(url, request, {
//       headers: {
//         'Content-Type': 'application/json',
//         'token': token,
//       }
      
//     });

//     return response; 
//   } catch (error) {
//     return error; 
//   }
// };

export const ProcessLink = async (megaLink,fields, token) => {
  try {
    // The backend endpoint where we send the JSON body

    const path = '/api/processor/processLink';
    const url=`${configuration.documentprocessing.urls.backend}${path}`;


    // Request body to send to Spring Boot
    const body = {
      mega_link: megaLink,
      fields:fields
    };

    const response = await axios.post(url, body, {
      headers: {
        'Content-Type': 'application/json',
        'token': token,
      }
    });

    return response;
  } catch (error) {
    console.error(error);
    throw error;
  }
};