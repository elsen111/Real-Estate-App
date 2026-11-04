import AuthHeaderSection from "../../../components/AuthPages/common/AuthHeaderSection";
import RegistrationForms from "../../../components/AuthPages/RegisterPages/AgencyOwnerRegistration/RegistrationForms";
import BackToRegisterSelection from "../../../components/AuthPages/RegisterPages/common/BackToRegisterSelection";
import { ownerRegistrationPageHeaderContent } from "../../../content/site.data";

const AgencyOwnerRegistrationPage = () => {
  return (
    <div>
      <BackToRegisterSelection />
      <AuthHeaderSection
        className="xxs:w-full! xxxs:w-[94%]! mt-20 w-[90%]!"
        heading={ownerRegistrationPageHeaderContent.heading}
        description={ownerRegistrationPageHeaderContent.description}
      />
      <RegistrationForms />
    </div>
  );
};

export default AgencyOwnerRegistrationPage;
