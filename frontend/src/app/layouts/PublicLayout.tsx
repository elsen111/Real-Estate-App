import { Outlet } from "react-router-dom"
import Footer from "../../components/common/Footer"
import Header from "../../components/common/header"
import SEO from "../../components/common/SEO"


const PublicLayout = () => {
      return (
            <>
                  {/* <SEO /> */}
                  <Header />
                  <main className="relative md:top-[9.6rem] top-[7.2rem]">
                        <Outlet />
                  </main>
                  <Footer />
            </>
      )
}

export default PublicLayout