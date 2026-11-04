import AuthHeaderSection from "../../../components/AuthPages/common/AuthHeaderSection";
import BackToRegisterSelection from "../../../components/AuthPages/RegisterPages/common/BackToRegisterSelection";
import { clientRegistrationPageHeaderContent } from "../../../content/site.data";
import ClientRegisterForm from "../../../components/AuthPages/RegisterPages/ClientRegistration/ClientRegisterForm";

const ClientRegistrationPage = () => {
  return (
    <div>
      <BackToRegisterSelection />
      <AuthHeaderSection
        className="xxs:w-full! xxxs:w-[94%]! mt-20 w-[90%]!"
        heading={clientRegistrationPageHeaderContent.heading}
        description={clientRegistrationPageHeaderContent.description}
      />
      <ClientRegisterForm />
    </div>
  );
};

export default ClientRegistrationPage;
