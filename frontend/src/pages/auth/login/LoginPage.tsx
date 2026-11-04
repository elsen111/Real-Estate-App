import AuthHeaderSection from "../../../components/AuthPages/common/AuthHeaderSection";
import AuthPageBadge from "../../../components/AuthPages/common/AuthPageBadge";
import LoginForm from "../../../components/AuthPages/LoginPage/LoginForm";
import { loginPageHeaderContent } from "../../../content/site.data";

const LoginPage = () => {
  return (
    <div>
      <AuthPageBadge badgeText="Sign in" />
      <AuthHeaderSection
        heading={loginPageHeaderContent.heading}
        description={loginPageHeaderContent.description}
      />
      <LoginForm />
    </div>
  );
};

export default LoginPage;
