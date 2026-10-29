import { Carousel } from "../../common/UI/Carousel";
import useCategories from "../../../hooks/useCategories";
import type { CategoryResponse } from "../../../types/api/category";
import { useNavigate } from "react-router-dom";
import SubsectionHeader from "../../common/SubsectionHeader";
import { propertyShowcaseSubsectionContents } from "../../../content/site.data";
import categoryDefaultImage from "../../../assets/images/category-default-bg.jpg";
import CategoryCard from "../../common/Cards/CategoryCard";

const CategoryFilter = () => {
  const { data: response } = useCategories();
  const categories = response?.data ?? [];

  const navigate = useNavigate();

  if (categories.length == 0) return;

  const renderCategoryCard = (category: CategoryResponse) => (
    <CategoryCard
      name={category.name}
      description={category.description}
      imageUrl={category.imageUrl}
      defaultImgUrl={categoryDefaultImage}
    />
  );

  const handleChange = (id: string | null) => {
    const category = categories.find((c) => c.id === id);
    if (category) navigate(`/properties?category=${category.slug}`);
  };

  return (
    <section className="mb-30">
      <SubsectionHeader {...propertyShowcaseSubsectionContents[0]} />
      <Carousel
        items={categories}
        renderItem={renderCategoryCard}
        onChange={handleChange}
        itemClassName="h-[25rem] sm:h-[30rem] w-[25rem] w-[60vw] xxs:w-[45vw] sm:w-[30rem]"
      />
    </section>
  );
};

export default CategoryFilter;
