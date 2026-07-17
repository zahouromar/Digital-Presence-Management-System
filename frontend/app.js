const API_BASE = "http://localhost:8080";
let currentUser = null;
let html5QrCode = null;
let autoRestartTimeout = null;
let attendanceChartInstance = null;
function getLocalDateString() {
  const tzOffset = new Date().getTimezoneOffset() * 60000;
  return new Date(Date.now() - tzOffset).toISOString().split("T")[0];
}
async function apiFetch(endpoint, options = {}) {
  const url = `${API_BASE}${endpoint}`;
  options.credentials = "include";
  if (!options.headers) {
    options.headers = {};
  }
  if (
    options.body &&
    !(options.body instanceof FormData) &&
    !options.headers["Content-Type"]
  ) {
    options.headers["Content-Type"] = "application/json";
  }
  try {
    const response = await fetch(url, options);
    if (response.status === 401) {
      handleUnauthorized();
      throw new Error("Session expired. Please login.");
    }
    if (response.status === 204) {
      return null;
    }
    const text = await response.text();
    const data = text ? JSON.parse(text) : null;
    if (!response.ok) {
      throw new Error((data && data.message) || "An error occurred");
    }
    return data;
  } catch (err) {
    console.error(`API Error on ${endpoint}:`, err);
    throw err;
  }
}
let toastContainer = document.getElementById("toast-container");
if (!toastContainer) {
  toastContainer = document.createElement("div");
  toastContainer.id = "toast-container";
  toastContainer.style.cssText =
    "position: fixed; top: 20px; right: 20px; z-index: 9999; display: flex; flex-direction: column; gap: 10px; align-items: flex-end;";
  document.body.appendChild(toastContainer);
}
function showMessage(text, isError = false) {
  const toast = document.createElement("div");
  const iconColor = isError ? "#EF4444" : "#22C55E";
  const title = isError ? "Error" : "Success";
  const iconPath = isError
    ? '<path stroke-linecap="round" stroke-linejoin="round" stroke-width="1.5" d="M12 9v2m0 4h.01m-6.938 4h13.856c1.54 0 2.502-1.667 1.732-3L13.732 4c-.77-1.333-2.694-1.333-3.464 0L3.34 16c-.77 1.333.192 3 1.732 3z"/>'
    : '<path d="M16.5 8.31V9a7.5 7.5 0 1 1-4.447-6.855M16.5 3 9 10.508l-2.25-2.25" stroke-width="1.5" stroke-linecap="round" stroke-linejoin="round"/>';
  toast.style.cssText = `
        background-color: white; 
        display: inline-flex; 
        gap: 12px; 
        padding: 12px 16px; 
        font-size: 14px; 
        border-radius: 6px; 
        border: 1px solid #e2e8f0; 
        box-shadow: 0 4px 6px -1px rgba(0, 0, 0, 0.1), 0 2px 4px -1px rgba(0, 0, 0, 0.06);
        animation: slideInRight 0.3s cubic-bezier(0.16, 1, 0.3, 1) forwards;
        max-width: 350px;
    `;
  toast.innerHTML = `
        <svg width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="${iconColor}" style="margin-top: 2px; flex-shrink: 0;" xmlns="http://www.w3.org/2000/svg">
            ${iconPath}
        </svg>
        <div style="flex-grow: 1;">
            <h3 style="color: #334155; font-weight: 600; margin: 0 0 2px 0; font-size: 14px;">${title}</h3>
            <p style="color: #64748b; margin: 0; line-height: 1.4; font-size: 13px;">${text}</p>
        </div>
        <button type="button" aria-label="close" style="background: none; border: none; cursor: pointer; margin-bottom: auto; color: #94a3b8; padding: 2px; transition: color 0.15s;" onmouseover="this.style.color='#475569'" onmouseout="this.style.color='#94a3b8'" onclick="this.parentElement.remove()">
            <svg width="12" height="12" viewBox="0 0 14 14" fill="none" xmlns="http://www.w3.org/2000/svg">
                <rect y="12.532" width="17.498" height="2.1" rx="1.05" transform="rotate(-45.74 0 12.532)" fill="currentColor"/>
                <rect x="12.531" y="13.914" width="17.498" height="2.1" rx="1.05" transform="rotate(-135.74 12.531 13.914)" fill="currentColor"/>
            </svg>
        </button>
    `;
  toastContainer.appendChild(toast);
  setTimeout(() => {
    if (toast.parentElement) {
      toast.style.animation = "slideOutRight 0.3s ease-in forwards";
      setTimeout(() => toast.remove(), 300);
    }
  }, 4000);
}
function handleUnauthorized() {
  currentUser = null;
  localStorage.removeItem("dpms_user");
  document.getElementById("app-layout").style.display = "none";
  updateHeader();
  showView("login-view");
}
function showView(viewId) {
  if (viewId === "login-view" || viewId === "register-parent-view") {
    document.getElementById("login-view").style.display =
      viewId === "login-view" ? "flex" : "none";
    document.getElementById("register-parent-view").style.display =
      viewId === "register-parent-view" ? "flex" : "none";
    document.getElementById("app-layout").style.display = "none";
    const views = document.querySelectorAll(".view");
    views.forEach((v) => v.classList.remove("active"));
  } else {
    document.getElementById("login-view").style.display = "none";
    document.getElementById("register-parent-view").style.display = "none";
    document.getElementById("app-layout").style.display = "flex";
    const views = document.querySelectorAll(".view");
    views.forEach((v) => v.classList.remove("active"));
    const target = document.getElementById(viewId);
    if (target) {
      target.classList.add("active");
    }
    updateSidebarActiveLink(viewId);
  }
  if (viewId === "admin-dashboard-view") {
    loadDashboardData();
  } else if (viewId === "manage-students-view") {
    loadStudents();
  } else if (viewId === "manage-teachers-view") {
    loadTeachers();
  } else if (viewId === "manage-classes-view") {
    loadClasses();
  } else if (viewId === "teacher-dashboard-view") {
    loadTeacherClasses();
  } else if (viewId === "student-portal-view") {
    loadStudentPortal();
  } else if (viewId === "parent-portal-view") {
    loadParentPortal();
  } else if (viewId === "attendance-view") {
    const dateInput = document.getElementById("attendance-date-filter");
    if (!dateInput.value) {
      const todayStr = getLocalDateString();
      dateInput.value = todayStr;
    }
    loadAttendance(dateInput.value);
  }
}
function renderSidebar() {
  const nav = document.getElementById("sidebar-navigation");
  nav.innerHTML = "";
  if (!currentUser) return;
  if (currentUser.role === "ADMIN") {
    nav.innerHTML = `
            <a href="#" class="sidebar-link" id="nav-dash" onclick="showView('admin-dashboard-view'); return false;">Dashboard</a>
            <a href="#" class="sidebar-link" id="nav-classes" onclick="showView('manage-classes-view'); return false;">Manage Classes</a>
            <a href="#" class="sidebar-link" id="nav-stud" onclick="showView('manage-students-view'); return false;">Manage Students</a>
            <a href="#" class="sidebar-link" id="nav-teach" onclick="showView('manage-teachers-view'); return false;">Manage Teachers</a>
            <a href="#" class="sidebar-link" id="nav-attn" onclick="showView('attendance-view'); return false;">Attendance Logs</a>
        `;
  } else if (currentUser.role === "TEACHER") {
    nav.innerHTML = `
            <a href="#" class="sidebar-link" id="nav-dash-t" onclick="showView('teacher-dashboard-view'); return false;">My Classes</a>
        `;
  } else if (currentUser.role === "STUDENT") {
    nav.innerHTML = `
            <a href="#" class="sidebar-link" id="nav-stud-dash" onclick="showView('student-portal-view'); return false;">My Dashboard</a>
        `;
  } else if (currentUser.role === "PARENT") {
    nav.innerHTML = `
            <a href="#" class="sidebar-link" id="nav-parent-dash" onclick="showView('parent-portal-view'); return false;">Parent Dashboard</a>
        `;
  }
}
function updateSidebarActiveLink(viewId) {
  const links = document.querySelectorAll(".sidebar-link");
  links.forEach((lnk) => lnk.classList.remove("active"));
  const map = {
    "admin-dashboard-view": "nav-dash",
    "teacher-dashboard-view": "nav-dash-t",
    "manage-students-view": "nav-stud",
    "add-student-view": "nav-stud",
    "manage-teachers-view": "nav-teach",
    "add-teacher-view": "nav-teach",
    "manage-classes-view": "nav-classes",
    "class-detail-view": "nav-classes",
    "attendance-view":
      currentUser && currentUser.role === "ADMIN" ? "nav-attn" : "nav-attn-t",
    "student-portal-view": "nav-stud-dash",
    "parent-portal-view": "nav-parent-dash",
  };
  const targetId = map[viewId];
  if (targetId) {
    const link = document.getElementById(targetId);
    if (link) link.classList.add("active");
  }
}
function updateHeader() {
  const userDisplay = document.getElementById("user-display");
  if (userDisplay) {
    if (currentUser) {
      userDisplay.innerText = `${currentUser.username} (${currentUser.role})`;
    } else {
      userDisplay.innerText = "Not logged in";
    }
  }
}
function initAuth() {
  const savedUser = localStorage.getItem("dpms_user");
  if (savedUser) {
    currentUser = JSON.parse(savedUser);
    updateHeader();
    renderSidebar();
    if (currentUser.role === "ADMIN") {
      showView("admin-dashboard-view");
    } else if (currentUser.role === "STUDENT") {
      showView("student-portal-view");
    } else if (currentUser.role === "PARENT") {
      showView("parent-portal-view");
    } else {
      showView("teacher-dashboard-view");
    }
  } else {
    showView("login-view");
  }
}
document.getElementById("login-form").addEventListener("submit", async (e) => {
  e.preventDefault();
  const username = document.getElementById("login-username").value;
  const password = document.getElementById("login-password").value;
  try {
    const user = await apiFetch("/login", {
      method: "POST",
      body: JSON.stringify({ username, password }),
    });
    currentUser = user;
    localStorage.setItem("dpms_user", JSON.stringify(user));
    updateHeader();
    renderSidebar();
    showMessage("Welcome back!");
    if (user.role === "ADMIN") {
      showView("admin-dashboard-view");
    } else if (user.role === "STUDENT") {
      showView("student-portal-view");
    } else if (user.role === "PARENT") {
      showView("parent-portal-view");
    } else {
      showView("teacher-dashboard-view");
    }
  } catch (err) {
    showMessage(err.message || "Login failed", true);
  }
});
function showRegisterParentView() {
  showView("register-parent-view");
}
function showLoginView() {
  showView("login-view");
}
document
  .getElementById("register-parent-form")
  ?.addEventListener("submit", async (e) => {
    e.preventDefault();
    const fullName = document
      .getElementById("reg-parent-fullname")
      .value.trim();
    const registrationNumber = document
      .getElementById("reg-student-regnumber")
      .value.trim();
    const secretKey = document.getElementById("reg-secret-key").value.trim();
    const username = document.getElementById("reg-username").value.trim();
    const password = document.getElementById("reg-password").value;
    try {
      await apiFetch("/register-parent", {
        method: "POST",
        body: JSON.stringify({
          fullName,
          registrationNumber,
          secretKey,
          username,
          password,
        }),
      });
      showMessage("Parent registered successfully. You can now login.");
      document.getElementById("register-parent-form").reset();
      showLoginView();
    } catch (err) {
      showMessage(err.message || "Registration failed", true);
    }
  });
