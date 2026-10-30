import Brand from "../Brand";
import HeaderNav from "./HeaderNav";

const Header = () => {
  return (
    <header className="bg-gray-scale/95 fixed top-0 right-0 left-0 z-50 flex h-[7.2rem] items-center justify-between gap-4 overflow-visible px-7 py-8 shadow-2xl sm:px-12 md:h-[9.6rem]">
      <Brand />
      <HeaderNav />
    </header>
  );
};

export default Header;
