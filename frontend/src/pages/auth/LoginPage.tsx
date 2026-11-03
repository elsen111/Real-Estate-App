import AuthHeaderSection from "../../components/AuthPage/AuthHeaderSection";
import LoginForm from "../../components/AuthPage/LoginForm";
import { loginPageHeaderContent } from "../../content/site.data";

const LoginPage = () => {
  return (
    <div>
      <AuthHeaderSection
        heading={loginPageHeaderContent.heading}
        description={loginPageHeaderContent.description}
      />
      <LoginForm />
    </div>
  );
};

export default LoginPage;
