import React from 'react';
import './LoadingState.css';

function LoadingState({ status, pollCount = 0 }) {
  const statusMessages = {
    QUEUED: 'Queued for analysis...',
    INGESTING: 'Ingesting comments...',
    ANALYZING: 'Analyzing comments...',
  };

  return (
    <div className="loading-state">
      <div className="spinner"></div>
      <h2>{statusMessages[status] || 'Processing...'}</h2>
      <p>Status: <strong>{status}</strong></p>
      {pollCount > 0 && <p className="poll-count">Poll attempts: {pollCount}</p>}
      <p className="loading-note">This may take a few moments. Do not close this page.</p>
    </div>
  );
}

export default LoadingState;
