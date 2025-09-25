import React, { useState, useEffect } from 'react';
import './Users.css'; // CSS file for styling
import { toast } from 'react-toastify';
import { AddUser } from '../../api/Users/addUser';
import { UpdateUser } from '../../api/Users/updateUser';

const UsersTab = (props) => {
    const generatePassword = () => {
        const length = 12;
        const charset = "ABCDEFGHIJKLMNOPQRSTUVWXYZ012345678!@#$%&";

        let password = "";
        for (let i = 0; i < length; i++) {
            password += charset.charAt(Math.floor(Math.random() * charset.length));
        }

        return password;
    };

    // Retrieve the 'editUser' from sessionStorage and parse it
    const storedEditUser = sessionStorage.getItem('editUser');
    const [editUser, setEditUser] = useState(storedEditUser ? JSON.parse(storedEditUser) : null);

    const [formData, setFormData] = useState({
        userId:'',
        userRole: 2, 
        userEmail: '',
        userName: '',
        userPassword: generatePassword(),
        confirmUserPassword: '',
        isActive: true // Default active status
    });

    useEffect(() => {
        if (editUser) {
            setFormData({
                userId: editUser.userId || '',
                userRole: editUser.userRole || 2,
                userEmail: editUser.userEmail || '',
                userName: editUser.userName || '',
                userPassword: '',
                confirmUserPassword: '',
                isActive: editUser.isActive !== undefined ? editUser.isActive : true,
            });
        }

        sessionStorage.removeItem('editUser')

    }, [editUser]);

    const handleChange = (e) => {
        const { name, value } = e.target;
        setFormData((prevData) => ({
            ...prevData,
            [name]: value
        }));
    };

    const handleSubmit = (e) => {
        e.preventDefault();

        if(props.editUser){
            UpdateUser(formData, sessionStorage.getItem('token')).then((response) => {
                if (response.status === 200) {
                    if (response.data.messageCode === 1016) {
                        toast.success(response.data.message + " ID: " + response.data.userId);
                    } else {
                        toast.error("Code:" + response.data.messageCode + " " + response.data.message);
                    }
                } else {
                    toast.error("An error occured. Please check console.");
                 }
            }).catch((error) => {
                console.log(error);
                toast.error("An error occurred. Please check console.");
            });
        }
        else{
            if (formData.userPassword !== formData.confirmUserPassword) {
                toast.error("Passwords do not match.");
                return;
            }
    
            AddUser(formData, sessionStorage.getItem('token')).then((response) => {
                if (response.status === 200) {
                    if (response.data.messageCode === 1014) {
                        toast.success(response.data.message + " ID: " + response.data.userId);
                    } else {
                        toast.error("Code:" + response.data.messageCode + " " + response.data.message);
                    }
                }
            }).catch((error) => {
                console.log(error);
                toast.error("An error occurred. Please check console.");
            });
        }
    };

    return (
        <div className="notify-tab">
            {(!props.editUser && !editUser) ? (
                <h4 style={{ marginTop: '0px' }}>Add a User</h4>
            ) : (
                <h4 style={{ marginTop: '0px' }}>Edit User</h4>
            )}
            <form onSubmit={handleSubmit}>
                <div className="user-fields">
                    <div className="notification-fields">
                        <div className="form-group">
                            <label htmlFor="userName">Username:</label>
                            <input
                                type="text"
                                id="userName"
                                name="userName"
                                value={formData.userName}
                                onChange={handleChange}
                                required
                            />
                        </div>
                        <div className="form-group">
                            <label htmlFor="userEmail">Email Address:</label>
                            <input
                                readOnly={props.editUser ? true : false}
                                type="email"
                                id="userEmail"
                                name="userEmail"
                                value={formData.userEmail}
                                onChange={handleChange}
                                required
                            />
                        </div>
                    </div>

                    {(!props.editUser && !editUser) ? (
                        <div className="notification-fields">
                            <div className="form-group">
                                <label htmlFor="userPassword">Password:</label>
                                <input
                                    type="text"
                                    id="userPassword"
                                    name="userPassword"
                                    value={formData.userPassword}
                                    onChange={handleChange}
                                    required
                                />
                            </div>

                            <div className="form-group">
                                <label htmlFor="userPassword">Confirm Password:</label>
                                <input
                                    type="text"
                                    id="confirmUserPassword"
                                    name="confirmUserPassword"
                                    value={formData.confirmUserPassword}
                                    onChange={handleChange}
                                    required
                                />
                            </div>
                        </div>
                    ) : null}
                </div>

                <div className="notification-fields">
                    <div className="form-group">
                        <label htmlFor="userRole">User Role:</label>
                        <select
                            id="userRole"
                            name="userRole"
                            value={formData.userRole}
                            onChange={handleChange}
                            required
                        >
                            <option value="1">Admin</option>
                            <option value="2">Salesman</option>
                        </select>
                    </div>

                    <div className="form-group">
                        <label htmlFor="isActive">Status:</label>
                        <select
                            id="isActive"
                            name="isActive"
                            value={formData.isActive}
                            onChange={handleChange}
                            required
                        >
                            <option value={true}>ACTIVE</option>
                            <option value={false}>BLOCKED</option>
                        </select>
                    </div>
                </div>

                <div className="submit-container">
                    <button type="submit" className="submit-button">
                        {(!props.editUser && !editUser) ? "Add User" : "Update"}
                    </button>
                </div>
            </form>
        </div>
    );
};

export default UsersTab;
