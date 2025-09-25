import axios from 'axios';
import configuration from '../../config/configuration.json';

export const downloadDocument = async (documentId, token) => {
  const url = `${configuration.documentprocessing.urls.backend}/api/processor/download/${documentId}`;
  const response = await axios.get(url, {
    responseType: 'blob',
    headers: { 'token': token },
  });

  // now that we've exposed it, this should work:
  const disposition = response.headers['content-disposition'];
  let filename = 'download';            // fallback
  if (disposition) {
    // try to parse RFC 5987 (UTF-8) or basic filename="..."
    const utf8Match = /filename\*\=UTF-8''(.+)$/.exec(disposition);
    const simpleMatch = /filename=\"?([^\";]+)\"?/.exec(disposition);
    if (utf8Match)   filename = decodeURIComponent(utf8Match[1]);
    else if (simpleMatch) filename = simpleMatch[1];
  }

  const blob = new Blob([response.data], { type: response.data.type });
  const link = document.createElement('a');
  link.href = window.URL.createObjectURL(blob);
  link.download = filename;
  document.body.appendChild(link);
  link.click();
  link.remove();
};
