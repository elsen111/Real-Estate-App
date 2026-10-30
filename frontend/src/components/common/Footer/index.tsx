import SectionContainer from "../../../app/layouts/SectionContainer";
import { footerContent } from "../../../content/site.data";
import NavGroup from "./FooterNavGroup";

const Footer = () => {
  return (
    <footer className="bg-secondary relative bottom-0">
      <SectionContainer>
        <div className="text-primary flex flex-col justify-between gap-15 text-center sm:items-start sm:gap-20 sm:text-left md:flex-row md:flex-wrap">
          <div className="max-w-auto sm:max-w-160">
            <h2 className="mb-4 text-[2.1rem] font-bold text-[#5eead4] sm:text-[2.8rem]">
              {footerContent.siteName}
            </h2>
            <p className="text-2xl text-[#cbd5e1] sm:text-[1.6rem]">{footerContent.tagline}</p>
          </div>

          {footerContent.footerNavGroups?.map((group) => (
            <NavGroup heading={group.heading} links={group.links} />
          ))}
        </div>
        <div className="mt-15 text-center text-2xl font-medium text-gray-500">
          <p className="xxs:text-[1.6rem] text-[1.4rem]"> {footerContent.copyright} </p>
        </div>
      </SectionContainer>
    </footer>
  );
};

export default Footer;
