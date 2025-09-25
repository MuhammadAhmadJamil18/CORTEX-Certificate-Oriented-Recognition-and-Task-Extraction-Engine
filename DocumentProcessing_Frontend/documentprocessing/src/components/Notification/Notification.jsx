import React, { useEffect } from "react";

import Tabs from "../Tabs/Tabs";
import NotifyTab from "./NotifyTab";
import ScheduleTab from "./ScheduleTab";
import NotificationsByMe from "./NotificationsByMe";

const Notification = () => {
   
    const [activeTab, setActiveTab] = React.useState(0);
    
    const tabsData = [
        { name: 'Notify', content: <div><NotifyTab /></div> },
        { name: 'Schedule', content: <div><ScheduleTab /></div> },
        { name: 'My Notifications', content: <div><NotificationsByMe /></div> },
     ];

    return (
        <div className="body-wrapper">
            <div className="container-fluid">
                <div className="row">
                    <div className="col-lg-8-process d-flex align-items-stretch">
                        <div className="card w-100">
                            <div className="card-body">
                                <div>
                                {/* className="d-sm-flex d-block align-items-center justify-content-between mb-9" */}
                                    <div className="mb-3 mb-sm-0">               
                                        <Tabs tabs={tabsData} selectedTab={activeTab} />
                                    </div>
                                </div>
                            </div>
                        </div>
                    </div>
                    
                    </div>
            </div>
        </div>
    );
};

export default Notification;
