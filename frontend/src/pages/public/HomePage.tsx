

import FeaturesSection from '../../components/homePage/FeaturesSection'
import HeroSection from '../../components/homePage/HeroSetion'
import PropertyShowcase from '../../components/homePage/PropertyShowcaseSection';
import TrendingCitiesSection from '../../components/homePage/TrendingCitiesSection';

const HomePage = () => {

  return (
    <div>
      <HeroSection />
      <FeaturesSection />
      <PropertyShowcase />
      <TrendingCitiesSection />
    </div>
  )
}

export default HomePage