document.getElementById("btn-logout").addEventListener("click", async () => {
  try {
    await apiFetch("/logout", { method: "POST" });
  } catch (err) {
    console.warn("Logout endpoint failed, clearing locally", err);
  }
  handleUnauthorized();
});
async function loadDashboardData() {
  try {
    const stats = await apiFetch("/dashboard");
    document.getElementById("stat-total-students").innerText =
      stats.totalStudents;
    document.getElementById("stat-today-attendance").innerText =
      stats.todayAttendance;
    document.getElementById("stat-total-teachers").innerText =
      stats.totalTeachers;
    renderAttendanceChart(stats.trendDates || [], stats.trendCounts || []);
  } catch (err) {
    showMessage("Failed to load dashboard statistics", true);
  }
}
function renderAttendanceChart(labels, data) {
  const canvas = document.getElementById("attendance-chart");
  if (!canvas) return;
  const ctx = canvas.getContext("2d");
  if (attendanceChartInstance) {
    attendanceChartInstance.destroy();
  }
  attendanceChartInstance = new Chart(ctx, {
    type: "line",
    data: {
      labels: labels,
      datasets: [
        {
          label: "Present Count",
          data: data,
          borderColor: "#2563eb",
          backgroundColor: "rgba(37, 99, 235, 0.05)",
          borderWidth: 3,
          fill: true,
          tension: 0,
          pointBackgroundColor: "#2563eb",
          pointRadius: 4,
          pointHoverRadius: 4,
        },
      ],
    },
    options: {
      animation: false,
      responsive: true,
      maintainAspectRatio: false,
      plugins: {
        legend: {
          display: false,
        },
      },
      scales: {
        y: {
          beginAtZero: true,
          ticks: {
            stepSize: 1,
            color: "#64748b",
            font: {
              family: "Outfit",
              weight: "500",
            },
          },
          grid: {
            color: "#f1f5f9",
          },
        },
        x: {
          ticks: {
            color: "#64748b",
            font: {
              family: "Outfit",
              weight: "500",
            },
          },
          grid: {
            display: false,
          },
        },
      },
    },
  });
}
async function loadStudents() {
  try {
    const students = await apiFetch("/students");
    const tbody = document.getElementById("students-table-body");
    tbody.innerHTML = "";
    if (students.length === 0) {
      tbody.innerHTML = `<tr><td colspan="6" style="text-align: center;">No students found</td></tr>`;
      return;
    }
    students.forEach((s) => {
      const tr = document.createElement("tr");
      tr.innerHTML = `
                <td>${s.registrationNumber || "--"}</td>
                <td><span style="font-family:monospace; background:#f1f5f9; padding:2px 6px; border-radius:4px; font-weight:600;">${s.parentSecretKey || "--"}</span></td>
                <td>${s.firstName} ${s.lastName}</td>
                <td>${s.schoolClasses && s.schoolClasses.length > 0 ? s.schoolClasses.map((c) => c.name).join(", ") : "None"}</td>
                <td>
                    ${s.qrCode ? `<a href="${API_BASE}/students/${s.id}/qrcode" download="student_${s.registrationNumber}.png" class="btn-link">Download QR</a>` : "Not Generated"}
                </td>
                <td>
                    <button onclick="editStudentForm(${s.id})">Edit</button>
                    <button onclick="deleteStudent(${s.id})" style="background:#ef4444;">Delete</button>
                </td>
            `;
      tbody.appendChild(tr);
    });
  } catch (err) {
    showMessage("Failed to load students", true);
  }
}
async function uploadCsvFile(input) {
  const file = input.files[0];
  if (!file) return;
  input.value = "";
  if (!file.name.toLowerCase().endsWith(".csv")) {
    showMessage("Please select a valid .csv file", true);
    return;
  }
  showMessage("Uploading and processing CSV...");
  const formData = new FormData();
  formData.append("file", file);
  try {
    const result = await apiFetch("/students/import", {
      method: "POST",
      body: formData,
    });
    showMessage(result.message || "Import completed");
    loadStudents();
  } catch (err) {
    showMessage(err.message || "CSV import failed", true);
  }
}
function downloadCsvTemplate() {
  const headers =
    "registration_number,first_name,last_name,class_name,gender,parent_name,parent_phone";
  const example = "REG001,Ali,Juma,Grade 10,Male,Asha Juma,0712345678";
  const csvContent = `${headers}\n${example}`;
  const blob = new Blob([csvContent], { type: "text/csv" });
  const url = URL.createObjectURL(blob);
  const a = document.createElement("a");
  a.href = url;
  a.download = "students_import_template.csv";
  a.click();
  URL.revokeObjectURL(url);
}
document.getElementById("btn-add-student-nav").addEventListener("click", () => {
  document.getElementById("student-form-title").innerText = "Add Student";
  document.getElementById("student-form").reset();
  document.getElementById("student-id-field").value = "";
  showView("add-student-view");
});
document.getElementById("btn-cancel-student").addEventListener("click", () => {
  showView("manage-students-view");
});
document
  .getElementById("student-form")
  .addEventListener("submit", async (e) => {
    e.preventDefault();
    const id = document.getElementById("student-id-field").value;
    const registrationNumber = document.getElementById("student-reg").value;
    const firstName = document.getElementById("student-firstname").value;
    const lastName = document.getElementById("student-lastname").value;
    const gender = document.getElementById("student-gender").value;
    const parentName = document.getElementById("student-parent-name").value;
    const parentPhone = document.getElementById("student-parent-phone").value;
    const payload = {
      registrationNumber,
      firstName,
      lastName,
      gender,
      parentName,
      parentPhone,
    };
    const method = id ? "PUT" : "POST";
    const endpoint = id ? `/students/${id}` : "/students";
    try {
      const savedStudent = await apiFetch(endpoint, {
        method: method,
        body: JSON.stringify(payload),
      });
      const msg = id
        ? "Student updated successfully"
        : `Student saved. Parent Secret Key: ${savedStudent.parentSecretKey}`;
      showMessage(msg);
      showView("manage-students-view");
    } catch (err) {
      showMessage(err.message || "Failed to save student", true);
    }
  });
