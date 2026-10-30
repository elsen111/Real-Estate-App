import SectionContainer from "../../../app/layouts/SectionContainer";
import heroImage from "../../../assets/images/home-hero.jpg";
import { heroSectionContent } from "../../../content/site.data";
import HeroForm from "./HeroForm";

const HeroSection = () => {
  return (
    <section
      style={{ backgroundImage: `url(${heroImage})` }}
      className="after:content-[] relative flex h-[calc(100vh-72px)] w-full flex-col items-start justify-center bg-cover bg-fixed bg-center after:absolute after:top-0 after:right-0 after:left-0 after:z-4 after:h-full after:bg-black/40 lg:h-[calc(100vh-96px)]"
    >
      <SectionContainer>
        <h1 className="mb-2 w-full text-[3.2rem] leading-15 font-bold text-white sm:w-[50%] md:leading-25 lg:text-[5.2rem]">
          {heroSectionContent.title}
        </h1>
        <p className="mb-10 w-full text-[1.8rem] text-[#dcfff7] sm:w-[50%]">
          {heroSectionContent.description}
        </p>
        <HeroForm />
      </SectionContainer>
    </section>
  );
};

export default HeroSection;
