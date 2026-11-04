import RegistrationOptionCard from "./RegistrationOptionCard";

const RegisterOptionsSection = () => {
  return (
    <section className="flex w-full flex-col items-center justify-between gap-5 md:flex-row">
      <RegistrationOptionCard variant="clientRegistration" />
      <RegistrationOptionCard variant="agencyRegistration" />
    </section>
  );
};

export default RegisterOptionsSection;
