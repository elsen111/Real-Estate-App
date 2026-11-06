import { useState, type ChangeEvent, type FormEvent } from "react";
import { Eye, EyeClosed, Mail, Lock, User } from "lucide-react";
import { isValidPhoneNumber } from "react-phone-number-input";
import { useNavigate } from "react-router-dom";
import { ClipLoader } from "react-spinners";
import toast from "react-hot-toast";
import clsx from "clsx";

import AuthSwitch from "../../common/AuthSwitch";
import ValidationItem from "../../../common/UI/ValidationItem";
import PhoneNumberInput from "../common/PhoneNumberInput";
import { useAuth } from "../../../../context/AuthContext";
import { getErrorMessage } from "../../../../utils/getErrorMessage";
import type { UserRegisterRequest } from "../../../../types/api/auth";

const EMAIL_REGEX = /^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\.[a-zA-Z]{2,}$/;

const FULL_NAME_MIN_LENGTH = 3;
const FULL_NAME_MAX_LENGTH = 50;
const PASSWORD_MIN_LENGTH = 8;

const ERROR_ID = "register-error";

const INITIAL_FORM_DATA: UserRegisterRequest = {
  fullName: "",
  email: "",
  password: "",
  phoneNumber: "",
};

const INPUT_BASE_CLASS =
  "focus:border-secondary w-full rounded-2xl border border-gray-500 bg-transparent py-4 pl-26 text-[1.6rem] transition-all duration-200 outline-none focus:ring-2 focus:ring-blue-300 md:border-gray-400";

const INPUT_ICON_CLASS = "absolute top-1/2 left-10 -translate-y-1/2 text-gray-400";

const TOGGLE_BUTTON_CLASS =
  "absolute top-1/2 right-10 -translate-y-1/2 cursor-pointer text-gray-400";

