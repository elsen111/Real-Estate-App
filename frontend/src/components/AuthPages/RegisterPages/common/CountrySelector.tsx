import { useEffect, useMemo, useRef, useState } from "react";
import type { ElementType } from "react";
import { Check, ChevronDown, Search } from "lucide-react";
import { getCountryCallingCode } from "react-phone-number-input";
import flags from "react-phone-number-input/flags";

type CountryOption = {
  value?: string;
  label: string;
};

interface CountrySelectorProps {
  value?: string;
  onChange: (country?: string) => void;
  options: CountryOption[];
  disabled?: boolean;
  readOnly?: boolean;
  tabIndex?: number;
}

const flagMap = flags as unknown as Record<string, ElementType>;

const Flag = ({ country, label }: { country?: string; label?: string }) => {
  const FlagSvg = country ? flagMap[country] : undefined;

  if (!FlagSvg) {
    return <span className="text-[1.6rem]">🌐</span>;
  }

  return (
    <span className="inline-block h-[1.8rem] w-[2.6rem] shrink-0 overflow-hidden rounded-[0.3rem] border border-gray-200 [&>svg]:h-full [&>svg]:w-full">
      <FlagSvg title={label ?? country} />
    </span>
  );
};

const CountrySelector = ({
  value,
  onChange,
  options,
  disabled = false,
  readOnly = false,
  tabIndex,
}: CountrySelectorProps) => {
  const [isOpen, setIsOpen] = useState(false);
  const [search, setSearch] = useState("");

  const containerRef = useRef<HTMLDivElement>(null);
  const searchRef = useRef<HTMLInputElement>(null);

  const selectedCountry = options.find((country) => country.value === value);

  const filteredCountries = useMemo(() => {
    const query = search.trim().toLowerCase();

    return options.filter(({ value: country, label }) => {
      if (!country || !label) return false;

      let callingCode = "";

      try {
        callingCode = `+${getCountryCallingCode(country as never)}`;
      } catch {
        return false;
      }

      return (
        label.toLowerCase().includes(query) ||
        country.toLowerCase().includes(query) ||
        callingCode.includes(query)
      );
    });
  }, [options, search]);

  useEffect(() => {
    if (!isOpen) return;

    const handleOutsideClick = (event: MouseEvent) => {
      if (containerRef.current && !containerRef.current.contains(event.target as Node)) {
        setIsOpen(false);
        setSearch("");
      }
    };

    const handleEscape = (event: KeyboardEvent) => {
      if (event.key === "Escape") {
        setIsOpen(false);
        setSearch("");
      }
    };

    document.addEventListener("mousedown", handleOutsideClick);
    document.addEventListener("keydown", handleEscape);

    searchRef.current?.focus();

    return () => {
      document.removeEventListener("mousedown", handleOutsideClick);
      document.removeEventListener("keydown", handleEscape);
    };
  }, [isOpen]);

  const handleSelect = (country?: string) => {
    onChange(country);
    setIsOpen(false);
    setSearch("");
  };

  return (
    <div ref={containerRef} className="relative shrink-0">
      <button
        type="button"
        disabled={disabled || readOnly}
        tabIndex={tabIndex}
        aria-label="Select country"
        aria-expanded={isOpen}
        aria-haspopup="listbox"
        onClick={() => setIsOpen((prev) => !prev)}
        className="flex items-center gap-2 rounded-lg py-1 pr-1 transition hover:opacity-80 focus-visible:ring-2 focus-visible:ring-blue-300 focus-visible:outline-none disabled:cursor-not-allowed"
      >
        <Flag country={value} label={selectedCountry?.label} />

        <ChevronDown
          size={16}
          className={`text-gray-400 transition-transform ${isOpen ? "rotate-180" : ""}`}
        />
      </button>

      {isOpen && (
        <div className="absolute top-full left-0 z-50 mt-5 w-[28rem] overflow-hidden rounded-xl border border-gray-200 bg-white shadow-xl">
          <div className="border-b border-gray-100 p-3">
            <div className="flex items-center gap-2 rounded-lg border border-gray-200 px-3">
              <Search size={17} className="shrink-0 text-gray-400" />

              <input
                ref={searchRef}
                type="search"
                value={search}
                onChange={(event) => setSearch(event.target.value)}
                placeholder="Search country or code..."
                aria-label="Search countries"
                className="w-full bg-transparent py-3 text-[1.4rem] text-gray-800 outline-none placeholder:text-gray-400"
              />
            </div>
          </div>

          <div role="listbox" aria-label="Countries" className="max-h-64 overflow-y-auto p-1">
            {filteredCountries.length > 0 ? (
              filteredCountries.map(({ value: country, label }) => {
                const selected = country === value;

                return (
                  <button
                    key={country}
                    type="button"
                    role="option"
                    aria-selected={selected}
                    onClick={() => handleSelect(country)}
                    className={`flex w-full items-center gap-3 rounded-lg px-3 py-3 text-left transition hover:bg-gray-100 ${
                      selected ? "bg-indigo-50" : ""
                    }`}
                  >
                    <Flag country={country} label={label} />

                    <span className="flex-1 truncate text-[1.4rem] text-gray-700">{label}</span>

                    <span className="text-[1.3rem] text-gray-400">
                      +{getCountryCallingCode(country as never)}
                    </span>

                    {selected && <Check size={16} className="text-indigo-600" />}
                  </button>
                );
              })
            ) : (
              <p className="px-3 py-6 text-center text-[1.3rem] text-gray-400">
                No countries found.
              </p>
            )}
          </div>
        </div>
      )}
    </div>
  );
};

export default CountrySelector;
