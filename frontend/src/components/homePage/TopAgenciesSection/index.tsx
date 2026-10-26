import { useEffect, useState } from "react";
import SectionContainer from "../../../app/layouts/SectionContainer";
import type { PopularCityResponse } from "../../../types/api/property";
import { Carousel } from "../../common/UI/Carousel";
import SubsectionHeader from "../../common/SubsectionHeader";
import { useNavigate } from "react-router-dom";
import axios from "axios";
import { propertyShowcaseSubsectionContents } from "../../../content/site.data";
import { ClipLoader } from "react-spinners";
import cityCardBgImg from "../../../assets/images/city-card-bg.png";
import { Building2, Eye, House, Houses, MapPin, Star } from "lucide-react";
import type { TopAgencyResponse } from "../../../types/api/agency";
import agencyService from "../../../services/agencyService";

const TopAgenciesSection = () => {
  const [agencies, setAgencies] = useState<TopAgencyResponse[]>([]);
  const [isLoading, setIsLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);

  const navigate = useNavigate();

  useEffect(() => {
    let isMounted = true;

    const fetchTopAgencies = async () => {
      try {
        setIsLoading(true);
        setError(null);

        const response = await agencyService.getTop(0, 10);

        if (!isMounted) return;

        setAgencies(response.content);
      } catch (error: unknown) {
        if (!isMounted) return;

        if (axios.isAxiosError(error)) {
          const status = error.response?.status;

          if (!error.response) {
            setError("Unable to connect to the server. Please check your internet connection.");
          } else if (status === 404) {
            setError("Top agencies were not found.");
          } else if (status === 500) {
            setError("A server error occurred. Please try again later.");
          } else {
            setError(error.response.data?.message || "Failed to load top agencies.");
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

    fetchTopAgencies();

    return () => {
      isMounted = false;
    };
  }, []);

  const handleChange = (id: string | null): void => {
    if (id) navigate(`/agencies/${decodeURIComponent(id)}`);
  };

  const renderAgencyCard = (agency: TopAgencyResponse) => {
    return (
      <section className="group border-secondary/10 relative flex h-full w-full cursor-pointer flex-col justify-end overflow-hidden rounded-2xl border bg-gradient-to-t from-black/10 via-black/5 to-transparent bg-cover bg-center text-white transition-transform duration-300">
        <div className="relative z-10 flex h-full flex-col justify-between p-6">
          <div className="flex items-center gap-5">
            <div className="h-23 w-25 shrink-0 overflow-hidden rounded-2xl">
              {agency?.logoUrl ? (
                <img
                  src={agency.logoUrl}
                  alt={`${agency.name} agency's logo`}
                  className="h-full w-full rounded-2xl object-cover"
                />
              ) : (
                <div className="flex h-full flex-col items-center justify-center rounded-2xl border border-gray-500/20 bg-white">
                  <Houses size={28} color="#bbb" />
                </div>
              )}
            </div>

            <div className="min-w-0">
              <h6 className="text-secondary truncate text-[1.8rem] font-bold">{agency.name}</h6>
              <p className="mt-1 text-lg text-gray-500">Agency</p>
            </div>
          </div>

          <div className="grid grid-cols-3 divide-x divide-gray-300/60 border-t border-gray-300/50 pt-5">
            <div className="flex flex-col items-center gap-2 px-2">
              <div className="flex items-center gap-2">
                <Building2 size={18} className="text-secondary" />
                <span className="text-lg font-bold text-gray-700">
                  {agency.listingCount?.toLocaleString() ?? 0}
                </span>
              </div>
              <span className="text-md font-medium text-gray-500">Listings</span>
            </div>

            {/* Rating */}
            <div className="flex flex-col items-center gap-2 px-2">
              <div className="flex items-center gap-2">
                <Star size={18} className="fill-amber-400 text-amber-400" />
                <span className="text-lg font-bold text-gray-700">
                  {agency.averageRating?.toFixed(1) ?? "0.0"}
                </span>
              </div>
              <span className="text-md font-medium text-gray-500">Rating</span>
            </div>

            <div className="flex flex-col items-center gap-2 px-2">
              <div className="flex items-center gap-2">
                <Eye size={18} className="text-secondary" />
                <span className="text-lg font-bold text-gray-700">
                  {agency.aggregateViews?.toLocaleString() ?? 0}
                </span>
              </div>
              <span className="text-md font-medium text-gray-500">Views</span>
            </div>
          </div>
        </div>
      </section>
    );
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

  if (agencies.length === 0) {
    return null;
  }

  return (
    <SectionContainer>
      <section>
        <SubsectionHeader
          title={propertyShowcaseSubsectionContents[4].title}
          tagline={propertyShowcaseSubsectionContents[4].tagline}
        />
        <Carousel
          items={agencies.filter((a) => a.averageRating != 0 && a.averageRating != null)}
          renderItem={renderAgencyCard}
          itemClassName="w-[30rem] h-[15rem]"
          onChange={handleChange}
        />
      </section>
    </SectionContainer>
  );
};

export default TopAgenciesSection;
