import Brand from "../Brand"
import HeaderNav from "./HeaderNav"


const Header = () => {
  return (
    <header className="h-[7.2rem] md:h-[9.6rem] fixed top-0 left-0 right-0 bg-gray-scale px-7 sm:px-12 py-8 flex items-center justify-between gap-4">
      <Brand />
      <HeaderNav />
    </header>
  )
}

export default Header