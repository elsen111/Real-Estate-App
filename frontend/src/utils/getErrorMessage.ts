import axios from "axios";
import type { ApiResponse } from "../types/api/api";

export function getErrorMessage(error: unknown, fallback = "Something went wrong."): string {
  if (axios.isAxiosError<ApiResponse>(error)) {
    const data = error.response?.data;

    if (data?.error && Object.keys(data.error).length > 0) {
      return Object.values(data.error).join(" ");
    }

    return data?.message ?? fallback;
  }

  if (error instanceof Error) {
    return error.message || fallback;
  }

  return fallback;
}