const SUCCESS_TOAST_OPTIONS = {
  duration: 1000,
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

const ClientRegisterForm = () => {
  const navigate = useNavigate();
  const { registerClient } = useAuth();

  const [formData, setFormData] = useState<UserRegisterRequest>(INITIAL_FORM_DATA);
  const [confirmPassword, setConfirmPassword] = useState("");
  const [passwordVisible, setPasswordVisible] = useState(false);
  const [confirmPasswordVisible, setConfirmPasswordVisible] = useState(false);
  const [error, setError] = useState("");
  const [isSubmitting, setIsSubmitting] = useState(false);

  const fullNameLength = formData.fullName.trim().length;
  const isFullNameValid =
    fullNameLength >= FULL_NAME_MIN_LENGTH && fullNameLength <= FULL_NAME_MAX_LENGTH;

  const isEmailValid = EMAIL_REGEX.test(formData.email.trim());

  const isPhoneValid = isValidPhoneNumber(formData.phoneNumber);

  const isPasswordValid =
    formData.password.length >= PASSWORD_MIN_LENGTH &&
    /[A-Z]/.test(formData.password) &&
    /[a-z]/.test(formData.password) &&
    /\d/.test(formData.password) &&
    /[^A-Za-z0-9]/.test(formData.password);

  const doPasswordsMatch = confirmPassword.length > 0 && confirmPassword === formData.password;

  const isFormValid =
    isFullNameValid && isEmailValid && isPhoneValid && isPasswordValid && doPasswordsMatch;

  const clearError = () => {
    if (error) setError("");
  };

  const handleInputChange = (event: ChangeEvent<HTMLInputElement>) => {
    clearError();

    const { name, value } = event.target;

    setFormData((prev) => ({ ...prev, [name]: value }));
  };

  const handleConfirmPasswordChange = (event: ChangeEvent<HTMLInputElement>) => {
    clearError();
    setConfirmPassword(event.target.value);
  };

  const handlePhoneChange = (phoneNumber: string) => {
    clearError();
    setFormData((prev) => ({ ...prev, phoneNumber }));
  };

  const handleSubmit = async (event: FormEvent<HTMLFormElement>) => {
    event.preventDefault();

    if (!isFormValid || isSubmitting) return;

    setError("");
    setIsSubmitting(true);

    try {
      await registerClient({
        ...formData,
        fullName: formData.fullName.trim(),
        email: formData.email.trim().toLowerCase(),
      });

      toast.success("User registration is successful!", SUCCESS_TOAST_OPTIONS);

      navigate("/dashboard", { replace: true });
    } catch (err) {
      setError(getErrorMessage(err, "Registration failed."));
    } finally {
      setIsSubmitting(false);
    }
  };

  return (
    <section className="xxs:w-full xxxs:w-[94%] mx-auto w-[90%] max-w-200">
      <form onSubmit={handleSubmit} className="space-y-6" noValidate>
        {/* Full name */}
        <div className="relative">
          <User size={20} aria-hidden="true" className={INPUT_ICON_CLASS} />

          <input
            type="text"
            name="fullName"
            id="fullName"
            autoComplete="name"
            value={formData.fullName}
            placeholder="Full name"
            required
            aria-label="Full name"
            aria-invalid={!!error}
            aria-describedby={ERROR_ID}
            onChange={handleInputChange}
            className={clsx(INPUT_BASE_CLASS, "pr-4")}
          />
        </div>
        <ValidationItem
          valid={isFullNameValid}
          label={`Full name must be between ${FULL_NAME_MIN_LENGTH} and ${FULL_NAME_MAX_LENGTH} characters.`}
        />

        {/* Email */}
        <div className="relative">
          <Mail size={20} aria-hidden="true" className={INPUT_ICON_CLASS} />

          <input
            type="email"
            name="email"
            id="email"
            autoComplete="email"
            autoCapitalize="none"
            spellCheck={false}
            value={formData.email}
            placeholder="Email"
            required
            aria-label="Email"
            aria-invalid={!!error}
            aria-describedby={ERROR_ID}
            onChange={handleInputChange}
            className={clsx(INPUT_BASE_CLASS, "pr-4")}
          />
        </div>
        <ValidationItem valid={isEmailValid} label="Email must be a valid email address." />

        {/* Phone */}
        <PhoneNumberInput
          value={formData.phoneNumber}
          onChange={handlePhoneChange}
          defaultCountry="AZ"
          placeholder="Enter phone number"
        />
        <ValidationItem
          valid={isPhoneValid}
          label="Please enter a valid international phone number."
        />

        {/* Password */}
        <div className="relative">
          <Lock size={20} aria-hidden="true" className={INPUT_ICON_CLASS} />

          <input
            type={passwordVisible ? "text" : "password"}
            name="password"
            id="password"
            autoComplete="new-password"
            value={formData.password}
            placeholder="Password"
            required
            aria-label="Password"
            aria-invalid={!!error}
            aria-describedby={ERROR_ID}
            onChange={handleInputChange}
            className={clsx(INPUT_BASE_CLASS, "pr-16")}
          />

          <button
            type="button"
            onClick={() => setPasswordVisible((prev) => !prev)}
            aria-label={passwordVisible ? "Hide password" : "Show password"}
            aria-pressed={passwordVisible}
            className={TOGGLE_BUTTON_CLASS}
          >
            {passwordVisible ? (
              <Eye size={20} aria-hidden="true" />
            ) : (
              <EyeClosed size={20} aria-hidden="true" />
            )}
          </button>
        </div>
        <ValidationItem
          valid={isPasswordValid}
          label={`Password must contain at least ${PASSWORD_MIN_LENGTH} characters, including an uppercase letter, a lowercase letter, a number, and a special character.`}
        />

        {/* Confirm password */}
        <div className="relative">
          <Lock size={20} aria-hidden="true" className={INPUT_ICON_CLASS} />

          <input
            type={confirmPasswordVisible ? "text" : "password"}
            name="confirmPassword"
            id="confirmPassword"
            autoComplete="new-password"
            value={confirmPassword}
            placeholder="Confirm Password"
            required
            aria-label="Confirm password"
            aria-invalid={confirmPassword.length > 0 && !doPasswordsMatch}
            onChange={handleConfirmPasswordChange}
            className={clsx(INPUT_BASE_CLASS, "pr-16")}
          />

          <button
            type="button"
            onClick={() => setConfirmPasswordVisible((prev) => !prev)}
            aria-label={confirmPasswordVisible ? "Hide confirm password" : "Show confirm password"}
            aria-pressed={confirmPasswordVisible}
            className={TOGGLE_BUTTON_CLASS}
          >
            {confirmPasswordVisible ? (
              <Eye size={20} aria-hidden="true" />
            ) : (
              <EyeClosed size={20} aria-hidden="true" />
            )}
          </button>
        </div>
        <ValidationItem valid={doPasswordsMatch} label="Passwords matching" />

        {/* Error message */}
        <p
          id={ERROR_ID}
          role="alert"
          aria-live="polite"
          className={clsx(
            "overflow-hidden text-[1.4rem] font-medium text-red-500 transition-all duration-300 ease-in-out",
            error
              ? "mt-2 max-h-10 translate-y-0 opacity-100"
              : "mt-0 max-h-0 -translate-y-1 opacity-0",
          )}
        >
          {error}
        </p>

        <button
          type="submit"
          disabled={!isFormValid || isSubmitting}
          className="bg-secondary hover:bg-secondary/90 flex w-full cursor-pointer items-center justify-center rounded-xl py-3 text-[1.6rem] font-semibold text-white transition active:scale-95 disabled:cursor-not-allowed disabled:opacity-80"
        >
          {isSubmitting ? (
            <ClipLoader
              color="#ffffff"
              size={25}
              speedMultiplier={0.8}
              aria-label="Registering"
              cssOverride={{ borderWidth: "3px" }}
            />
          ) : (
            "Register"
          )}
        </button>

        <AuthSwitch switchTo="toLogin" />
      </form>
    </section>
  );
};

export default ClientRegisterForm;
