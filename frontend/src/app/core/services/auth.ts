import { Injectable, signal, computed } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable, tap, catchError, of, throwError } from 'rxjs';
import { environment } from '../../../environments/environment';

export interface AuthResponse {
  type: string;
  username: string;
  rol: string;
  expiration: Date;
}

export interface UserState {
  username: string;
  role: string;
  isAuthenticated: boolean;
}

@Injectable({
  providedIn: 'root'
})
export class AuthService {
  private readonly API_URL = `${environment.apiUrl}/auth`;
  
  private userState = signal<UserState>({
    username: '',
    role: '',
    isAuthenticated: false
  });

  public currentUser = computed(() => this.userState());

  constructor(private http: HttpClient) {
    const storedState = localStorage.getItem('userState');
    if (storedState) {
      this.userState.set(JSON.parse(storedState));
    }
  }

  login(credentials: any): Observable<AuthResponse> {
    return this.http.post<AuthResponse>(`${this.API_URL}/login`, credentials, {
      withCredentials: true 
    }).pipe(
      tap((response) => {
        const newState = {
          username: response.username,
          role: response.rol,
          isAuthenticated: true
        };
        this.userState.set(newState);
        localStorage.setItem('userState', JSON.stringify(newState));
      })
    );
  }

  logout(): Observable<any> {
    return this.http.post(`${this.API_URL}/logout`, {}, {
      withCredentials: true
    }).pipe(
      tap(() => {
        this.userState.set({ username: '', role: '', isAuthenticated: false });
        localStorage.removeItem('userState');
      }),
      catchError(() => {
        this.userState.set({ username: '', role: '', isAuthenticated: false });
        localStorage.removeItem('userState');
        return of(null);
      })
    );
  }

  getRole(): string {
    return this.userState().role;
  }
}
