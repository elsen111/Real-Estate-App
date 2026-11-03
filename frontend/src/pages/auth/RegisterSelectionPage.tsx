import AuthHeaderSection from "../../components/AuthPage/AuthHeaderSection";
import AuthPageBadge from "../../components/AuthPage/AuthPageBadge";
import AuthSwitch from "../../components/AuthPage/AuthSwitch";
import RegisterOptionsSection from "../../components/AuthPage/RegisterOptionsSection";
import RegisterSelectionPageFooter from "../../components/AuthPage/RegisterSelectionPageFooter";
import { registerSelectionPageHeaderContent } from "../../content/site.data";

const RegisterSelectionPage = () => {
  return (
    <div className="relative flex h-full w-full flex-col items-start justify-center gap-1">
      <AuthSwitch switchTo="toLogin" className="absolute top-1.5 right-0 self-end sm:top-1" />
      <AuthPageBadge badgeText="Create account" className="relative top-14 max-w-65 text-center" />
      <AuthHeaderSection
        heading={registerSelectionPageHeaderContent.heading}
        description={registerSelectionPageHeaderContent.description}
      />
      <RegisterOptionsSection />
      <RegisterSelectionPageFooter />
    </div>
  );
};

export default RegisterSelectionPage;