async function editStudentForm(id) {
  try {
    const student = await apiFetch(`/students/${id}`);
    document.getElementById("student-form-title").innerText = "Edit Student";
    document.getElementById("student-id-field").value = student.id;
    document.getElementById("student-reg").value = student.registrationNumber;
    document.getElementById("student-firstname").value = student.firstName;
    document.getElementById("student-lastname").value = student.lastName;
    document.getElementById("student-gender").value = student.gender;
    document.getElementById("student-parent-name").value = student.parentName;
    document.getElementById("student-parent-phone").value = student.parentPhone;
    showView("add-student-view");
  } catch (err) {
    showMessage("Failed to retrieve student details", true);
  }
}
async function deleteStudent(id) {
  if (
    !confirm("Are you sure you want to delete this student and their QR Code?")
  ) {
    return;
  }
  try {
    await apiFetch(`/students/${id}`, { method: "DELETE" });
    showMessage("Student deleted");
    loadStudents();
  } catch (err) {
    showMessage("Failed to delete student", true);
  }
}
async function loadTeachers() {
  try {
    const teachers = await apiFetch("/teachers");
    const tbody = document.getElementById("teachers-table-body");
    tbody.innerHTML = "";
    if (teachers.length === 0) {
      tbody.innerHTML = `<tr><td colspan="4" style="text-align: center;">No teachers found</td></tr>`;
      return;
    }
    teachers.forEach((t) => {
      const tr = document.createElement("tr");
      tr.innerHTML = `
                <td>${t.fullName || "N/A"}</td>
                <td>${t.phone || "N/A"}</td>
                <td>${t.username}</td>
                <td>
                    <button onclick="editTeacherForm(${t.id})">Edit</button>
                    <button onclick="deleteTeacher(${t.id})" style="background:#ef4444;">Delete</button>
                </td>
            `;
      tbody.appendChild(tr);
    });
  } catch (err) {
    showMessage("Failed to load teachers", true);
  }
}
document.getElementById("btn-add-teacher-nav").addEventListener("click", () => {
  document.getElementById("teacher-form-title").innerText = "Add Teacher";
  document.getElementById("teacher-form").reset();
  document.getElementById("teacher-id-field").value = "";
  document.getElementById("teacher-password").required = true;
  document.getElementById("password-help").style.display = "none";
  showView("add-teacher-view");
});
document.getElementById("btn-cancel-teacher").addEventListener("click", () => {
  showView("manage-teachers-view");
});
document
  .getElementById("teacher-form")
  .addEventListener("submit", async (e) => {
    e.preventDefault();
    const id = document.getElementById("teacher-id-field").value;
    const fullName = document.getElementById("teacher-name").value;
    const phone = document.getElementById("teacher-phone").value;
    const username = document.getElementById("teacher-username").value;
    const password = document.getElementById("teacher-password").value;
    const payload = { fullName, phone, username, password };
    const method = id ? "PUT" : "POST";
    const endpoint = id ? `/teachers/${id}` : "/teachers";
    try {
      await apiFetch(endpoint, {
        method: method,
        body: JSON.stringify(payload),
      });
      showMessage(
        id ? "Teacher updated successfully" : "Teacher created successfully",
      );
      showView("manage-teachers-view");
    } catch (err) {
      showMessage(err.message || "Failed to save teacher", true);
    }
  });
