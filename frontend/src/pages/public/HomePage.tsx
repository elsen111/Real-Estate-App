

import { Carousel } from '../../components/common/ui/Carousel'
import FeaturesSection from '../../components/homePage/FeaturesSection'
import HeroSection from '../../components/homePage/HeroSetion'
import PropertyShowcase from '../../components/homePage/PropertyShowcaseSection';

const HomePage = () => {

   const slides = [
  { id: "t1", type: "text", title: "Any type", description: "Show everything" },
  { id: "p1", type: "property", title: "Modern Apartment", location: "Baku, Yasamal", price: 145000, image: "https://picsum.photos/seed/estate-1/400/300", beds: 3, baths: 2, area: 96 },
  { id: "p2", type: "property", title: "Villa with Garden", location: "Baku, Bilgah", price: 420000, image: "https://picsum.photos/seed/estate-2/400/300", beds: 5, baths: 3, area: 240 },
  { id: "p3", type: "property", title: "Cozy Studio", location: "Baku, Nasimi", price: 68000, image: "https://picsum.photos/seed/estate-3/400/300", beds: 1, baths: 1, area: 38 },
  { id: "p4", type: "property", title: "Penthouse", location: "Baku, Sabail", price: 780000, image: "https://picsum.photos/seed/estate-4/400/300", beds: 4, baths: 3, area: 210 },
  { id: "p5", type: "property", title: "Townhouse", location: "Baku, Narimanov", price: 235000, image: "https://picsum.photos/seed/estate-5/400/300", beds: 3, baths: 2, area: 130 },
  { id: "p6", type: "property", title: "Modern Apartment", location: "Baku, Khatai", price: 112000, image: "https://picsum.photos/seed/estate-6/400/300", beds: 2, baths: 1, area: 72 },
  { id: "p7", type: "property", title: "Family Apartment", location: "Baku, Binagadi", price: 98000, image: "https://picsum.photos/seed/estate-7/400/300", beds: 3, baths: 2, area: 88 },
  { id: "p8", type: "property", title: "Villa with Pool", location: "Baku, Mardakan", price: 560000, image: "https://picsum.photos/seed/estate-8/400/300", beds: 6, baths: 4, area: 320 },
  { id: "p9", type: "property", title: "Cozy Studio", location: "Baku, Nizami", price: 59000, image: "https://picsum.photos/seed/estate-9/400/300", beds: 1, baths: 1, area: 35 },
  { id: "t2", type: "text", title: "Apartments" },
  { id: "p10", type: "property", title: "Sea View Apartment", location: "Baku, Sabail", price: 310000, image: "https://picsum.photos/seed/estate-10/400/300", beds: 3, baths: 2, area: 115 },
  { id: "p11", type: "property", title: "Modern Apartment", location: "Baku, Yasamal", price: 134000, image: "https://picsum.photos/seed/estate-11/400/300", beds: 2, baths: 2, area: 84 },
  { id: "p12", type: "property", title: "Townhouse", location: "Baku, Bilgah", price: 275000, image: "https://picsum.photos/seed/estate-12/400/300", beds: 4, baths: 3, area: 165 },
  { id: "p13", type: "property", title: "Loft Apartment", location: "Baku, Nasimi", price: 158000, image: "https://picsum.photos/seed/estate-13/400/300", beds: 2, baths: 1, area: 78 },
  { id: "p14", type: "property", title: "Villa with Garden", location: "Baku, Zagulba", price: 480000, image: "https://picsum.photos/seed/estate-14/400/300", beds: 5, baths: 4, area: 260 },
  { id: "p15", type: "property", title: "Cozy Studio", location: "Baku, Narimanov", price: 64000, image: "https://picsum.photos/seed/estate-15/400/300", beds: 1, baths: 1, area: 41 },
  { id: "p16", type: "property", title: "Penthouse", location: "Baku, Khatai", price: 690000, image: "https://picsum.photos/seed/estate-16/400/300", beds: 4, baths: 3, area: 195 },
  { id: "p17", type: "property", title: "Modern Apartment", location: "Baku, Binagadi", price: 105000, image: "https://picsum.photos/seed/estate-17/400/300", beds: 2, baths: 1, area: 69 },
  { id: "p18", type: "property", title: "Family Apartment", location: "Baku, Nizami", price: 126000, image: "https://picsum.photos/seed/estate-18/400/300", beds: 3, baths: 2, area: 92 },
  { id: "p19", type: "property", title: "Townhouse", location: "Baku, Yasamal", price: 249000, image: "https://picsum.photos/seed/estate-19/400/300", beds: 3, baths: 2, area: 140 },
  { id: "t3", type: "text", title: "Villas", description: "Houses with a private garden" },
  { id: "p20", type: "property", title: "Villa with Pool", location: "Baku, Bilgah", price: 610000, image: "https://picsum.photos/seed/estate-20/400/300", beds: 6, baths: 5, area: 350 },
  { id: "p21", type: "property", title: "Cozy Studio", location: "Baku, Sabail", price: 92000, image: "https://picsum.photos/seed/estate-21/400/300", beds: 1, baths: 1, area: 44 },
  { id: "p22", type: "property", title: "Modern Apartment", location: "Baku, Nasimi", price: 141000, image: "https://picsum.photos/seed/estate-22/400/300", beds: 3, baths: 2, area: 90 },
  { id: "p23", type: "property", title: "Sea View Apartment", location: "Baku, Bayil", price: 285000, image: "https://picsum.photos/seed/estate-23/400/300", beds: 3, baths: 2, area: 108 },
  { id: "p24", type: "property", title: "Villa with Garden", location: "Baku, Mardakan", price: 395000, image: "https://picsum.photos/seed/estate-24/400/300", beds: 4, baths: 3, area: 225 },
  { id: "p25", type: "property", title: "Loft Apartment", location: "Baku, Khatai", price: 149000, image: "https://picsum.photos/seed/estate-25/400/300", beds: 2, baths: 1, area: 75 },
  { id: "p26", type: "property", title: "Family Apartment", location: "Baku, Narimanov", price: 118000, image: "https://picsum.photos/seed/estate-26/400/300", beds: 3, baths: 2, area: 86 },
  { id: "p27", type: "property", title: "Penthouse", location: "Baku, Yasamal", price: 540000, image: "https://picsum.photos/seed/estate-27/400/300", beds: 4, baths: 2, area: 180 },
  { id: "p28", type: "property", title: "Cozy Studio", location: "Baku, Binagadi", price: 52000, image: "https://picsum.photos/seed/estate-28/400/300", beds: 1, baths: 1, area: 33 },
  { id: "p29", type: "property", title: "Townhouse", location: "Baku, Nizami", price: 218000, image: "https://picsum.photos/seed/estate-29/400/300", beds: 3, baths: 3, area: 125 },
  { id: "t4", type: "text", title: "New builds" },
  { id: "p30", type: "property", title: "Modern Apartment", location: "Baku, Sabail", price: 265000, image: "https://picsum.photos/seed/estate-30/400/300", beds: 3, baths: 2, area: 102 },
  { id: "p31", type: "property", title: "Villa with Pool", location: "Baku, Zagulba", price: 720000, image: "https://picsum.photos/seed/estate-31/400/300", beds: 6, baths: 4, area: 340 },
  { id: "p32", type: "property", title: "Cozy Studio", location: "Baku, Yasamal", price: 71000, image: "https://picsum.photos/seed/estate-32/400/300", beds: 1, baths: 1, area: 40 },
  { id: "p33", type: "property", title: "Family Apartment", location: "Baku, Khatai", price: 129000, image: "https://picsum.photos/seed/estate-33/400/300", beds: 3, baths: 2, area: 94 },
  { id: "p34", type: "property", title: "Loft Apartment", location: "Baku, Nasimi", price: 163000, image: "https://picsum.photos/seed/estate-34/400/300", beds: 2, baths: 2, area: 80 },
  { id: "p35", type: "property", title: "Sea View Apartment", location: "Baku, Sabail", price: 340000, image: "https://picsum.photos/seed/estate-35/400/300", beds: 4, baths: 3, area: 128 },
  { id: "p36", type: "property", title: "Townhouse", location: "Baku, Bilgah", price: 292000, image: "https://picsum.photos/seed/estate-36/400/300", beds: 4, baths: 3, area: 158 },
  { id: "p37", type: "property", title: "Modern Apartment", location: "Baku, Narimanov", price: 109000, image: "https://picsum.photos/seed/estate-37/400/300", beds: 2, baths: 1, area: 70 },
  { id: "p38", type: "property", title: "Penthouse", location: "Baku, Nizami", price: 625000, image: "https://picsum.photos/seed/estate-38/400/300", beds: 5, baths: 3, area: 205 },
  { id: "p39", type: "property", title: "Villa with Garden", location: "Baku, Binagadi", price: 365000, image: "https://picsum.photos/seed/estate-39/400/300", beds: 4, baths: 3, area: 215 },
  { id: "t5", type: "text", title: "Near metro", description: "Under 10 minutes on foot" },
  { id: "p40", type: "property", title: "Cozy Studio", location: "Baku, Khatai", price: 66000, image: "https://picsum.photos/seed/estate-40/400/300", beds: 1, baths: 1, area: 37 },
  { id: "p41", type: "property", title: "Modern Apartment", location: "Baku, Yasamal", price: 138000, image: "https://picsum.photos/seed/estate-41/400/300", beds: 2, baths: 2, area: 82 },
  { id: "p42", type: "property", title: "Family Apartment", location: "Baku, Sabail", price: 192000, image: "https://picsum.photos/seed/estate-42/400/300", beds: 3, baths: 2, area: 98 },
  { id: "p43", type: "property", title: "Townhouse", location: "Baku, Nasimi", price: 256000, image: "https://picsum.photos/seed/estate-43/400/300", beds: 3, baths: 2, area: 136 },
  { id: "p44", type: "property", title: "Villa with Pool", location: "Baku, Mardakan", price: 585000, image: "https://picsum.photos/seed/estate-44/400/300", beds: 5, baths: 4, area: 300 },
  { id: "p45", type: "property", title: "Loft Apartment", location: "Baku, Binagadi", price: 121000, image: "https://picsum.photos/seed/estate-45/400/300", beds: 2, baths: 1, area: 66 },
  { id: "p46", type: "property", title: "Sea View Apartment", location: "Baku, Bayil", price: 298000, image: "https://picsum.photos/seed/estate-46/400/300", beds: 3, baths: 2, area: 112 },
  { id: "p47", type: "property", title: "Cozy Studio", location: "Baku, Nizami", price: 57000, image: "https://picsum.photos/seed/estate-47/400/300", beds: 1, baths: 1, area: 36 },
  { id: "p48", type: "property", title: "Modern Apartment", location: "Baku, Narimanov", price: 115000, image: "https://picsum.photos/seed/estate-48/400/300", beds: 2, baths: 1, area: 74 },
  { id: "p49", type: "property", title: "Penthouse", location: "Baku, Bilgah", price: 830000, image: "https://picsum.photos/seed/estate-49/400/300", beds: 5, baths: 4, area: 230 },
];

  return (
    <div>
      <HeroSection />
      <FeaturesSection />
      <PropertyShowcase />
    </div>
  )
}

export default HomePage