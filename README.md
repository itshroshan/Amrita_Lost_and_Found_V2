# Amrita Lost & Found System

A modern, secure, and intuitive web application designed to help students and administrators at Amrita Vishwa Vidyapeetham easily report, track, and claim lost and found items on campus. 

## 🌟 Features

### 🎓 For Students
*   **OTP-Based Authentication:** Secure, passwordless login using university email addresses (`@bl.students.amrita.edu`).
*   **Report Items:** Easily report lost items or items you have found on campus, complete with image uploads and detailed descriptions.
*   **Smart Match Notifications:** The system automatically cross-references lost and found reports. If a potential match is detected, the student is instantly notified via email!
*   **My Activity Dashboard:** A dedicated space to track the status of your reported items, beautifully paginated for easy navigation.
*   **Secure Claims:** Claiming an item requires a real-time OTP verification sent to your student email to ensure items are returned to their rightful owners.

### 🛡️ For Administrators
*   **Centralized Dashboard:** A powerful control panel to oversee all campus activity.
*   **Item Moderation:** Review and approve "Found" items before they are publicly listed to prevent spam or inappropriate content.
*   **Claim Management:** Oversee the secure OTP claim process and securely hand over items to verified students.
*   **Analytics & Filtering:** Filter items by timeframe (Today, Last 7 Days, Last 30 Days, All Time) to easily manage inventory.

## 🛠️ Technology Stack

### Frontend
*   **Framework:** React 18 + Vite
*   **Styling:** Tailwind CSS (with full Dark Mode support!)
*   **Routing:** React Router DOM
*   **Icons:** Lucide React
*   **HTTP Client:** Axios (with custom interceptors for JWT injection)
*   **Notifications:** React Toastify

### Backend
*   **Framework:** Spring Boot 3 (Java 21)
*   **Security:** Spring Security + Custom JWT (JSON Web Tokens)
*   **Database:** PostgreSQL
*   **ORM:** Spring Data JPA / Hibernate
*   **Email Services:** JavaMailSender (for OTPs and Smart Match notifications)

## 🚀 Getting Started

### Prerequisites
*   Node.js (v18+)
*   Java Development Kit (JDK 21+)
*   PostgreSQL (Running locally or hosted)
*   Maven

### Backend Setup
1. Navigate to the `backend` directory:
   ```bash
   cd backend
   ```
2. Configure your database and email credentials. Update the `application.properties` (or `.env`) file:
   ```properties
   spring.datasource.url=jdbc:postgresql://localhost:5432/lost_and_found_db
   spring.datasource.username=YOUR_DB_USERNAME
   spring.datasource.password=YOUR_DB_PASSWORD
   
   spring.mail.username=YOUR_EMAIL@gmail.com
   spring.mail.password=YOUR_APP_PASSWORD
   ```
3. Run the Spring Boot application:
   ```bash
   ./mvnw spring-boot:run
   ```
   *The backend will start on `http://localhost:8081`*

### Frontend Setup
1. Navigate to the `frontend` directory:
   ```bash
   cd frontend
   ```
2. Install dependencies:
   ```bash
   npm install
   ```
3. Start the development server:
   ```bash
   npm run dev
   ```
   *The frontend will start on `http://localhost:5173`*

## 📦 Production Deployment (VM)

### Building the Frontend
```bash
cd frontend
npm run build
```
This will generate a `dist` folder containing the optimized static files ready to be served by Nginx, Apache, or IIS.

### Building the Backend
```bash
cd backend
./mvnw clean package -DskipTests
```
This will generate an executable `.jar` file in the `target` directory. You can run this file as a background service on your VM using tools like **NSSM** (Windows) or `systemd` (Linux).

---
*Built for Amrita Vishwa Vidyapeetham.*