async function editTeacherForm(id) {
  try {
    const teachers = await apiFetch("/teachers");
    const teacher = teachers.find((t) => t.id === id);
    if (!teacher) throw new Error("Teacher not found");
    document.getElementById("teacher-form-title").innerText = "Edit Teacher";
    document.getElementById("teacher-id-field").value = teacher.id;
    document.getElementById("teacher-name").value = teacher.fullName || "";
    document.getElementById("teacher-phone").value = teacher.phone || "";
    document.getElementById("teacher-username").value = teacher.username;
    document.getElementById("teacher-password").value = "";
    document.getElementById("teacher-password").required = false;
    document.getElementById("password-help").style.display = "inline";
    showView("add-teacher-view");
  } catch (err) {
    showMessage("Failed to retrieve teacher details", true);
  }
}
async function deleteTeacher(id) {
  if (!confirm("Are you sure you want to delete this teacher?")) {
    return;
  }
  try {
    await apiFetch(`/teachers/${id}`, { method: "DELETE" });
    showMessage("Teacher deleted");
    loadTeachers();
  } catch (err) {
    showMessage("Failed to delete teacher", true);
  }
}
async function loadAttendance(dateFilter = "") {
  try {
    const todayStr = getLocalDateString();
    const targetDate = dateFilter || todayStr;
    const [students, records] = await Promise.all([
      apiFetch("/students"),
      apiFetch(`/attendance?date=${targetDate}`),
    ]);
    const tbody = document.getElementById("attendance-table-body");
    tbody.innerHTML = "";
    if (students.length === 0) {
      tbody.innerHTML = `<tr><td colspan="6" style="text-align: center;">No students registered in the system.</td></tr>`;
      return;
    }
    const recordsMap = new Map();
    records.forEach((r) => {
      recordsMap.set(r.registrationNumber, r);
    });
    students.forEach((student) => {
      const tr = document.createElement("tr");
      const record = recordsMap.get(student.registrationNumber);
      let status = "NOT_MARKED";
      let checkInTime = "--";
      let attendanceDate = targetDate;
      if (record) {
        status = record.status;
        checkInTime = record.checkInTime;
        attendanceDate = record.attendanceDate;
      }
      let badgeClass = "badge";
      if (status === "PRESENT") {
        badgeClass = "badge badge-present";
      } else if (status === "LATE") {
        badgeClass = "badge badge-late";
      } else if (status === "ABSENT") {
        badgeClass = "badge badge-absent";
      }
      tr.innerHTML = `
                <td>${student.firstName} ${student.lastName}</td>
                <td>${record ? record.className : student.schoolClasses && student.schoolClasses.length > 0 ? student.schoolClasses.map((c) => c.name).join(", ") : "--"}</td>
                <td>${attendanceDate}</td>
                <td>${checkInTime}</td>
                <td><span class="${badgeClass}">${status.replace("_", " ")}</span></td>
                <td>
                    <select onchange="updateManualAttendance(${student.id}, '${attendanceDate}', this.value)" style="padding: 4px 8px; border-radius: 4px; border: 1px solid #cbd5e1; font-size: 13px;">
                        <option value="NOT_MARKED" ${status === "NOT_MARKED" ? "selected" : ""}>Not Marked</option>
                        <option value="PRESENT" ${status === "PRESENT" ? "selected" : ""}>Present</option>
                        <option value="ABSENT" ${status === "ABSENT" ? "selected" : ""}>Absent</option>
                    </select>
                </td>
            `;
      tbody.appendChild(tr);
    });
  } catch (err) {
    showMessage("Failed to load attendance records", true);
  }
}
async function updateManualAttendance(studentId, date, status) {
  try {
    await apiFetch("/attendance/manual", {
      method: "POST",
      body: JSON.stringify({ studentId, date, status }),
    });
    showMessage("Attendance status updated");
    const dateFilter = document.getElementById("attendance-date-filter").value;
    loadAttendance(dateFilter);
  } catch (err) {
    showMessage("Failed to update manual attendance", true);
  }
}
document
  .getElementById("btn-apply-attendance-filter")
  .addEventListener("click", () => {
    const filter = document.getElementById("attendance-date-filter").value;
    loadAttendance(filter);
  });
