import type { InputHTMLAttributes } from "react";

interface InputProps extends InputHTMLAttributes<HTMLInputElement> {}

const Input = ({ className = "", ...props }: InputProps) => {
  return (
    <input
      className={`focus:border-purple rounded-2xl border-2 border-gray-300 px-8 py-4 text-sm text-[1.6rem] transition-all duration-200 outline-none lg:py-3 ${className}`}
      {...props}
    />
  );
};

export default Input;
