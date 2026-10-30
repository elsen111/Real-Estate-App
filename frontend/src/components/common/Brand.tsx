import { siteMetaData } from "../../content/site.data";
import logo from "../../assets/images/logo.png";
import { Link } from "react-router-dom";
import clsx from "clsx";

const Brand = ({ styles }: { styles?: string }) => {
  return (
    <Link to="/" className={clsx("flex h-full items-center justify-between gap-6", styles)}>
      <img src={logo} alt={`Site Logo - ${siteMetaData.name}`} className="h-full" />
      <h2 className="text-secondaryText text-5xl font-medium">
        {siteMetaData.name ? siteMetaData.name : "Real Estate"}
      </h2>
    </Link>
  );
};

export default Brand;
