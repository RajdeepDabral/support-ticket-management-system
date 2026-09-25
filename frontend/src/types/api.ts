export interface ApiErrorBody {
  timestamp?: string;
  status?: number;
  code?: string;
  message?: string;
  path?: string;
  fieldErrors?: Record<string, string>;
}

export class ApiError extends Error {
  readonly status: number;
  readonly code?: string;
  readonly path?: string;
  readonly fieldErrors?: Record<string, string>;
  readonly body?: ApiErrorBody;

  constructor(status: number, body?: ApiErrorBody) {
    super(body?.message ?? `Request failed with status ${status}`);
    this.name = 'ApiError';
    this.status = status;
    this.code = body?.code;
    this.path = body?.path;
    this.fieldErrors = body?.fieldErrors;
    this.body = body;
  }
}
