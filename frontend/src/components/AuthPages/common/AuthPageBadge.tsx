import clsx from "clsx";

type AuthPageBadgeProps = {
  badgeText: string;
  className?: string;
};

const AuthPageBadge = ({ badgeText, className = "" }: AuthPageBadgeProps) => {
  return (
    <span
      className={clsx(
        "text-secondary bg-secondary/10 mb-8 rounded-4xl px-6 py-2 text-[1.4rem] font-bold sm:py-3 sm:text-[1.6rem]",
        className,
      )}
    >
      {badgeText}
    </span>
  );
};

export default AuthPageBadge;
