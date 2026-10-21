import { RouterProvider } from "react-router-dom"
import { AppRouter } from "./routes"
import { AuthProvider } from "../context/AuthContext"

function App() {

  return (
    <>
      <AuthProvider>
        <RouterProvider router={AppRouter} />
      </AuthProvider>
    </>
  )
}

export default App
