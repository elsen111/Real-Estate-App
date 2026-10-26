import { apiClient } from "../config/api";
import type { PageResponse } from "../types/api/api";
import type { PropertyResponse, PopularCityResponse } from "../types/api/property";

const propertyService = {
  getFeatured: async (
    page: number = 0,
    size: number = 0,
  ): Promise<PageResponse<PropertyResponse>> => {
    const response = await apiClient.get<PageResponse<PropertyResponse>>("/properties/featured", {
      params: { page, size },
    });

    return response.data;
  },

  getRecent: async (
    page: number = 0,
    size: number = 0,
  ): Promise<PageResponse<PropertyResponse>> => {
    const response = await apiClient.get<PageResponse<PropertyResponse>>("/properties/recent", {
      params: { page, size },
    });

    return response.data;
  },

  getPopularCities: async (
    page: number = 0,
    size: number = 0,
  ): Promise<PageResponse<PopularCityResponse>> => {
    const response = await apiClient.get<PageResponse<PopularCityResponse>>(
      "/properties/cities/popular",
      {
        params: { page, size },
      },
    );

    return response.data;
  },
};

export default propertyService;
