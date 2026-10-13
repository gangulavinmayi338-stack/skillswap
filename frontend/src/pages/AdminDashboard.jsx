import { useEffect, useState } from "react";
import { useNavigate } from "react-router-dom";
import Navbar from "../components/Navbar";
import api from "../services/api";
import "./AdminDashboard.css";

function AdminDashboard(){
  const navigate=useNavigate();
  const raw=localStorage.getItem("skillswapUser");
  const current=raw?JSON.parse(raw):null;
  const [data,setData]=useState(null);
  const [error,setError]=useState("");
  useEffect(()=>{
    if(current?.role!=="ADMIN"){navigate("/dashboard");return;}
    api.get("/admin/dashboard").then(r=>setData(r.data)).catch(()=>setError("Unable to load admin dashboard."));
  },[]);
  return <div className="admin-page"><Navbar/><main className="admin-container">
    <div className="admin-hero"><div><p>Administration</p><h1>SkillSwap Admin Dashboard</h1><span>Platform overview for your final project demonstration.</span></div></div>
    {error&&<div className="admin-error">{error}</div>}
    {!data?<div className="admin-loading">Loading platform data...</div>:<>
      <section className="admin-stats">
        <Stat label="Students" value={data.totalUsers}/><Stat label="Skills" value={data.totalSkills}/><Stat label="Sessions" value={data.totalSessions}/><Stat label="Completed" value={data.completedSessions}/><Stat label="Connections" value={data.activeConnections}/><Stat label="Quiz Attempts" value={data.quizAttempts}/>
      </section>
      <section className="admin-panel"><div className="admin-panel-title"><h2>Registered Users</h2><span>{data.users?.length||0} accounts</span></div><div className="table-wrap"><table><thead><tr><th>Name</th><th>Email</th><th>Department</th><th>Role</th><th>Rating</th></tr></thead><tbody>{data.users?.map(u=><tr key={u.id}><td>{u.name}</td><td>{u.email}</td><td>{u.department||"—"}</td><td><span className={`role-pill ${String(u.role).toLowerCase()}`}>{u.role||"USER"}</span></td><td>{Number(u.averageRating||0).toFixed(1)}</td></tr>)}</tbody></table></div></section>
      <section className="admin-panel"><div className="admin-panel-title"><h2>All Learning Sessions</h2><span>Teacher → Learner</span></div><div className="table-wrap"><table><thead><tr><th>Skill</th><th>Teacher</th><th>Learner</th><th>Date</th><th>Status</th></tr></thead><tbody>{data.sessions?.map(s=><tr key={s.id}><td>{s.skill?.skillName}</td><td>{s.teacher?.name}</td><td>{s.learner?.name}</td><td>{s.sessionDate} {s.startTime}</td><td><span className={`status-pill ${String(s.status).toLowerCase()}`}>{s.status}</span></td></tr>)}</tbody></table></div></section>
    </>}
  </main></div>;
}
function Stat({label,value}){return <div className="admin-stat"><span>{label}</span><strong>{value??0}</strong></div>}
export default AdminDashboard;
