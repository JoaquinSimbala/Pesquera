import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { PaginatedResponse } from './auditoria';
import { environment } from '../../../environments/environment';

export interface ControlCalidad {
  id?: number;
  loteReferencia: string;
  temperatura: number;
  ph: number;
  higienePersonal: boolean;
  limpiezaEquipos: boolean;
  estadoHaccp: string;
  observaciones?: string;
  fechaRegistro?: string;
}

export interface CalidadMetricas {
  total: number;
  aprobados: number;
  rechazados: number;
  observados: number;
  alertasCriticas: number;
  aprobadosPct: number;
}

@Injectable({
  providedIn: 'root'
})
export class CalidadService {
  private readonly API_URL = `${environment.apiUrl}/calidad`;

  constructor(private http: HttpClient) {}

  getMetricas(): Observable<CalidadMetricas> {
    return this.http.get<CalidadMetricas>(`${this.API_URL}/metricas`, {
      withCredentials: true
    });
  }

  getHistorial(page = 0, size = 10): Observable<PaginatedResponse<ControlCalidad>> {
    return this.http.get<PaginatedResponse<ControlCalidad>>(`${this.API_URL}/historial?page=${page}&size=${size}`, {
      withCredentials: true
    });
  }

  registrarControl(form: ControlCalidad): Observable<any> {
    return this.http.post<any>(`${this.API_URL}/registrar`, form, {
      withCredentials: true
    });
  }
}
