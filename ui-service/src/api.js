import axios from 'axios';

const API_BASE_URL = import.meta.env.VITE_API_BASE_URL || 'http://localhost:8080';

const apiClient = axios.create({
  baseURL: API_BASE_URL,
  headers: {
    'Content-Type': 'application/json',
  },
});

const normalizeAnalysisResponse = (data, fallbackJobId = null) => {
  const results = Array.isArray(data?.results) ? data.results : [];

  let status = data?.status;

  if (!status && (results.length > 0 || data?.completedAt)) {
    status = 'COMPLETED';
  }

  if (!status) {
    status = 'QUEUED';
  }

  return {
    ...data,
    jobId: data?.jobId || data?.id || fallbackJobId,
    status,
    completedAt: data?.completedAt || null,
    results,
    error: data?.error || data?.message || null,
  };
};

export const analyzeUrl = async (url, maxComments = null) => {
  const payload = {
    url,
  };

  if (maxComments !== null && maxComments !== undefined) {
    payload.maxComments = maxComments;
  }

  try {
    const response = await apiClient.post('/api/analyze', payload);
    return normalizeAnalysisResponse(response.data);
  } catch (error) {
    const errorMessage =
        error.response?.data?.message ||
        error.response?.data?.error ||
        error.message ||
        'Failed to submit analysis';

    throw new Error(errorMessage);
  }
};

export const getAnalysisResult = async (jobId) => {
  try {
    const response = await apiClient.get(`/api/analyze/${encodeURIComponent(jobId)}`);
    return normalizeAnalysisResponse(response.data, jobId);
  } catch (error) {
    const errorMessage =
        error.response?.data?.message ||
        error.response?.data?.error ||
        error.message ||
        'Failed to fetch results';

    throw new Error(errorMessage);
  }
};

export default apiClient;