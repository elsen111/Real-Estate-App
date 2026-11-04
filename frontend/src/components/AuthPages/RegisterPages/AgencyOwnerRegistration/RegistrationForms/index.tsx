import { useState } from "react";
import { useNavigate } from "react-router-dom";
import toast from "react-hot-toast";
import type {
  AgencyOwnerRegisterRequest,
  AgencyRegisterRequest,
  UserRegisterRequest,
} from "../../../../../types/api/auth";
import { authService } from "../../../../../services/authService";
import { getErrorMessage } from "../../../../../utils/getErrorMessage";
import RegistrationStepper from "./RegistrationStepper";
import AgencyOwnerDetailsForm from "./AgencyOwnerDetailsForm";
import AgencyDetailsForm from "./AgencyDetailsForm";
import AuthSwitch from "../../../common/AuthSwitch";

const INITIAL_FORM_DATA: AgencyOwnerRegisterRequest = {
  owner: {
    fullName: "",
    email: "",
    password: "",
    phoneNumber: "",
  },
  agency: {
    agencyName: "",
    agencyDescription: "",
    agencyBusinessEmail: "",
    agencyBusinessPhone: "",
    agencyWebsiteUrl: "",
    agencyCity: "",
    agencyAddress: "",
  },
};

const SUCCESS_TOAST_OPTIONS = {
  duration: 3000,
  position: "top-center",
  iconTheme: {
    primary: "#000066",
    secondary: "#eee",
  },
  style: {
    background: "#eee",
    color: "#100a55",
    fontSize: "1.6rem",
    fontWeight: "medium",
  },
} as const;

const AgencyOwnerRegisterPage = () => {
  const navigate = useNavigate();

  const [step, setStep] = useState(1);

  const [formData, setFormData] = useState<AgencyOwnerRegisterRequest>(INITIAL_FORM_DATA);

  const [error, setError] = useState("");
  const [isSubmitting, setIsSubmitting] = useState(false);

  const handleOwnerChange = (owner: UserRegisterRequest) => {
    setError("");

    setFormData((prev) => ({
      ...prev,
      owner,
    }));
  };

  const handleAgencyChange = (agency: AgencyRegisterRequest) => {
    setError("");

    setFormData((prev) => ({
      ...prev,
      agency,
    }));
  };

  const handleContinue = () => {
    setError("");
    setStep(2);
  };

  const handleBack = () => {
    setError("");
    setStep(1);
  };

  const handleSubmit = async () => {
    if (isSubmitting) return;

    setError("");
    setIsSubmitting(true);

    try {
      const request: AgencyOwnerRegisterRequest = {
        owner: {
          ...formData.owner,
          fullName: formData.owner.fullName.trim(),
          email: formData.owner.email.trim().toLowerCase(),
          phoneNumber: formData.owner.phoneNumber.trim(),
        },
        agency: {
          ...formData.agency,
          agencyName: formData.agency.agencyName.trim(),
          agencyDescription: formData.agency.agencyDescription.trim(),
          agencyBusinessEmail: formData.agency.agencyBusinessEmail.trim().toLowerCase(),
          agencyBusinessPhone: formData.agency.agencyBusinessPhone.trim(),
          agencyWebsiteUrl: formData.agency.agencyWebsiteUrl.trim(),
          agencyCity: formData.agency.agencyCity.trim(),
          agencyAddress: formData.agency.agencyAddress.trim(),
        },
      };

      await authService.registerAgencyOwner(request);

      toast.success("Registration submitted successfully!", SUCCESS_TOAST_OPTIONS);

      navigate("/auth/agency-registration/submitted", { replace: true });
    } catch (err) {
      setError(getErrorMessage(err, "Agency registration failed. Please try again."));
    } finally {
      setIsSubmitting(false);
    }
  };

  return (
    <section className="xxs:w-full xxxs:w-[94%] mx-auto w-[90%] max-w-200">
      <form onSubmit={(event) => event.preventDefault()} className="space-y-6" noValidate>
        <RegistrationStepper currentStep={step} />

        {step === 1 && (
          <AgencyOwnerDetailsForm
            owner={formData.owner}
            onChange={handleOwnerChange}
            onContinue={handleContinue}
          />
        )}

        {step === 2 && (
          <AgencyDetailsForm
            agency={formData.agency}
            onChange={handleAgencyChange}
            onBack={handleBack}
            onSubmit={handleSubmit}
            isSubmitting={isSubmitting}
            error={error}
          />
        )}

        <AuthSwitch switchTo="toLogin" />
      </form>
    </section>
  );
};

export default AgencyOwnerRegisterPage;
