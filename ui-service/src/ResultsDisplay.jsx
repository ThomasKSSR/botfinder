import React, { useState, useMemo } from 'react';
import './ResultsDisplay.css';

function ResultsDisplay({ results, completedAt }) {
  const [filterLabel, setFilterLabel] = useState('all');
  const [sortBy, setSortBy] = useState('score');
  const scoreValue = (value) => {
    const n = Number(value ?? 0);
    return Number.isFinite(n) ? n : 0;
  };

  const filteredResults = useMemo(() => {
    let filtered = [...results];
    if (filterLabel !== 'all') {
      filtered = results.filter((r) => r.label === filterLabel);
    }

    return filtered.sort((a, b) => {
      if (sortBy === 'score') {
        return b.score - a.score;
      } else if (sortBy === 'rule') {
        return b.ruleScore - a.ruleScore;
      } else if (sortBy === 'spam') {
        return b.spamMlScore - a.spamMlScore;
      } else if (sortBy === 'troll') {
        return b.trollMlScore - a.trollMlScore;
      }  else if (sortBy === 'account') {
      return scoreValue(b.accountScore ?? b.accountHeuristicScore) -
          scoreValue(a.accountScore ?? a.accountHeuristicScore);
    }
      return 0;
    });
  }, [results, filterLabel, sortBy]);

  const uniqueLabels = [...new Set(results.map((r) => r.label))];
  const labelCounts = uniqueLabels.reduce((acc, label) => {
    acc[label] = results.filter((r) => r.label === label).length;
    return acc;
  }, {});

  const getLabelBadgeClass = (label) => {
    const labelLower = label?.toLowerCase() || '';
    if (labelLower.includes('spam')) return 'badge-spam';
    if (labelLower.includes('bot')) return 'badge-bot';
    if (labelLower.includes('troll')) return 'badge-troll';
    if (labelLower.includes('suspicious')) return 'badge-suspicious';
    return 'badge-normal';
  };

  return (
    <div className="results-display">
      <div className="results-header">
        <h2>Analysis Results</h2>
        {completedAt && (
          <p className="completed-at">Completed: {new Date(completedAt).toLocaleString()}</p>
        )}
      </div>

      <div className="results-summary">
        <div className="summary-stat">
          <span className="stat-label">Total Comments:</span>
          <span className="stat-value">{results.length}</span>
        </div>
        <div className="summary-stat">
          <span className="stat-label">Suspicious:</span>
          <span className="stat-value">{labelCounts.suspicious || 0}</span>
        </div>
        <div className="summary-stat">
          <span className="stat-label">Spam:</span>
          <span className="stat-value">{labelCounts.spam || 0}</span>
        </div>
        <div className="summary-stat">
          <span className="stat-label">Troll:</span>
          <span className="stat-value">{labelCounts.troll || 0}</span>
        </div>
      </div>

      <div className="results-controls">
        <div className="control-group">
          <label htmlFor="filter-label">Filter by Label:</label>
          <select
            id="filter-label"
            value={filterLabel}
            onChange={(e) => setFilterLabel(e.target.value)}
            className="control-select"
          >
            <option value="all">All Labels</option>
            {uniqueLabels.map((label) => (
              <option key={label} value={label}>
                {label} ({labelCounts[label]})
              </option>
            ))}
          </select>
        </div>

        <div className="control-group">
          <label htmlFor="sort-by">Sort by:</label>
          <select
            id="sort-by"
            value={sortBy}
            onChange={(e) => setSortBy(e.target.value)}
            className="control-select"
          >
            <option value="score">Final Score (Highest)</option>
            <option value="rule">Rule Score (Highest)</option>
            <option value="spam">Spam ML Score (Highest)</option>
            <option value="troll">Troll ML Score (Highest)</option>
            <option value="account">Account Score (Highest)</option>
          </select>
        </div>
      </div>

      <div className="results-table-wrapper">
        <table className="results-table">
          <thead>
            <tr>
              <th>Author</th>
              <th>Label</th>
              <th>Final Score</th>
              <th>Rule Score</th>
              <th>Spam ML Score</th>
              <th>Troll ML Score</th>
              <th>Account Score</th>
              <th>Reason</th>
              <th>Comment Preview</th>
            </tr>
          </thead>
          <tbody>
            {filteredResults.map((result) => (
              <tr key={result.commentId}>
                <td className="cell-author">{result.authorName || 'Anonymous'}</td>
                <td className="cell-label">
                  <span className={`badge ${getLabelBadgeClass(result.label)}`}>
                    {result.label}
                  </span>
                </td>
                <td className="cell-score">
                  <span className={`score ${result.score > 0.5 ? 'score-high' : 'score-low'}`}>
                    {scoreValue(result.score).toFixed(2)}
                  </span>
                </td>
                <td className="cell-score">
                  <span>{result.ruleScore.toFixed(2)}</span>
                </td>
                <td className="cell-score">
                  <span className={result.spamMlScore > 0.5 ? 'score-high' : ''}>
                    {result.spamMlScore.toFixed(2)}
                  </span>
                </td>
                <td className="cell-score">
                  <span className={result.trollMlScore > 0.5 ? 'score-high' : ''}>
                    {result.trollMlScore.toFixed(2)}
                  </span>
                </td>
                <td className="cell-score">
                  <span className={result.accountHeuristicScore > 0.5 ? 'score-high' : ''}>
                    {result.accountHeuristicScore.toFixed(2)}
                  </span>
                </td>
                <td className="cell-reason" title={result.reason}>
                  {result.reason}
                </td>
                <td className="cell-preview" title={result.commentPreview}>
                  {result.commentPreview}
                </td>
              </tr>
            ))}
          </tbody>
        </table>
      </div>

      {filteredResults.length === 0 && (
        <div className="no-results">
          <p>No comments found matching the selected filter.</p>
        </div>
      )}
    </div>
  );
}

export default ResultsDisplay;
