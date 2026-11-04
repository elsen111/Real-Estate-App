import SectionContainer from "../../common/Section/SectionContainer";
import CategoryFilter from "./CategoryFilter";
import FeaturedProperties from "./FeaturedProperties";
import RecentProperties from "./RecentProperties";

const PropertyShowcase = () => {
  return (
    <SectionContainer>
      <CategoryFilter />
      <FeaturedProperties />
      <RecentProperties />
    </SectionContainer>
  );
};

export default PropertyShowcase;
