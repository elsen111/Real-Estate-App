import { useEffect, useState, type ReactEventHandler } from "react";
import Button from "../../common/ui/Button"
import Input from "../../common/ui/Input"
import Dropdown, { type Option } from "../../common/ui/Dropdown"
import { useNavigate } from "react-router-dom";
import type { CategoryResponse } from "../../../types/api/category";
import categoryService from "../../../services/categoryService";

const HeroForm = () => {

      const navigate = useNavigate();

      const [query, setQuery] = useState("")
      const [city, setCity] = useState("");
      const [category, setCategory] = useState("");
      const [selectedListing, setSelectedListing] = useState("buy");
      const [categories, setCategories] = useState<CategoryResponse[]>([])

      const handleSubmit = (e: React.FormEvent<HTMLFormElement>): void => {
            e.preventDefault();

            const params = new URLSearchParams();
            const filters = {
                  q: query.trim(),
                  city: city.trim(),
                  category: category,
                  listingType: selectedListing
            }

            Object.entries(filters).forEach(([key, value]) => {
                  if (value && value !== 'ANY') params.set(key, value)
            })

            navigate(`/properties?${params}`);
      }

      const getCategories = async () => {
            try {
                  const response = await categoryService.getAll();
                  setCategories(response.data);
            } catch (error) {
                  console.error("Failed to fetch categories", error);
                  setCategories([])
            }
      }

      useEffect(() => {
            void getCategories();
      }, [])

      const options: Option[] = [
            { value: "ANY", label: "Any" },
            ...categories.map((c) => ({ value: c.name, label: c.name })),
      ]

      const handleSelectListing = (e: React.MouseEvent<HTMLButtonElement>, listingLabel: string): void => {
            e.preventDefault();
            setSelectedListing(listingLabel);
      }

      return (
            <form onSubmit={handleSubmit} className="bg-primary flex flex-col gap-8 rounded-2xl p-10 w-full sm:w-[80%] sm:min-w-[60rem]">
                  <div className="flex gap-2 items-center justify-between text-[1.6rem] font-bold w-[10rem]">
                        <Button variant={selectedListing == 'buy' ? 'blue' : 'white'} onClick={((e) => handleSelectListing(e, "buy"))} > Buy </Button>
                        <Button variant={selectedListing == 'rent' ? 'blue' : 'white'} onClick={((e) => handleSelectListing(e, "rent"))}> Rent </Button>
                  </div>

                  <div className="flex flex-col md:flex-row gap-3 w-full jpustify-betweentext-[1.6rem] items-center">
                        <Input
                              placeholder="Search by district, address or title"
                              className="md:w-[45%] w-full"
                              onChange={(e) => setQuery(e.target.value)}
                              value={query}
                        />
                        <Input
                              placeholder="City"
                              className="md:w-[30%] w-full"
                              onChange={(e) => setCity(e.target.value)}
                              value={city}
                        />

                        <Dropdown
                              options={options}
                              className="w-full md:w-100"
                              onChange={setCategory}
                              value={category}

                        />

                        <div className="self-end text-[1.6rem] sm:self-center">
                              <Button
                                    variant="purple"
                                    type="submit"
                              >
                                    Search
                              </Button>
                        </div>
                  </div>
            </form>
      )
}

export default HeroForm