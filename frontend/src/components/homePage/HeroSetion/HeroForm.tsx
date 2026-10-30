import { useState } from "react";
import Button from "../../common/UI/Button";
import Input from "../../common/UI/Input";
import Dropdown, { type Option } from "../../common/UI/Dropdown";
import { useNavigate } from "react-router-dom";
import useCategories from "../../../hooks/useCategories";

const HeroForm = () => {
  const navigate = useNavigate();

  const [query, setQuery] = useState("");
  const [city, setCity] = useState("");
  const [category, setCategory] = useState("");
  const [selectedListing, setSelectedListing] = useState("buy");

  const { data: response } = useCategories();
  const categories = response?.data ?? [];

  const handleSubmit = (e: React.FormEvent<HTMLFormElement>): void => {
    e.preventDefault();

    const params = new URLSearchParams();
    const filters = {
      q: query.trim(),
      city: city.trim(),
      category: category,
      listingType: selectedListing,
    };

    Object.entries(filters).forEach(([key, value]) => {
      if (value && value !== "ANY") params.set(key, value);
    });

    navigate(`/properties?${params}`);
  };

  const options: Option[] = [
    { value: "ANY", label: "Any" },
    ...categories.map((c) => ({ value: c.slug, label: c.name })),
  ];

  const handleSelectListing = (
    e: React.MouseEvent<HTMLButtonElement>,
    listingLabel: string,
  ): void => {
    e.preventDefault();
    setSelectedListing(listingLabel);
  };

  return (
    <form
      onSubmit={handleSubmit}
      className="bg-primary flex w-full flex-col gap-8 rounded-2xl p-10 sm:w-[80%] sm:min-w-240"
    >
      <div className="flex w-40 items-center justify-between gap-2 text-[1.6rem] font-bold">
        <Button
          variant={selectedListing == "buy" ? "blue" : "white"}
          onClick={(e) => handleSelectListing(e, "buy")}
        >
          Buy
        </Button>
        <Button
          variant={selectedListing == "rent" ? "blue" : "white"}
          onClick={(e) => handleSelectListing(e, "rent")}
        >
          Rent
        </Button>
      </div>

      <div className="jpustify-betweentext-[1.6rem] flex w-full flex-col items-center gap-3 md:flex-row">
        <Input
          placeholder="Search by district, address or title"
          className="w-full md:w-[45%]"
          onChange={(e) => setQuery(e.target.value)}
          value={query}
        />
        <Input
          placeholder="City"
          className="w-full md:w-[30%]"
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
          <Button variant="purple" type="submit">
            Search
          </Button>
        </div>
      </div>
    </form>
  );
};

export default HeroForm;
