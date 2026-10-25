import HomePage from "../../pages/public/HomePage";
import PublicLayout from "../layouts/PublicLayout";
import LoginPage from "../../pages/auth/LoginPage";
import RegisterPage from "../../pages/auth/RegisterPage";
import RentPage from "../../pages/public/RentPage";
import BuyPage from "../../pages/public/BuyPage";
import PricingPage from "../../pages/public/PricingPage";
import MapPage from "../../pages/public/MapPage";
import PropertySearchPage from "../../pages/public/PropertySearchPage";
import PropertyDetailsPage from "../../pages/public/PropertyDetailsPage";
import AgencyDetailsPage from "../../pages/public/AgencyDetailsPage";

export const PublicRoutes = [
      {
            path: "/",
            element: <PublicLayout />,
            children: [
                   {index: true, element: <HomePage />},
                   {path: "/rent", element: <RentPage />},
                   {path: "/buy", element: <BuyPage />},
                   {path: "/map", element: <MapPage />},
                   {path: "/pricing", element: <PricingPage />},
                   {path: "/properties", element: <PropertySearchPage />},
                   {path: "/properties/:id", element: <PropertyDetailsPage />},
                   {path: "/agencies/:id", element: <AgencyDetailsPage />}
            ],
      },

      {
            path: "/login",
            element: <LoginPage />
      },

      {
            path: "/register",
            element: <RegisterPage />
      },
]