document
  .getElementById("btn-clear-attendance-filter")
  .addEventListener("click", () => {
    const todayStr = getLocalDateString();
    document.getElementById("attendance-date-filter").value = todayStr;
    loadAttendance(todayStr);
  });
function startScanner() {}
async function stopScanner() {}
window.addEventListener("DOMContentLoaded", () => {
  initAuth();
});
async function loadStudentPortal() {
  try {
    const [profile, history] = await Promise.all([
      apiFetch("/my/profile"),
      apiFetch("/my/attendance"),
    ]);
    document.getElementById("student-portal-greeting").innerText =
      `Welcome, ${profile.firstName}!`;
    if (profile.qrCode) {
      const qrUrl = `${API_BASE}/my/qrcode`;
      document.getElementById("student-qr-image").src = qrUrl;
      document.getElementById("student-qr-download").href = qrUrl;
      document.getElementById("student-qr-download").style.display =
        "inline-block";
    } else {
      document.getElementById("student-qr-image").src = "";
      document.getElementById("student-qr-download").style.display = "none";
    }
    let present = 0,
      late = 0,
      absent = 0;
    history.forEach((r) => {
      if (r.status === "PRESENT") present++;
      else if (r.status === "LATE") late++;
      else if (r.status === "ABSENT") absent++;
    });
    document.getElementById("student-stat-total").innerText = history.length;
    document.getElementById("student-stat-present").innerText = present;
    document.getElementById("student-stat-late").innerText = late;
    document.getElementById("student-stat-absent").innerText = absent;
    const tbody = document.getElementById("student-attendance-body");
    tbody.innerHTML = "";
    if (history.length === 0) {
      tbody.innerHTML =
        '<tr><td colspan="3" style="text-align: center;">No attendance history found.</td></tr>';
      return;
    }
    history.forEach((r) => {
      const tr = document.createElement("tr");
      let badgeClass = "badge badge-absent";
      if (r.status === "PRESENT") badgeClass = "badge badge-present";
      else if (r.status === "LATE") badgeClass = "badge badge-late";
      tr.innerHTML = `
                <td>${r.attendanceDate}</td>
                <td>${r.checkInTime}</td>
                <td><span class="${badgeClass}">${r.status}</span></td>
            `;
      tbody.appendChild(tr);
    });
  } catch (err) {
    showMessage("Failed to load student portal data", true);
  }
}
async function loadParentPortal() {
  try {
    const [profile, history] = await Promise.all([
      apiFetch("/parent/child"),
      apiFetch("/parent/attendance"),
    ]);
    document.getElementById("parent-child-name").innerText =
      `${profile.firstName} ${profile.lastName}`;
    document.getElementById("parent-child-reg").innerText =
      `Reg: ${profile.registrationNumber || "--"}`;
    const total = history.length;
    const present = history.filter((r) => r.status === "PRESENT").length;
    const absent = history.filter((r) => r.status === "ABSENT").length;
    document.getElementById("parent-stat-total").innerText = total;
    document.getElementById("parent-stat-present").innerText = present;
    document.getElementById("parent-stat-absent").innerText = absent;
    const accordion = document.getElementById("parent-attendance-accordion");
    accordion.innerHTML = "";
    if (history.length === 0) {
      accordion.innerHTML =
        '<p style="color:#64748b; padding:15px 0;">No attendance history found.</p>';
      return;
    }
    const byDate = {};
    history.forEach((r) => {
      const d = r.attendanceDate;
      if (!byDate[d]) byDate[d] = [];
      byDate[d].push(r);
    });
    const dates = Object.keys(byDate).sort((a, b) => b.localeCompare(a));
    dates.forEach((date) => {
      const rows = byDate[date];
      const dayOfWeek = new Date(date).toLocaleDateString("en-US", {
        weekday: "long",
        timeZone: "UTC",
      });
      const allPresent = rows.every((r) => r.status === "PRESENT");
      const headerColor = allPresent ? "#059669" : "#dc2626";
      const block = document.createElement("div");
      block.style.cssText =
        "border:1px solid #e2e8f0; border-radius:8px; margin-bottom:10px; overflow:hidden;";
      const header = document.createElement("div");
      header.style.cssText = `display:flex; justify-content:space-between; align-items:center; padding:12px 16px; background:#f8fafc; cursor:pointer; border-bottom:1px solid #e2e8f0;`;
      header.innerHTML = `
                <div>
                  <span style="font-weight:700; color:#0f172a;">${date}</span>
                  <span style="color:#64748b; font-size:13px; margin-left:8px;">${dayOfWeek}</span>
                </div>
                <span style="font-weight:600; color:${headerColor}; font-size:13px;">${allPresent ? " All Present" : " Has Absences"}</span>
            `;
      const body = document.createElement("div");
      body.style.cssText = "display:none;";
      rows.forEach((r) => {
        let badgeClass = "badge badge-absent";
        if (r.status === "PRESENT") badgeClass = "badge badge-present";
        else if (r.status === "NOT_MARKED") badgeClass = "badge";
        const row = document.createElement("div");
        row.style.cssText =
          "display:flex; justify-content:space-between; align-items:center; padding:10px 16px; border-bottom:1px solid #f1f5f9; font-size:14px;";
        row.innerHTML = `
                    <span style="color:#334155;">${r.className || "Unknown Class"}</span>
                    <span class="${badgeClass}">${r.status}</span>
                `;
        body.appendChild(row);
      });
      header.addEventListener("click", () => {
        body.style.display = body.style.display === "none" ? "block" : "none";
      });
      block.appendChild(header);
      block.appendChild(body);
      accordion.appendChild(block);
    });
  } catch (err) {
    showMessage("Failed to load parent portal data", true);
  }
}
let currentClassId = null;
let currentClassDate = null;
async function loadTeacherClasses() {
  try {
    const classes = await apiFetch("/classes/my-classes");
    const container = document.getElementById("teacher-classes-list");
    if (!container) return;
    container.innerHTML = "";
    if (classes.length === 0) {
      container.innerHTML =
        '<p style="color:#64748b;">No classes assigned to you yet.</p>';
      document.getElementById("teacher-class-detail").style.display = "none";
      return;
    }
    classes.forEach((cls) => {
      const card = document.createElement("div");
      card.className = "card";
      card.style.cssText =
        "padding:20px; min-width:180px; cursor:pointer; text-align:center;";
      card.innerHTML = `
                <div style="margin-bottom:12px; display:flex; justify-content:center;">
                    <svg xmlns="http://www.w3.org/2000/svg" fill="none" viewBox="0 0 24 24" stroke-width="1.5" stroke="currentColor" style="width: 36px; height: 36px; color: #3b82f6;">
                        <path stroke-linecap="round" stroke-linejoin="round" d="M12 6.042A8.967 8.967 0 0 0 6 3.75c-1.052 0-2.062.18-3 .512v14.25A8.987 8.987 0 0 1 6 18c2.305 0 4.408.867 6 2.292m0-14.25a8.966 8.966 0 0 1 6-2.292c1.052 0 2.062.18 3 .512v14.25A8.987 8.987 0 0 0 18 18a8.967 8.967 0 0 0-6 2.292m0-14.25v14.25" />
                    </svg>
                </div>
                <div style="font-weight:700; font-size:15px; color:#0f172a;">${cls.name}</div>
                <div style="font-size:12px; color:#64748b; margin-top:4px;">${(cls.students || []).length} students</div>
            `;
      card.addEventListener("click", () => openTeacherClassView(cls));
      container.appendChild(card);
    });
    openTeacherClassView(classes[0]);
  } catch (err) {
    showMessage("Failed to load your classes", true);
  }
}
function openTeacherClassView(cls) {
  currentClassId = cls.id;
  currentClassDate = getLocalDateString();
  document.getElementById("teacher-class-name").innerText =
    `${cls.name}  ${currentClassDate}`;
  document.getElementById("teacher-class-detail").style.display = "block";
  document.getElementById("teacher-reg-input").value = "";
  loadClassAttendance();
}
async function loadClassAttendance() {
  if (!currentClassId || !currentClassDate) return;
  try {
    const records = await apiFetch(
      `/attendance/class/${currentClassId}?date=${currentClassDate}`,
    );
    const tbody = document.getElementById("teacher-class-students-body");
    tbody.innerHTML = "";
    if (records.length === 0) {
      tbody.innerHTML =
        '<tr><td colspan="3" style="text-align:center; color:#64748b;">No students in this class.</td></tr>';
      return;
    }
    records.forEach((r) => {
      const tr = document.createElement("tr");
      let badgeClass = "badge";
      let badgeStyle = "background:#e2e8f0; color:#475569;";
      if (r.status === "PRESENT") {
        badgeClass = "badge badge-present";
        badgeStyle = "";
      } else if (r.status === "ABSENT") {
        badgeClass = "badge badge-absent";
        badgeStyle = "";
      }
      tr.innerHTML = `
                <td>${r.registrationNumber || "--"}</td>
                <td>${r.studentName}</td>
                <td><span class="${badgeClass}" style="${badgeStyle}">${r.status}</span></td>
            `;
      tbody.appendChild(tr);
    });
  } catch (err) {
    showMessage("Failed to load class attendance", true);
  }
}
async function markStudentPresent() {
  const regInput = document.getElementById("teacher-reg-input");
  const reg = regInput.value.trim();
  if (!reg) {
    showMessage("Please enter a registration number", true);
    return;
  }
  if (!currentClassId) {
    showMessage("No class selected", true);
    return;
  }
  try {
    await apiFetch("/attendance/scan", {
      method: "POST",
      body: JSON.stringify({ studentId: reg, classId: currentClassId }),
    });
    showMessage(`Marked PRESENT: ${reg}`);
    regInput.value = "";
    loadClassAttendance();
  } catch (err) {
    showMessage(err.message || "Failed to mark attendance", true);
  }
}
async function markRemainingAbsent() {
  if (!currentClassId || !currentClassDate) return;
  if (!confirm("Mark all unmarked students as ABSENT for today?")) return;
  try {
    await apiFetch(
      `/attendance/class/${currentClassId}/mark-absent?date=${currentClassDate}`,
      {
        method: "POST",
      },
    );
    showMessage("All remaining students marked ABSENT");
    loadClassAttendance();
  } catch (err) {
    showMessage(err.message || "Failed to mark absent", true);
  }
}
let currentAdminClassId = null;
async function loadClasses() {
  try {
    const [classes, teachers] = await Promise.all([
      apiFetch("/classes"),
      apiFetch("/teachers"),
    ]);
    const sel = document.getElementById("new-class-teacher");
    if (sel) {
      sel.innerHTML = '<option value="">-- None (assign later) --</option>';
      teachers.forEach((t) => {
        const opt = document.createElement("option");
        opt.value = t.id;
        opt.textContent = t.fullName || t.username;
        sel.appendChild(opt);
      });
    }
    const tbody = document.getElementById("classes-table-body");
    if (!tbody) return;
    tbody.innerHTML = "";
    if (classes.length === 0) {
      tbody.innerHTML =
        '<tr><td colspan="4" style="text-align:center; color:#64748b;">No classes created yet.</td></tr>';
      return;
    }
    classes.forEach((cls) => {
      const tr = document.createElement("tr");
      const teacherName = cls.teacher
        ? cls.teacher.fullName || cls.teacher.username
        : '<span style="color:#94a3b8;"></span>';
      const studentCount = (cls.students || []).length;
      tr.innerHTML = `
                <td><strong>${cls.name}</strong></td>
                <td>${teacherName}</td>
                <td>${studentCount} students</td>
                <td>
                    <button onclick="openClassDetail(${cls.id}, '${cls.name}')" style="padding:4px 10px; font-size:12px; background:#3b82f6; color:#fff; border:none; border-radius:4px; cursor:pointer;">Manage</button>
                    <button onclick="deleteClass(${cls.id})" style="padding:4px 10px; font-size:12px; background:#ef4444; color:#fff; border:none; border-radius:4px; cursor:pointer; margin-left:8px;">Delete</button>
                </td>
            `;
      tbody.appendChild(tr);
    });
  } catch (err) {
    showMessage("Failed to load classes", true);
  }
}
function showAddClassForm() {
  document.getElementById("add-class-form-container").style.display = "block";
}
function hideAddClassForm() {
  document.getElementById("add-class-form-container").style.display = "none";
  document.getElementById("add-class-form").reset();
}
document
  .getElementById("add-class-form")
  ?.addEventListener("submit", async (e) => {
    e.preventDefault();
    const name = document.getElementById("new-class-name").value.trim();
    const teacherId =
      document.getElementById("new-class-teacher").value || null;
    try {
      await apiFetch("/classes", {
        method: "POST",
        body: JSON.stringify({ name, teacherId }),
      });
      showMessage(`Class "${name}" created`);
      hideAddClassForm();
      loadClasses();
    } catch (err) {
      showMessage(err.message || "Failed to create class", true);
    }
  });
