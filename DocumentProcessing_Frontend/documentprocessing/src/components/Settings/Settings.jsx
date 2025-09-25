import React, { useState, useEffect } from "react";
import { toast } from "react-toastify";
import { FaSave, FaSyncAlt } from "react-icons/fa";

import "./Settings.css";
import { FetchSettings } from "../../api/Settings/FetchSettings";
import { SaveSettings  } from "../../api/Settings/SaveSettings";

const Settings = () => {

  /* ───────── state ───────── */
  const [form, setForm] = useState({
    daysBefore:       1,
    notificationType: 3,
    emailSubject:     "",
    emailMessage:     ""
  });
  const [loaded,   setLoaded]   = useState(false);
  const [saving,   setSaving]   = useState(false);
  const [dirty,    setDirty]    = useState(false);

  /* ───────── load once ───────── */
  useEffect(() => {
    FetchSettings(sessionStorage.getItem("token"))
      .then(r => {
        if (r.status === 200) setForm(r.data);
        else toast.error("Unable to load settings");
        setLoaded(true);
      })
      .catch(e => {
        console.error(e);
        toast.error("Error loading settings");
        setLoaded(true);
      });
  }, []);

  /* ───────── handlers ───────── */
  const handleChange = (k,v) => {
    setForm(f => ({...f, [k]:v}));
    setDirty(true);
  };

  const save = () => {
    setSaving(true);
    SaveSettings(form, sessionStorage.getItem("token"))
      .then(r => {
        setSaving(false);
        if (r.status === 200) {
          setForm(r.data);
          setDirty(false);
          toast.success("Settings saved");
        } else {
          toast.error("Unable to save settings");
        }
      })
      .catch(e => {
        setSaving(false);
        console.error(e);
        toast.error("Error saving settings");
      });
  };

  if (!loaded) {
    return (
      <div className="body-wrapper">
        <div className="container-fluid">
          <div className="skeleton-card shimmer" />
        </div>
      </div>
    );
  }

  return (
    <div className="body-wrapper">
      <div className="container-fluid">

        <div className="card settings-card fade-in">
          <h3 className="settings-title">🔔 Notification Settings</h3>

          <div className="settings-grid">

            {/* daysBefore */}
            <div className="input-col">
              <label>Days before inspection</label>
              <input type="number" min={0} max={30}
                     value={form.daysBefore}
                     onChange={e=>handleChange("daysBefore", parseInt(e.target.value,10)||0)}
              />
            </div>

            {/* notification type */}
            <div className="input-col">
              <label>Notification type</label>
              <select value={form.notificationType}
                      onChange={e=>handleChange("notificationType", parseInt(e.target.value,10))}>
                <option value={1}>Web + Email</option>
                <option value={2}>Email only</option>
                <option value={3}>Web only</option>
              </select>
            </div>

            {/* email subject */}
            <div className="input-col span-2">
              <label>Email subject&nbsp;
                     <small>(tokens: <code>&#123;&#123;date&#125;&#125;</code>, <code>&#123;&#123;document&#125;&#125;</code>)</small>
              </label>
              <input type="text"
                     value={form.emailSubject||""}
                     onChange={e=>handleChange("emailSubject", e.target.value)}
                     placeholder="Inspection due {{date}}"/>
            </div>

            {/* email body */}
            <div className="input-col span-2">
              <label>Email message</label>
              <textarea rows={4}
                        value={form.emailMessage||""}
                        onChange={e=>handleChange("emailMessage", e.target.value)}
                        placeholder="The certificate “{{document}}” must be inspected on {{date}}."/>
            </div>

          </div>

          {/* actions */}
          <div className="settings-actions">
            <button
              className="btn btn-primary"
              disabled={!dirty || saving}
              onClick={save}>
              {saving ? <FaSyncAlt className="spin"/> : <FaSave/>}
              &nbsp;Save
            </button>
          </div>
        </div>

      </div>
    </div>
  );
};

export default Settings;
