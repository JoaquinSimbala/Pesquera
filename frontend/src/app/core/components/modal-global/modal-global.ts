import { Component } from '@angular/core';
import { CommonModule } from '@angular/common';
import { DialogoService } from '../../services/dialogo';

@Component({
  selector: 'app-modal-global',
  standalone: true,
  imports: [CommonModule],
  templateUrl: './modal-global.html',
  styleUrl: './modal-global.scss'
})
export class ModalGlobal {
  constructor(protected dialogoService: DialogoService) {}

  aceptar(): void {
    const cfg = this.dialogoService.config();
    if (cfg && cfg.onAceptar) {
      cfg.onAceptar();
    }
    this.dialogoService.cerrar();
  }

  cancelar(): void {
    const cfg = this.dialogoService.config();
    if (cfg && cfg.onCancelar) {
      cfg.onCancelar();
    }
    this.dialogoService.cerrar();
  }
}
