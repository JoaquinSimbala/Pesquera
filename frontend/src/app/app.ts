import { Component, signal } from '@angular/core';
import { RouterOutlet } from '@angular/router';
import { ModalGlobal } from './core/components/modal-global/modal-global';

@Component({
  selector: 'app-root',
  imports: [RouterOutlet, ModalGlobal],
  templateUrl: './app.html',
  styleUrl: './app.scss'
})
export class App {
  protected readonly title = signal('frontend');
}
