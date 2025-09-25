import React, { useEffect } from "react";

import Tabs from "../Tabs/Tabs";
import AllUsersTab from "./AllUsersTab";
import UsersTab from "./UserTab";

const Notification = () => {
   
    const [activeTab, setActiveTab] = React.useState(0);
    useEffect(() => {
        const queryString = window.location.search;
        const urlParams = new URLSearchParams(queryString);
        const type = urlParams.get('tab');

       if (type === 'add-user') {
            setActiveTab(2);
        } else if (type === 'edit-user') {
            setActiveTab(1);
        }
    }, []);

    
    const tabsData = [
        { name: 'Users', content: <div><AllUsersTab /> </div> },
        { name: 'Edit User', content: <div><UsersTab editUser={true} /></div> },
        { name: 'Add User', content: <div><UsersTab editUser={false}/></div> },
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
