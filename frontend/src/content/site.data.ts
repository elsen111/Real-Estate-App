import { BadgeCheck, ChartNoAxesCombined, MessageCircle, Search } from "lucide-react";
import type {
  CtaSectionContent,
  FeaturesSectionContent,
  FooterContent,
  HeroSectionContent,
  PropertyShowcaseSubsectionContent,
  SiteMetaData,
} from "../types/content/siteContentTypes";

export const siteMetaData: SiteMetaData = {
  name: "EstateFlow",
  description: "A real estate platform backend for agencies, agents, landlords, and clients.",
};

export const heroSectionContent: HeroSectionContent = {
  title: "Find your next home in nearby",
  description: "Apartments, houses and offices from verified agencies.",
};

export const featuresSectionContent: FeaturesSectionContent = {
  title: "The new way to find your new home",
  description: "Apartments, houses and offices from verified agencies.",
  features: [
    {
      icon: BadgeCheck,
      name: "Verified agencies",
      description:
        "Agencies are reviewed and approved by our team before they can publish a single listing.",
    },

    {
      icon: Search,
      name: "Search your way",
      description: "Filter by city, price, rooms and area, or explore listings on the map.",
    },

    {
      icon: MessageCircle,
      name: "Talk to the agent",
      description: "Send an inquiry or book a viewing straight from the property page.",
    },

    {
      icon: ChartNoAxesCombined,
      name: "Best Price",
      description:
        "Not sure what  you should be charging for your property? No need to worry, let us do the numbers for you.",
    },
  ],
};

export const propertyShowcaseSubsectionContents: PropertyShowcaseSubsectionContent[] = [
  {
    title: "Browse by Category",
    tagline: "Spaces tailored to your lifestyle.",
  },

  {
    title: "Featured properties",
  },

  {
    title: "Recently added",
    tagline: "Fresh listings from agencies.",
  },

  {
    title: "Popular cities",
    tagline: "Where most people are searching right now.",
  },

  {
    title: "Top agencies",
    tagline: `Meet some of the agencies listing on ${siteMetaData.name}.`,
  },
];

export const ctaSectionContent: CtaSectionContent = {
  heading: "Grow Your Real Estate Agency",
  description: "Showcase your listings and connect with potential clients.",
};

export const footerContent: FooterContent = {
  siteName: "EstateFlow",
  tagline:
    "Buy, rent and list properties across your country. Every agency on the platform is reviewed and approved by our team.",
  footerNavGroups: [
    {
      heading: "Browse",
      links: [
        { label: "Buy", path: "/buy" },
        { label: "Rent", path: "/rent" },
        { label: "Map search", path: "/map" },
        { label: "Property categories", path: "/categpory" },
      ],
    },

    {
      heading: "Agencies",
      links: [
        { label: "Explore agencies", path: "/agencies" },
        { label: "List your agency", path: "/register" },
        { label: "Pricing plans", path: "/pricing" },
      ],
    },

    {
      heading: "Company",
      links: [
        { label: "About us", path: "/about" },
        { label: "Contact", path: "/contact" },
        { label: "Terms of use", path: "/terms" },
        { label: "Privacy policy", path: "/privacy-policy" },
      ],
    },
  ],
  copyright: `\u00A9 ${new Date().getFullYear()} ${siteMetaData.name}. All rights reserved.`,
};
