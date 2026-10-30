import { Outlet } from "react-router-dom";
import Header from "../../components/common/Header";
import Footer from "../../components/common/Footer";
import ScrollToTop from "../../components/common/ScrollToTop";
// import SEO from "../../components/common/SEO";

const PublicLayout = () => {
  return (
    <>
      {/* For fallbacks, add SEO component to the layout roots also */}
      {/* <SEO /> */}
      <ScrollToTop />
      <Header />
      <main className="relative pt-[7.2rem] md:pt-[9.6rem]">
        <Outlet />
      </main>
      <Footer />
    </>
  );
};

export default PublicLayout;
