import { Link } from "react-router-dom";
import type { FooterNavLink } from "../../../types/content/siteContentTypes";

type FooterNavGroupProps = {
  heading: string;
  links: FooterNavLink[];
};

const NavGroup = ({ heading, links }: FooterNavGroupProps) => {
  return (
    <section className="min-w-[15rem]">
      <h6 className="mb-4 text-[2.1rem] font-bold sm:text-[2rem]">{heading}</h6>
      <ul>
        {links.map((link) => (
          <li key={link.label} className="py-1">
            <Link
              className="inline-block text-[1.6rem] text-[#cbd5e1] transition-all duration-300 hover:translate-x-2"
              to={link.path}
            >
              {link.label}
            </Link>
          </li>
        ))}
      </ul>
    </section>
  );
};

export default NavGroup;
