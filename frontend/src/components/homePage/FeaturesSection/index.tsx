import SectionContainer from "../../../app/layouts/SectionContainer";
import featuresImg from "../../../assets/images/features.jpg";
import { featuresSectionContent } from "../../../content/site.data";
import Button from "../../common/UI/Button";
import FeatureCard from "./FeatureCard";

const FeaturesSection = () => {
  return (
    <SectionContainer>
      <div className="relative flex h-auto flex-col gap-10 sm:gap-20 md:h-[calc(100vh-220px)] md:flex-row">
        <div className="after:content-[] relative h-full flex-1 after:absolute after:top-0 after:left-0 after:z-5 after:h-full after:w-full after:rounded-2xl after:bg-black/50">
          <img
            src={featuresImg}
            alt="Features section's image"
            className="relative h-full min-h-[43rem] w-full rounded-2xl md:w-auto"
          />
          <div className="text-primary absolute top-[5%] left-[5%] z-6 w-[90%] rounded-3xl bg-black/70 p-5 sm:top-10 md:left-10 md:w-[80%]">
            <h2 className="mb-5 text-5xl leading-15 font-bold"> {featuresSectionContent.title} </h2>
            <p className="mb-10 text-2xl"> {featuresSectionContent.description} </p>
            <Button
              path="/properties"
              variant="white"
              isLink
              hasFixedDimensions={false}
              className="w-[15rem]"
            >
              {" "}
              Browse All{" "}
            </Button>
          </div>
        </div>
        <div className="xs:grid-cols-2 grid flex-1 grid-cols-1 gap-15">
          {featuresSectionContent.features.map((feature) => (
            <FeatureCard {...feature} key={feature.name} />
          ))}
        </div>
      </div>
    </SectionContainer>
  );
};

export default FeaturesSection;
