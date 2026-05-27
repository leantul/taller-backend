import { CommonModule } from '@angular/common';
import { Component, OnInit } from '@angular/core';
import { ButtonModule } from 'primeng/button';
import { CardModule } from 'primeng/card';
import { DialogModule } from 'primeng/dialog';
import { MessageService } from 'primeng/api';
import { ApiService } from '../../core/services/api.service';
import { NotificationStateService } from '../../core/services/notification-state.service';
import { NotificationItem } from '../../shared/models/notification.model';

@Component({
  selector: 'app-notifications-page',
  standalone: true,
  imports: [CommonModule, CardModule, DialogModule, ButtonModule],
  template: `
    <section class="page-heading">
      <div>
        <span class="eyebrow">Seguimiento</span>
        <h1>Notificaciones</h1>
      </div>
      <p>Seguimiento de garantías y oportunidades para volver a contactar al cliente.</p>
    </section>

    <section class="notifications-page">
      @if (notifications.length) {
        <div class="notifications-list">
          @for (item of notifications; track item.id) {
            <article class="notification-card" (click)="openDetail(item)">
              <button class="notification-dismiss" type="button" aria-label="Marcar como leída" (click)="markAsRead($event, item)">
                <i class="pi pi-times"></i>
              </button>
              <div class="notification-card-head">
                <div>
                  <span class="notification-tag">{{ typeLabel(item.type) }}</span>
                  <h2>{{ item.title }}</h2>
                </div>
                <time>{{ formatDate(item.eventDate) }}</time>
              </div>
              <p>{{ item.message }}</p>
              <div class="notification-meta">
                <span><i class="pi pi-hashtag"></i> Orden #{{ item.orderNumber || '-' }}</span>
                <span><i class="pi pi-user"></i> {{ clientLabel(item) }}</span>
                <span><i class="pi pi-desktop"></i> {{ deviceLabel(item) }}</span>
              </div>
            </article>
          }
        </div>
      } @else {
        <section class="placeholder-panel">
          <div class="placeholder-icon">
            <i class="pi pi-bell-slash"></i>
          </div>
          <div class="placeholder-copy">
            <strong>Sin avisos pendientes</strong>
            <p>No hay notificaciones sin leer por garantía en este momento.</p>
          </div>
        </section>
      }
    </section>

    <p-dialog
      header="Detalle del aviso"
      [(visible)]="showDetail"
      [modal]="true"
      [style]="{width:'72rem', maxWidth:'95vw'}"
      [contentStyle]="{overflow:'hidden', padding:'0.35rem'}">
      @if (selectedNotification) {
        <div class="detail-dialog-body">
          <div class="detail-grid repair-detail-grid">
            <div class="detail-item"><label>Aviso</label><strong>{{ typeLabel(selectedNotification.type) }}</strong></div>
            <div class="detail-item"><label>Fecha del hito</label><strong>{{ formatDate(selectedNotification.eventDate) }}</strong></div>
            <div class="detail-item"><label>Cliente</label><strong>{{ clientLabel(selectedNotification) }}</strong></div>
            <div class="detail-item"><label>Teléfono</label><strong>{{ selectedNotification.clientPhone || '-' }}</strong></div>
            <div class="detail-item"><label>Email</label><strong>{{ selectedNotification.clientEmail || '-' }}</strong></div>
            <div class="detail-item"><label>Equipo</label><strong>{{ deviceLabel(selectedNotification) }}</strong></div>
            <div class="detail-item"><label>Serie</label><strong>{{ selectedNotification.deviceSerialNumber || '-' }}</strong></div>
            <div class="detail-item"><label>Orden</label><strong>#{{ selectedNotification.orderNumber || '-' }}</strong></div>
            <div class="detail-item"><label>Estado</label><strong>{{ statusLabel(selectedNotification.status) }}</strong></div>
            <div class="detail-item"><label>Ingreso</label><strong>{{ formatDateTime(selectedNotification.receiveDateTime) }}</strong></div>
            <div class="detail-item"><label>Retiro</label><strong>{{ formatDateTime(selectedNotification.returnDateTime) }}</strong></div>
            <div class="detail-item"><label>Presupuesto</label><strong>{{ selectedNotification.quotedAmount || 0 | currency:'ARS':'symbol':'1.2-2':'es-AR' }}</strong></div>
            <div class="detail-item"><label>Monto final</label><strong>{{ selectedNotification.price || 0 | currency:'ARS':'symbol':'1.2-2':'es-AR' }}</strong></div>
            <div class="detail-item detail-wide detail-text-block">
              <label>Trabajo realizado</label>
              <div class="detail-scrollable">{{ selectedNotification.quoteNotes || selectedNotification.repairDescription || 'Sin detalle cargado' }}</div>
            </div>
            <div class="detail-item detail-wide detail-text-block">
              <label>Falla reportada</label>
              <div class="detail-scrollable">{{ selectedNotification.repairDescription || 'Sin descripción cargada' }}</div>
            </div>
            <div class="detail-item detail-wide">
              <label>Repuestos</label>
              @if (selectedNotification.parts?.length) {
                <div class="native-table-wrap compact-table-wrap">
                  <table class="native-table compact-native-table">
                    <thead><tr><th>Repuesto</th><th>Cant.</th><th>Proveedor</th><th>Costo</th><th>Venta</th></tr></thead>
                    <tbody>
                      @for (part of selectedNotification.parts || []; track part.id || $index) {
                        <tr>
                          <td>{{ part.name }}</td>
                          <td>{{ part.quantity }}</td>
                          <td>{{ part.provider || '-' }}</td>
                          <td>{{ part.cost | currency:'ARS':'symbol':'1.2-2':'es-AR' }}</td>
                          <td>{{ part.salePrice | currency:'ARS':'symbol':'1.2-2':'es-AR' }}</td>
                        </tr>
                      }
                    </tbody>
                  </table>
                </div>
              } @else {
                <strong>Sin repuestos cargados</strong>
              }
            </div>
          </div>

          <div class="notification-detail-actions">
            <button
              pButton
              type="button"
              icon="pi pi-whatsapp"
              label="Abrir WhatsApp"
              [disabled]="!whatsAppLink(selectedNotification.clientPhone)"
              (click)="openWhatsApp(selectedNotification.clientPhone)">
            </button>
          </div>
        </div>
      }
    </p-dialog>
  `
})
export class NotificationsPageComponent implements OnInit {
  notifications: NotificationItem[] = [];
  selectedNotification: NotificationItem | null = null;
  showDetail = false;

