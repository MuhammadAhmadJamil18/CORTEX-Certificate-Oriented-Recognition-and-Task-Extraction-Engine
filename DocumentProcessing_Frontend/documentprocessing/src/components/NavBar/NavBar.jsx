import React, { useState, useEffect } from 'react';
import { Stomp } from "@stomp/stompjs";
import SockJS from "sockjs-client";
import configuration from '../../config/configuration.json';

import NotificationBox from '../NotificationBox/NotificationBox';

const NavBar = () => {
    const [notifications, setNotifications] = useState(() => {
        return JSON.parse(sessionStorage.getItem("notifications")) || [];
    });
    const [stompClient, setStompClient] = useState(null);
    const [isOpen, setIsOpen] = useState(false);

    const toggleDropdown = () => {
        setIsOpen(!isOpen);
    };

    useEffect(() => {
        const userId = sessionStorage.getItem("userId");
        if (!userId) return;
        const socket = new SockJS(`${configuration.documentprocessing.urls.backend}/socket/notification`);
        const stompClientInstance = Stomp.over(socket);
        stompClientInstance.connect({}, () => {
            stompClientInstance.subscribe(`/socket/notification/${userId}`, (message) => {
                const result = JSON.parse(message.body);
                setNotifications((prevNotifications) => {
                    const isDuplicate = prevNotifications.some(
                        (notification) => notification.id === result.notificationId
                    );
                    if (isDuplicate) {
                        return prevNotifications;
                    }
                    const updatedNotifications = [
                        {
                            id: result.notificationId,
                            subject: result.notificationSubject,
                            message: result.notificationMessage,
                            time: result.scheduleTime,
                        },
                        ...prevNotifications,
                    ];
                    sessionStorage.setItem("notifications", JSON.stringify(updatedNotifications));
                    return updatedNotifications;
                });
            });
        });
        setStompClient(stompClientInstance);
        return () => {
            if (stompClientInstance.connected) {
                stompClientInstance.disconnect();
            }
        };
    }, []);
    

    return (
        <div className="body-wrapper">
            <header className="app-header">
                <nav className="navbar navbar-expand-lg navbar-light">
                    <ul className="navbar-nav">
                        <li className="nav-item d-block d-xl-none"></li>
                    </ul>
                    <div className="navbar-collapse justify-content-end px-0" id="navbarNav">
                        <ul className="navbar-nav flex-row ms-auto align-items-center justify-content-end">
                            <li className="nav-item dropdown">
                                <a className="nav-link nav-icon-hover" onClick={toggleDropdown} aria-expanded="false">
                                    <img src="/icons/notificationBox.png" alt="" width="50" height="50" className="rounded-circle" />
                                    {notifications.length > 0 && (
                                        <span className="notification-count">{notifications.length}</span>
                                    )}
                                </a>
                            </li>
                            <li className="nav-item dropdown">
                                <a className="nav-link nav-icon-hover" href={`${configuration.documentprocessing.routes.PORTAL.PROFILE}`} aria-expanded="false">
                                    <img src="/images/user.png" alt="" width="35" height="35" className="rounded-circle" />
                                </a>
                            </li>
                        </ul>
                    </div>
                </nav>

                {isOpen && (
                   <div className="notification-box-wrapper">
                       <NotificationBox notifications={notifications} />
                   </div>
                )}
            </header>
        </div>
    );
};

export default NavBar;
