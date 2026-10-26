import { apiClient } from "../config/api";
import type { TopAgencyResponse } from "../types/api/agency";
import type { PageResponse } from "../types/api/api";

const agencyService = {
  getTop: async (page: number = 0, size: number = 0): Promise<PageResponse<TopAgencyResponse>> => {
    const response = await apiClient.get<PageResponse<TopAgencyResponse>>("/agencies/public/top", {
      params: { page, size },
    });

    return response.data;
  },
};

export default agencyService;
