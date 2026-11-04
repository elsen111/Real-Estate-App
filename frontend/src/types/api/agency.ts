export interface TopAgencyResponse {
  id: string;
  name: string;
  city: string;
  logoUrl: string;
  averageRating: number;
  listingCount: number;
  aggregateViews: number;
}

export interface AgencyResponse {
  id: string;
  name: string;
  description: string;
  logoUrl?: string;
  email: string;
  phoneNumber?: string;
  website: string;
  city: string;
  address: string;
  totalAgents: number;
  status: string;
}
