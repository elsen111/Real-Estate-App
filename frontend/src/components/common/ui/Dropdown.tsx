import { useState } from "react";
import { twMerge } from "tailwind-merge";

export interface Option {
  value: string;
  label: string;
}

interface DropdownProps {
  options: Option[];
  value: string;
  onChange: (value: string) => void;
  className?: string;
}

const Dropdown = ({ options, value, onChange, className }: DropdownProps) => {
  const [open, setOpen] = useState(false);

  const selected = options.find((o) => o.value === value);

  const handleSelect = (option: Option): void => {
    onChange(option.value);
    setOpen(false);
  };

  return (
    <div className={twMerge("relative w-[20rem]", className)}>
      <button
        type="button"
        aria-expanded={open}
        onClick={() => setOpen(!open)}
        className={`focus:border-purple flex w-full items-center justify-between gap-4 border-2 border-gray-300 px-8 py-4 text-[1.6rem] transition-all duration-200 outline-none lg:py-3 ${
          open ? "border-purple rounded-t-2xl" : "rounded-2xl"
        }`}
      >
        {selected?.label ?? "Select"}
        <svg
          className={`h-5 w-5 transition-transform duration-200 ${open ? "rotate-180" : ""}`}
          viewBox="0 0 20 20"
          fill="currentColor"
        >
          <path d="M5.23 7.21a.75.75 0 011.06.02L10 11.17l3.71-3.94a.75.75 0 111.08 1.04l-4.25 4.5a.75.75 0 01-1.08 0l-4.25-4.5a.75.75 0 01.02-1.06z" />
        </svg>
      </button>

      <ul
        className={`border-purple absolute top-full z-10 max-h-60 w-full origin-top overflow-y-auto rounded-b-2xl border-2 border-t-0 bg-white shadow-lg transition-all duration-200 ${
          open
            ? "visible translate-y-0 scale-y-100 opacity-100"
            : "pointer-events-none invisible -translate-y-1 scale-y-95 opacity-0"
        }`}
      >
        {options.map((o) => (
          <li key={o.value}>
            <button
              type="button"
              onClick={() => handleSelect(o)}
              className={`w-full px-8 py-3 text-left text-[1.6rem] transition-colors duration-150 hover:bg-gray-100 ${
                o.value === value ? "text-purple font-semibold" : ""
              }`}
            >
              {o.label}
            </button>
          </li>
        ))}
      </ul>
    </div>
  );
};

export default Dropdown;
