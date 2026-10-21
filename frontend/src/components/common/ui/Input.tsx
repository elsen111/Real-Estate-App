import type { InputHTMLAttributes } from "react"

interface InputProps extends InputHTMLAttributes<HTMLInputElement> { }

const Input = ({ className = "", ...props }: InputProps) => {
      return (
            <input
                  className={`border-2 lg:py-3 px-8 py-4 text-[1.6rem] transition-all duration-200 rounded-2xl border-gray-300 text-sm outline-none focus:border-purple ${className}`}
                  {...props}
            />
      )
}

export default Input