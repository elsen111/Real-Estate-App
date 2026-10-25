import type { LucideIcon } from "lucide-react"

type FeatureCardProps = {
      icon: LucideIcon,
      name: string,
      description: string
}

const FeatureCard = ({ icon, name, description }: FeatureCardProps) => {

      const Icon = icon;

      return (
            <section className="flex flex-col sm:items-baseline items-center gap-4 bg-gray-scale p-5 rounded-2xl transition-all duration-300 hover:rotate-6">
                  <div className="bg-purple rounded-full flex items-center justify-center h-17 w-17">
                        <Icon size={25} strokeWidth={1.8} className="text-primary" />
                  </div>
                  <h6 className="text-[2.5rem] manrope-600 xs:text-5xl font-medium mb-5 text-secondary sm:text-start text-center"> {name} </h6>
                  <p className="text-[1.6rem] text-gray-600 font-medium sm:text-start text-center"> {description} </p>
            </section>
      )
}

export default FeatureCard