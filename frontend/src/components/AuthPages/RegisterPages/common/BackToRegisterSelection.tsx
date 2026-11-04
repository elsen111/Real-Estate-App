import { ArrowLeft } from "lucide-react";
import { Link } from "react-router-dom";

const BackToRegisterSelection = () => {
  return (
    <Link
      to="/auth/register"
      className="text-secondary group/link absolute top-8 right-10 flex items-center gap-1 text-end text-[1.2rem] font-bold transition-all duration-200 active:scale-90"
    >
      <ArrowLeft
        size={16}
        className="transition-all duration-200 group-hover/link:-translate-x-2"
      />
      Back
    </Link>
  );
};

export default BackToRegisterSelection;
