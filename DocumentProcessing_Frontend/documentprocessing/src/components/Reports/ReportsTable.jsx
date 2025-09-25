// src/components/ReportsTable.jsx
import React, { useEffect, useState } from 'react';
import $ from 'jquery';
import 'datatables.net-dt/css/dataTables.dataTables.min.css';
import 'datatables.net';
import RightBar from '../SideBar/RightBar';
import { downloadDocument } from '../../api/Reports/Download';

const ReportsTable = ({ reports }) => {
  const [processedBys, setProcessedBys]     = useState([]);
  const [salesPersons, setSalesPersons]     = useState([]);
  const [isSidebarOpen, setIsSidebarOpen]   = useState(false);
  const [sideBarData, setSideBarData]       = useState({});

  useEffect(() => {
    // extract unique dropdown values
    const pb = new Set(), sp = new Set();
    reports.forEach(r => {
      if (r.userName)        pb.add(r.userName);
      if (r.salesPersonName) sp.add(r.salesPersonName);
    });
    setProcessedBys([...pb].sort());
    setSalesPersons([...sp].sort());
  }, [reports]);

  useEffect(() => {
    window.openSidebar = id => {
      setSideBarData(reports.find(r => r.documentId === id) || {});
      setIsSidebarOpen(true);
      $('#sidebar').css('right', '0');
    };
    window.downloadDocument = id =>
      downloadDocument(id, sessionStorage.getItem('token'))
        .catch(_ => console.error("download failed"));

    // figure out all the dynamic keys
    const allKeys = new Set();
    reports.forEach(r => {
      // console.log('searchResultList for document', r.documentId, r.searchResultList);
      (r.searchResultList || []).forEach(sr => {
        allKeys.add(sr.searchKey);
      });
    });
    const keys = [...allKeys].sort();

    // build DataTables columns
    const columns = [
      { data: 'autoId',          title: 'S. No.' },
      { data: 'documentName',    title: 'Document Name (ID)' },
      { data: 'userName',        title: 'Processed By' },
      { data: 'salesPersonName', title: 'Sales Person' },
      ...keys.map(k => ({ data: k, title: k })),
      {
        data: null,
        title: 'View',
        render: d =>
          `<button
              style="background:#007bff;color:#fff;padding:5px 10px;border:none;border-radius:4px;cursor:pointer"
              onclick="openSidebar(${d.documentId})">View</button>`
      },
      {
        data: null,
        title: 'Download',
        render: d =>
          `<button
              style="background:#28a745;color:#fff;padding:5px 10px;border:none;border-radius:4px;cursor:pointer"
              onclick="downloadDocument(${d.documentId})">Download</button>`
      }
    ];

    // build the flat row data, making sure to stringify any objects
    const data = reports.map(r => {
      const row = {
        autoId:           r.autoId,
        documentName:     `${r.documentName} (${r.documentId})`,
        documentId:       r.documentId,
        userName:         r.userName || '',
        salesPersonName:  r.salesPersonName || ''
      };

      (r.searchResultList || []).forEach(sr => {
        let v = sr.searchResult;
        if (v !== null && typeof v === 'object') {
          // try common properties
          v = v.value ?? v.text ?? JSON.stringify(v);
        }
        row[sr.searchKey] = v ?? 'N/A';
      });

      return row;
    });

    const $tbl = $('#reportsTable');
    if ($.fn.DataTable.isDataTable($tbl)) {
      $tbl.DataTable().clear().destroy();
    }

    $tbl.DataTable({
      data, columns,
      paging:    false,
      searching: false,
      info:      false,
      destroy:   true,
      scrollX:   true
    });

    // custom dropdown filters
    $.fn.dataTable.ext.search.push((_, __, ___, rowData) => {
      const byPB = $('#filterProcessedBy').val();
      const bySP = $('#filterSalesPerson').val();
      if (byPB && rowData.userName        !== byPB) return false;
      if (bySP && rowData.salesPersonName !== bySP) return false;
      return true;
    });
    $('#filterProcessedBy,#filterSalesPerson')
      .off('change')
      .on('change', () => $tbl.DataTable().draw());

    return () => {
      $.fn.dataTable.ext.search.pop();
      if ($.fn.DataTable.isDataTable($tbl)) {
        $tbl.DataTable().clear().destroy();
      }
      delete window.openSidebar;
      delete window.downloadDocument;
    };
  }, [reports]);

  return (
    <div>
      {/* <div style={{ marginBottom: 10 }}>
        <label style={{ marginRight: 20 }}>
          Processed By:&nbsp;
          <select id="filterProcessedBy">
            <option value="">All</option>
            {processedBys.map(u => <option key={u} value={u}>{u}</option>)}
          </select>
        </label>
        <label>
          Sales Person:&nbsp;
          <select id="filterSalesPerson">
            <option value="">All</option>
            {salesPersons.map(s => <option key={s} value={s}>{s}</option>)}
          </select>
        </label>
      </div> */}

      <div style={{ overflowX: 'auto' }}>
        <table
          id="reportsTable"
          className="display"
          style={{ minWidth: 800, width: '100%' }}
        >
          <thead>
            <tr style={{ background: '#007bff', color: '#fff' }}>
              <th>S. No.</th>
              <th>Document Name (ID)</th>
              <th>Processed By</th>
              <th>Sales Person</th>
              {/* the dynamic search-term columns get injected here by DataTable */}
              <th>View</th>
              <th>Download</th>
            </tr>
          </thead>
          <tbody />
        </table>
      </div>

      <RightBar
        isOpen={isSidebarOpen}
        data={sideBarData}
        onClose={() => setIsSidebarOpen(false)}
        type="report"
      />
    </div>
  );
};

export default ReportsTable;
