import React, { useState, useEffect } from 'react';
import { analyzeUrl, getAnalysisResult } from './api';
import AnalysisForm from './AnalysisForm';
import ResultsDisplay from './ResultsDisplay';
import LoadingState from './LoadingState';
import ErrorState from './ErrorState';
import './App.css';

function App() {
  const [jobId, setJobId] = useState(null);
  const [status, setStatus] = useState(null);
  const [results, setResults] = useState([]);
  const [error, setError] = useState(null);
  const [isLoading, setIsLoading] = useState(false);
  const [completedAt, setCompletedAt] = useState(null);
  const [pollCount, setPollCount] = useState(0);

  useEffect(() => {
    if (!jobId || status === 'COMPLETED' || status === 'FAILED') {
      return;
    }

    const pollInterval = setInterval(async () => {
      try {
        const result = await getAnalysisResult(jobId);
        console.log('Full API response:', result);
        setStatus(result.status);
        setPollCount((prev) => prev + 1);

        if (result.status === 'COMPLETED') {
          setResults(result.results || []);
          setCompletedAt(result.completedAt || new Date().toISOString());
          setIsLoading(false);
          clearInterval(pollInterval);
        } else if (result.status === 'FAILED') {
          setError(result.error || 'Analysis failed. Please try again.');
          setIsLoading(false);
          clearInterval(pollInterval);
        }
      } catch (err) {
        console.error('Polling error:', err);
        setError(`Polling error: ${err.message}`);
        setStatus('FAILED');
        setIsLoading(false);
        clearInterval(pollInterval);
      }
    }, 2000);

    return () => {
      clearInterval(pollInterval);
    };
  }, [jobId, status]);

  const handleSubmit = async (formData) => {
    setError(null);
    setIsLoading(true);
    setJobId(null);
    setStatus(null);
    setResults([]);
    setCompletedAt(null);
    setPollCount(0);

    try {
      const response = await analyzeUrl(formData.url, formData.maxComments);

      if (!response.jobId) {
        throw new Error('Invalid response: missing jobId');
      }

      setJobId(response.jobId);
      setStatus(response.status);

      if (response.status === 'COMPLETED') {
        setResults(response.results || []);
        setCompletedAt(response.completedAt || new Date().toISOString());
        setIsLoading(false);
        return;
      }

      if (response.status === 'FAILED') {
        setError(response.error || 'Analysis failed. Please try again.');
        setIsLoading(false);
        return;
      }

      setIsLoading(true);
    } catch (err) {
      console.error('Submit error:', err);
      setError(`Failed to submit analysis: ${err.message}`);
      setStatus('FAILED');
      setIsLoading(false);
    }
  };

  const handleReset = () => {
    setJobId(null);
    setStatus(null);
    setResults([]);
    setError(null);
    setIsLoading(false);
    setCompletedAt(null);
    setPollCount(0);
  };

  return (
    <div className="app">
      <header className="app-header">
        <h1>Comment Analysis Tool</h1>
        <p>Analyze YouTube video comments for spam, trolls, and suspicious activity</p>
      </header>

      <main className="app-container">
        {!jobId ? (
          <AnalysisForm onSubmit={handleSubmit} />
        ) : (
          <>
            <div className="job-info">
              <div className="job-id-display">
                <label>Job ID:</label>
                <code>{jobId}</code>
              </div>
              <div className="job-status">
                <label>Status:</label>
                <span className={`status-badge status-${status?.toLowerCase() || 'unknown'}`}>
                  {status || 'UNKNOWN'}
                </span>
              </div>
              <button onClick={handleReset} className="btn-reset">
                New Analysis
              </button>
            </div>

            {error && <ErrorState message={error} />}
            {isLoading && ['QUEUED', 'INGESTING', 'ANALYZING'].includes(status) && (
                <LoadingState status={status} pollCount={pollCount} />
            )}
            {status === 'COMPLETED' && results.length > 0 && (
              <ResultsDisplay results={results} completedAt={completedAt} />
            )}
            {status === 'COMPLETED' && results.length === 0 && (
              <div className="no-results-message">
                <p>Analysis completed but no results were found.</p>
                <p>This may indicate the video has no comments or they are disabled.</p>
              </div>
            )}
            {status === 'FAILED' && !error && (
                <ErrorState message="Analysis failed. Please try again with a different URL." />
            )}
          </>
        )}
      </main>
    </div>
  );
}

export default App;
