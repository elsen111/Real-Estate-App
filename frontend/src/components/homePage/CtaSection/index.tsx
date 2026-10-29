import SectionContainer from "../../../app/layouts/SectionContainer";
import { ctaSectionContent } from "../../../content/site.data";
import Button from "../../common/UI/Button";

const CtaSection = () => {
  return (
    <SectionContainer>
      <div className="flex h-full w-full flex-col items-center justify-between gap-8 rounded-2xl bg-[#0f766e] p-10 text-center md:flex-row md:p-18 md:text-left">
        <div>
          <h5 className="text-primary mb-5 text-[2.4rem] font-bold sm:text-[2.8rem] lg:text-[3.2rem]">
            {ctaSectionContent.heading}
          </h5>
          <p className="text-[1.8rem] font-medium text-[#ccfbf1] lg:text-3xl">
            {ctaSectionContent.description}
          </p>
        </div>
        <Button variant={"white"} isLink path="/pricing" className="text-[2.6rem]">
          View Plans
        </Button>
      </div>
    </SectionContainer>
  );
};

export default CtaSection;
