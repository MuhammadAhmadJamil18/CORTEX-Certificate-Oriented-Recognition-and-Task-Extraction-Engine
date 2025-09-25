import React, { useState, useEffect } from "react";
import { toast }           from "react-toastify";
import { FaEye, FaEyeSlash, FaKey, FaUserCircle } from "react-icons/fa";

import "./Profile.css";
import { FetchProfile }   from "../../api/Profile/FetchProfile";
import { ChangePassword } from "../../api/Profile/ChangePassword";

/* ─────────── helper ─────────── */
const strength = pwd => {
  if (!pwd) return 0;
  let s = 0;
  if (pwd.length >= 8) s++;
  if (/[A-Z]/.test(pwd)) s++;
  if (/[a-z]/.test(pwd)) s++;
  if (/[0-9]/.test(pwd)) s++;
  if (/[^A-Za-z0-9]/.test(pwd)) s++;
  return Math.min(s, 4);
};

const Profile = () => {
  const [user, setUser]         = useState(null);
  const [loading, setLoading]   = useState(true);

  const [editing, setEditing]   = useState(false);
  const [currPw, setCurrPw]     = useState("");
  const [newPw,  setNewPw]      = useState("");
  const [confPw, setConfPw]     = useState("");
  const [showPw, setShowPw]     = useState(false);
  const [isSaving, setIsSaving] = useState(false);

  /* ─────── fetch profile once ─────── */
  useEffect(() => {
    FetchProfile(sessionStorage.getItem("token"))
      .then(r => {
        if (r.status === 200) setUser(r.data);
        else toast.error("Unable to load profile");
      })
      .catch(e => {
        console.error(e);
        toast.error("Error loading profile");
      })
      .finally(() => setLoading(false));
  }, []);

  /* ─────── handlers ─────── */
  const resetForm = () => {
    setCurrPw(""); setNewPw(""); setConfPw("");
    setEditing(false); setShowPw(false);
  };

  const savePassword = () => {
    if (!currPw || !newPw || !confPw) {
      toast.warn("All fields are required"); return;
    }
    if (newPw !== confPw) {
      toast.warn("Passwords do not match"); return;
    }

    setIsSaving(true);
    ChangePassword(
      { currentPassword: currPw, newPassword: newPw, confirmPassword: confPw },
      sessionStorage.getItem("token"))
      .then(r => {
        setIsSaving(false);
        if (r.status === 200 && r.data.messageCode === 200) {
          toast.success("Password changed");
          resetForm();
        } else {
          toast.error(r.data.message || "Unable to change password");
        }
      })
      .catch(e => {
        setIsSaving(false);
        console.error(e);
        toast.error("Error changing password");
      });
  };

  /* ─────── render ─────── */
  if (loading) {
    return (
      <div className="body-wrapper">
        <div className="container-fluid">
          <div className="skeleton-card shimmer" />
        </div>
      </div>
    );
  }

  if (!user) return null;

  const pwStrength = strength(newPw);

  return (
    <div className="body-wrapper">
      <div className="container-fluid">

        {/* ── info ─────────────────────────────────────────────── */}
        <div className="card profile-card fade-in">
          <div className="info-grid">
            <div className="avatar-col">
              <FaUserCircle className="avatar-icon" />
              <h4 className="user-name">{user.userName}</h4>
              <span className={`status-dot ${user.isActive ? "online" : "offline"}`} />
            </div>

            <div className="details-col">
              <div className="detail-row">
                <span>Email:</span><b>{user.userEmail}</b>
              </div>
              <div className="detail-row">
                <span>Role:</span>
                <b>{user.userRole === 1 ? "Admin" : "Salesman"}</b>
              </div>
              <div className="detail-row">
                <span>Status:</span>
                <b className={user.isActive ? "text-success" : "text-danger"}>
                  {user.isActive ? "Active" : "Inactive"}
                </b>
              </div>
              <div className="joined-row">
                Joined&nbsp;
                {new Date(user.createdAt).toLocaleDateString(undefined,
                  { year: "numeric", month: "short", day: "numeric" })}
              </div>
            </div>
          </div>
        </div>

        {/* ── password ─────────────────────────────────────────── */}
        <div className="card profile-card fade-in">
          <div className="pw-header">
            <h3><FaKey />&nbsp;Password</h3>
            {!editing && (
              <button className="btn btn-outline-primary" onClick={()=>setEditing(true)}>
                Change
              </button>
            )}
          </div>

          {editing && (
            <div className="pw-form">
              {["Current password","New password","Confirm new password"].map((ph,idx)=>(
                <div key={ph} className="input-group">
                  <input
                    type={showPw ? "text" : "password"}
                    placeholder={ph}
                    value={idx===0?currPw:idx===1?newPw:confPw}
                    onChange={e=>{
                      idx===0?setCurrPw(e.target.value):
                      idx===1?setNewPw(e.target.value):setConfPw(e.target.value);
                    }}
                  />
                  {idx>0 && idx<3 && (
                    <button
                      className="toggle-visibility"
                      onClick={()=>setShowPw(!showPw)}
                      type="button">
                      {showPw ? <FaEyeSlash /> : <FaEye />}
                    </button>
                  )}
                </div>
              ))}

              {/* strength meter */}
              {newPw && (
                <div className="strength">
                  <div className={`bar lvl-${pwStrength}`} />
                  <small>
                    {["Weak","Fair","Good","Strong","Excellent"][pwStrength]}
                  </small>
                </div>
              )}

              <div className="pw-actions">
                <button
                  className="btn btn-success"
                  disabled={isSaving}
                  onClick={savePassword}>
                  {isSaving ? "Saving…" : "Save"}
                </button>
                <button
                  className="btn btn-secondary"
                  disabled={isSaving}
                  onClick={resetForm}>
                  Cancel
                </button>
              </div>
            </div>
          )}
        </div>

      </div>
    </div>
  );
};

export default Profile;
