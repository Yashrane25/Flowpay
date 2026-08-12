import { useEffect, useState } from "react";
import { useNavigate } from "react-router-dom";
import { apiFetch } from "../api/apiClient";
import { useAuth } from "../context/AuthContext";
import TransferForm from "../components/TransferForm";
import TransactionHistory from "../components/TransactionHistory";
import "./Dashboard.css";

function Dashboard() {
  const navigate = useNavigate();
  const { user, setUser } = useAuth();

  const [balance, setBalance] = useState(null);
  const [isLoadingBalance, setIsLoadingBalance] = useState(true);
  const [balanceError, setBalanceError] = useState("");
  const [historyRefreshCount, setHistoryRefreshCount] = useState(0);
  const [walletId, setWalletId] = useState(null);

  let nameDisplay = "Unknown user";
  if (user && user.fullName) {
    nameDisplay = user.fullName;
  }

  useEffect(() => {
    loadWallet();
  }, []);

  async function loadWallet() {
    setIsLoadingBalance(true);
    setBalanceError("");

    try {
      const wallet = await apiFetch("/api/wallet/me");
      setBalance(wallet.balance);
      setWalletId(wallet.walletId);
    } catch (error) {
      setBalanceError(error.message);
    } finally {
      setIsLoadingBalance(false);
    }
  }

  function handleTransferSuccess() {
    loadWallet();
    setHistoryRefreshCount((previous) => previous + 1);
  }

  async function handleLogout() {
    try {
      await apiFetch("/api/auth/logout", { method: "POST" });
    } catch (error) {
      //Still clear local state below even if this call fails.
    }
    setUser(null);
    navigate("/login");
  }

  return (
    <div className="dashboard-page">
      <header className="dashboard-header">
        <h1 className="dashboard-title">FlowPay Dashboard</h1>
        <button className="logout-button" onClick={handleLogout}>
          Log Out
        </button>
      </header>

      <p className="dashboard-welcome">Logged in as: {nameDisplay}</p>

      <div className="balance-card">
        <p className="balance-label">Wallet Balance</p>

        {isLoadingBalance && <p className="balance-loading">Loading...</p>}

        {!isLoadingBalance && balanceError && (
          <p className="balance-error">{balanceError}</p>
        )}

        {!isLoadingBalance && !balanceError && (
          <p className="balance-amount">₹{balance}</p>
        )}

        {!isLoadingBalance && !balanceError && (
          <p className="wallet-id-note">Your Wallet ID: {walletId}</p>
        )}
      </div>

      <TransferForm onTransferSuccess={handleTransferSuccess} />

      <TransactionHistory refreshTrigger={historyRefreshCount} />
    </div>
  );
}

export default Dashboard;
