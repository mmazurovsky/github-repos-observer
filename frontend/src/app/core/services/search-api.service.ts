import { HttpClient, HttpParams } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { Observable, timeout } from 'rxjs';
import { RepositoriesSearchIn } from '../models/repositories-search-in.model';
import { RepositoriesSearchOut } from '../models/repositories-search-out.model';

@Injectable({
  providedIn: 'root',
})
export class SearchApiService {
  private readonly API_URL = '/api/search';
  private readonly REQUEST_TIMEOUT = 120000; // 2 minutes timeout

  constructor(private http: HttpClient) { }

  searchRepositories(
    params: RepositoriesSearchIn
  ): Observable<RepositoriesSearchOut[]> {
    let httpParams = new HttpParams();
    Object.entries(params).forEach(([key, value]) => {
      if (value !== undefined && value !== null && value !== '') {
        httpParams = httpParams.set(key, value);
      }
    });

    return this.http
      .get<RepositoriesSearchOut[]>(this.API_URL, {
        params: httpParams,
      })
      .pipe(timeout(this.REQUEST_TIMEOUT));
  }
}
