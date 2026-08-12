import { useEffect, useState } from "react";
import { apiFetch } from "../api/apiClient";
import "./TransactionHistory.css";

function TransactionHistory({ refreshTrigger }) {
  const [entries, setEntries] = useState([]);
  const [isLoading, setIsLoading] = useState(true);
  const [errorMessage, setErrorMessage] = useState("");

  useEffect(() => {
    loadHistory();
  }, [refreshTrigger]);

  async function loadHistory() {
    setIsLoading(true);
    setErrorMessage("");

    try {
      const data = await apiFetch("/api/wallet/me/history");
      setEntries(data);
    } catch (error) {
      setErrorMessage(error.message);
    } finally {
      setIsLoading(false);
    }
  }

  function formatDate(isoString) {
    const date = new Date(isoString);
    return date.toLocaleString();
  }

  return (
    <div className="history-card">
      <h2 className="history-title">Transaction History</h2>

      {isLoading && <p className="history-loading">Loading...</p>}

      {!isLoading && errorMessage && (
        <p className="history-error">{errorMessage}</p>
      )}

      {!isLoading && !errorMessage && entries.length === 0 && (
        <p className="history-empty">No transactions yet.</p>
      )}

      {!isLoading && !errorMessage && entries.length > 0 && (
        <ul className="history-list">
          {entries.map((entry, index) => {
            const isCredit = entry.type === "CREDIT";
            const rowClass = isCredit
              ? "history-row credit"
              : "history-row debit";
            const sign = isCredit ? "+" : "-";
            const counterpartyLabel = isCredit
              ? `Received from ${entry.counterpartyName}`
              : `Sent to ${entry.counterpartyName}`;

            return (
              <li key={index} className={rowClass}>
                <div className="history-main">
                  <span className="history-type">{entry.type}</span>
                  <span className="history-counterparty">
                    {counterpartyLabel}
                  </span>
                </div>
                <div className="history-side">
                  <span className="history-amount">
                    {sign}₹{entry.amount}
                  </span>
                  <span className="history-date">
                    {formatDate(entry.createdAt)}
                  </span>
                </div>
              </li>
            );
          })}
        </ul>
      )}
    </div>
  );
}

export default TransactionHistory;
