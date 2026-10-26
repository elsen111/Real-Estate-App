import { createContext, useContext, useEffect, useState, type ReactNode } from "react";
import { dummyUser } from "../content/dummyData";
import { type User } from "../types/api/responseTypes";

interface AuthContextValue {
  user: User | null;
  isLoading: boolean;
  login: (user: User) => void;
  logout: () => void;
}

const AuthContext = createContext<AuthContextValue | null>(null);

export function AuthProvider({ children }: { children: ReactNode }) {
  const [user, setUser] = useState<User | null>(null);
  const [isLoading, setIsLoading] = useState<boolean>(true);

  useEffect(() => {
    //           api.get("/auth/me")
    // .then((res) => setUser(res.data))
    // .catch(() => setUser(null))
    // .finally(() => setIsLoading(false));

    console.log(dummyUser);

    // setUser(dummyUser);
    setUser(null);
    setIsLoading(false);
  }, []);

  return (
    <AuthContext.Provider value={{ user, isLoading, login: setUser, logout: () => setUser(null) }}>
      {children}
    </AuthContext.Provider>
  );
}

export function useAuth() {
  const ctx = useContext(AuthContext);
  if (!ctx) throw new Error("useAuth must be used inside <AuthProvider>");
  return ctx;
}
