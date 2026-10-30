import clsx from "clsx";

type TooltipProps = {
  title: string;
  placement: "bottom" | "top";
  visible: boolean;
};

const Tooltip = ({ title, placement, visible }: TooltipProps) => {
  const positions = {
    bottom: "top-[105%] top-1/2 -translate-y-1/2",
    top: "bottom-[100%] top-1/2 -translate-y-1/2",
  };

  return (
    <span
      className={clsx(
        "pointer-events-none absolute z-50 rounded-md bg-black/70 px-2.5 py-1 text-[12px] font-normal whitespace-nowrap text-white shadow-md transition-all duration-200",
        positions[placement],
        visible ? "visible opacity-100" : "invisible opacity-0",
      )}
    >
      {title}
    </span>
  );
};

export default Tooltip;