  constructor(
    private readonly api: ApiService,
    private readonly messageService: MessageService,
    private readonly notificationState: NotificationStateService
  ) {}

  ngOnInit(): void {
    this.loadNotifications();
  }

  openDetail(item: NotificationItem): void {
    this.selectedNotification = item;
    this.showDetail = true;
  }

  markAsRead(event: Event, item: NotificationItem): void {
    event.stopPropagation();
    this.api.markNotificationAsRead(item.id).subscribe({
      next: () => {
        this.notifications = this.notifications.filter((notification) => notification.id !== item.id);
        if (this.selectedNotification?.id === item.id) {
          this.selectedNotification = null;
          this.showDetail = false;
        }
        this.notificationState.decrementUnreadCount();
      },
      error: () => {
        this.messageService.add({ severity: 'error', summary: 'Error', detail: 'No se pudo marcar el aviso como leído.' });
      }
    });
  }

  clientLabel(item: NotificationItem): string {
    return `${item.clientName || ''} ${item.clientLastName || ''}`.trim() || '-';
  }

  deviceLabel(item: NotificationItem): string {
    return [item.deviceType, item.deviceBrand, item.deviceModel].filter(Boolean).join(' ') || '-';
  }

  formatDate(value?: string): string {
    if (!value) return '-';
    return new Date(value).toLocaleDateString('es-AR');
  }

  formatDateTime(value?: string): string {
    if (!value) return '-';
    return new Date(value).toLocaleString('es-AR');
  }

  typeLabel(type: string): string {
    switch (type) {
      case 'WARRANTY_6_MONTHS': return 'Garantía 6 meses';
      case 'WARRANTY_1_YEAR': return 'Seguimiento 1 año';
      default: return 'Aviso';
    }
  }

  statusLabel(status?: string): string {
    switch (status) {
      case 'POR_RECIBIR': return 'Por recibir';
      case 'RECIBIDA': return 'Recibida';
      case 'PRESUPUESTADA_ESPERANDO_RESPUESTA': return 'Presupuestada';
      case 'HACIENDO': return 'Haciendo';
      case 'ESPERANDO_RETIRO': return 'Esperando retiro';
      case 'RETIRADA': return 'Retirada';
      default: return status || '-';
    }
  }

  whatsAppLink(phone?: string): string {
    const digits = (phone || '').replace(/\D/g, '');
    return digits ? `https://wa.me/${digits}` : '';
  }

  openWhatsApp(phone?: string): void {
    const url = this.whatsAppLink(phone);
    if (!url) return;
    window.open(url, '_blank', 'noopener');
  }

  private loadNotifications(): void {
    this.api.getNotifications().subscribe({
      next: (notifications) => {
        this.notifications = notifications;
        this.notificationState.refreshUnreadCount();
      },
      error: () => {
        this.messageService.add({ severity: 'error', summary: 'Error', detail: 'No se pudieron cargar los avisos.' });
      }
    });
  }
}
