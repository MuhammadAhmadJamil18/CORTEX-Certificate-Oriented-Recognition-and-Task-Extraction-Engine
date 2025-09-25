import React, { useEffect, useState } from 'react';
import './Tabs.css';

const Tabs = ({ tabs, selectedTab }) => {
   const [activeTab, setActiveTab] = useState(0);

   useEffect(() => {
      setActiveTab(selectedTab);
   }, [selectedTab]);

   // Function to change the active tab
   const handleTabClick = (index) => {
      setActiveTab(index);
   };

   return (
      <div>
         <div className="tab-buttons tab-container">
            {tabs.map((tab, index) => (
               <button
                  key={index}
                  onClick={() => handleTabClick(index)}
                  className={activeTab === index ? 'active' : ''}
               >
                  {tab.name}
               </button>
            ))}
         </div>
         <div className="tab-content">
            {tabs[activeTab] && tabs[activeTab].content}
         </div>
      </div>
   );
};

export default Tabs;
