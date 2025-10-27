/**
 * API: AI Chat
 */

import axios from 'axios';

const API_BASE_URL = import.meta.env.VITE_API_BASE_URL || 'http://localhost:28080/api';

const http = axios.create({
  baseURL: `${API_BASE_URL}/v1`,
  headers: { 'Content-Type': 'application/json' },
  withCredentials: true,
});

export interface ChatRequest {
  message: string;
  conversationId?: string;
}

export interface ChatResponse {
  response: string;
  conversationId: string;
  timestamp: number;
}

export const chatApi = {
  sendMessage: async (request: ChatRequest): Promise<ChatResponse> => {
    const response = await http.post<ChatResponse>('/ai/chat', request);
    return response.data;
  },
};
