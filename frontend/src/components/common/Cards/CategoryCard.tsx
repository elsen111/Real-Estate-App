import { ArrowRight } from "lucide-react";

type CategoryCardProps = {
  name: string;
  description: string;
  imageUrl: string;
  defaultImgUrl: string;
};

const CategoryCard = ({ name, description, imageUrl, defaultImgUrl }: CategoryCardProps) => {
  return (
    <section
      style={{
        backgroundImage: `url(${imageUrl ? imageUrl : defaultImgUrl})`,
      }}
      className="group relative isolate flex h-full min-h-70 w-full flex-col justify-end overflow-hidden rounded-2xl bg-cover bg-center text-white transition-transform duration-500 hover:-translate-y-1"
    >
      <div className="absolute inset-0 -z-20 bg-cover bg-center transition-transform duration-700 ease-out group-hover:scale-110" />

      <div className="absolute inset-0 -z-10 bg-linear-to-t from-slate-950 via-slate-950/45 to-slate-950/10 opacity-90 transition-all duration-500" />

      <div className="relative z-10 p-6 sm:p-7">
        <h3 className="text-2xl font-bold tracking-tight transition-transform duration-300 sm:text-3xl">
          {name}
        </h3>

        <p className="mt-2 line-clamp-2 max-w-md text-[1.4rem] leading-8 text-white/75">
          {description}
        </p>

        <div className="mt-5 flex items-center gap-2 text-xl font-semibold text-white transition-all duration-300 group-hover:gap-3">
          Explore {name}s
          <ArrowRight
            size={17}
            className="transition-transform duration-300 group-hover:translate-x-1"
          />
        </div>
      </div>
    </section>
  );
};

export default CategoryCard;
