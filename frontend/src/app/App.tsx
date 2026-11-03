import { RouterProvider } from "react-router-dom";
import { AppRouter } from "./routes";
import { AuthProvider } from "../context/AuthContext";
import { QueryClientProvider } from "@tanstack/react-query";
import queryClient from "../config/queryClient";
import { Toaster } from "react-hot-toast";

function App() {
  return (
    <>
      <AuthProvider>
        <QueryClientProvider client={queryClient}>
          <Toaster
            position="top-center"
            reverseOrder={true}
            gutter={8}
            containerStyle={{}}
            containerClassName="text-center px-2! py-1! sm:px-3 sm:py-2"
            toastOptions={{
              duration: 5000,
              removeDelay: 1000,
              style: {
                background: "#363636",
                color: "#fff",
              },

              success: {
                duration: 3000,
                iconTheme: {
                  primary: "green",
                  secondary: "black",
                },
              },
            }}
          />
          <RouterProvider router={AppRouter} />
        </QueryClientProvider>
      </AuthProvider>
    </>
  );
}

export default App;
