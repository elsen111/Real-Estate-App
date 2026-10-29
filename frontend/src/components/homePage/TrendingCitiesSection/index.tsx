import { useEffect, useState } from "react";
import SectionContainer from "../../../app/layouts/SectionContainer";
import type { PopularCityResponse } from "../../../types/api/property";
import { Carousel } from "../../common/UI/Carousel";
import SubsectionHeader from "../../common/SubsectionHeader";
import { useNavigate } from "react-router-dom";
import propertyService from "../../../services/propertyService";
import axios from "axios";
import { propertyShowcaseSubsectionContents } from "../../../content/site.data";
import { ClipLoader } from "react-spinners";
import CityCard from "./CityCard";

const TrendingCitiesSection = () => {
  const [popularCities, setPopularCities] = useState<PopularCityResponse[]>([]);
  const [isLoading, setIsLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);

  const navigate = useNavigate();

  useEffect(() => {
    let isMounted = true;

    const fetchPopularCities = async () => {
      try {
        setIsLoading(true);
        setError(null);

        const response = await propertyService.getPopularCities(0, 10);

        if (!isMounted) return;

        setPopularCities(response.content);
      } catch (error: unknown) {
        if (!isMounted) return;

        if (axios.isAxiosError(error)) {
          const status = error.response?.status;

          if (!error.response) {
            setError("Unable to connect to the server. Please check your internet connection.");
          } else if (status === 404) {
            setError("Popular cities were not found.");
          } else if (status === 500) {
            setError("A server error occurred. Please try again later.");
          } else {
            setError(error.response.data?.message || "Failed to load popular cities.");
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

    fetchPopularCities();

    return () => {
      isMounted = false;
    };
  }, []);

  const handleChange = (id: string | null): void => {
    if (id) navigate(`/properties?city=${decodeURIComponent(id)}`);
  };

  const renderCityCard = ({
    cityName,
    propertyCount,
    totalViews,
  }: PopularCityResponse & { id: string }) => (
    <CityCard cityName={cityName} propertyCount={propertyCount} totalViews={totalViews} />
  );

  const getCarouselCities = (cities: PopularCityResponse[]) => {
    return cities.map((city) => ({
      ...city,
      id: city.cityName,
    }));
  };

  if (isLoading) {
    return (
      <div className="flex justify-center">
        <ClipLoader
          color="#470bb7ff"
          size={40}
          loading={isLoading}
          className="mt-15 flex justify-center"
        />
      </div>
    );
  }

  if (error) {
    return <div>{error}</div>;
  }

  if (popularCities.length === 0) {
    return null;
  }

  return (
    <SectionContainer>
      <section>
        <SubsectionHeader
          title={propertyShowcaseSubsectionContents[3].title}
          tagline={propertyShowcaseSubsectionContents[3].tagline}
        />
        <Carousel
          items={getCarouselCities(popularCities)}
          renderItem={renderCityCard}
          itemClassName="w-[25rem] h-[20rem]"
          onChange={handleChange}
        />
      </section>
    </SectionContainer>
  );
};

export default TrendingCitiesSection;
