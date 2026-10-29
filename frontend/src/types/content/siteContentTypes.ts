import type { LucideIcon } from "lucide-react";

export interface SiteMetaData {
  name: string;
  description: string;
}

export interface HeroSectionContent {
  title: string;
  description: string;
}

export interface Feature {
  icon: LucideIcon;
  name: string;
  description: string;
}

export interface FeaturesSectionContent {
  title: string;
  description: string;
  features: Feature[];
}

export interface PropertyShowcaseSubsectionContent {
  title: string;
  tagline?: string;
}

export interface CtaSectionContent {
  heading: string;
  description: string;
}
