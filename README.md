# Digital Presence Management System (DPMS)

Welcome to the Digital Presence Management System! DPMS is a modern, comprehensive school attendance tracking platform that bridges the gap between administrators, teachers, students, and parents.

---

## 🚀 System User Flows

The system is designed with four distinct user roles, each with a tailored experience and specific capabilities.

### 1. Administrator Flow

The Admin acts as the central manager of the school's ecosystem.

- **Onboarding:** Since there are no default credentials, the very first user to log into the system will securely and automatically be registered as the master administrator.
- **Infrastructure Setup:** Create classes, register teachers, and assign teachers to their respective classes.
- **Student Roster:** Add students manually or perform a bulk import using a CSV file. The system automatically assigns a unique Registration Number and generates a digital QR Code for every student.
- **Global Monitoring:** Access the Admin Dashboard to view school-wide statistics, interactive weekly attendance charts, and oversee attendance records across all classes.
- **Manual Attendance Adjustments:** If a student is absent and contacts the school administration with a valid reason, a high-level Admin has the authority to go into the attendance records and manually change their status (e.g., to "Present" or "Not Marked").

### 2. Teacher Flow

Teachers manage the daily classroom attendance efficiently.

- **My Classes:** Log in to see a personalized dashboard displaying only the classes assigned to them.
- **Taking Roll Call:** Click on a class to view the day's roster and mark attendance using three convenient methods:
  - **QR Scanner:** Open the built-in webcam scanner and have students scan their digital QR badges as they walk in.
  - **Manual Entry:** Type a student's Registration Number to quickly mark them `PRESENT`.
  - **Auto-Absent:** Click "Mark Remaining ABSENT" at the end of the roll call to instantly finalize the roster.

### 3. Student Flow

Students are empowered to track their own attendance and utilize their digital identity.

- **Dashboard Access:** Log in to the Student Portal using their Registration Number.
- **Digital Badge:** View and download their personal Digital QR Badge directly from the dashboard to their smartphone.
- **History Tracking:** Review their personal attendance history (Present, Absent, Late) and monitor their overall attendance statistics.

### 4. Parent Flow

Parents stay informed and can proactively communicate with the school regarding absences.

- **Secure Registration:** Create a parent account by linking to their child using a Registration Number and a secure Secret Key provided by the school administration.
- **Attendance Monitoring:** Log in to view an accordion-style daily breakdown of their child's attendance records.

---

## Technical Architecture

- **Frontend:** A fast, responsive Single Page Application (SPA) built with HTML5, Vanilla JavaScript, and modern Custom CSS. It features dynamic top-right sliding toast notifications, Chart.js for data visualization, and HTML5-QRCode for browser-based scanning.
- **Backend:** Java Spring BootREST API, utilizing Spring Data JPA, and powered by a PostgreSQL database.

---

## How to Run

### 1. Database Setup

1. Ensure PostgreSQL is running.
2. Create the database named `dpms`: `CREATE DATABASE dpms;`
3. Verify credentials in `backend/src/main/resources/application.properties`.

### 2. Compile & Run Backend

You can easily build and start the backend using the provided shell script:

```bash
cd backend
./start-backend.sh
```

*Note on Admin Setup:* There are no default credentials. The very first username and password entered during login will automatically be registered as the master Admin account!

### 3. Run Frontend

You can easily start the frontend using the provided shell script:

```bash
cd frontend
./start-frontend.sh
```

Navigate to: `http://localhost:3000`
