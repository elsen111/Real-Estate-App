import type { LucideIcon } from "lucide-react";

type FeatureCardProps = {
  icon: LucideIcon;
  name: string;
  description: string;
};

const FeatureCard = ({ icon, name, description }: FeatureCardProps) => {
  const Icon = icon;

  return (
    <section className="bg-gray-scale flex flex-col items-center gap-4 rounded-2xl p-5 transition-all duration-300 hover:rotate-6 sm:items-baseline">
      <div className="bg-purple flex h-17 w-17 items-center justify-center rounded-full">
        <Icon size={25} strokeWidth={1.8} className="text-primary" />
      </div>
      <h6 className="manrope-600 xs:text-5xl text-secondary mb-5 text-center text-[2.5rem] font-medium sm:text-start">
        {name}
      </h6>
      <p className="text-center text-[1.6rem] font-medium text-gray-600 sm:text-start">
        {description}
      </p>
    </section>
  );
};

export default FeatureCard;
