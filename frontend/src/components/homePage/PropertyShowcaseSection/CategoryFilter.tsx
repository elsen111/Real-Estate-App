

import { Carousel } from '../../common/ui/Carousel'
import useCategories from '../../../hooks/useCategories'
import type { CategoryResponse } from '../../../types/api/category';
import { useNavigate } from 'react-router-dom';

const CategoryFilter = () => {

      const { data: response } = useCategories();
      const categories = response?.data ?? []

      const navigate = useNavigate();

      if (categories.length == 0) return;

      const renderCategory = (category: CategoryResponse) => {
            return (
                  <div className="flex h-full items-center justify-center rounded-2xl border-2 cursor-pointer bg-purple-600 p-2 text-[1.8rem] text-white">
                        {category.name}
                  </div>
            )
      }

      const handleChange = (id: string | null) => {
            const category = categories.find((c) => c.id === id)
            if (category) navigate(`/properties?category=${category.slug}`)
      }

      return (
            <>
                  <Carousel items={categories} renderItem={renderCategory} onChange={handleChange} />
            </>
      )
}

export default CategoryFilter