import { useState, type ChangeEvent } from "react";
import { Eye, EyeClosed, Lock, Mail, User } from "lucide-react";
import { isValidPhoneNumber } from "react-phone-number-input";
import clsx from "clsx";
import type { UserRegisterRequest } from "../../../../../types/api/auth";
import ValidationItem from "../../../../common/UI/ValidationItem";
import PhoneNumberInput from "../../common/PhoneNumberInput";

interface AgencyOwnerDetailsFormProps {
  owner: UserRegisterRequest;
  onChange: (owner: UserRegisterRequest) => void;
  onContinue: () => void;
}

const EMAIL_REGEX = /^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\.[a-zA-Z]{2,}$/;

const FULL_NAME_MIN_LENGTH = 3;
const FULL_NAME_MAX_LENGTH = 50;
const PASSWORD_MIN_LENGTH = 8;

const INPUT_BASE_CLASS =
  "focus:border-secondary w-full rounded-2xl border border-gray-500 bg-transparent py-4 pl-26 text-[1.6rem] transition-all duration-200 outline-none focus:ring-2 focus:ring-blue-300 md:border-gray-400";

const INPUT_ICON_CLASS = "absolute top-1/2 left-10 -translate-y-1/2 text-gray-400";

const TOGGLE_BUTTON_CLASS =
  "absolute top-1/2 right-10 -translate-y-1/2 cursor-pointer text-gray-400";

const AgencyOwnerDetailsForm = ({ owner, onChange, onContinue }: AgencyOwnerDetailsFormProps) => {
  const [confirmPassword, setConfirmPassword] = useState("");
  const [passwordVisible, setPasswordVisible] = useState(false);
  const [confirmPasswordVisible, setConfirmPasswordVisible] = useState(false);

  const fullNameLength = owner.fullName.trim().length;

  const isFullNameValid =
    fullNameLength >= FULL_NAME_MIN_LENGTH && fullNameLength <= FULL_NAME_MAX_LENGTH;

  const isEmailValid = owner.email.length <= 150 && EMAIL_REGEX.test(owner.email.trim());

  const isPhoneValid = isValidPhoneNumber(owner.phoneNumber);

  const isPasswordValid =
    owner.password.length >= PASSWORD_MIN_LENGTH &&
    /[A-Z]/.test(owner.password) &&
    /[a-z]/.test(owner.password) &&
    /\d/.test(owner.password) &&
    /[^A-Za-z0-9]/.test(owner.password);

  const doPasswordsMatch = confirmPassword.length > 0 && confirmPassword === owner.password;

  const isFormValid =
    isFullNameValid && isEmailValid && isPhoneValid && isPasswordValid && doPasswordsMatch;

  const handleInputChange = (event: ChangeEvent<HTMLInputElement>) => {
    const { name, value } = event.target;

    onChange({
      ...owner,
      [name]: value,
    });
  };

  const handlePhoneChange = (phoneNumber: string) => {
    onChange({
      ...owner,
      phoneNumber,
    });
  };

  return (
    <div className="space-y-6">
      <div className="space-y-1">
        <h2 className="text-secondary text-[2rem] font-semibold">Personal information</h2>

        <p className="text-[1.4rem] text-gray-500">Create your account as the agency owner.</p>
      </div>

      {/* Full name */}
      <div className="relative">
        <User size={20} aria-hidden="true" className={INPUT_ICON_CLASS} />

        <input
          type="text"
          name="fullName"
          id="ownerFullName"
          autoComplete="name"
          maxLength={FULL_NAME_MAX_LENGTH}
          value={owner.fullName}
          placeholder="Full name"
          required
          aria-label="Full name"
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
          id="ownerEmail"
          autoComplete="email"
          autoCapitalize="none"
          spellCheck={false}
          maxLength={150}
          value={owner.email}
          placeholder="Personal email"
          required
          aria-label="Personal email"
          onChange={handleInputChange}
          className={clsx(INPUT_BASE_CLASS, "pr-4")}
        />
      </div>

      <ValidationItem
        valid={isEmailValid}
        label="Please enter a valid email address (maximum 150 characters)."
      />

      {/* Phone */}
      <PhoneNumberInput
        value={owner.phoneNumber}
        onChange={handlePhoneChange}
        defaultCountry="AZ"
        placeholder="Personal phone number"
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
          id="ownerPassword"
          autoComplete="new-password"
          value={owner.password}
          placeholder="Password"
          required
          aria-label="Password"
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
          id="ownerConfirmPassword"
          autoComplete="new-password"
          value={confirmPassword}
          placeholder="Confirm password"
          required
          aria-label="Confirm password"
          aria-invalid={confirmPassword.length > 0 && !doPasswordsMatch}
          onChange={(event) => setConfirmPassword(event.target.value)}
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

      <ValidationItem valid={doPasswordsMatch} label="Passwords matching." />

      <button
        type="button"
        onClick={onContinue}
        disabled={!isFormValid}
        className="bg-secondary hover:bg-secondary/90 flex w-full cursor-pointer items-center justify-center rounded-xl py-3 text-[1.6rem] font-semibold text-white transition active:scale-95 disabled:cursor-not-allowed disabled:opacity-80"
      >
        Continue to agency details
      </button>
    </div>
  );
};

export default AgencyOwnerDetailsForm;
