import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';

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
  private readonly API_URL = 'http://localhost:8080/api/calidad';

  constructor(private http: HttpClient) {}

  getMetricas(): Observable<CalidadMetricas> {
    return this.http.get<CalidadMetricas>(`${this.API_URL}/metricas`, {
      withCredentials: true
    });
  }

  getHistorial(): Observable<ControlCalidad[]> {
    return this.http.get<ControlCalidad[]>(`${this.API_URL}/historial`, {
      withCredentials: true
    });
  }

  registrarControl(form: ControlCalidad): Observable<any> {
    return this.http.post<any>(`${this.API_URL}/registrar`, form, {
      withCredentials: true
    });
  }
}
