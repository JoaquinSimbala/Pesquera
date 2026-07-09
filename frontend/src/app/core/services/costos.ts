import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { PaginatedResponse } from './auditoria';
import { environment } from '../../../environments/environment';

export interface Costo {
  id: number;
  categoria: string;
  concepto: string;
  monto: number;
  fechaCosto: string;
  descripcion: string;
  fechaRegistro: string;
}

export interface ResumenCostos {
  totalMes: number;
  totalGeneral: number;
  porCategoria: { [categoria: string]: number };
}

export interface DatosCostos {
  costos: Costo[];
  resumen: ResumenCostos;
  categorias: string[];
  totalPages?: number;
  totalElements?: number;
  currentPage?: number;
}

export interface NuevoCosto {
  categoria: string;
  concepto: string;
  monto: number;
  fechaCosto: string;
  descripcion: string;
}

@Injectable({ providedIn: 'root' })
export class CostosService {

  private readonly URL_BASE = `${environment.apiUrl}/costos`;

  constructor(private http: HttpClient) {}

  obtenerDatos(page = 0, size = 10): Observable<DatosCostos> {
    return this.http.get<DatosCostos>(`${this.URL_BASE}?page=${page}&size=${size}`, { withCredentials: true });
  }

  registrar(costo: NuevoCosto): Observable<any> {
    return this.http.post(`${this.URL_BASE}/registrar`, costo, { withCredentials: true });
  }
}
