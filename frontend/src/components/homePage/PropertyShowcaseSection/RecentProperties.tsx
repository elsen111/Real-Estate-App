import { useEffect, useState, type ReactNode } from "react"
import type { PropertyResponse } from "../../../types/api/property"
import propertyService from "../../../services/propertyService";
import axios from "axios";
import SubsectionHeader from "./SubsectionHeader";
import { propertyShowcaseSubsectionContents } from "../../../content/site.data";
import { Carousel } from "../../common/ui/Carousel";
import PropertyCard from "../../common/ui/PropertyCard";
import { ClipLoader } from "react-spinners";
import { useNavigate } from "react-router-dom";
import { ChevronRight } from "lucide-react";


const RecentProperties = () => {

      const [featuredProperties, setFeaturedProperties] = useState<PropertyResponse[]>([])
      const [isLoading, setIsLoading] = useState(true);
      const [error, setError] = useState<string | null>(null)

      const navigate = useNavigate();

      useEffect(() => {
            let isMounted = true;

            const fetchFeaturedProperties = async () => {
                  try {
                        setIsLoading(true);
                        setError(null);

                        const response = await propertyService.getRecent(0, 10);

                        if (!isMounted) return;

                        setFeaturedProperties(response.content);
                  } catch (error: unknown) {
                        if (!isMounted) return;

                        if (axios.isAxiosError(error)) {
                              const status = error.response?.status;

                              if (!error.response) {
                                    setError("Unable to connect to the server. Please check your internet connection.");
                              } else if (status === 404) {
                                    setError("Featured properties were not found.");
                              } else if (status === 500) {
                                    setError("A server error occurred. Please try again later.");
                              } else {
                                    setError(
                                          error.response.data?.message ||
                                          "Failed to load featured properties."
                                    );
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
            if (id) navigate(`/properties/${id}`)
      }

      const renderItem = ({ title, description, price, currency, address, bathrooms, area, mainImageUrl, featured, listingType }: PropertyResponse) => {
            return <PropertyCard
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
      }

      const endItem = (
            <button
                  type="button"
                  onClick={() => navigate("/properties?sort=createdAt,desc")}
                  className="flex text-[1.8rem] h-[40rem] w-[30rem] cursor-pointer shrink-0 items-center justify-center rounded-2xl border border-gray-200 bg-gray-50 text-lg font-semibold text-gray-500 transition-colors hover:bg-gray-200"
            >
                  View All
                  <ChevronRight size={20} className="ml-2" />
            </button>
      );

      if (isLoading) {
            return (
                  <div className="flex justify-center">
                        <ClipLoader color="#470bb7ff" size={40} loading={isLoading} className="flex justify-center" />
                  </div>
            );
      }

      if (error) {
            return <div>{error}</div>;
      }

      if (featuredProperties.length === 0) {
            return null;
      }

      return (
            <section className="mt-30">
                  <SubsectionHeader title={propertyShowcaseSubsectionContents[2].title} />
                  <Carousel items={featuredProperties} renderItem={renderItem} itemClassName="w-[30rem] h-[40rem]" onChange={handleChange} endItem={endItem} />
            </section>
      )
}

export default RecentProperties