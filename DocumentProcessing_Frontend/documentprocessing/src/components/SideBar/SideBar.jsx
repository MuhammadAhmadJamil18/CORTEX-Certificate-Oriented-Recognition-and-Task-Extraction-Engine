import configuration from '../../config/configuration.json';

const SideBar = (props) => {
  return (
    <aside className="left-sidebar">
      <div>
        <div className="brand-logo d-flex align-items-center justify-content-between">
          <a href="/" className="text-nowrap logo-img">
            <img src="/logo/qualitydxbLogo.png" width="180" alt="" />
          </a>
          <div className="close-btn d-xl-none d-block sidebartoggler cursor-pointer" id="sidebarCollapse">
            <i className="ti ti-x fs-8"></i>
          </div>
        </div>

        {props.properties.isAdmin && (
          <nav className="sidebar-nav scroll-sidebar" data-simplebaxr="">
            <ul id="sidebarnav">
              <li className="nav-small-cap">
                <i className="ti ti-dots nav-small-cap-icon fs-4"></i>
                <span className="hide-menu">Home</span>
              </li>
              <li className="sidebar-item">
                <a className="sidebar-link" href={configuration.documentprocessing.routes.PORTAL.DASHBOARD} aria-expanded="false">
                  <span>
                    <i className="ti ti-layout-dashboard"></i>
                  </span>
                  <span className="hide-menu">Dashboard</span>
                </a>
              </li>
              <li className="nav-small-cap">
                <i className="ti ti-dots nav-small-cap-icon fs-4"></i>
                <span className="hide-menu">Process</span>
              </li>
              <li className="sidebar-item">
                <a className="sidebar-link" href={configuration.documentprocessing.routes.PORTAL.PROCESS} aria-expanded="false">
                  <span><i className="ti ti-login"></i></span>
                  <span className="hide-menu">Process Document</span>
                </a>
              </li>
              <li className="nav-small-cap">
                <i className="ti ti-dots nav-small-cap-icon fs-4"></i>
                <span className="hide-menu">Notifications</span>
              </li>
              <li className="sidebar-item">
                <a className="sidebar-link" href={`${configuration.documentprocessing.routes.PORTAL.NOTIFICATION}`} aria-expanded="false">
                  <span><i className="ti ti-login"></i></span>
                  <span className="hide-menu">Notify</span>
                </a>
              </li>
              <li className="sidebar-item">
                <a className="sidebar-link" href={`${configuration.documentprocessing.routes.PORTAL.MYNOTIFICATIONS}`} aria-expanded="false">
                  <span><i className="ti ti-user-plus"></i></span>
                  <span className="hide-menu">My Notifications</span>
                </a>
              </li>
              <li className="nav-small-cap">
                <i className="ti ti-dots nav-small-cap-icon fs-4"></i>
                <span className="hide-menu">Reports</span>
              </li>
              <li className="sidebar-item">
                <a className="sidebar-link" href={`${configuration.documentprocessing.routes.PORTAL.REPORTS}`} aria-expanded="false">
                  <span><i className="ti ti-login"></i></span>
                  <span className="hide-menu">Reports</span>
                </a>
              </li>
              <li className="nav-small-cap">
                <i className="ti ti-dots nav-small-cap-icon fs-4"></i>
                <span className="hide-menu">Users</span>
              </li>
              <li className="sidebar-item">
                <a className="sidebar-link" href={`${configuration.documentprocessing.routes.PORTAL.USERS}`}>
                  <span><i className="ti ti-login"></i></span>
                  <span className="hide-menu">Users</span>
                </a>
              </li>
              <li className="sidebar-item">
                <a className="sidebar-link" href={`${configuration.documentprocessing.routes.PORTAL.USERS}?tab=add-user`}>
                  <span><i className="ti ti-login"></i></span>
                  <span className="hide-menu">Add Users</span>
                </a>
              </li>
              <li className="nav-small-cap">
                <i className="ti ti-dots nav-small-cap-icon fs-4"></i>
                <span className="hide-menu">Account</span>
              </li>
              <li className="sidebar-item">
                <a className="sidebar-link" href={configuration.documentprocessing.routes.PORTAL.PROFILE} aria-expanded="false">
                  <span><i className="ti ti-article"></i></span>
                  <span className="hide-menu">Profile</span>
                </a>
              </li>
              <li className="sidebar-item">
                <a className="sidebar-link" href={configuration.documentprocessing.routes.PORTAL.SETTINGS} aria-expanded="false">
                  <span><i className="ti ti-alert-circle"></i></span>
                  <span className="hide-menu">Settings</span>
                </a>
              </li>
              <li className="nav-small-cap">
                <i className="ti ti-dots nav-small-cap-icon fs-4"></i>
                <span className="hide-menu">OTHERS</span>
              </li>
              <li className="sidebar-item">
                <a className="sidebar-link" href={configuration.documentprocessing.routes.USER.LOGOUT} aria-expanded="false">
                  <span><i className="ti ti-aperture"></i></span>
                  <span className="hide-menu">Log Out</span>
                </a>
              </li>
            </ul>
          </nav>
        )}

        {!props.properties.isAdmin && (
          <nav className="sidebar-nav scroll-sidebar" data-simplebaxr="">
            <ul id="sidebarnav">
              <li className="nav-small-cap">
                <i className="ti ti-dots nav-small-cap-icon fs-4"></i>
                <span className="hide-menu">Home</span>
              </li>
              <li className="sidebar-item">
                <a className="sidebar-link" href={configuration.documentprocessing.routes.PORTAL.DASHBOARD} aria-expanded="false">
                  <span>
                    <i className="ti ti-layout-dashboard"></i>
                  </span>
                  <span className="hide-menu">Dashboard</span>
                </a>
              </li>
              <li className="nav-small-cap">
                <i className="ti ti-dots nav-small-cap-icon fs-4"></i>
                <span className="hide-menu">Process</span>
              </li>
              <li className="sidebar-item">
                <a className="sidebar-link" href={configuration.documentprocessing.routes.PORTAL.PROCESS} aria-expanded="false">
                  <span><i className="ti ti-login"></i></span>
                  <span className="hide-menu">Process Document</span>
                </a>
              </li>
              <li className="nav-small-cap">
                <i className="ti ti-dots nav-small-cap-icon fs-4"></i>
                <span className="hide-menu">Notifications</span>
              </li>
              <li className="sidebar-item">
                <a className="sidebar-link" href={`${configuration.documentprocessing.routes.PORTAL.MYNOTIFICATIONS}`} aria-expanded="false">
                  <span><i className="ti ti-user-plus"></i></span>
                  <span className="hide-menu">My Notifications</span>
                </a>
              </li>
              <li className="nav-small-cap">
                <i className="ti ti-dots nav-small-cap-icon fs-4"></i>
                <span className="hide-menu">Reports</span>
              </li>
              <li className="sidebar-item">
                <a className="sidebar-link" href={`${configuration.documentprocessing.routes.PORTAL.REPORTS}`} aria-expanded="false">
                  <span><i className="ti ti-login"></i></span>
                  <span className="hide-menu">Reports</span>
                </a>
              </li>
              <li className="nav-small-cap">
                <i className="ti ti-dots nav-small-cap-icon fs-4"></i>
                <span className="hide-menu">Account</span>
              </li>
              <li className="sidebar-item">
                <a className="sidebar-link" href={configuration.documentprocessing.routes.PORTAL.PROFILE} aria-expanded="false">
                  <span><i className="ti ti-article"></i></span>
                  <span className="hide-menu">Profile</span>
                </a>
              </li>
              <li className="nav-small-cap">
                <i className="ti ti-dots nav-small-cap-icon fs-4"></i>
                <span className="hide-menu">OTHERS</span>
              </li>
              <li className="sidebar-item">
                <a className="sidebar-link" href={configuration.documentprocessing.routes.USER.LOGOUT} aria-expanded="false">
                  <span><i className="ti ti-aperture"></i></span>
                  <span className="hide-menu">Log Out</span>
                </a>
              </li>
            </ul>
          </nav>
        )}
      </div>
    </aside>
  );
};

export default SideBar;