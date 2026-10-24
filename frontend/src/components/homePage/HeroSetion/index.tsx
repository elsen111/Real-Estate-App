
import SectionContainer from "../../../app/layouts/SectionContainer"
import heroImage from "../../../assets/images/home-hero.jpg"
import { heroSectionContent } from "../../../content/site.data"
import HeroForm from "./HeroForm"

const HeroSection = () => {

      return (
            <section
                  style={{ backgroundImage: `url(${heroImage})` }}
                  className="flex flex-col justify-center items-start lg:h-[calc(100vh-96px)] h-[calc(100vh-72px)] relative bg-fixed w-full bg-cover bg-center after:content-[] after:absolute after:left-0 after:z-4 after:right-0 after:top-0 after:bg-black/80 after:h-full"
            >
                  <SectionContainer>
                        <h1 className="mb-2 sm:w-[50%] w-full lg:text-[5.2rem] text-[3.2rem] font-bold leading-15 text-white"> {heroSectionContent.title} </h1>
                        <p className="mb-10 text-[#dcfff7] sm:w-[50%] w-full text-[1.8rem]"> {heroSectionContent.description} </p>
                        <HeroForm />
                  </SectionContainer>
            </section>
      )
}

export default HeroSection