import { Outlet } from "react-router-dom";
import authBgImg from "../../assets/images/auth-bg.jpg";

const AuthLayout = () => {
  return (
    <>
      <main
        style={{
          backgroundImage: `url(${authBgImg})`,
          backgroundPosition: "left bottom",
        }}
        className="relative flex min-h-screen bg-[#e0e9f9] bg-size-[200%_100%] bg-no-repeat p-6 sm:p-10 lg:bg-size-[48%_100%]"
      >
        <section className="xxs:p-6 flex w-full flex-col items-center justify-center rounded-2xl bg-gray-300/80 p-4 lg:ml-auto lg:w-[50%] lg:bg-transparent">
          <Outlet />
        </section>
      </main>
    </>
  );
};

export default AuthLayout;
