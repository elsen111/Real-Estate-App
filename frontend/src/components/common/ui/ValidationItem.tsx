import { CheckCircle2, XCircle } from "lucide-react";

type ValidationItemProps = {
  valid: boolean;
  label: string;
};

const ValidationItem = ({ valid, label }: ValidationItemProps) => (
  <div className={`flex items-center gap-2 text-lg ${valid ? "text-green-600" : "text-red-500"}`}>
    {valid ? (
      <CheckCircle2 size={16} className="shrink-0" />
    ) : (
      <XCircle size={16} className="shrink-0" />
    )}
    <span>{label}</span>
  </div>
);

export default ValidationItem;
