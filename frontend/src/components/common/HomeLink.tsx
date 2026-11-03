import { Home } from "lucide-react";
import { Link } from "react-router-dom";
import Tooltip from "./UI/Tooltip";
import { useState } from "react";
import clsx from "clsx";

const HomeLink = ({ className = "" }: { className?: string }) => {
  const [homeHovered, setHomeHovered] = useState(false);

  return (
    <div>
      <Link
        to="/"
        className={clsx(
          "text-secondaryText group absolute top-6 left-6 z-10 flex items-center gap-2 text-[1.8rem] font-bold transition-all duration-200 active:scale-90",
          className,
        )}
        onMouseEnter={() => setHomeHovered(true)}
        onMouseLeave={() => setHomeHovered(false)}
      >
        <Home size={24} />

        <Tooltip title="Go Home Page" placement="right" visible={homeHovered} />
      </Link>
    </div>
  );
};

export default HomeLink;
