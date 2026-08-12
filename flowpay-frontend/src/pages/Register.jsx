import { useState } from "react";
import { useNavigate, Link } from "react-router-dom";
import { apiFetch } from "../api/apiClient";
import "./Register.css";

function Register() {
  const [fullName, setFullName] = useState("");
  const [email, setEmail] = useState("");
  const [password, setPassword] = useState("");
  const [errorMessage, setErrorMessage] = useState("");
  const [isSubmitting, setIsSubmitting] = useState(false);
  const navigate = useNavigate();

  async function handleSubmit(event) {
    event.preventDefault();
    setErrorMessage("");
    setIsSubmitting(true);

    try {
      await apiFetch("/api/users", {
        method: "POST",
        body: JSON.stringify({ fullName, email, password }),
      });

      navigate("/login");
    } catch (error) {
      if (error.fieldErrors) {
        const firstFieldMessage = Object.values(error.fieldErrors)[0];
        setErrorMessage(firstFieldMessage);
      } else {
        setErrorMessage(error.message);
      }
    } finally {
      setIsSubmitting(false);
    }
  }

  return (
    <div className="auth-page">
      <form className="auth-card" onSubmit={handleSubmit}>
        <h1 className="auth-title">FlowPay</h1>
        <p className="auth-subtitle">Create your account</p>

        {errorMessage && <div className="auth-error">{errorMessage}</div>}

        <label className="auth-label" htmlFor="fullName">
          Full Name
        </label>
        <input
          id="fullName"
          type="text"
          className="auth-input"
          value={fullName}
          onChange={(event) => setFullName(event.target.value)}
          required
        />

        <label className="auth-label" htmlFor="email">
          Email
        </label>
        <input
          id="email"
          type="email"
          className="auth-input"
          value={email}
          onChange={(event) => setEmail(event.target.value)}
          required
        />

        <label className="auth-label" htmlFor="password">
          Password
        </label>
        <input
          id="password"
          type="password"
          className="auth-input"
          value={password}
          onChange={(event) => setPassword(event.target.value)}
          required
        />

        <button type="submit" className="auth-button" disabled={isSubmitting}>
          {isSubmitting ? "Creating account..." : "Register"}
        </button>

        <p className="auth-footer">
          Already have an account? <Link to="/login">Log in</Link>
        </p>
      </form>
    </div>
  );
}

export default Register;
