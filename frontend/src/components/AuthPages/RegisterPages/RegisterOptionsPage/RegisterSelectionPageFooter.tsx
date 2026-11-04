import { registrationSelectionPageFooter } from "../../../../content/site.data";

const RegisterSelectionPageFooter = () => {
  const Icon = registrationSelectionPageFooter.securityNote.icon;

  return (
    <div className="mx-auto mt-5 w-[80%]">
      <div className="flex items-center justify-between gap-5">
        <div className="h-px flex-1 bg-gray-300" />
        <span className="text-secondary/80 text-[1.4rem] font-medium">
          {registrationSelectionPageFooter.dividerText}
        </span>
        <div className="h-px flex-1 bg-gray-300" />
      </div>
      <p className="text-secondary mx-auto mt-5 flex w-fit items-center gap-3 text-[1.4rem] font-bold">
        <Icon size={20} />
        {registrationSelectionPageFooter.securityNote.text}
      </p>
    </div>
  );
};

export default RegisterSelectionPageFooter;
