import { useState, type ComponentProps } from "react"
import { twMerge } from "tailwind-merge"

interface Category {
  value: string
  label: string
}

interface DropdownProps {
  categories: Category[],
  className?: string,
  onChange: (value: string) => void
}

const Dropdown = ({ categories, className, onChange }: DropdownProps) => {
  const [selectedCategory, setSelectedCategory] = useState<Category>(categories[0])
  const [categoriesOpen, setCategoriesOpen] = useState(false)

  const handleSelectCategory = (category: Category):void => {
    setSelectedCategory(category)
    onChange(category.value)
    setCategoriesOpen(false)
  }

  return (
    <div className={twMerge('relative w-[20rem]', className)}>
      <button
        type="button"
        aria-expanded={categoriesOpen}
        onClick={() => setCategoriesOpen(!categoriesOpen)}
        className={`flex w-full items-center justify-between gap-4 border-2 border-gray-300 px-8 py-4 text-[1.6rem] outline-none transition-all duration-200 focus:border-purple lg:py-3 ${
          categoriesOpen ? "rounded-t-2xl border-purple" : "rounded-2xl"
        }`}
      >
        {selectedCategory.label}
        <svg
          className={`h-5 w-5 transition-transform duration-200 ${categoriesOpen ? "rotate-180" : ""}`}
          viewBox="0 0 20 20"
          fill="currentColor"
        >
          <path d="M5.23 7.21a.75.75 0 011.06.02L10 11.17l3.71-3.94a.75.75 0 111.08 1.04l-4.25 4.5a.75.75 0 01-1.08 0l-4.25-4.5a.75.75 0 01.02-1.06z" />
        </svg>
      </button>

      <ul
        className={`h-60 overflow-y-auto absolute top-full z-10 w-full origin-top overflow-hidden rounded-b-2xl border-2 border-t-0 border-purple bg-white shadow-lg transition-all duration-200 ${
          categoriesOpen
            ? "visible translate-y-0 scale-y-100 opacity-100"
            : "pointer-events-none invisible -translate-y-1 scale-y-95 opacity-0"
        }`}
      >
        {categories.map((c) => (
          <li key={c.value}>
            <button
              type="button"
              onClick={() => handleSelectCategory(c)}
              className={`w-full px-8 py-3 text-left text-[1.6rem] transition-colors duration-150 hover:bg-gray-100 ${
                c.value === selectedCategory.value ? "font-semibold text-purple" : ""
              }`}
            >
              {c.label}
            </button>
          </li>
        ))}
      </ul>
    </div>
  )
}

export default Dropdown