import { Menu, UserRound, X } from "lucide-react";
import { useEffect, useState } from "react";
import { NavLink } from "react-router-dom";
import { useAuth } from "../../../context/AuthContext";
import Brand from "../Brand";
import Button from "../UI/Button";
import { MdOutlineFavorite } from "react-icons/md";
import Tooltip from "../UI/Tooltip";

type TopNavLink = {
  label: string;
  path: string;
};

const topNavLinks: TopNavLink[] = [
  { label: "Buy", path: "/buy" },
  { label: "Rent", path: "/rent" },
  { label: "Map", path: "/map" },
  { label: "Agencies", path: "/agencies" },
  { label: "Pricing", path: "/pricing" },
];

const HeaderNav = () => {
  const [menuOpen, setMenuOpen] = useState<boolean>(false);
  const [favHovered, setFavHovered] = useState<boolean>(false);

  const { user } = useAuth();

  useEffect(() => {
    document.body.style.overflow = menuOpen ? "hidden" : "";
    return () => {
      document.body.style.overflow = "";
    };
  }, [menuOpen]);

  return (
    <>
      <nav
        className={` ${menuOpen ? "translate-x-0" : "-translate-x-full"} bg-gray-scale/95 fixed inset-y-0 left-0 z-20 flex h-screen w-[80%] max-w-xl min-w-120 flex-col justify-between gap-10 overflow-y-auto rounded-r-3xl p-8 text-[20px] shadow-2xl transition-transform duration-500 lg:static lg:h-auto lg:w-[80%] lg:max-w-none lg:translate-x-0 lg:flex-row lg:items-center lg:justify-between lg:overflow-visible lg:rounded-none lg:bg-transparent lg:p-0 lg:shadow-none`}
      >
        <button
          aria-label="Close menu"
          onClick={() => setMenuOpen(false)}
          className="self-end rounded-2xl p-3 shadow-md transition-all duration-300 active:scale-95 lg:hidden"
        >
          <X size={32} className="text-primaryText" />
        </button>

        <Brand styles="lg:hidden w-85! h-10! pl-10" />

        <div className="flex flex-col gap-4 lg:flex-row xl:gap-10">
          {topNavLinks.map((link) => (
            <NavLink
              key={link.label}
              to={link.path}
              onClick={() => setMenuOpen(false)}
              className={({ isActive }) =>
                `text-purple before:bg-purple lg:after:bg-purple relative flex items-center gap-4 rounded-2xl px-5 py-4 transition-all duration-300 before:h-8 before:w-1.5 before:rounded-full before:transition-all before:duration-300 before:content-[''] lg:px-4 lg:py-3 lg:before:hidden lg:after:absolute lg:after:right-0 lg:after:bottom-0 lg:after:left-0 lg:after:h-1 lg:after:transition-all lg:after:duration-300 lg:after:content-[''] ${
                  isActive
                    ? "bg-purple/10 font-semibold shadow-md before:scale-y-100 before:opacity-100 lg:-translate-y-1 lg:bg-transparent lg:shadow-none lg:after:translate-y-0 lg:after:opacity-100"
                    : "hover:bg-purple/5 before:scale-y-0 before:opacity-0 hover:shadow-sm hover:before:scale-y-100 hover:before:opacity-100 lg:after:translate-y-2 lg:after:opacity-0 lg:hover:-translate-y-1 lg:hover:bg-transparent lg:hover:shadow-none lg:hover:after:translate-y-0 lg:hover:after:opacity-100"
                } `
              }
            >
              {link.label}
            </NavLink>
          ))}
        </div>

        <div className="mt-auto flex flex-col gap-4 lg:mt-0 lg:flex-row">
          <div
            className="relative hidden lg:block"
            onMouseEnter={() => setFavHovered(true)}
            onMouseLeave={() => setFavHovered(false)}
          >
            <Button
              path="/me/favorites"
              variant="white"
              isLink
              className="flex items-center justify-center bg-transparent! shadow-none! hover:scale-110"
            >
              <MdOutlineFavorite size={28} color="#100a55" />
            </Button>

            <Tooltip title="Favorites" placement="bottom" visible={favHovered} />
          </div>

          {user ? (
            <div className="flex items-center gap-8 px-8 py-4 lg:p-0">
              <Button
                path="/dashboard"
                variant="blue"
                circled={true}
                hasFixedDimensions={true}
                isLink={true}
              >
                {user.image ? (
                  <img
                    src={user.image}
                    className="block h-full! w-full! rounded-full object-cover"
                    alt="Profile avatar"
                  />
                ) : (
                  <UserRound size="24" />
                )}
              </Button>
              <span className="text-primaryText font-medium lg:hidden"> Account </span>
            </div>
          ) : (
            <>
              <Button path="/login" variant="white" isLink={true}>
                Login
              </Button>

              <Button path="/register" variant="blue" isLink={true}>
                List your agency
              </Button>
            </>
          )}
        </div>
      </nav>

      <div className="flex">
        <div
          className="relative block lg:hidden"
          onMouseEnter={() => setFavHovered(true)}
          onMouseLeave={() => setFavHovered(false)}
        >
          <Button
            path="/me/favorites"
            variant="white"
            isLink
            className="flex items-center justify-center bg-transparent! shadow-none! hover:scale-110"
          >
            <MdOutlineFavorite size={28} color="#100a55" />
          </Button>

          <Tooltip title="Favorites" placement="bottom" visible={favHovered} />
        </div>

        <button
          aria-label="Open menu"
          onClick={() => setMenuOpen(true)}
          className="block rounded-2xl p-2 shadow-md transition-all duration-300 active:scale-95 lg:hidden"
        >
          <Menu size={32} className="text-primaryText" />
        </button>
      </div>

      <div
        onClick={() => setMenuOpen(false)}
        className={` ${menuOpen ? "pointer-events-auto opacity-100" : "pointer-events-none opacity-0"} fixed inset-0 z-10 bg-black/40 backdrop-blur-[2px] transition-opacity duration-500 lg:hidden`}
      />
    </>
  );
};

export default HeaderNav;
