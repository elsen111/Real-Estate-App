import clsx from "clsx";
import { registrationOptionsContent } from "../../content/site.data";
import Button from "../common/UI/Button";
import { ArrowRight } from "lucide-react";

type RegistrationOptionCardProps = {
  variant: "clientRegistration" | "agencyRegistration";
  className?: string;
};

const RegistrationOptionCard = ({ variant, className }: RegistrationOptionCardProps) => {
  const Icon = registrationOptionsContent[variant].icon;

  return (
    <div
      className={clsx(
        "hover:border-secondary/80 group flex h-full w-full flex-1 flex-col items-center justify-center gap-7 rounded-xl border-2 border-gray-500/40 p-10 transition-all duration-300",
        className,
      )}
    >
      <div className="text-secondary bg-secondary/10 flex h-25 w-25 items-center justify-center rounded-full font-bold">
        <Icon size={32} />
      </div>
      <h6 className="text-secondaryText text-[2.2rem] font-bold">
        {" "}
        {registrationOptionsContent[variant].heading}{" "}
      </h6>
      <p className="max-w-84 text-center text-[1.6rem] font-medium text-gray-600">
        {" "}
        {registrationOptionsContent[variant].description}{" "}
      </p>

      <Button
        variant={"blue"}
        isLink
        path={registrationOptionsContent[variant].optionLink.path}
        className="text-secondary! bg-secondary/10! group-hover:bg-secondary! group/button text-[1.6rem]! font-bold! group-hover:text-blue-100!"
      >
        {registrationOptionsContent[variant].optionLink.label}
        <ArrowRight
          size={20}
          className="ml-2 transition-all duration-300 group-hover/button:translate-x-2"
        />
      </Button>
    </div>
  );
};

export default RegistrationOptionCard;
