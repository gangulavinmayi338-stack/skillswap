import { Link, useNavigate } from "react-router-dom";
import "./Navbar.css";
function Navbar(){
  const navigate=useNavigate();
  const userId=localStorage.getItem("userId");
  const raw=localStorage.getItem("skillswapUser");
  const user=raw?JSON.parse(raw):null;
  const logout=()=>{localStorage.removeItem("userId");localStorage.removeItem("skillswapUser");navigate("/login");};
  return <nav className="navbar">
    <div className="navbar-logo"><Link to="/">SkillSwap</Link></div>
    {userId?<div className="navbar-links logged-in-links">
      <Link to={user?.role==="ADMIN"?"/admin":"/dashboard"}>{user?.role==="ADMIN"?"Admin":"Dashboard"}</Link>
      {user?.role!=="ADMIN"&&<><Link to="/matches">Matches</Link><Link to="/requests">Requests</Link><Link to="/messages">Messages</Link><Link to="/sessions">Sessions</Link><Link to="/availability">Availability</Link><Link to="/progress">Progress</Link><Link to="/profile">Profile</Link></>}
      <div className="navbar-user">{user?.name||"Student"}</div><button className="logout-btn" onClick={logout}>Logout</button>
    </div>:<div className="navbar-links"><Link to="/">Home</Link><Link to="/login">Login</Link><Link to="/register" className="register-btn">Register</Link></div>}
  </nav>;
}
export default Navbar;
