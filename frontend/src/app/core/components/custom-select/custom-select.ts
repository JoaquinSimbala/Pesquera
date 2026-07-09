import {
  Component, Input, Output, EventEmitter, forwardRef,
  HostListener, ElementRef, signal, computed, OnChanges, SimpleChanges
} from '@angular/core';
import { ControlValueAccessor, NG_VALUE_ACCESSOR } from '@angular/forms';
import { CommonModule } from '@angular/common';

export interface SelectOption {
  value: any;
  label: string;
}

@Component({
  selector: 'app-custom-select',
  standalone: true,
  imports: [CommonModule],
  templateUrl: './custom-select.html',
  styleUrl: './custom-select.scss',
  providers: [
    {
      provide: NG_VALUE_ACCESSOR,
      useExisting: forwardRef(() => CustomSelect),
      multi: true
    }
  ]
})
export class CustomSelect implements ControlValueAccessor, OnChanges {
  @Input() options: SelectOption[] = [];
  @Input() placeholder = 'Selecciona una opción';
  @Input() disabled = false;

  isOpen = signal(false);
  selectedValue = signal<any>(null);

  private onChange: (val: any) => void = () => {};
  private onTouched: () => void = () => {};

  constructor(private elRef: ElementRef) {}

  ngOnChanges(changes: SimpleChanges): void {
    
  }

  get selectedLabel(): string {
    if (this.selectedValue() === null || this.selectedValue() === undefined || this.selectedValue() === '') {
      return '';
    }
    const found = this.options.find(o => o.value == this.selectedValue());
    return found ? found.label : '';
  }

  toggle(): void {
    if (this.disabled) return;
    this.isOpen.update(v => !v);
    this.onTouched();
  }

  select(option: SelectOption): void {
    this.selectedValue.set(option.value);
    this.onChange(option.value);
    this.isOpen.set(false);
  }

  isSelected(option: SelectOption): boolean {
    return option.value == this.selectedValue();
  }

  
  @HostListener('document:click', ['$event'])
  onDocumentClick(event: MouseEvent): void {
    if (!this.elRef.nativeElement.contains(event.target)) {
      this.isOpen.set(false);
    }
  }

  
  writeValue(val: any): void {
    this.selectedValue.set(val);
  }

  registerOnChange(fn: any): void {
    this.onChange = fn;
  }

  registerOnTouched(fn: any): void {
    this.onTouched = fn;
  }

  setDisabledState(isDisabled: boolean): void {
    this.disabled = isDisabled;
  }
}
