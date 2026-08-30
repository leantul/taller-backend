import { HttpClient, HttpParams } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { Observable } from 'rxjs';
import { APP_CONFIG } from '../config/app-config';
import { CommitmentOutcome, FollowUpCommitment, FollowUpDetail, FollowUpPage, FollowUpSave } from '../../shared/models/follow-up.model';

@Injectable({ providedIn: 'root' })
export class FollowUpApiService {
  private readonly url = `${APP_CONFIG.apiUrl}/follow-up`;

  constructor(private readonly http: HttpClient) {}

  getPage(page: number, size: number, term = '', sortBy = 'createdAt', sortDir: 'asc' | 'desc' = 'desc'): Observable<FollowUpPage> {
    const params = new HttpParams()
      .set('page', page).set('size', size).set('term', term)
      .set('sortBy', sortBy).set('sortDir', sortDir);
    return this.http.get<FollowUpPage>(`${this.url}/page`, { params });
  }

  getById(id: string): Observable<FollowUpDetail> { return this.http.get<FollowUpDetail>(`${this.url}/${id}`); }
  save(payload: FollowUpSave): Observable<FollowUpDetail> { return this.http.post<FollowUpDetail>(this.url, payload); }
  delete(id: string): Observable<void> { return this.http.delete<void>(`${this.url}/${id}`); }
  addCommitment(id: string, payload: FollowUpCommitment): Observable<FollowUpCommitment> {
    return this.http.post<FollowUpCommitment>(`${this.url}/${id}/commitments`, payload);
  }
  updateCommitmentOutcome(id: string, commitmentId: string, outcome: CommitmentOutcome): Observable<FollowUpCommitment> {
    return this.http.patch<FollowUpCommitment>(`${this.url}/${id}/commitments/${commitmentId}/outcome`, { outcome });
  }
}
