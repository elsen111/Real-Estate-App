import clsx from "clsx";
import { ArrowRight } from "lucide-react";
import { Link } from "react-router-dom";

type AuthSwitchProps = {
  switchTo: "toLogin" | "toRegister";
  className?: string;
};

const AuthSwitch = ({ switchTo, className }: AuthSwitchProps) => {
  const data = {
    toLogin: {
      questionText: "Already have an account?",
      linkText: "Sign in",
      linkPath: "/auth/login",
    },

    toRegister: {
      questionText: "Don't have an account?",
      linkText: "Sign up",
      linkPath: "/auth/register",
    },
  };

  return (
    <p
      className={clsx(
        "xxxs:flex xxs:gap-4 block items-center gap-2 text-[1.6rem] font-medium",
        className,
      )}
    >
      <span className="text-gray-700">{data[switchTo].questionText}</span>

      <Link
        to={data[switchTo].linkPath}
        className="text-secondary group/link flex items-center gap-1 font-bold transition-all duration-200 active:scale-90"
      >
        {data[switchTo].linkText}
        <ArrowRight
          size={20}
          className="transition-all duration-200 group-hover/link:translate-x-2"
        />
      </Link>
    </p>
  );
};

export default AuthSwitch;
