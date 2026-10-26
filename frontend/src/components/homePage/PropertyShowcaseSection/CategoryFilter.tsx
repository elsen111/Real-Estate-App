import { Carousel } from "../../common/UI/Carousel";
import useCategories from "../../../hooks/useCategories";
import type { CategoryResponse } from "../../../types/api/category";
import { useNavigate } from "react-router-dom";
import SubsectionHeader from "../../common/SubsectionHeader";
import { propertyShowcaseSubsectionContents } from "../../../content/site.data";
import categoryDefaultImage from "../../../assets/images/category-default-bg.jpg";
import CategoryCard from "./CategoryCard";

const CategoryFilter = () => {
  const { data: response } = useCategories();
  const categories = response?.data ?? [];

  const navigate = useNavigate();

  if (categories.length == 0) return;

  const renderCategory = (category: CategoryResponse) => (
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
        renderItem={renderCategory}
        onChange={handleChange}
        itemClassName="h-[30rem] w-[25rem]"
      />
    </section>
  );
};

export default CategoryFilter;
