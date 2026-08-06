import { Navigate } from 'react-router-dom';

const ProtectedRoute = ({ children, allowedRoles }) => {
  const userString = localStorage.getItem('user');

  if (!userString) {
    // Not logged in
    return <Navigate to="/login" replace />;
  }

  let user = null;
  try {
    user = JSON.parse(userString);
  } catch (err) {
    return <Navigate to="/login" replace />;
  }

  if (allowedRoles && !allowedRoles.includes(user.role)) {
    // Logged in, but trying to access a route meant for another role
    // Redirect admins to admin dashboard, students to student dashboard
    if (user.role === 'admin') {
      return <Navigate to="/admin" replace />;
    }
    return <Navigate to="/dashboard" replace />;
  }

  // Authorized
  return children;
};

export default ProtectedRoute;
