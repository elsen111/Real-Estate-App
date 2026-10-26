import { Carousel } from "../../common/UI/Carousel";
import useCategories from "../../../hooks/useCategories";
import type { CategoryResponse } from "../../../types/api/category";
import { Link, useNavigate } from "react-router-dom";
import SubsectionHeader from "../../common/SubsectionHeader";
import { propertyShowcaseSubsectionContents } from "../../../content/site.data";
import { ArrowRight } from "lucide-react";
import categoryDefaultImage from "../../../assets/images/category-default-bg.jpg";

const CategoryFilter = () => {
  const { data: response } = useCategories();
  const categories = response?.data ?? [];

  const navigate = useNavigate();

  if (categories.length == 0) return;

  const renderCategory = (category: CategoryResponse) => {
    return (
      <section
        style={{
          backgroundImage: `url(${category.imageUrl ? category.imageUrl : categoryDefaultImage})`,
        }}
        className="group relative isolate flex h-full min-h-[280px] w-full flex-col justify-end overflow-hidden rounded-2xl bg-cover bg-center text-white transition-transform duration-500 hover:-translate-y-1"
      >
        <div className="absolute inset-0 -z-20 bg-cover bg-center transition-transform duration-700 ease-out group-hover:scale-110" />

        <div className="absolute inset-0 -z-10 bg-gradient-to-t from-slate-950 via-slate-950/45 to-slate-950/10 opacity-90 transition-all duration-500" />

        <div className="relative z-10 p-6 sm:p-7">
          <h3 className="text-2xl font-bold tracking-tight transition-transform duration-300 sm:text-3xl">
            {category.name}
          </h3>

          <p className="mt-2 line-clamp-2 max-w-md text-[1.4rem] leading-8 text-white/75">
            {category.description}
          </p>

          <div className="mt-5 flex items-center gap-2 text-xl font-semibold text-white transition-all duration-300 group-hover:gap-3">
            Explore {category.name}s
            <ArrowRight
              size={17}
              className="transition-transform duration-300 group-hover:translate-x-1"
            />
          </div>
        </div>
      </section>
    );
  };

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
