import { createContext, useContext, useEffect, useState } from "react";
import { apiFetch } from "../api/apiClient";

const AuthContext = createContext(null);

export function AuthProvider({ children }) {
  const [user, setUser] = useState(null);
  const [isLoading, setIsLoading] = useState(true);

  async function refreshAuth() {
    try {
      const me = await apiFetch("/api/auth/me");
      setUser(me);
    } catch (error) {
      setUser(null);
    } finally {
      setIsLoading(false);
    }
  }

  useEffect(() => {
    refreshAuth();
  }, []);

  const value = { user, isLoading, refreshAuth, setUser };

  return <AuthContext.Provider value={value}>{children}</AuthContext.Provider>;
}

export function useAuth() {
  return useContext(AuthContext);
}