async function deleteClass(classId) {
  if (
    !confirm(
      "Are you sure you want to delete this class? This will also remove its attendance records.",
    )
  )
    return;
  try {
    await apiFetch(`/classes/${classId}`, { method: "DELETE" });
    showMessage("Class deleted successfully");
    loadClasses();
  } catch (err) {
    showMessage("Failed to delete class", true);
  }
}
async function openClassDetail(classId, className) {
  currentAdminClassId = classId;
  document.getElementById("class-detail-title").innerText =
    `${className}  Enrolled Students`;
  showView("class-detail-view");
  loadClassStudents(classId);
}
async function loadClassStudents(classId) {
  try {
    const classes = await apiFetch("/classes");
    const cls = classes.find((c) => c.id === classId);
    const tbody = document.getElementById("class-students-table-body");
    if (!tbody || !cls) return;
    tbody.innerHTML = "";
    if (!cls.students || cls.students.length === 0) {
      tbody.innerHTML =
        '<tr><td colspan="3" style="text-align:center; color:#64748b;">No students enrolled yet.</td></tr>';
      return;
    }
    cls.students.forEach((s) => {
      const tr = document.createElement("tr");
      tr.innerHTML = `
                <td>${s.registrationNumber}</td>
                <td>${s.firstName} ${s.lastName}</td>
                <td>
                    <button onclick="removeStudentFromClass(${classId}, ${s.id})" style="padding:4px 10px; font-size:12px; background:#ef4444; color:#fff; border:none; border-radius:4px; cursor:pointer;">Remove</button>
                </td>
            `;
      tbody.appendChild(tr);
    });
  } catch (err) {
    showMessage("Failed to load class students", true);
  }
}
async function addStudentToClass() {
  const reg = document.getElementById("class-add-student-reg").value.trim();
  if (!reg || !currentAdminClassId) return;
  try {
    const students = await apiFetch("/students");
    const student = students.find((s) => s.registrationNumber === reg);
    if (!student) {
      showMessage(`No student found with registration: ${reg}`, true);
      return;
    }
    await apiFetch(`/classes/${currentAdminClassId}/students/${student.id}`, {
      method: "POST",
    });
    showMessage(`${student.firstName} added to class`);
    document.getElementById("class-add-student-reg").value = "";
    loadClassStudents(currentAdminClassId);
  } catch (err) {
    showMessage(err.message || "Failed to add student", true);
  }
}
async function removeStudentFromClass(classId, studentId) {
  if (!confirm("Remove this student from the class?")) return;
  try {
    await apiFetch(`/classes/${classId}/students/${studentId}`, {
      method: "DELETE",
    });
    showMessage("Student removed from class");
    loadClassStudents(classId);
  } catch (err) {
    showMessage(err.message || "Failed to remove student", true);
  }
}
