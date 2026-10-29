import { Bath, Heart, MapPinHouse, Ruler, Sparkles, Image } from "lucide-react";

type PropertyCardProps = {
  image: string;
  price: number;
  currency: string;
  description: string;
  period?: string;
  title: string;
  address: string;
  bathrooms: number;
  area: number;
  featured?: boolean;
  listingType: "SALE" | "RENT";
};

const PropertyCard = ({
  image,
  price,
  currency,
  period = "month",
  title,
  description,
  address,
  bathrooms,
  area,
  featured,
  listingType,
}: PropertyCardProps) => {
  return (
    <section className="relative flex h-full w-full max-w-xl cursor-pointer flex-col overflow-hidden rounded-xl bg-white shadow-md">
      {/* Image */}
      <div className="relative h-76 w-full">
        <div className="absolute inset-0 bg-linear-to-t from-black/10 via-black/5 to-transparent transition-opacity duration-30" />

        {image ? (
          <img src={image} alt={title} className="h-76 w-full object-cover" loading="lazy" />
        ) : (
          <div className="flex h-76 flex-col items-center justify-center gap-3 rounded-t-2xl border border-gray-500/20">
            <Image size={28} color="#bbb" />
            <p className="text-xl text-gray-400"> No preview image available</p>
          </div>
        )}

        {featured && (
          <div className="bg-purple absolute top-4 left-0 flex items-center gap-2 rounded-r-md px-3 py-2 text-white">
            <Sparkles size={16} />
            <span className="text-[1.1rem] font-semibold uppercase">Featured</span>
          </div>
        )}

        {listingType && (
          <div
            className={`xm:right-4 absolute top-4 right-1 flex items-center gap-2 rounded-full bg-white px-4 py-2 text-[1.1rem] font-semibold shadow-lg ${
              listingType === "SALE" ? "text-emerald-700" : "text-blue-700"
            }`}
          >
            <span
              className={`h-2 w-2 rounded-full ${
                listingType === "SALE" ? "bg-emerald-500" : "bg-blue-500"
              }`}
            />
            {listingType === "SALE" ? "For Sale" : "For Rent"}
          </div>
        )}
      </div>

      {/* Details */}
      <div className="relative flex flex-1 flex-col justify-between gap-1 p-5 sm:gap-3">
        <div className="flex items-center justify-between">
          <div className="flex items-baseline">
            <span className="text-purple xs:text-[2rem] text-[1.8rem] font-bold">
              {price.toLocaleString("en-US")}
              <small>
                {" "}
                {currency}
                {listingType == "RENT" && (
                  <span className="ml-[0.2rem] text-[1.3rem] text-gray-500 sm:ml-1">/{period}</span>
                )}
              </small>
            </span>
          </div>

          <button
            type="button"
            aria-label="Add to favorites"
            className="border-purple/20 text-purple hover:bg-purple flex h-12 w-12 cursor-pointer items-center justify-center rounded-full border transition-all hover:text-white"
            onClick={(e) => {
              e.stopPropagation();
              alert("Add to favorites not implemented yet");
            }}
          >
            <Heart size={21} />
          </button>
        </div>

        <h3 className="text-secondaryText line-clamp-1 text-[1.8rem] font-bold">{title}</h3>

        <p className="line-clamp-1 text-[1.3rem] text-gray-500">{description}</p>

        <div className="my-1 h-px w-full bg-gray-200" />

        <div className="flex flex-wrap items-center justify-between gap-x-5 gap-y-3 text-[1.2rem] text-gray-600">
          <div className="flex items-center gap-2">
            <MapPinHouse size={17} className="text-purple" />
            <span className="line-clamp-1"> {address} </span>
          </div>

          <div className="flex items-center gap-2">
            <Bath size={17} className="text-purple" />
            <span>{bathrooms} Bathrooms</span>
          </div>

          <div className="flex items-center gap-2">
            <Ruler size={17} className="text-purple" />
            <span>
              {area} m <sup>2</sup>
            </span>
          </div>
        </div>
      </div>
    </section>
  );
};

export default PropertyCard;
