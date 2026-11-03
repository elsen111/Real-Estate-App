import { createContext, useContext, useEffect, useState, type ReactNode } from "react";

import type { LoginRequest, AuthResponse, UserResponse } from "../types/api/auth";

import { authService } from "../services/authService";
import { tokenStore } from "../config/api/tokenStore";

interface AuthContextValue {
  user: UserResponse | null;
  isLoading: boolean;

  login: (credentials: LoginRequest) => Promise<void>;
  logout: () => Promise<void>;
}

const AuthContext = createContext<AuthContextValue | null>(null);

export function AuthProvider({ children }: { children: ReactNode }) {
  const [user, setUser] = useState<UserResponse | null>(null);
  const [isLoading, setIsLoading] = useState(true);

  useEffect(() => {
    let cancelled = false;
    (async () => {
      try {
        const { data } = await authService.refresh();
        tokenStore.setAccessToken(data.accessToken);
        const me = await authService.getCurrentUser();
        if (!cancelled) setUser(me.data);
      } catch {
        if (!cancelled) {
          tokenStore.clear();
          setUser(null);
        }
      } finally {
        if (!cancelled) setIsLoading(false);
      }
    })();
    return () => {
      cancelled = true;
    };
  }, []);

  const startSession = (auth: AuthResponse) => {
    tokenStore.setAccessToken(auth.accessToken);
    setUser({
      user: auth.user,
      agency: auth.agency,
    });
  };

  const login = async (credentials: LoginRequest) => {
    const response = await authService.login(credentials);

    startSession(response.data);
  };

  const logout = async () => {
    try {
      await authService.logout();
    } finally {
      tokenStore.clear();
      setUser(null);
    }
  };

  return (
    <AuthContext.Provider value={{ user, isLoading, login, logout }}>
      {children}
    </AuthContext.Provider>
  );
}

export function useAuth() {
  const context = useContext(AuthContext);

  if (!context) {
    throw new Error("useAuth must be used inside <AuthProvider>");
  }

  return context;
}
