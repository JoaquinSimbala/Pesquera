import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../../environments/environment';

@Injectable({
  providedIn: 'root'
})
export class GestionService {
  private readonly API_URL = `${environment.apiUrl}/supervisor/gestion`;

  constructor(private http: HttpClient) {}

  obtenerDashboard(tipo?: string, anio?: number, mes?: number, trimestre?: number): Observable<any> {
    let params: any = {};
    if (tipo) params.tipo = tipo;
    if (anio) params.anio = anio.toString();
    if (mes) params.mes = mes.toString();
    if (trimestre) params.trimestre = trimestre.toString();

    return this.http.get<any>(`${this.API_URL}/dashboard`, {
      params,
      withCredentials: true
    });
  }

  obtenerReporte(tipo?: string, anio?: number, mes?: number, trimestre?: number): Observable<any> {
    let params: any = {};
    if (tipo) params.tipo = tipo;
    if (anio) params.anio = anio.toString();
    if (mes) params.mes = mes.toString();
    if (trimestre) params.trimestre = trimestre.toString();

    return this.http.get<any>(`${this.API_URL}/reporte`, {
      params,
      withCredentials: true
    });
  }
}
