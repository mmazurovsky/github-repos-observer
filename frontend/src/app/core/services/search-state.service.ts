import { Injectable } from '@angular/core';
import { BehaviorSubject } from 'rxjs';
import { RepositoriesSearchOut } from '../models/repositories-search-out.model';

@Injectable({
  providedIn: 'root',
})
export class SearchStateService {
  private readonly _loading = new BehaviorSubject<boolean>(false);
  private readonly _results = new BehaviorSubject<RepositoriesSearchOut[]>([]);
  private readonly _error = new BehaviorSubject<string | null>(null);
  private readonly _hasSearched = new BehaviorSubject<boolean>(false);

  readonly loading$ = this._loading.asObservable();
  readonly results$ = this._results.asObservable();
  readonly error$ = this._error.asObservable();
  readonly hasSearched$ = this._hasSearched.asObservable();

  setLoading(isLoading: boolean): void {
    this._loading.next(isLoading);
    if (isLoading) {
      this._hasSearched.next(true);
    }
  }

  setResults(results: RepositoriesSearchOut[]): void {
    this._results.next(results);
  }

  setError(error: string | null): void {
    this._error.next(error);
  }
}
