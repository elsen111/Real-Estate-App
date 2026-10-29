import type { PropertyShowcaseSubsectionContent } from "../../types/content/siteContentTypes";

const SubsectionHeader = ({ title, tagline = "" }: PropertyShowcaseSubsectionContent) => {
  return (
    <header className="mb-12">
      <h5 className="text-secondary text-center text-[2.8rem] leading-15 font-bold tracking-tight sm:text-start md:text-5xl">
        {title}
      </h5>
      {tagline && (
        <p className="mt-2 text-center text-[1.6rem] leading-relaxed font-medium text-gray-400 sm:text-start">
          {tagline}
        </p>
      )}
    </header>
  );
};

export default SubsectionHeader;
