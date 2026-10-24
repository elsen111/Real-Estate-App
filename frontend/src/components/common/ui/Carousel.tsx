import {
  useCallback,
  useEffect,
  useRef,
  useState,
  type KeyboardEvent,
  type ReactNode,
} from "react";
import { ChevronLeft, ChevronRight } from "lucide-react";

type CarouselProps<T extends { id: string }> = {
  items: T[];
  value?: string | null;
  onChange?: (id: string | null) => void;
  renderItem: (item: T, selected: boolean) => ReactNode;
  itemClassName?: string;
};

export function Carousel<T extends { id: string }>({
  items,
  value = null,
  onChange,
  renderItem,
  itemClassName = "w-72",
}: CarouselProps<T>) {
  const track = useRef<HTMLDivElement>(null);
  const [mounted, setMounted] = useState(false);
  const [edge, setEdge] = useState({ start: true, end: false });

  const updateEdges = useCallback(() => {
    const el = track.current;
    if (!el) return;
    setEdge({
      start: el.scrollLeft <= 4,
      end: el.scrollLeft + el.clientWidth >= el.scrollWidth - 4,
    });
  }, []);

  useEffect(() => {
    const raf = requestAnimationFrame(() => setMounted(true));
    updateEdges();
    window.addEventListener("resize", updateEdges);

    return () => {
      cancelAnimationFrame(raf);
      window.removeEventListener("resize", updateEdges);
    };
  }, [items, updateEdges]);

  const scrollByPage = (dir: 1 | -1) => {
    const el = track.current;
    el?.scrollBy({
      left: dir * el.clientWidth * 0.8,
      behavior: "smooth",
    });
  };

  // Arrow keys move focus between options; the browser scrolls the focused one into view
  const onKeyDown = (e: KeyboardEvent<HTMLDivElement>) => {
    if (e.key !== "ArrowRight" && e.key !== "ArrowLeft") return;

    const options = Array.from(
      track.current!.querySelectorAll<HTMLButtonElement>("[role=option]")
    );

    const i = options.indexOf(document.activeElement as HTMLButtonElement);
    options[i + (e.key === "ArrowRight" ? 1 : -1)]?.focus();
    e.preventDefault();
  };

  return (
    <div className="relative">
      <div
        ref={track}
        role="listbox"
        onScroll={updateEdges}
        onKeyDown={onKeyDown}
        className="snap-x snap-mandatory overflow-x-auto scroll-smooth px-1 py-3
          [scrollbar-width:none] [&::-webkit-scrollbar]:hidden"
      >
        <div className="flex w-max min-w-full justify-center gap-4">
          {items.map((item, i) => {
            const selected = item.id === value;

            return (
              <div
                key={item.id}
                style={{ transitionDelay: `${i * 70}ms` }}
                className={`shrink-0 snap-start transition-all duration-500 ${itemClassName} ${
                  mounted
                    ? "translate-y-0 opacity-100"
                    : "translate-y-4 opacity-0"
                }`}
              >
                <button
                  type="button"
                  role="option"
                  aria-selected={selected}
                  onClick={() => onChange?.(selected ? null : item.id)}
                  className={`h-full w-full rounded-2xl text-left outline-none transition-all duration-300
                    focus-visible:ring-2 focus-visible:ring-indigo-400 ${
                      selected
                        ? "scale-[1.02] ring-2 ring-indigo-600 shadow-lg"
                        : "hover:-translate-y-1 hover:shadow-md"
                    }`}
                >
                  {renderItem(item, selected)}
                </button>
              </div>
            );
          })}
        </div>
      </div>

      {(["start", "end"] as const).map((side) => (
        <button
          key={side}
          type="button"
          aria-label={side === "start" ? "Previous" : "Next"}
          disabled={edge[side]}
          onClick={() => scrollByPage(side === "start" ? -1 : 1)}
          className={`absolute top-1/2 z-10 -translate-y-1/2 rounded-full
            bg-white p-1.5 sm:p-2 shadow-md transition-all
            hover:bg-gray-100
            disabled:pointer-events-none disabled:opacity-0
            ${side === "start" ? "left-0" : "right-0"}`}
        >
          {side === "start" ? (
            <ChevronLeft className="h-5 w-5 sm:h-6 sm:w-6 text-gray-700" />
          ) : (
            <ChevronRight className="h-5 w-5 sm:h-6 sm:w-6 text-gray-700" />
          )}
        </button>
      ))}
    </div>
  );
}