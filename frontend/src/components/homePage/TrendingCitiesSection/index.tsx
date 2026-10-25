
import { useEffect, useState } from 'react'
import SectionContainer from '../../../app/layouts/SectionContainer'
import type { PopularCityResponse } from '../../../types/api/property'
import { Carousel } from '../../common/UI/Carousel'
import SubsectionHeader from '../PropertyShowcaseSection/SubsectionHeader'
import { useNavigate } from 'react-router-dom'
import propertyService from '../../../services/propertyService'
import axios from 'axios'
import { propertyShowcaseSubsectionContents } from '../../../content/site.data'
import { ClipLoader } from 'react-spinners'
import cityCardBgImg from "../../../assets/images/city-card-bg.png"
import { Building2, Eye, MapPin } from 'lucide-react'

const TrendingCitiesSection = () => {

      const [popularCities, setPopularCities] = useState<PopularCityResponse[]>([])
      const [isLoading, setIsLoading] = useState(true);
      const [error, setError] = useState<string | null>(null)

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
                                    setError(
                                          error.response.data?.message ||
                                          "Failed to load popular cities."
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

            fetchPopularCities();

            return () => {
                  isMounted = false;
            };
      }, []);

      const handleChange = (id: string | null): void => {
            if (id) navigate(`/properties?city=${decodeURIComponent(id)}`)
      }


const renderItem = ({
  cityName,
  propertyCount,
  totalViews,
}: PopularCityResponse & { id: string }) => {
  return (
    <section
      style={{ backgroundImage: `url(${cityCardBgImg})` }}
      className="
        group relative flex h-full w-full cursor-pointer
        flex-col justify-end overflow-hidden rounded-2xl
        bg-cover bg-center text-white
        transition-transform duration-300 hover:rotate-x-30
      "
    >
      <div className="absolute inset-0 bg-gradient-to-t from-black/85 via-black/50 to-transparent transition-colors duration-300 group-hover:from-black/90" />

      <div className="relative z-10 flex flex-col gap-5 p-6">

        <div className="flex items-center gap-2 text-white/80">
          <MapPin size={17} />
          <span className="text-sm font-medium tracking-wide uppercase">
            Popular destination
          </span>
        </div>

        <div>
          <h3 className="text-4xl font-bold tracking-tight">
            {cityName}
          </h3>
          <p className="mt-2 text-[1.2rem] text-white/75">
            Explore properties in {cityName}
          </p>
        </div>

        <div className="flex items-center justify-between gap-6 border-t border-white/20 pt-4">
          <div className="flex items-center gap-2.5">
            <div className="rounded-lg bg-white/15 p-2">
              <Building2 size={19} />
            </div>
            <div>
              <p className="text-xl font-bold">
                {propertyCount.toLocaleString()}
              </p>
              <p className="text-[1.1rem] text-white/70">Properties</p>
            </div>
          </div>

          <div className="h-9 w-px bg-white/20" />

          <div className="flex items-center gap-2.5">
            <div className="rounded-lg bg-white/15 p-2">
              <Eye size={19} />
            </div>
            <div>
              <p className="text-xl font-bold">
                {totalViews.toLocaleString()}
              </p>
              <p className="text-[1.1rem] text-white/70">Total views</p>
            </div>
          </div>

        </div>
      </div>
    </section>
  );
};

      const getCarouselCities = (cities: PopularCityResponse[]) => {
            return cities.map((city) => ({
                  ...city,
                  id: city.cityName,
            }));
      };

      if (isLoading) {
            return (
                  <div className="flex justify-center">
                        <ClipLoader color="#470bb7ff" size={40} loading={isLoading} className="mt-15 flex justify-center" />
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
                        <SubsectionHeader title={propertyShowcaseSubsectionContents[3].title} tagline={propertyShowcaseSubsectionContents[3].tagline} />
                        <Carousel items={getCarouselCities(popularCities)} renderItem={renderItem} itemClassName="w-[25rem] h-[20rem]" onChange={handleChange} />
                  </section>
            </SectionContainer>
      )
}

export default TrendingCitiesSection