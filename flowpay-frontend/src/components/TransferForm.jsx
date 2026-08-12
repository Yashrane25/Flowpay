import { useState } from "react";
import { apiFetch } from "../api/apiClient";
import "./TransferForm.css";

function TransferForm({ onTransferSuccess }) {
  const [toWalletId, setToWalletId] = useState("");
  const [amount, setAmount] = useState("");
  const [errorMessage, setErrorMessage] = useState("");
  const [successMessage, setSuccessMessage] = useState("");
  const [isSubmitting, setIsSubmitting] = useState(false);

  async function handleSubmit(event) {
    event.preventDefault();
    setErrorMessage("");
    setSuccessMessage("");
    setIsSubmitting(true);

    const idempotencyKey = crypto.randomUUID();

    try {
      const response = await apiFetch("/api/transfer", {
        method: "POST",
        headers: {
          "Idempotency-Key": idempotencyKey,
        },
        body: JSON.stringify({
          toWalletId: Number(toWalletId),
          amount: Number(amount),
        }),
      });

      setSuccessMessage(
        `Transfer successful. New balance: ₹${response.fromWalletNewBalance}`,
      );
      setToWalletId("");
      setAmount("");

      if (onTransferSuccess) {
        onTransferSuccess();
      }
    } catch (error) {
      setErrorMessage(error.message);
    } finally {
      setIsSubmitting(false);
    }
  }

  return (
    <div className="transfer-card">
      <h2 className="transfer-title">Send Money</h2>

      <form onSubmit={handleSubmit}>
        {errorMessage && <div className="transfer-error">{errorMessage}</div>}

        {successMessage && (
          <div className="transfer-success">{successMessage}</div>
        )}

        <label className="transfer-label" htmlFor="toWalletId">
          Recipient Wallet ID
        </label>
        <input
          id="toWalletId"
          type="number"
          className="transfer-input"
          value={toWalletId}
          onChange={(event) => setToWalletId(event.target.value)}
          required
        />

        <label className="transfer-label" htmlFor="amount">
          Amount (₹)
        </label>
        <input
          id="amount"
          type="number"
          step="0.01"
          min="0.01"
          className="transfer-input"
          value={amount}
          onChange={(event) => setAmount(event.target.value)}
          required
        />

        <button
          type="submit"
          className="transfer-button"
          disabled={isSubmitting}
        >
          {isSubmitting ? "Sending..." : "Send"}
        </button>
      </form>
    </div>
  );
}

export default TransferForm;
