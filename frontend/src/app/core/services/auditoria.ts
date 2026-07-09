import { Injectable } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../../environments/environment';

export interface UsuarioDto {
  id: number;
  username: string;
  rol: string;
}

export interface Auditoria {
  id: number;
  accion: string;
  detalle: string;
  fecha: string;
  usuario: UsuarioDto;
}

export interface PaginatedResponse<T> {
  content: T[];
  totalPages: number;
  totalElements: number;
  currentPage: number;
  pageSize: number;
}

@Injectable({
  providedIn: 'root'
})
export class AuditoriaService {
  private readonly API_URL = `${environment.apiUrl}/supervisor/auditorias`;

  constructor(private http: HttpClient) {}

  obtenerAuditorias(usuarioId?: number, rango?: string, fechaEspecifica?: string, page = 0, size = 10): Observable<PaginatedResponse<Auditoria>> {
    let params = new HttpParams()
      .set('page', page.toString())
      .set('size', size.toString());
    
    if (usuarioId !== undefined && usuarioId !== null && usuarioId > 0) {
      params = params.set('usuarioId', usuarioId.toString());
    }
    if (rango) {
      params = params.set('rango', rango);
    }
    if (rango === 'especifica' && fechaEspecifica) {
      params = params.set('fechaEspecifica', fechaEspecifica);
    }
    return this.http.get<PaginatedResponse<Auditoria>>(`${this.API_URL}/lista`, {
      params,
      withCredentials: true
    });
  }

  obtenerUsuarios(): Observable<UsuarioDto[]> {
    return this.http.get<UsuarioDto[]>(`${this.API_URL}/usuarios`, {
      withCredentials: true
    });
  }
}
