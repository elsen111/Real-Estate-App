import { type ReactNode } from "react";

const SectionContainer = ({ children }: { children: ReactNode }) => {
  return (
    <section className="relative z-30 w-full px-6 py-15 sm:px-8 sm:py-25 md:px-10 lg:px-14">
      {children}
    </section>
  );
};

export default SectionContainer;
