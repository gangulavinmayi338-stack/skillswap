import { BrowserRouter, Routes, Route } from "react-router-dom";
import Home from "./pages/Home";
import Login from "./pages/Login";
import Register from "./pages/Register";
import Dashboard from "./pages/Dashboard";
import AdminDashboard from "./pages/AdminDashboard";
import Matches from "./pages/Matches";
import Requests from "./pages/Requests";
import Messages from "./pages/Messages";
import Sessions from "./pages/Sessions";
import SessionRoom from "./pages/SessionRoom";
import Profile from "./pages/Profile";
import Availability from "./pages/Availability";
import Progress from "./pages/Progress";
import Settings from "./pages/Settings";
import NotFound from "./pages/NotFound";
import ProtectedRoute from "./components/ProtectedRoute";

function App(){
  return <BrowserRouter><Routes>
    <Route path="/" element={<Home/>}/>
    <Route path="/login" element={<Login/>}/>
    <Route path="/register" element={<Register/>}/>
    <Route path="/dashboard" element={<ProtectedRoute><Dashboard/></ProtectedRoute>}/>
    <Route path="/admin" element={<ProtectedRoute><AdminDashboard/></ProtectedRoute>}/>
    <Route path="/matches" element={<ProtectedRoute><Matches/></ProtectedRoute>}/>
    <Route path="/requests" element={<ProtectedRoute><Requests/></ProtectedRoute>}/>
    <Route path="/messages" element={<ProtectedRoute><Messages/></ProtectedRoute>}/>
    <Route path="/sessions" element={<ProtectedRoute><Sessions/></ProtectedRoute>}/>
    <Route path="/sessions/:sessionId/room" element={<ProtectedRoute><SessionRoom/></ProtectedRoute>}/>
    <Route path="/profile" element={<ProtectedRoute><Profile/></ProtectedRoute>}/>
    <Route path="/availability" element={<ProtectedRoute><Availability/></ProtectedRoute>}/>
    <Route path="/progress" element={<ProtectedRoute><Progress/></ProtectedRoute>}/>
    <Route path="/settings" element={<ProtectedRoute><Settings/></ProtectedRoute>}/>
    <Route path="*" element={<NotFound/>}/>
  </Routes></BrowserRouter>;
}
export default App;
