export interface ApiResponse<T = unknown> {
  success: boolean;
  message?: string;
  data: T;
  error?: string;
  timestamp?: string;
}

export interface PageMetadata {
  size: number;
  number: number;
  totalElements: number;
  totalPages: number;
}

export interface PageResponse<T> {
  content: T[];
  page: PageMetadata;
}
