import CtaSection from "../../components/homePage/CtaSection";
import FeaturesSection from "../../components/homePage/FeaturesSection";
import HeroSection from "../../components/homePage/HeroSetion";
import PropertyShowcase from "../../components/homePage/PropertyShowcaseSection";
import TopAgenciesSection from "../../components/homePage/TopAgenciesSection";
import TrendingCitiesSection from "../../components/homePage/TrendingCitiesSection";

const HomePage = () => {
  return (
    <div>
      <HeroSection />
      <FeaturesSection />
      <PropertyShowcase />
      <TrendingCitiesSection />
      <TopAgenciesSection />
      <CtaSection />
    </div>
  );
};

export default HomePage;
