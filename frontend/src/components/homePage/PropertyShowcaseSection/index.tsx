

import SectionContainer from '../../../app/layouts/SectionContainer'
import CategoryFilter from './CategoryFilter'
import FeaturedProperties from './FeaturedProperties'

const PropertyShowcase = () => {
  return (
    <SectionContainer>
      <CategoryFilter />
      <FeaturedProperties />
    </SectionContainer>
  )
}

export default PropertyShowcase