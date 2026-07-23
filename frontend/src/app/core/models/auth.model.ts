export type Role = 'CLIENT' | 'ANALYSTE' | 'DIRECTEUR';

export interface LoginRequest {
  username: string;
  password: string;
}

export interface LoginResponse {
  token: string;
}

export interface CurrentUser {
  username: string;
  role: Role;
  exp: number;
}
