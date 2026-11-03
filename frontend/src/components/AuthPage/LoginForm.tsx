import clsx from "clsx";
import { Eye, EyeClosed, Lock, Mail } from "lucide-react";
import { useState, type ChangeEvent, type FormEvent } from "react";
import { useNavigate } from "react-router-dom";
import { ClipLoader } from "react-spinners";
import { useAuth } from "../../context/AuthContext";
import { toast } from "react-hot-toast";
import { getErrorMessage } from "../../utils/getErrorMessage";
import AuthSwitch from "./AuthSwitch";

const EMAIL_REGEX = /^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\.[a-zA-Z]{2,}$/;

const LoginForm = () => {
  const navigate = useNavigate();
  const { login } = useAuth();

  const [email, setEmail] = useState("");
  const [password, setPassword] = useState("");
  const [passwordVisible, setPasswordVisible] = useState(false);
  const [isSubmitting, setIsSubmitting] = useState(false);
  const [error, setError] = useState("");

  const formValid = EMAIL_REGEX.test(email.trim()) && password.length > 0;

  const clearError = () => {
    if (error) setError("");
  };

  const handleEmailChange = (e: ChangeEvent<HTMLInputElement>) => {
    setEmail(e.target.value);
    clearError();
  };

  const handlePasswordChange = (e: ChangeEvent<HTMLInputElement>) => {
    setPassword(e.target.value);
    clearError();
  };

  const handleSubmit = async (e: FormEvent<HTMLFormElement>) => {
    e.preventDefault();

    if (!formValid || isSubmitting) return;

    setError("");
    setIsSubmitting(true);

    try {
      await login({
        email: email.trim(),
        password,
      });

      toast.success("Logged into account successfully!", {
        duration: 1000,
        position: "top-center",

        iconTheme: {
          primary: "#000066",
          secondary: "#eee",
        },
        style: {
          background: "#eee",
          color: "#100a55",
          fontSize: "1.6rem",
          fontWeight: "medium",
        },
      });

      navigate("/dashboard", { replace: true });
    } catch (err) {
      setError(getErrorMessage(err, "Login failed."));
    } finally {
      setIsSubmitting(false);
    }
  };

  return (
    <section className="w-full max-w-200">
      <form onSubmit={handleSubmit} className="space-y-6">
        <div className="relative">
          <Mail
            size={20}
            aria-hidden="true"
            className="absolute top-1/2 left-10 -translate-y-1/2 text-gray-400"
          />

          <input
            type="email"
            name="email"
            id="email"
            autoComplete="email"
            value={email}
            placeholder="Email"
            required
            aria-label="Email"
            aria-invalid={!!error}
            onChange={handleEmailChange}
            className="focus:border-secondary w-full rounded-2xl border border-gray-500 bg-transparent py-4 pr-4 pl-26 text-[1.6rem] transition-all duration-200 outline-none focus:ring-2 focus:ring-blue-300 md:border-gray-400"
          />
        </div>

        <div className="relative">
          <Lock
            size={20}
            aria-hidden="true"
            className="absolute top-1/2 left-10 -translate-y-1/2 text-gray-400"
          />

          <input
            type={passwordVisible ? "text" : "password"}
            name="password"
            id="password"
            autoComplete="current-password"
            value={password}
            placeholder="Password"
            required
            aria-label="Password"
            aria-invalid={!!error}
            onChange={handlePasswordChange}
            className="focus:border-secondary w-full rounded-2xl border border-gray-500 bg-transparent py-4 pr-16 pl-26 text-[1.6rem] transition-all duration-200 outline-none focus:ring-2 focus:ring-blue-300 md:border-gray-400"
          />

          <button
            type="button"
            onClick={() => setPasswordVisible((prev) => !prev)}
            aria-label={passwordVisible ? "Hide password" : "Show password"}
            aria-pressed={passwordVisible}
            className="absolute top-1/2 right-10 -translate-y-1/2 cursor-pointer text-gray-400"
          >
            {passwordVisible ? (
              <Eye size={20} aria-hidden="true" />
            ) : (
              <EyeClosed size={20} aria-hidden="true" />
            )}
          </button>
        </div>

        <p
          id="login-error"
          role="alert"
          aria-live="polite"
          className={clsx(
            "overflow-hidden text-[1.4rem] font-medium text-red-500 transition-all duration-300 ease-in-out",
            error
              ? "mt-2 max-h-10 translate-y-0 opacity-100"
              : "mt-0 max-h-0 -translate-y-1 opacity-0",
          )}
        >
          {error}
        </p>

        <button
          type="submit"
          disabled={!formValid || isSubmitting}
          className="bg-secondary hover:bg-secondary/90 flex w-full cursor-pointer items-center justify-center rounded-xl py-3 text-[1.6rem] font-semibold text-white transition active:scale-95 disabled:cursor-not-allowed disabled:opacity-80"
        >
          {isSubmitting ? (
            <ClipLoader
              color="#ffffff"
              size={25}
              speedMultiplier={0.8}
              aria-label="Logging in"
              cssOverride={{ borderWidth: "3px" }}
            />
          ) : (
            "Login"
          )}
        </button>
      </form>

      <AuthSwitch switchTo="toRegister" className="mt-6 justify-center" />
    </section>
  );
};

export default LoginForm;
