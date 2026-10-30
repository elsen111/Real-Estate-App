import { useEffect, useState } from "react";
import type { PropertyResponse } from "../../../types/api/property";
import propertyService from "../../../services/propertyService";
import axios from "axios";
import SubsectionHeader from "../../common/SubsectionHeader";
import { propertyShowcaseSubsectionContents } from "../../../content/site.data";
import { Carousel } from "../../common/UI/Carousel";
import PropertyCard from "../../common/Cards/PropertyCard";
import { ClipLoader } from "react-spinners";
import { useNavigate } from "react-router-dom";
import { ChevronRight } from "lucide-react";
import OfflineErrorMessage from "../../common/UI/OfflineErrorMessage";

const FeaturedProperties = () => {
  const [featuredProperties, setFeaturedProperties] = useState<PropertyResponse[]>([]);
  const [isLoading, setIsLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);

  const navigate = useNavigate();

  useEffect(() => {
    let isMounted = true;

    const fetchFeaturedProperties = async () => {
      try {
        setIsLoading(true);
        setError(null);

        const response = await propertyService.getFeatured(0, 10);

        if (!isMounted) return;

        setFeaturedProperties(response.content);
      } catch (error: unknown) {
        if (!isMounted) return;

        if (axios.isAxiosError(error)) {
          const status = error.response?.status;

          if (!error.response) {
            setError("Failed get featured properties. Please check your internet connection.");
          } else if (status === 404) {
            setError("Featured properties were not found.");
          } else if (status === 500) {
            setError("A server error occurred. Please try again later.");
          } else {
            setError(error.response.data?.message || "Failed to load featured properties.");
          }
        } else {
          setError("An unexpected error occurred. Please try again.");
        }
      } finally {
        if (isMounted) {
          setIsLoading(false);
        }
      }
    };

    fetchFeaturedProperties();

    return () => {
      isMounted = false;
    };
  }, []);

  const handleChange = (id: string | null): void => {
    if (id) navigate(`/properties/${id}`);
  };

  const renderPropertyCard = ({
    title,
    description,
    price,
    currency,
    address,
    bathrooms,
    area,
    mainImageUrl,
    featured,
    listingType,
  }: PropertyResponse) => {
    return (
      <PropertyCard
        title={title}
        price={price}
        currency={currency}
        address={address}
        description={description}
        bathrooms={bathrooms}
        area={area}
        image={mainImageUrl}
        featured={featured}
        listingType={listingType}
      />
    );
  };

  const endItem = (
    <button
      type="button"
      onClick={() => navigate("/properties?featured=true")}
      className="xxs:w-[45vw] flex h-180 w-[60vw] shrink-0 cursor-pointer items-center justify-center rounded-2xl border border-gray-200 bg-gray-50 text-lg text-[1.8rem] font-semibold text-gray-500 transition-colors hover:bg-gray-200 sm:w-120"
    >
      View All
      <ChevronRight size={20} className="ml-2" />
    </button>
  );

  if (isLoading) {
    return (
      <div className="flex justify-center">
        <ClipLoader
          color="#470bb7ff"
          size={40}
          loading={isLoading}
          className="flex justify-center"
        />
      </div>
    );
  }

  if (error) {
    return (
      <div className="mb-11">
        <OfflineErrorMessage message={error} />;
      </div>
    );
  }

  if (featuredProperties.length === 0) {
    return null;
  }

  return (
    <section>
      <SubsectionHeader title={propertyShowcaseSubsectionContents[1].title} />
      <Carousel
        items={featuredProperties}
        renderItem={renderPropertyCard}
        itemClassName="w-[60vw] xxs:w-[45vw] sm:w-[30rem] h-[45rem]"
        onChange={handleChange}
        endItem={endItem}
      />
    </section>
  );
};

export default FeaturedProperties;
