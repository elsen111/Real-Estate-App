import PhoneInput from "react-phone-number-input";
import type { Country } from "react-phone-number-input";
import CountrySelector from "./CountrySelector";
import "react-phone-number-input/style.css";

interface PhoneNumberInputProps {
  value: string;
  onChange: (value: string) => void;
  defaultCountry?: Country;
  placeholder?: string;
  disabled?: boolean;
}

const PhoneNumberInput = ({
  value,
  onChange,
  defaultCountry = "AZ",
  placeholder = "Enter phone number",
  disabled = false,
}: PhoneNumberInputProps) => {
  return (
    <div className="focus-within:border-secondary flex w-full items-center gap-5 rounded-2xl border border-gray-500 bg-transparent py-4 pr-4 pl-10 text-[1.6rem] transition-all duration-200 focus-within:ring-2 focus-within:ring-blue-300 md:border-gray-400">
      <PhoneInput
        international
        withCountryCallingCode
        defaultCountry={defaultCountry}
        value={value || undefined}
        onChange={(phone) => onChange(phone ?? "")}
        disabled={disabled}
        placeholder={placeholder}
        countrySelectComponent={CountrySelector}
        className="estate-phone-input min-w-0 flex-1"
      />
    </div>
  );
};

export default PhoneNumberInput;
