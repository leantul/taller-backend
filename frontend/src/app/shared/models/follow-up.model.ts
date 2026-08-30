import { PageResponse } from './client.model';

export type FollowUpStatus = 'PENDING' | 'CONFIRMED' | 'NO_RESPONSE' | 'CANCELLED' | 'COMPLETED';
export type CommitmentOutcome = 'PENDING' | 'RESCHEDULED' | 'NOT_COMPLETED' | 'COMPLETED';

export interface FollowUpSave {
  id?: string;
  clientId?: string | null;
  contactName?: string;
  contactChannel?: string;
  contactValue?: string;
  deviceDescription: string;
  reportedProblem?: string;
  nextContactDate?: string | null;
  status?: FollowUpStatus;
  notes?: string;
}

export interface FollowUpListItem {
  id: string;
  clientId?: string;
  displayName: string;
  contactChannel?: string;
  contactValue?: string;
  deviceDescription: string;
  nextContactDate?: string;
  currentPromisedDate?: string;
  status: FollowUpStatus;
  commitmentCount: number;
  missedCommitmentCount: number;
}

export interface FollowUpCommitment {
  id?: string;
  promisedDate: string;
  outcome?: CommitmentOutcome;
  notes?: string;
  createdAt?: string;
}

export interface FollowUpDetail extends FollowUpSave {
  id: string;
  clientName?: string;
  status: FollowUpStatus;
  commitments: FollowUpCommitment[];
}

export type FollowUpPage = PageResponse<FollowUpListItem>;
