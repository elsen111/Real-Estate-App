import AuthHeaderSection from "../../../components/AuthPages/common/AuthHeaderSection";
import AuthPageBadge from "../../../components/AuthPages/common/AuthPageBadge";
import AuthSwitch from "../../../components/AuthPages/common/AuthSwitch";
import RegisterOptionsSection from "../../../components/AuthPages/RegisterPages/RegisterOptionsPage/RegisterOptionsSection";
import RegisterSelectionPageFooter from "../../../components/AuthPages/RegisterPages/RegisterOptionsPage/RegisterSelectionPageFooter";
import { registerSelectionPageHeaderContent } from "../../../content/site.data";

const RegisterSelectionPage = () => {
  return (
    <div className="relative flex h-full w-full flex-col items-start justify-center gap-1">
      <AuthSwitch switchTo="toLogin" className="absolute top-1.5 right-0 self-end sm:top-1" />
      <AuthPageBadge
        badgeText="Create account"
        className="relative top-23 max-w-65 text-center sm:top-14"
      />
      <AuthHeaderSection
        heading={registerSelectionPageHeaderContent.heading}
        description={registerSelectionPageHeaderContent.description}
        className="mt-10 md:mt-0"
      />
      <RegisterOptionsSection />
      <RegisterSelectionPageFooter />
    </div>
  );
};

export default RegisterSelectionPage;
