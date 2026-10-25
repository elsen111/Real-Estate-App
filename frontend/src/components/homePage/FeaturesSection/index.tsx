import SectionContainer from "../../../app/layouts/SectionContainer"
import featuresImg from "../../../assets/images/features.jpg"
import { featuresSectionContent } from "../../../content/site.data"
import Button from "../../common/ui/Button"
import FeatureCard from "./FeatureCard"

const FeaturesSection = () => {
  return (
    <SectionContainer>
      <div className="flex md:h-[calc(100vh-220px)] h-auto md:flex-row flex-col gap-10 sm:gap-20 relative">
        <div className="flex-1 relative h-full after:content-[] after:absolute after:w-full after:h-full after:z-5 after:bg-black/50 after:left-0 after:top-0 after:rounded-2xl">
          <img src={featuresImg} alt="Features section's image" className=" relative rounded-2xl md:w-auto w-full h-full min-h-[43rem]" />
          <div className="absolute md:left-10 left-[5%] top-[5%] sm:top-10 bg-black/70 w-[90%] md:w-[80%] text-primary z-6 rounded-3xl p-5">
            <h2 className="text-5xl font-bold mb-5 leading-15"> {featuresSectionContent.title} </h2>
            <p className="text-2xl mb-10"> {featuresSectionContent.description} </p>
            <Button path="/properties" variant="white" isLink hasFixedDimensions={false} className="w-[15rem]"> Browse All </Button>
          </div>
        </div>
        <div className="flex-1 grid grid-cols-1 xs:grid-cols-2 gap-15">
          {
            featuresSectionContent.features.map((feature) => (
              <FeatureCard
                {...feature}
                key={feature.name}
              />
            ))
          }
        </div>
      </div>
    </SectionContainer>
  )
}

export default FeaturesSection