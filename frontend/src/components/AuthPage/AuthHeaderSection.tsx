import type { AuthHeaderContent } from "../../types/content/siteContentTypes";

const AuthHeaderSection = ({ heading, description }: AuthHeaderContent) => {
  return (
    <header className="flex max-w-200 flex-col gap-3 p-10 md:mx-auto">
      <h5 className="text-secondary text-[3rem] font-bold sm:text-[3.4rem]"> {heading} </h5>
      <p className="text-[1.6rem] font-medium text-gray-700"> {description} </p>
    </header>
  );
};

export default AuthHeaderSection;
