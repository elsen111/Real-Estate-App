import clsx from "clsx";

interface RegistrationStepperProps {
  currentStep: number;
}

const STEPS = [
  {
    number: 1,
    title: "Personal Details",
    description: "Create your owner account",
  },
  {
    number: 2,
    title: "Agency Details",
    description: "Provide your agency information",
  },
];

const RegistrationStepper = ({ currentStep }: RegistrationStepperProps) => {
  return (
    <div className="mb-8">
      <div className="flex items-center">
        {STEPS.map((step, index) => {
          const isCompleted = currentStep > step.number;
          const isActive = currentStep === step.number;

          return (
            <div
              key={step.number}
              className={clsx("flex items-center", index === 0 ? "flex-1" : "")}
            >
              <div className="flex items-center gap-3">
                <div
                  className={clsx(
                    "flex size-11 shrink-0 items-center justify-center rounded-full border-2 text-[1.5rem] font-semibold transition",
                    isActive || isCompleted
                      ? "border-secondary bg-secondary text-white"
                      : "border-gray-400 text-gray-500",
                  )}
                >
                  {step.number}
                </div>

                <div className="hidden sm:block">
                  <p
                    className={clsx(
                      "text-[1.4rem] font-semibold",
                      isActive ? "text-secondary" : "text-gray-600",
                    )}
                  >
                    {step.title}
                  </p>

                  <p className="text-[1.2rem] text-gray-500">{step.description}</p>
                </div>
              </div>

              {index === 0 && (
                <div
                  className={clsx(
                    "mx-4 h-0.5 flex-1",
                    isCompleted ? "bg-secondary" : "bg-gray-300",
                  )}
                />
              )}
            </div>
          );
        })}
      </div>
    </div>
  );
};

export default RegistrationStepper;
