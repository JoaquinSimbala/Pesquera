import { Component, OnInit, ChangeDetectorRef } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { AuditoriaService, Auditoria, UsuarioDto } from '../../../core/services/auditoria';
import { CustomSelect, SelectOption } from '../../../core/components/custom-select/custom-select';
import { DialogoService } from '../../../core/services/dialogo';

@Component({
  selector: 'app-auditorias',
  standalone: true,
  imports: [CommonModule, FormsModule, CustomSelect],
  templateUrl: './auditorias.html',
  styleUrl: './auditorias.scss'
})
export class AuditoriasComponent implements OnInit {

  auditorias: Auditoria[] = [];
  usuarios: UsuarioDto[] = [];
  usuarioSeleccionadoId: number | null = 0; 
  
  
  rangoFecha = 'total';
  fechaEspecifica = '';
  
  rangoOptions: SelectOption[] = [
    { value: 'total', label: 'Total' },
    { value: 'ultimo-mes', label: 'Último Mes' },
    { value: 'especifica', label: 'Fecha Específica' }
  ];

  
  paginaActual = 0;
  elementosPorPagina = 10;
  totalElementos = 0;
  totalPaginas = 0;

  cargando = true;

  constructor(
    private auditoriaService: AuditoriaService,
    private dialogoService: DialogoService,
    private cdr: ChangeDetectorRef
  ) {}

  ngOnInit(): void {
    this.cargarUsuarios();
    this.cargarAuditorias();
  }

  cargarUsuarios(): void {
    this.auditoriaService.obtenerUsuarios().subscribe({
      next: (data) => {
        this.usuarios = data || [];
        this.cdr.detectChanges();
      },
      error: (err) => {
        console.error('Error cargando usuarios para auditoría:', err);
      }
    });
  }

  cargarAuditorias(): void {
    this.cargando = true;
    const filterId = this.usuarioSeleccionadoId || undefined;
    this.auditoriaService.obtenerAuditorias(
      filterId, 
      this.rangoFecha, 
      this.fechaEspecifica, 
      this.paginaActual, 
      this.elementosPorPagina
    ).subscribe({
      next: (data) => {
        this.auditorias = data.content || [];
        this.totalPaginas = data.totalPages || 0;
        this.totalElementos = data.totalElements || 0;
        this.cargando = false;
        this.cdr.detectChanges();
      },
      error: (err) => {
        console.error('Error cargando auditorías:', err);
        this.dialogoService.error('Error de Carga', 'No se pudo obtener el historial de actividades.');
        this.cargando = false;
        this.cdr.detectChanges();
      }
    });
  }

  get usuariosOptions(): SelectOption[] {
    const options: SelectOption[] = [
      { value: 0, label: '— Todos los usuarios —' }
    ];
    this.usuarios.forEach(u => {
      options.push({
        value: u.id,
        label: `${u.username} (${u.rol})`
      });
    });
    return options;
  }

  onUsuarioChange(val: any): void {
    this.usuarioSeleccionadoId = val;
    this.paginaActual = 0;
    this.cargarAuditorias();
  }

  onRangoChange(val: any): void {
    this.rangoFecha = val;
    this.paginaActual = 0;
    this.cargarAuditorias();
  }

  onFechaEspecificaChange(val: any): void {
    this.fechaEspecifica = val;
    this.paginaActual = 0;
    this.cargarAuditorias();
  }

  get auditoriasFiltradas(): Auditoria[] {
    return this.auditorias;
  }

  siguientePagina(): void {
    if (this.paginaActual < this.totalPaginas - 1) {
      this.paginaActual++;
      this.cargarAuditorias();
    }
  }

  anteriorPagina(): void {
    if (this.paginaActual > 0) {
      this.paginaActual--;
      this.cargarAuditorias();
    }
  }
}
