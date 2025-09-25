import React, { useState, useEffect } from "react";
import '../Tabs/Tabs.css';
import { toast } from 'react-toastify';
import ReportsTable from "./ReportsTable";
import * as XLSX from 'xlsx';

import { Fetch } from "../../api/Reports/Fetch";
import { Filters } from "../../api/Reports/Filters";

const Reports = () => {
  const [reports, setReports]           = useState([]);
  const [dateCategory, setDateCategory] = useState("processing");
  const [dateFrom,     setDateFrom]     = useState("");
  const [dateTo,       setDateTo]       = useState("");
  const [docTypes,     setDocTypes]     = useState([]);
  const [docFormats,   setDocFormats]   = useState([]);
  const [selectedTypeId,   setSelectedTypeId]   = useState(null);
  const [selectedFormatId, setSelectedFormatId] = useState(null);
  const [additionalFilters, setAdditionalFilters] = useState({
    processedBy: "all",
    salesPerson: "all"
  });
  const [pageNumber, setPageNumber] = useState(0);
  const [totalPages, setTotalPages] = useState(0);
  const [isLoading,  setIsLoading]  = useState(false);
  const [filtersExpanded, setFiltersExpanded] = useState(true);
  const [filtersChanged, setFiltersChanged] = useState(false);
  const pageSize = 20;

  // default to yesterday→today
  useEffect(() => {
    const now = new Date();
    const today = now.toISOString().slice(0,10);
    const yd = new Date(now.setDate(now.getDate()-1)).toISOString().slice(0,10);
    setDateFrom(yd);
    setDateTo(today);
  }, []);

  // load document‐type → formats
  useEffect(() => {
    Filters(sessionStorage.getItem("token"))
      .then(r => {
        if (r.status === 200) setDocTypes(r.data);
        else                  toast.error("Unable to fetch filters");
      })
      .catch(e => {
        console.error(e);
        toast.error("Error fetching filters");
      });
  }, []);

  const handleTypeChange = e => {
    const id = parseInt(e.target.value,10) || null;
    setSelectedTypeId(id);
    setSelectedFormatId(null);
    const t = docTypes.find(d => d.documentTypeId === id);
    setDocFormats(t?.documentFormats || []);
    setReports([]);
    setFiltersChanged(true);
  };

  const handleFilterChange = (setter, value) => {
    setter(value);
    setReports([]);
    setFiltersChanged(true);
  };

  const clearAllFilters = () => {
    setSelectedTypeId(null);
    setSelectedFormatId(null);
    setDocFormats([]);
    setAdditionalFilters({ processedBy: "all", salesPerson: "all" });
    const now = new Date();
    const today = now.toISOString().slice(0,10);
    const yd = new Date(now.setDate(now.getDate()-1)).toISOString().slice(0,10);
    setDateFrom(yd);
    setDateTo(today);
    setDateCategory("processing");
    setReports([]);
    setFiltersChanged(true);
  };

  const fetchReports = page => {
    setIsLoading(true);
    setFiltersChanged(false);
    const payload = {
      documentType:   selectedTypeId,
      documentFormat: selectedFormatId,
      dateCategory,
      dateFrom,
      dateTo,
      pageNumber: page,
      pageSize
    };

    Fetch(payload, sessionStorage.getItem("token"))
      .then(res => {
        setIsLoading(false);
        if (res.status === 200) {
          let data = res.data.report.map((r,i)=>({
            ...r,
            autoId: page * pageSize + i + 1
          }));
          // apply client-side dropdowns
          if (additionalFilters.processedBy !== "all") {
            data = data.filter(r => r.userName === additionalFilters.processedBy);
          }
          if (additionalFilters.salesPerson !== "all") {
            data = data.filter(r => r.salesPersonName === additionalFilters.salesPerson);
          }
          setReports(data);
          setTotalPages(res.data.totalPages);
          setPageNumber(page);
        } else {
          toast.error("Unable to generate report");
          setReports([]);
        }
      })
      .catch(e => {
        setIsLoading(false);
        console.error(e);
        toast.error("Error generating report");
      });
  };

  const handleExportExcel = () => {
    const rows = [];
    const allKeys = new Set();
    reports.forEach(r => {
      const base = {
        Serial:      r.autoId,
        Document:    r.documentName,
        DocumentId:  r.documentId,
        ProcessedBy: r.userName,
        SalesPerson: r.salesPersonName
      };
      (r.searchResultList || []).forEach(sr => {
        base[sr.searchKey] = sr.searchResult;
        allKeys.add(sr.searchKey);
      });
      rows.push(base);
    });
    const headers = ["Serial","Document","DocumentId","ProcessedBy","SalesPerson", ...[...allKeys].sort()];
    const ws = XLSX.utils.json_to_sheet(rows, { header: headers });
    const wb = XLSX.utils.book_new();
    XLSX.utils.book_append_sheet(wb, ws, "Reports");
    XLSX.writeFile(wb, `Reports_${new Date().toISOString().slice(0,10)}.xlsx`);
    toast.success("Exported Excel");
  };

  const handleExportCSV = () => {
    const rows = [];
    const allKeys = new Set();
    reports.forEach(r => {
      const base = {
        Serial:      r.autoId,
        Document:    r.documentName,
        DocumentId:  r.documentId,
        ProcessedBy: r.userName,
        SalesPerson: r.salesPersonName
      };
      (r.searchResultList || []).forEach(sr => {
        base[sr.searchKey] = sr.searchResult;
        allKeys.add(sr.searchKey);
      });
      rows.push(base);
    });
    const headers = ["Serial","Document","DocumentId","ProcessedBy","SalesPerson", ...[...allKeys].sort()];
    const csv = [
      headers.join(','),
      ...rows.map(row =>
        headers.map(h => `"${(row[h]||"").toString().replace(/"/g,'""')}"`).join(',')
      )
    ].join('\n');
    const blob = new Blob([csv], { type: "text/csv;charset=utf-8;" });
    const url = URL.createObjectURL(blob);
    const a = document.createElement('a');
    a.href = url;
    a.download = `Reports_${new Date().toISOString().slice(0,10)}.csv`;
    document.body.appendChild(a);
    a.click();
    document.body.removeChild(a);
    toast.success("Exported CSV");
  };

  const getActiveFiltersCount = () => {
    let count = 0;
    if (selectedTypeId) count++;
    if (selectedFormatId) count++;
    if (additionalFilters.processedBy !== "all") count++;
    if (additionalFilters.salesPerson !== "all") count++;
    return count;
  };

  const filterStyles = {
    container: {
      background: 'linear-gradient(135deg, #f8f9fa 0%, #e9ecef 100%)',
      border: '1px solid #dee2e6',
      borderRadius: '16px',
      padding: '24px',
      marginBottom: '24px',
      boxShadow: '0 4px 20px rgba(0,0,0,0.08)',
      transition: 'all 0.3s ease'
    },
    header: {
      display: 'flex',
      alignItems: 'center',
      justifyContent: 'space-between',
      marginBottom: filtersExpanded ? '24px' : '0',
      cursor: 'pointer',
      padding: '8px 0'
    },
    title: {
      fontSize: '20px',
      fontWeight: '600',
      color: '#495057',
      display: 'flex',
      alignItems: 'center',
      gap: '12px',
      margin: 0
    },
    badge: {
      backgroundColor: '#007bff',
      color: 'white',
      borderRadius: '12px',
      padding: '4px 12px',
      fontSize: '12px',
      fontWeight: '600',
      minWidth: '24px',
      textAlign: 'center'
    },
    toggleBtn: {
      background: 'none',
      border: 'none',
      fontSize: '18px',
      color: '#6c757d',
      cursor: 'pointer',
      padding: '4px',
      borderRadius: '4px',
      transition: 'color 0.2s ease'
    },
    gridContainer: {
      display: 'grid',
      gridTemplateColumns: 'repeat(auto-fit, minmax(280px, 1fr))',
      gap: '20px',
      marginBottom: '24px'
    },
    dateGrid: {
      display: 'grid',
      gridTemplateColumns: 'minmax(200px, 1fr) repeat(2, minmax(150px, 1fr))',
      gap: '16px',
      marginBottom: '20px'
    },
    inputGroup: {
      display: 'flex',
      flexDirection: 'column',
      gap: '8px'
    },
    label: {
      fontSize: '14px',
      fontWeight: '600',
      color: '#495057',
      display: 'flex',
      alignItems: 'center',
      gap: '8px'
    },
    input: {
      padding: '12px 16px',
      border: '2px solid #e9ecef',
      borderRadius: '12px',
      fontSize: '14px',
      transition: 'all 0.2s ease',
      backgroundColor: '#fff',
      ':focus': {
        outline: 'none',
        borderColor: '#007bff',
        boxShadow: '0 0 0 3px rgba(0,123,255,0.1)'
      }
    },
    select: {
      padding: '12px 16px',
      border: '2px solid #e9ecef',
      borderRadius: '12px',
      fontSize: '14px',
      backgroundColor: '#fff',
      cursor: 'pointer',
      transition: 'all 0.2s ease'
    },
    actionsContainer: {
      display: 'flex',
      justifyContent: 'space-between',
      alignItems: 'center',
      flexWrap: 'wrap',
      gap: '12px',
      paddingTop: '20px',
      borderTop: '1px solid #e9ecef'
    },
    actionGroup: {
      display: 'flex',
      gap: '12px',
      flexWrap: 'wrap'
    },
    btn: {
      padding: '12px 24px',
      borderRadius: '12px',
      border: 'none',
      fontSize: '14px',
      fontWeight: '600',
      cursor: 'pointer',
      transition: 'all 0.2s ease',
      display: 'flex',
      alignItems: 'center',
      gap: '8px',
      textDecoration: 'none'
    },
    btnPrimary: {
      backgroundColor: '#007bff',
      color: 'white',
      boxShadow: '0 4px 12px rgba(0,123,255,0.3)'
    },
    btnSecondary: {
      backgroundColor: '#6c757d',
      color: 'white'
    },
    btnSuccess: {
      backgroundColor: '#28a745',
      color: 'white'
    },
    btnInfo: {
      backgroundColor: '#17a2b8',
      color: 'white'
    },
    resultsHeader: {
      display: 'flex',
      justifyContent: 'space-between',
      alignItems: 'center',
      marginBottom: '20px',
      padding: '16px 20px',
      backgroundColor: '#fff',
      borderRadius: '12px',
      border: '1px solid #e9ecef',
      boxShadow: '0 2px 8px rgba(0,0,0,0.05)'
    },
    resultsTitle: {
      fontSize: '18px',
      fontWeight: '600',
      color: '#495057',
      margin: 0
    },
    resultsCount: {
      fontSize: '14px',
      color: '#6c757d',
      backgroundColor: '#f8f9fa',
      padding: '6px 12px',
      borderRadius: '8px',
      fontWeight: '500'
    },
    pagination: {
      display: 'flex',
      justifyContent: 'center',
      alignItems: 'center',
      gap: '12px',
      marginTop: '24px',
      padding: '20px',
      backgroundColor: '#fff',
      borderRadius: '12px',
      border: '1px solid #e9ecef'
    },
    pageBtn: {
      padding: '10px 16px',
      border: '2px solid #e9ecef',
      borderRadius: '8px',
      backgroundColor: '#fff',
      color: '#495057',
      cursor: 'pointer',
      transition: 'all 0.2s ease',
      fontSize: '14px',
      fontWeight: '500'
    },
    pageInfo: {
      fontSize: '14px',
      color: '#495057',
      fontWeight: '500',
      padding: '0 16px'
    }
  };

  return (
    <div className="body-wrapper">
      <div className="container-fluid">
        
        {/* Enhanced Filters Section */}
        <div style={filterStyles.container}>
          <div style={filterStyles.header} onClick={() => setFiltersExpanded(!filtersExpanded)}>
            <h2 style={filterStyles.title}>
              <span>🎛️</span>
              Report Filters
              {getActiveFiltersCount() > 0 && (
                <span style={filterStyles.badge}>{getActiveFiltersCount()}</span>
              )}
            </h2>
            <button style={filterStyles.toggleBtn}>
              {filtersExpanded ? '🔼' : '🔽'}
            </button>
          </div>

          {filtersExpanded && (
            <>
              {/* Date Filters */}
              <div style={filterStyles.dateGrid}>
                <div style={filterStyles.inputGroup}>
                  <label style={filterStyles.label}>
                    <span>📅</span>
                    Date Category
                  </label>
                  <select
                    style={filterStyles.select}
                    value={dateCategory}
                    onChange={e=>handleFilterChange(setDateCategory, e.target.value)}
                  >
                    <option value="processing">Processing Date</option>
                    <option value="inspection">Date of Inspection</option>
                    <option value="nextInspection">Next Inspection</option>
                  </select>
                </div>
                
                <div style={filterStyles.inputGroup}>
                  <label style={filterStyles.label}>
                    <span>📅</span>
                    From Date
                  </label>
                  <input
                    type="date"
                    style={filterStyles.input}
                    value={dateFrom}
                    onChange={e=>handleFilterChange(setDateFrom, e.target.value)}
                  />
                </div>
                
                <div style={filterStyles.inputGroup}>
                  <label style={filterStyles.label}>
                    <span>📅</span>
                    To Date
                  </label>
                  <input
                    type="date"
                    style={filterStyles.input}
                    value={dateTo}
                    onChange={e=>handleFilterChange(setDateTo, e.target.value)}
                  />
                </div>
              </div>

              {/* Document Filters */}
              <div style={filterStyles.gridContainer}>
                <div style={filterStyles.inputGroup}>
                  <label style={filterStyles.label}>
                    <span>📋</span>
                    Document Type
                  </label>
                  <select
                    style={filterStyles.select}
                    value={selectedTypeId||""}
                    onChange={handleTypeChange}
                  >
                    <option value="">All Document Types</option>
                    {docTypes.map(d =>
                      <option key={d.documentTypeId} value={d.documentTypeId}>
                        {d.documentTypeName}
                      </option>
                    )}
                  </select>
                </div>

                <div style={filterStyles.inputGroup}>
                  <label style={filterStyles.label}>
                    <span>📄</span>
                    Document Format
                  </label>
                  <select
                    style={filterStyles.select}
                    value={selectedFormatId||""}
                    onChange={e=>handleFilterChange(setSelectedFormatId, parseInt(e.target.value,10))}
                    disabled={!selectedTypeId}
                  >
                    <option value="">All Document Formats</option>
                    {docFormats.map(f =>
                      <option key={f.formatId} value={f.formatId}>
                        {f.formatName} ({f.fileExtension})
                      </option>
                    )}
                  </select>
                </div>

                <div style={filterStyles.inputGroup}>
                  <label style={filterStyles.label}>
                    <span>👤</span>
                    Processed By
                  </label>
                  <select
                    style={filterStyles.select}
                    value={additionalFilters.processedBy}
                    onChange={e=>{
                      setAdditionalFilters(f=>({...f, processedBy: e.target.value}));
                      setReports([]);
                      setFiltersChanged(true);
                    }}
                  >
                    <option value="all">All Processors</option>
                    {[...new Set(reports.map(r=>r.userName))].sort().map(u=>
                      <option key={u} value={u}>{u}</option>
                    )}
                  </select>
                </div>

                <div style={filterStyles.inputGroup}>
                  <label style={filterStyles.label}>
                    <span>👨‍💼</span>
                    Sales Person
                  </label>
                  <select
                    style={filterStyles.select}
                    value={additionalFilters.salesPerson}
                    onChange={e=>{
                      setAdditionalFilters(f=>({...f, salesPerson: e.target.value}));
                      setReports([]);
                      setFiltersChanged(true);
                    }}
                  >
                    <option value="all">All Sales Persons</option>
                    {[...new Set(reports.map(r=>r.salesPersonName))].sort().map(s=>
                      <option key={s} value={s}>{s}</option>
                    )}
                  </select>
                </div>
              </div>

              {/* Action Buttons */}
              <div style={filterStyles.actionsContainer}>
                <div style={filterStyles.actionGroup}>
                  <button 
                    style={{...filterStyles.btn, ...filterStyles.btnSecondary}} 
                    onClick={clearAllFilters}
                  >
                    <span>🗑️</span>
                    Clear All Filters
                  </button>
                </div>
                
                <div style={filterStyles.actionGroup}>
                  <button 
                    style={{...filterStyles.btn, ...filterStyles.btnPrimary, 
                      ...(filtersChanged ? {
                        backgroundColor: '#ffc107',
                        color: '#212529',
                        animation: 'pulse 2s infinite'
                      } : {})
                    }} 
                    onClick={()=>fetchReports(0)} 
                    disabled={isLoading}
                  >
                    <span>{isLoading ? "⏳" : filtersChanged ? "🔄" : "📊"}</span>
                    {isLoading ? "Generating..." : filtersChanged ? "Update Report" : "Generate Report"}
                  </button>
                  
                  <button 
                    style={{...filterStyles.btn, ...filterStyles.btnSuccess}} 
                    onClick={handleExportExcel} 
                    disabled={!reports.length}
                  >
                    <span>📥</span>
                    Export Excel
                  </button>
                  
                  <button 
                    style={{...filterStyles.btn, ...filterStyles.btnInfo}} 
                    onClick={handleExportCSV} 
                    disabled={!reports.length}
                  >
                    <span>💾</span>
                    Export CSV
                  </button>
                </div>
              </div>
            </>
          )}
        </div>

        {/* Filters Changed Notification */}
        {filtersChanged && reports.length === 0 && (
          <div style={{
            backgroundColor: '#fff3cd',
            border: '1px solid #ffeaa7',
            borderRadius: '12px',
            padding: '16px 20px',
            marginBottom: '20px',
            display: 'flex',
            alignItems: 'center',
            gap: '12px',
            boxShadow: '0 2px 8px rgba(255, 193, 7, 0.2)'
          }}>
            <span style={{ fontSize: '20px' }}>⚠️</span>
            <div>
              <strong style={{ color: '#856404' }}>Filters have been modified</strong>
              <p style={{ margin: '4px 0 0 0', color: '#856404', fontSize: '14px' }}>
                Please click "Update Report" to apply the new filters and see updated results.
              </p>
            </div>
          </div>
        )}

        {/* Results Section */}
        {reports.length > 0 && (
          <div className="card" style={{ borderRadius: '16px', border: '1px solid #e9ecef', boxShadow: '0 4px 20px rgba(0,0,0,0.08)' }}>
            <div style={filterStyles.resultsHeader}>
              <h3 style={filterStyles.resultsTitle}>
                📊 Report Results
              </h3>
              <span style={filterStyles.resultsCount}>
                {reports.length} record{reports.length !== 1 ? 's' : ''} found
              </span>
            </div>
            
            <div className="card-body" style={{ padding: '0' }}>
              <ReportsTable reports={reports} />
            </div>

            {/* Enhanced Pagination */}
            {totalPages > 1 && (
              <div style={filterStyles.pagination}>
                <button
                  style={{
                    ...filterStyles.pageBtn,
                    opacity: pageNumber === 0 ? 0.5 : 1,
                    cursor: pageNumber === 0 ? 'not-allowed' : 'pointer'
                  }}
                  onClick={() => fetchReports(pageNumber-1)}
                  disabled={pageNumber===0}
                >
                  ← Previous
                </button>
                
                <span style={filterStyles.pageInfo}>
                  Page {pageNumber + 1} of {totalPages}
                </span>
                
                <button
                  style={{
                    ...filterStyles.pageBtn,
                    opacity: pageNumber + 1 >= totalPages ? 0.5 : 1,
                    cursor: pageNumber + 1 >= totalPages ? 'not-allowed' : 'pointer'
                  }}
                  onClick={() => fetchReports(pageNumber+1)}
                  disabled={pageNumber+1>=totalPages}
                >
                  Next →
                </button>
              </div>
            )}
          </div>
        )}

        {/* Empty State */}
        {!isLoading && reports.length === 0 && !filtersChanged && (
          <div style={{
            textAlign: 'center',
            padding: '60px 20px',
            backgroundColor: '#fff',
            borderRadius: '16px',
            border: '1px solid #e9ecef',
            boxShadow: '0 4px 20px rgba(0,0,0,0.08)'
          }}>
            <div style={{ fontSize: '48px', marginBottom: '16px' }}>📊</div>
            <h3 style={{ color: '#6c757d', marginBottom: '8px' }}>No Reports Found</h3>
            <p style={{ color: '#adb5bd', margin: 0 }}>
              Try adjusting your filters or generate a report to see results
            </p>
          </div>
        )}

      </div>
    </div>
  );
};

export default Reports;