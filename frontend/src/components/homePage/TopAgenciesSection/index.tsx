import { useEffect, useState } from "react";
import SectionContainer from "../../../app/layouts/SectionContainer";
import { Carousel } from "../../common/UI/Carousel";
import SubsectionHeader from "../../common/SubsectionHeader";
import { useNavigate } from "react-router-dom";
import axios from "axios";
import { propertyShowcaseSubsectionContents } from "../../../content/site.data";
import { ClipLoader } from "react-spinners";
import type { TopAgencyResponse } from "../../../types/api/agency";
import agencyService from "../../../services/agencyService";
import AgencyCard from "../../common/Cards/AgencyCard";
import OfflineErrorMessage from "../../common/UI/OfflineErrorMessage";

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
            setError("Failed to get top agencies. Please check your internet connection.");
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

  const renderAgencyCard = (agency: TopAgencyResponse) => (
    <AgencyCard
      name={agency.name}
      logoUrl={agency.logoUrl}
      listingCount={agency.listingCount}
      aggregateViews={agency.aggregateViews}
      averageRating={agency.averageRating}
    />
  );

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
    return <OfflineErrorMessage message={error} />;
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
          itemClassName="w-[90vw] xs:w-[30rem] h-[15rem]"
          onChange={handleChange}
        />
      </section>
    </SectionContainer>
  );
};

export default TopAgenciesSection;
