import { Outlet } from "react-router-dom";
import Header from "../../components/common/Header";
import SEO from "../../components/common/SEO";
import Footer from "../../components/common/Footer";

const PublicLayout = () => {
  return (
    <>
      <SEO />
      <Header />
      <main className="relative pt-[7.2rem] md:pt-[9.6rem]">
        <Outlet />
      </main>
      <Footer />
    </>
  );
};

export default PublicLayout;
