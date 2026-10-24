import { Link } from "react-router-dom";

type ButtonProps = React.ButtonHTMLAttributes<HTMLButtonElement> & {
      path?: string,
      variant: 'blue' | 'purple' | 'white'
      circled?: boolean;
      hasFixedDimensions?: boolean;
      isLink?: boolean;
      children: React.ReactNode,
      className?: string
}

const variants = {
  blue: "bg-secondary/80 hover:bg-secondary text-white",
  purple: "bg-purple/80 hover:bg-purple text-white",
  white: "bg-white hover:bg-gray-100 text-secondary",
}

const Button = ({ path, variant, circled, hasFixedDimensions, isLink, children, className, ...props }: ButtonProps) => {
      return (
            isLink ?
                  (<Link
                        to={path}
                        className={
                              `
                              rounded-2xl text-[2rem] ${circled && 'rounded-full'} ${hasFixedDimensions ? 'h-20 w-20' : 'lg:py-3 px-8 py-4'} ${variants[variant]} shrink-0 overflow-hidden flex justify-center items-center  text-center font-semibold shadow-md transition-all duration-300 hover:shadow-lg active:scale-[0.98] hover:-translate-y-1 ${className}
                              `
                        }
                  >
                        {children}
                  </Link>)
                  : (
                        <button
                              className={
                                    `
                                    rounded-2xl ${circled && 'rounded-full'} ${hasFixedDimensions ? 'h-20 w-20' : 'lg:py-3 px-8 py-4'} ${variants[variant]} shrink-0 overflow-hidden flex justify-center items-center cursor-pointer  text-center font-semibold shadow-md transition-all duration-300 hover:shadow-lg active:scale-[0.98] hover:-translate-y-1 ${className}
                              `
                              }
                              {...props}
                        >
                              {children}
                        </button>
                  )
      )
}

export default Button