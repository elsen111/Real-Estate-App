import { apiClient } from "../config/api";
import type { ApiResponse } from "../types/api/api";
import type { CategoryResponse } from "../types/api/category";

const categoryService = {
      getAll: async (): Promise<ApiResponse<CategoryResponse[]>> => {
            const response = await apiClient.get<CategoryResponse[]>(`/categories`);
            return response;
      },
};

export default categoryService;
