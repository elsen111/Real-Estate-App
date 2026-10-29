import { Building2, Eye, Houses, Star } from "lucide-react";

type AgencyCardProps = {
  name: string;
  logoUrl: string;
  listingCount: number;
  aggregateViews: number;
  averageRating: number;
};

const AgencyCard = ({
  name,
  logoUrl,
  listingCount,
  aggregateViews,
  averageRating,
}: AgencyCardProps) => {
  return (
    <section className="group border-secondary/10 relative flex h-full w-full cursor-pointer flex-col justify-end overflow-hidden rounded-2xl border bg-linear-to-t from-black/10 via-black/5 to-transparent bg-cover bg-center text-white transition-transform duration-300">
      <div className="relative z-10 flex h-full flex-col justify-between p-6">
        <div className="flex items-center gap-5">
          <div className="h-23 w-25 shrink-0 overflow-hidden rounded-2xl">
            {logoUrl ? (
              <img
                src={logoUrl}
                alt={`${name} agency's logo`}
                className="h-full w-full rounded-2xl object-cover"
              />
            ) : (
              <div className="flex h-full flex-col items-center justify-center rounded-2xl border border-gray-500/20 bg-white">
                <Houses size={28} color="#bbb" />
              </div>
            )}
          </div>

          <div className="min-w-0">
            <h6 className="text-secondary truncate text-[1.8rem] font-bold">{name}</h6>
            <p className="mt-1 text-lg text-gray-500">Agency</p>
          </div>
        </div>

        <div className="grid grid-cols-3 divide-x divide-gray-300/60 border-t border-gray-300/50 pt-5">
          <div className="flex flex-col items-center gap-2 px-2">
            <div className="flex items-center gap-2">
              <Building2 size={18} className="text-secondary" />
              <span className="text-lg font-bold text-gray-700">
                {listingCount?.toLocaleString() ?? 0}
              </span>
            </div>
            <span className="text-md font-medium text-gray-500">Listings</span>
          </div>

          {/* Rating */}
          <div className="flex flex-col items-center gap-2 px-2">
            <div className="flex items-center gap-2">
              <Star size={18} className="fill-amber-400 text-amber-400" />
              <span className="text-lg font-bold text-gray-700">
                {averageRating?.toFixed(1) ?? "0.0"}
              </span>
            </div>
            <span className="text-md font-medium text-gray-500">Rating</span>
          </div>

          <div className="flex flex-col items-center gap-2 px-2">
            <div className="flex items-center gap-2">
              <Eye size={18} className="text-secondary" />
              <span className="text-lg font-bold text-gray-700">
                {aggregateViews?.toLocaleString() ?? 0}
              </span>
            </div>
            <span className="text-md font-medium text-gray-500">Views</span>
          </div>
        </div>
      </div>
    </section>
  );
};

export default AgencyCard;
