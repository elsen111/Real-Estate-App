import SectionContainer from "../../../app/layouts/SectionContainer";
import { footerContent } from "../../../content/site.data";
import NavGroup from "./FooterNavGroup";

const Footer = () => {
  return (<footer className="relative bottom-0 bg-secondary">
    <SectionContainer>
      <div className="text-primary flex flex-col md:flex-row md:flex-wrap justify-between sm:items-start sm:text-left text-center gap-15 sm:gap-20">
        <div className="sm:max-w-160 max-w-auto">
          <h2 className="text-[#5eead4] font-bold mb-4 text-[2.1rem] sm:text-[2.8rem]">{footerContent.siteName}</h2>
          <p className="text-2xl sm:text-[1.6rem] text-[#cbd5e1]">{footerContent.tagline}</p>
        </div>

        {
          footerContent.footerNavGroups?.map(group => (
            <NavGroup heading={group.heading} links={group.links} />
          ))
        }
      </div>
      <div className="mt-15 text-center text-2xl font-medium text-gray-500 mt-5">
        <p className="text-[1.6rem]"> {footerContent.copyright} </p>
      </div>
    </SectionContainer>
  </footer>)
};

export default Footer;
