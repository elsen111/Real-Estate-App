import { Link } from "react-router-dom"
import type { FooterNavGroup, FooterNavLink } from "../../../types/content/siteContentTypes"

type FooterNavGroupProps = {
      heading: string,
      links: FooterNavLink[]
}

const NavGroup = ({heading, links} : FooterNavGroupProps) => {
  return (
    <section className="min-w-[15rem]">
      <h6 className="font-bold mb-4 text-[2.1rem] sm:text-[2rem]">{heading}</h6>
      <ul>
            {links.map(link => (
                  <li key={link.label} className="py-1">
                        <Link className="text-[1.6rem] inline-block text-[#cbd5e1] hover:translate-x-2 duration-300 transition-all" to={link.path}> {link.label} </Link>
                  </li>
            ))}
      </ul>
    </section>
  )
}

export default NavGroup