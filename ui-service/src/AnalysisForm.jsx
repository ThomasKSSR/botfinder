import React, { useState } from 'react';
import './AnalysisForm.css';

function AnalysisForm({ onSubmit }) {
  const [url, setUrl] = useState('');
  const [maxComments, setMaxComments] = useState('');
  const [error, setError] = useState('');

  const handleSubmit = (e) => {
    e.preventDefault();
    setError('');

    if (!url.trim()) {
      setError('YouTube URL is required');
      return;
    }

    // Basic YouTube URL validation
    if (
      !url.includes('youtube.com') &&
      !url.includes('youtu.be') &&
      !url.includes('youtube-nocookie.com')
    ) {
      setError('Please enter a valid YouTube URL');
      return;
    }

    const formData = {
      url: url.trim(),
      maxComments: maxComments ? parseInt(maxComments) : null,
    };

    onSubmit(formData);
  };

  return (
    <div className="analysis-form">
      <h2>Analyze Video Comments</h2>
      <form onSubmit={handleSubmit}>
        <div className="form-group">
          <label htmlFor="url">YouTube Video URL *</label>
          <input
            id="url"
            type="text"
            value={url}
            onChange={(e) => setUrl(e.target.value)}
            placeholder="https://www.youtube.com/watch?v=..."
            className="form-input"
          />
          <small>Enter the full URL of a YouTube video</small>
        </div>

        <div className="form-group">
          <label htmlFor="maxComments">Max Comments to Analyze (Optional)</label>
          <input
            id="maxComments"
            type="number"
            value={maxComments}
            onChange={(e) => setMaxComments(e.target.value)}
            placeholder="Leave empty for all comments"
            min="1"
            className="form-input"
          />
          <small>Leave empty to analyze all available comments</small>
        </div>

        {error && <div className="form-error">{error}</div>}

        <button type="submit" className="btn-submit">
          Analyze Comments
        </button>
      </form>
    </div>
  );
}

export default AnalysisForm;
