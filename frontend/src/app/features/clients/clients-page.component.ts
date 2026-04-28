import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { ApiService } from '../../core/services/api.service';
import { Client } from '../../shared/models/client.model';

@Component({
  selector: 'app-clients-page',
  standalone: true,
  imports: [CommonModule, FormsModule],
  template: `
    <h2>Clientes</h2>
    <form (ngSubmit)="save()">
      <input [(ngModel)]="draft.name" name="name" placeholder="Nombre" required />
      <input [(ngModel)]="draft.lastName" name="lastName" placeholder="Apellido" required />
      <input [(ngModel)]="draft.dni" name="dni" placeholder="DNI" required />
      <input [(ngModel)]="draft.email" name="email" placeholder="Email" required />
      <input [(ngModel)]="draft.phone" name="phone" placeholder="Celular" required />
      <button type="submit">Agregar cliente</button>
    </form>
    <ul>
      @for (client of clients; track client.dni) {
        <li>{{ client.name }} {{ client.lastName }} · {{ client.dni }}</li>
      }
    </ul>
  `
})
export class ClientsPageComponent implements OnInit {
  clients: Client[] = [];
  draft: Client = { name: '', lastName: '', dni: '', email: '', phone: '' };

  constructor(private readonly api: ApiService) {}

  ngOnInit(): void {
    this.reload();
  }

  save(): void {
    this.api.createClient(this.draft).subscribe(() => {
      this.draft = { name: '', lastName: '', dni: '', email: '', phone: '' };
      this.reload();
    });
  }

  private reload(): void {
    this.api.getClients().subscribe((clients) => (this.clients = clients));
  }
}
