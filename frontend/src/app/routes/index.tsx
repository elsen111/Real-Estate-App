

import { createBrowserRouter } from 'react-router-dom'
import { PublicRoutes } from './PublicRoutes'
import NotFoundPage from '../../pages/notFound/NotFoundPage'

export const AppRouter = createBrowserRouter([
  ...PublicRoutes,
  {path: "*", element: <NotFoundPage />}
])