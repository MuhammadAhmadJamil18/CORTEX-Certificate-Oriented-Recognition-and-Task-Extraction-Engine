import React, { useEffect, useState } from 'react';
import $ from 'jquery';
import 'datatables.net-dt/css/dataTables.dataTables.min.css';
import 'datatables.net';
import './Users.css';
import { toast } from 'react-toastify';
import { getUsers } from '../../api/Users/getUsers';

const AllUsersTab = () => {
   const [users, setUsers] = useState([]);

   useEffect(() => {
      getUsers(sessionStorage.getItem('token'))
         .then((response) => {
            if (response.status === 200) {
               setUsers(response.data);
            } else {
               toast.error("Unable to fetch users.");
            }
         })
         .catch((error) => {
            console.log(error);
            toast.error("An error occurred. Please check console.");
         });
   }, []);

   const viewUser = (id) => {
      const user = users.find((user) => user.userId === id);
      sessionStorage.setItem('editUser', JSON.stringify(user));

      window.location.href = `?tab=edit-user`;
   };

   useEffect(() => {
      window.viewUser = viewUser;

      const table = $('#usersTable').DataTable({
         data: users,
         columns: [
            { data: null, render: (data, type, row, meta) => meta.row + 1 }, // S. No.
            { data: 'userName' }, // Name
            { data: 'userEmail' }, // Email
            {
               data: 'userRole', // Role column
               render: (data) => {
                  return data === 1 ? 'ADMIN' : 'SALESMAN';
               }
            },
            {
               data: 'isActive', // Status column
               render: (data) => {
                  return data ? 
                     `<button style="background-color: forestgreen; width: 100%; color: black; border: none; padding: 5px 10px; border-radius: 4px; cursor: pointer;" ">ACTIVE</button>` : 
                     `<button style="background-color: indianred; width: 100%; color: white; border: none; padding: 5px 10px; border-radius: 4px; cursor: pointer;" ">BLOCKED</button>`;
               }
            },
            {
               data: 'userId',
               render: (data) => ` <button style="background-color: #007bff; width: 100%, color: white; border: none; padding: 5px 10px; border-radius: 4px; cursor: pointer;" onclick="viewUser(${data})">View</button>`
            } // View button
         ],
         destroy: true // Ensures the table is re-initialized on data change
      });

      return () => {
         if ($.fn.DataTable.isDataTable('#usersTable')) {
            $('#usersTable').DataTable().clear().destroy();
         }
         delete window.viewUser;
      };
   }, [users]);

   return (
      <div className="notify-tab">
         <table id="usersTable" className="display" style={{ marginTop: '25px' }}>
            <thead>
               <tr style={{ backgroundColor: '#007bff', color: 'white' }}>
                  <th>S. No.</th> 
                  <th>Name</th>
                  <th>Email</th>
                  <th>Role</th>
                  <th>Status</th>
                  <th>View</th>
               </tr>
            </thead>
            <tbody></tbody>
         </table>
      </div>
   );
};

export default AllUsersTab;
