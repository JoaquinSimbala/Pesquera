import { Injectable, signal } from '@angular/core';

export interface DialogoConfig {
  titulo: string;
  mensaje: string;
  tipo: 'exito' | 'error' | 'advertencia' | 'confirmacion';
  onAceptar?: () => void;
  onCancelar?: () => void;
}

@Injectable({ providedIn: 'root' })
export class DialogoService {
  config = signal<DialogoConfig | null>(null);

  mostrar(config: DialogoConfig): void {
    this.config.set(config);
  }

  exito(titulo: string, mensaje: string): void {
    this.mostrar({ titulo, mensaje, tipo: 'exito' });
  }

  error(titulo: string, mensaje: string): void {
    this.mostrar({ titulo, mensaje, tipo: 'error' });
  }

  advertencia(titulo: string, mensaje: string): void {
    this.mostrar({ titulo, mensaje, tipo: 'advertencia' });
  }

  confirmar(titulo: string, mensaje: string, onAceptar: () => void, onCancelar?: () => void): void {
    this.mostrar({ titulo, mensaje, tipo: 'confirmacion', onAceptar, onCancelar });
  }

  cerrar(): void {
    this.config.set(null);
  }
}
