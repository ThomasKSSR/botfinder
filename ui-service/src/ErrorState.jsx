import React from 'react';
import './ErrorState.css';

function ErrorState({ message }) {
  return (
    <div className="error-state">
      <div className="error-icon">⚠️</div>
      <h2>Error</h2>
      <p>{message}</p>
    </div>
  );
}

export default ErrorState;
