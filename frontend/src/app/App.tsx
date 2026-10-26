import { RouterProvider } from "react-router-dom";
import { AppRouter } from "./routes";
import { AuthProvider } from "../context/AuthContext";
import { QueryClientProvider } from "@tanstack/react-query";
import queryClient from "../config/queryClient";

function App() {
  return (
    <>
      <AuthProvider>
        <QueryClientProvider client={queryClient}>
          <RouterProvider router={AppRouter} />
        </QueryClientProvider>
      </AuthProvider>
    </>
  );
}

export default App;
