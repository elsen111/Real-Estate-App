import { Building2, Eye, MapPin } from "lucide-react";
import cityCardBgImg from "../../../assets/images/city-card-bg.png";

type CityCardProps = {
  cityName: string;
  propertyCount: number;
  totalViews: number;
};

const CityCard = ({ cityName, propertyCount, totalViews }: CityCardProps) => {
  return (
    <section
      style={{ backgroundImage: `url(${cityCardBgImg})` }}
      className="group relative flex h-full w-full cursor-pointer flex-col justify-end overflow-hidden rounded-2xl bg-cover bg-center text-white transition-transform duration-300 hover:rotate-x-30"
    >
      <div className="absolute inset-0 bg-linear-to-t from-black/85 via-black/50 to-transparent transition-colors duration-300 group-hover:from-black/90" />

      <div className="relative z-10 flex flex-col gap-5 p-6">
        <div className="flex items-center gap-2 text-white/80">
          <MapPin size={17} />
          <span className="text-sm font-medium tracking-wide uppercase">Popular destination</span>
        </div>

        <div>
          <h3 className="text-4xl font-bold tracking-tight">{cityName}</h3>
          <p className="mt-2 text-[1.2rem] text-white/75">Explore properties in {cityName}</p>
        </div>

        <div className="flex items-center justify-between gap-6 border-t border-white/20 pt-4">
          <div className="flex items-center gap-2.5">
            <div className="rounded-lg bg-white/15 p-2">
              <Building2 size={19} />
            </div>
            <div>
              <p className="text-xl font-bold">{propertyCount.toLocaleString()}</p>
              <p className="text-[1.1rem] text-white/70">Properties</p>
            </div>
          </div>

          <div className="h-9 w-px bg-white/20" />

          <div className="flex items-center gap-2.5">
            <div className="rounded-lg bg-white/15 p-2">
              <Eye size={19} />
            </div>
            <div>
              <p className="text-xl font-bold">{totalViews.toLocaleString()}</p>
              <p className="text-[1.1rem] text-white/70">Total views</p>
            </div>
          </div>
        </div>
      </div>
    </section>
  );
};

export default CityCard;
