import { type ChangeEvent } from "react";
import { Building2, Globe, Mail, MapPin } from "lucide-react";
import { isValidPhoneNumber } from "react-phone-number-input";
import clsx from "clsx";
import { ClipLoader } from "react-spinners";
import type { AgencyRegisterRequest } from "../../../../../types/api/auth";
import ValidationItem from "../../../../common/UI/ValidationItem";
import PhoneNumberInput from "../../common/PhoneNumberInput";

interface AgencyDetailsFormProps {
  agency: AgencyRegisterRequest;
  onChange: (agency: AgencyRegisterRequest) => void;
  onBack: () => void;
  onSubmit: () => void;
  isSubmitting: boolean;
  error: string;
}

const EMAIL_REGEX = /^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\.[a-zA-Z]{2,}$/;

const INPUT_BASE_CLASS =
  "focus:border-secondary w-full rounded-2xl border border-gray-500 bg-transparent py-4 pl-26 text-[1.6rem] transition-all duration-200 outline-none focus:ring-2 focus:ring-blue-300 md:border-gray-400";

const INPUT_ICON_CLASS = "absolute top-1/2 left-10 -translate-y-1/2 text-gray-400";

const ERROR_ID = "agency-register-error";

const AgencyDetailsForm = ({
  agency,
  onChange,
  onBack,
  onSubmit,
  isSubmitting,
  error,
}: AgencyDetailsFormProps) => {
  const agencyNameLength = agency.agencyName.trim().length;

  const isAgencyNameValid = agencyNameLength >= 2 && agencyNameLength <= 150;

  const isBusinessEmailValid =
    agency.agencyBusinessEmail.length <= 150 && EMAIL_REGEX.test(agency.agencyBusinessEmail.trim());

  const isBusinessPhoneValid =
    isValidPhoneNumber(agency.agencyBusinessPhone) && agency.agencyBusinessPhone.length <= 30;

  const isWebsiteValid = (() => {
    const website = agency.agencyWebsiteUrl.trim();

    if (!website) return true;

    if (website.length > 255) return false;

    try {
      const url = new URL(
        /^[a-zA-Z][a-zA-Z\d+.-]*:/.test(website) ? website : `https://${website}`,
      );

      return (url.protocol === "http:" || url.protocol === "https:") && url.hostname.includes(".");
    } catch {
      return false;
    }
  })();

  const isCityValid = agency.agencyCity.trim().length >= 2 && agency.agencyCity.length <= 100;

  const isAddressValid =
    agency.agencyAddress.trim().length >= 3 && agency.agencyAddress.length <= 255;

  const isDescriptionValid = agency.agencyDescription.length <= 2000;

  const isFormValid =
    isAgencyNameValid &&
    isBusinessEmailValid &&
    isBusinessPhoneValid &&
    isWebsiteValid &&
    isCityValid &&
    isAddressValid &&
    isDescriptionValid;

  const handleInputChange = (event: ChangeEvent<HTMLInputElement | HTMLTextAreaElement>) => {
    const { name, value } = event.target;

    onChange({
      ...agency,
      [name]: value,
    });
  };

  const handlePhoneChange = (agencyBusinessPhone: string) => {
    onChange({
      ...agency,
      agencyBusinessPhone,
    });
  };

  return (
    <div className="space-y-6">
      <div className="space-y-1">
        <h2 className="text-secondary text-[2rem] font-semibold">Agency information</h2>

        <p className="text-[1.4rem] text-gray-500">
          Provide the business information of your real estate agency.
        </p>
      </div>

      {/* Agency name */}
      <div className="relative">
        <Building2 size={20} aria-hidden="true" className={INPUT_ICON_CLASS} />

        <input
          type="text"
          name="agencyName"
          id="agencyName"
          maxLength={150}
          value={agency.agencyName}
          placeholder="Agency name"
          required
          aria-label="Agency name"
          onChange={handleInputChange}
          className={clsx(INPUT_BASE_CLASS, "pr-4")}
        />
      </div>

      <ValidationItem
        valid={isAgencyNameValid}
        label="Agency name must contain between 2 and 150 characters."
      />

      {/* Business email */}
      <div className="relative">
        <Mail size={20} aria-hidden="true" className={INPUT_ICON_CLASS} />

        <input
          type="email"
          name="agencyBusinessEmail"
          id="agencyBusinessEmail"
          autoComplete="organization"
          autoCapitalize="none"
          spellCheck={false}
          maxLength={150}
          value={agency.agencyBusinessEmail}
          placeholder="Business email"
          required
          aria-label="Business email"
          onChange={handleInputChange}
          className={clsx(INPUT_BASE_CLASS, "pr-4")}
        />
      </div>

      <ValidationItem
        valid={isBusinessEmailValid}
        label="Please enter a valid business email (maximum 150 characters)."
      />

      {/* Business phone */}
      <PhoneNumberInput
        value={agency.agencyBusinessPhone}
        onChange={handlePhoneChange}
        defaultCountry="AZ"
        placeholder="Business phone number"
      />

      <ValidationItem
        valid={isBusinessPhoneValid}
        label="Please enter a valid international phone number (maximum 30 characters)."
      />

      {/* Website */}
      <div className="relative">
        <Globe size={20} aria-hidden="true" className={INPUT_ICON_CLASS} />

        <input
          type="text"
          name="agencyWebsiteUrl"
          id="agencyWebsiteUrl"
          autoComplete="url"
          maxLength={255}
          value={agency.agencyWebsiteUrl}
          placeholder="Agency website (optional)"
          aria-label="Agency website"
          onChange={handleInputChange}
          className={clsx(INPUT_BASE_CLASS, "pr-4")}
        />
      </div>

      <ValidationItem
        valid={isWebsiteValid}
        label="Enter a valid website URL (maximum 255 characters), or leave it empty."
      />

      {/* City */}
      <div className="relative">
        <MapPin size={20} aria-hidden="true" className={INPUT_ICON_CLASS} />

        <input
          type="text"
          name="agencyCity"
          id="agencyCity"
          autoComplete="address-level2"
          maxLength={100}
          value={agency.agencyCity}
          placeholder="City"
          required
          aria-label="City"
          onChange={handleInputChange}
          className={clsx(INPUT_BASE_CLASS, "pr-4")}
        />
      </div>

      <ValidationItem valid={isCityValid} label="City must contain between 2 and 100 characters." />

      {/* Address */}
      <div className="relative">
        <MapPin size={20} aria-hidden="true" className={INPUT_ICON_CLASS} />

        <input
          type="text"
          name="agencyAddress"
          id="agencyAddress"
          autoComplete="street-address"
          maxLength={255}
          value={agency.agencyAddress}
          placeholder="Business address"
          required
          aria-label="Business address"
          onChange={handleInputChange}
          className={clsx(INPUT_BASE_CLASS, "pr-4")}
        />
      </div>

      <ValidationItem
        valid={isAddressValid}
        label="Business address must contain between 3 and 255 characters."
      />

      {/* Description */}
      <div>
        <textarea
          name="agencyDescription"
          id="agencyDescription"
          rows={4}
          maxLength={2000}
          value={agency.agencyDescription}
          placeholder="Tell us about your agency (optional)"
          aria-label="Agency description"
          onChange={handleInputChange}
          className="focus:border-secondary w-full resize-y rounded-2xl border border-gray-500 bg-transparent px-6 py-4 text-[1.6rem] transition-all duration-200 outline-none focus:ring-2 focus:ring-blue-300 md:border-gray-400"
        />

        <div className="mt-1 flex justify-end">
          <span className="text-[1.2rem] text-gray-500">
            {agency.agencyDescription.length}/2000
          </span>
        </div>
      </div>

      <ValidationItem
        valid={isDescriptionValid}
        label="Description must not exceed 2000 characters."
      />

      {/* API error */}
      <p
        id={ERROR_ID}
        role="alert"
        aria-live="polite"
        className={clsx(
          "overflow-hidden text-[1.4rem] font-medium text-red-500 transition-all duration-300",
          error ? "max-h-20 opacity-100" : "max-h-0 opacity-0",
        )}
      >
        {error}
      </p>

      {/* Navigation */}
      <div className="flex flex-col-reverse gap-3 sm:flex-row">
        <button
          type="button"
          onClick={onBack}
          disabled={isSubmitting}
          className="text-secondary flex flex-1 cursor-pointer items-center justify-center rounded-xl border border-gray-400 py-3 text-[1.6rem] font-semibold transition hover:bg-gray-100 disabled:cursor-not-allowed disabled:opacity-60"
        >
          Back
        </button>

        <button
          type="button"
          onClick={onSubmit}
          disabled={!isFormValid || isSubmitting}
          className="bg-secondary hover:bg-secondary/90 flex flex-1 cursor-pointer items-center justify-center rounded-xl py-3 text-[1.6rem] font-semibold text-white transition active:scale-95 disabled:cursor-not-allowed disabled:opacity-80"
        >
          {isSubmitting ? (
            <ClipLoader
              color="#ffffff"
              size={25}
              speedMultiplier={0.8}
              aria-label="Registering agency"
              cssOverride={{ borderWidth: "3px" }}
            />
          ) : (
            "Submit registration"
          )}
        </button>
      </div>
    </div>
  );
};

export default AgencyDetailsForm;
