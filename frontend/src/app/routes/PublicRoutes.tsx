import HomePage from "../../pages/public/HomePage";
import PublicLayout from "../layouts/PublicLayout";
import LoginPage from "../../pages/auth/login/LoginPage";
import RentPage from "../../pages/public/RentPage";
import BuyPage from "../../pages/public/BuyPage";
import PricingPage from "../../pages/public/PricingPage";
import MapPage from "../../pages/public/MapPage";
import PropertySearchPage from "../../pages/public/PropertySearchPage";
import PropertyDetailsPage from "../../pages/public/PropertyDetailsPage";
import AgencyDetailsPage from "../../pages/public/AgencyDetailsPage";
import AuthLayout from "../layouts/AuthLayout";
import ClientRegistrationPage from "../../pages/auth/register/ClientRegistrationPage";
import RegisterSelectionPage from "../../pages/auth/register/RegisterSelectionPage";
import AgencyOnwerRegistrationPage from "../../pages/auth/register/AgencyOwnerRegistrationPage";
import AgencyRegistrationSubmissionPage from "../../pages/public/AgencyRegistrationSubmissionPage";

export const PublicRoutes = [
  {
    path: "/",
    element: <PublicLayout />,
    children: [
      { index: true, element: <HomePage /> },
      { path: "rent", element: <RentPage /> },
      { path: "buy", element: <BuyPage /> },
      { path: "map", element: <MapPage /> },
      { path: "pricing", element: <PricingPage /> },
      { path: "properties", element: <PropertySearchPage /> },
      { path: "properties/:id", element: <PropertyDetailsPage /> },
      { path: "agencies/:id", element: <AgencyDetailsPage /> },
    ],
  },

  {
    path: "/auth",
    element: <AuthLayout />,
    children: [
      { path: "login", element: <LoginPage /> },
      { path: "register", element: <RegisterSelectionPage /> },
      { path: "register/client", element: <ClientRegistrationPage /> },
      { path: "register/agency-owner", element: <AgencyOnwerRegistrationPage /> },
      {
        path: "/auth/agency-registration/submitted",
        element: <AgencyRegistrationSubmissionPage />,
      },
    ],
  },
];
