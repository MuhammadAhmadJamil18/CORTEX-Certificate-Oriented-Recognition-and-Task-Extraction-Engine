import React, { useEffect, useState, useRef } from 'react';
import Chart from 'chart.js/auto';
import { toast } from 'react-toastify';

import { getStatistics } from '../../api/Reports/Statistics';

const Statistics = () => {
  const [chartData, setChartData] = useState({
    labels: [],
    datasets: [{
      label: 'Documents Processed',
      data: [],
      borderColor: '#5D87FF',
      backgroundColor: 'rgba(22, 137, 245, 0.2)',
      fill: true,
    }]
  });
  const [chartType, setChartType] = useState('line');
  const chartRef = useRef(null);

  useEffect(() => {
    const ctx = document.getElementById('processingChart').getContext('2d');

    const fetchDataAndInitializeChart = async () => {
      try {
        const response = await getStatistics(sessionStorage.getItem('token'));
        if (response.status === 200) {
          const { months, values } = response.data.statistics;

          const maxValue     = Math.max(...values);
          const suggestedMax = Math.ceil(maxValue * 1.1);

          setChartData({
            labels: months,
            datasets: [{
              label: 'Documents Processed',
              data: values,
              borderColor: '#5D87FF',
              backgroundColor: 'rgba(22, 137, 245, 0.2)',
              fill: true,
            }]
          });

          if (chartRef.current) {
            chartRef.current.destroy();
          }

          const config = {
            type: chartType,
            data: {
              labels: months,
              datasets: [{
                label: 'Documents Processed',
                data: values,
                borderColor: '#5D87FF',
                backgroundColor: 'rgba(22, 137, 245, 0.2)',
                fill: true,
              }]
            },
            options: {
              responsive: true,
              maintainAspectRatio: false,
              plugins: {
                legend: { display: true, position: 'top' },
                tooltip:{ enabled: true }
              },
              scales: {
                x: {
                  display: true,
                  title: { display: true, text: 'Months' }
                },
                y: {
                  display: true,
                  title: { display: true, text: 'Documents Processed' },
                  suggestedMin: 0,
                  suggestedMax
                }
              }
            }
          };

          chartRef.current = new Chart(ctx, config);
        } else {
          toast.error('Unable to fetch statistics.');
        }
      } catch (error) {
        console.error(error);
        toast.error('An error occurred. Please check console.');
      }
    };

    fetchDataAndInitializeChart();

    return () => {
      if (chartRef.current) {
        chartRef.current.destroy();
      }
    };
  }, [chartType]);

  const handleChartTypeChange = (newType) => {
    setChartType(newType);
  };

  return (
    <div className="body-wrapper">
      <div className="container-fluid">
        <div className="card">
          <div className="card-header d-flex justify-content-between align-items-center">
            <h5 className="mb-0">Processing Overview</h5>
            <div className="btn-group">
              <button
                onClick={() => handleChartTypeChange('line')}
                className={`btn ${chartType === 'line' ? 'btn-primary' : 'btn-outline-primary'}`}
              >
                Line
              </button>
              <button
                onClick={() => handleChartTypeChange('bar')}
                className={`btn ${chartType === 'bar' ? 'btn-primary' : 'btn-outline-primary'}`}
              >
                Bar
              </button>
            </div>
          </div>
          <div className="card-body" style={{ height: '60vh' }}>
            <canvas
              id="processingChart"
              style={{ width: '100%', height: '100%' }}
            />
          </div>
        </div>
      </div>
    </div>
  );
};

export default Statistics;
