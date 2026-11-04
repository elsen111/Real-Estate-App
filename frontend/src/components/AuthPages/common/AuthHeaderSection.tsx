import { clsx } from "clsx";

type AuthHeaderSectionProps = {
  heading: string;
  description: string;
  className?: string;
};

const AuthHeaderSection = ({ heading, description, className = "" }: AuthHeaderSectionProps) => {
  return (
    <header
      className={clsx(
        "xxs:w-full xxxs:w-[94%] mx-auto flex w-full max-w-200 flex-col gap-3 py-10",
        className,
      )}
    >
      <h5 className="text-secondary text-[2.6rem] leading-14 font-bold sm:text-[3.4rem] sm:leading-18">
        {heading}
      </h5>
      <p className="text-[1.6rem] font-medium text-gray-700"> {description} </p>
    </header>
  );
};

export default AuthHeaderSection;
