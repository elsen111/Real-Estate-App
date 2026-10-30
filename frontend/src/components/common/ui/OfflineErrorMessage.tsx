const OfflineErrorMessage = ({ message }: { message: string }) => {
  return (
    <p className="mx-auto w-100 text-center text-[1.6rem] text-red-500 sm:w-auto">{message}</p>
  );
};

export default OfflineErrorMessage;
