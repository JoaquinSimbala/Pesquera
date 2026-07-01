import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';

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

  private readonly URL_BASE = 'http://localhost:8080/api/costos';

  constructor(private http: HttpClient) {}

  obtenerDatos(): Observable<DatosCostos> {
    return this.http.get<DatosCostos>(this.URL_BASE, { withCredentials: true });
  }

  registrar(costo: NuevoCosto): Observable<any> {
    return this.http.post(`${this.URL_BASE}/registrar`, costo, { withCredentials: true });
  }
}
