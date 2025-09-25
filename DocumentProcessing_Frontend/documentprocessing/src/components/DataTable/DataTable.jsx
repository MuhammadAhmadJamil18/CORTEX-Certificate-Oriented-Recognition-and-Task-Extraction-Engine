// import React, { useEffect, useRef } from 'react';
// import $ from 'jquery';
// import 'datatables.net';



// const DataTable = ({ columns, data }) => {
//     const tableRef = useRef();

//     useEffect(() => {
//         $(tableRef.current).DataTable({
//             data: data,
//             columns: columns,
//             destroy: true,
//         });

//         return () => {
//             $(tableRef.current).DataTable().destroy(true);
//         };
//     }, [data, columns]);

//     return (
//         <div>
//             <table ref={tableRef} className="display" style={{ width: '100%' }}></table>
//         </div>
//     );
// };

// export default DataTable;
