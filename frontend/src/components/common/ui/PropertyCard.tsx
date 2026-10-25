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
      listingType: 'SALE' | 'RENT'
}

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
      listingType
}: PropertyCardProps) => {

      return (
            <section className="relative w-full cursor-pointer max-w-[36rem] overflow-hidden rounded-xl bg-white shadow-md h-full flex flex-col">

                  {/* Image */}
                  <div className="relative h-[19rem] w-full">
                        <div className="absolute inset-0 bg-gradient-to-t from-black/10 via-black/5 to-transparent transition-opacity duration-30" />

                        {image ? (
                              <img
                                    src={image}
                                    alt={title}
                                    className="h-full w-full object-cover"
                                    loading="lazy"
                              />
                        )

                              : (
                                    <div className="border-1 border-gray-500/20 rounded-t-2xl h-full flex flex-col items-center justify-center gap-3">
                                          <Image size={28} color="#bbb" />
                                          <p className="text-gray-400 text-xl"> No preview image available</p>
                                    </div>
                              )
                        }

                        {featured && (
                              <div className="absolute left-0 top-4 flex items-center gap-2 rounded-r-md bg-purple px-3 py-2 text-white">
                                    <Sparkles size={16} />
                                    <span className="text-[1.1rem] font-semibold uppercase">
                                          Featured
                                    </span>
                              </div>
                        )}

                        {listingType && (
                              <div
                                    className={`absolute right-4 top-4 flex items-center gap-2 rounded-full
                                    bg-white px-4 py-2 text-[1.1rem] font-semibold shadow-lg
                                    ${listingType === "SALE"
                                                ? "text-emerald-700"
                                                : "text-blue-700"
                                          }`}
                              >
                                    <span
                                          className={`h-2 w-2 rounded-full ${listingType === "SALE" ? "bg-emerald-500" : "bg-blue-500"
                                                }`}
                                    />
                                    {listingType === "SALE" ? "For Sale" : "For Rent"}
                              </div>
                        )}
                  </div>

                  {/* Details */}
                  <div className="relative flex flex-col gap-3 p-5 justify-between flex-1">
                        <div className="flex items-center justify-between">
                              <div className="flex items-baseline">
                                    <span className="text-[2rem] font-bold text-purple">
                                          {price.toLocaleString("en-US")}
                                          <small> {currency} </small>
                                    </span>
                                    {
                                          listingType == 'RENT' &&

                                          <span className="ml-1 text-[1.3rem] text-gray-500">
                                                /{period}
                                          </span>
                                    }
                              </div>

                              <button
                                    type="button"
                                    aria-label="Add to favorites"
                                    className="flex h-12 w-12 cursor-pointer items-center justify-center rounded-full border border-purple/20 text-purple transition-all hover:bg-purple hover:text-white"
                                    onClick={(e) => {
                                          e.stopPropagation();
                                          alert("Add to favorites not implemented yet")
                                    }
                                    }
                              >
                                    <Heart size={21} />
                              </button>
                        </div>

                        <h3 className="text-[1.8rem] font-bold text-secondaryText line-clamp-1">
                              {title}
                        </h3>

                        <p className="text-[1.3rem] text-gray-500 line-clamp-1">
                              {description}
                        </p>

                        <div className="my-1 h-px w-full bg-gray-200" />

                        <div className="flex flex-wrap items-center justify-between gap-x-5 gap-y-3 text-[1.2rem] text-gray-600">

                              <div className="flex items-center gap-2">
                                    <MapPinHouse size={17} className="text-purple" />
                                    <span> {address} </span>
                              </div>

                              <div className="flex items-center gap-2">
                                    <Bath size={17} className="text-purple" />
                                    <span>{bathrooms} Bathrooms</span>
                              </div>

                              <div className="flex items-center gap-2">
                                    <Ruler size={17} className="text-purple" />
                                    <span>{area} m <sup>2</sup></span>
                              </div>

                        </div>
                  </div>
            </section>
      )
}

export default PropertyCard