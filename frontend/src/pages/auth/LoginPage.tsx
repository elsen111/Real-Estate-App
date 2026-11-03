import AuthHeaderSection from "../../components/AuthPage/AuthHeaderSection";
import AuthPageBadge from "../../components/AuthPage/AuthPageBadge";
import LoginForm from "../../components/AuthPage/LoginForm";
import { loginPageHeaderContent } from "../../content/site.data";

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
