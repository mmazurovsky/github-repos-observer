import { Injectable } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable } from 'rxjs';
import { RepositoriesSearchOut } from '../models/repositories-search-out.model';
import { RepositoriesSearchIn } from '../models/repositories-search-in.model';

@Injectable({
  providedIn: 'root',
})
export class SearchApiService {
  private readonly API_URL = '/api/search';

  constructor(private http: HttpClient) {}

  searchRepositories(
    params: RepositoriesSearchIn
  ): Observable<RepositoriesSearchOut[]> {
    let httpParams = new HttpParams();
    Object.entries(params).forEach(([key, value]) => {
      if (value !== undefined && value !== null && value !== '') {
        httpParams = httpParams.set(key, value);
      }
    });

    return this.http.get<RepositoriesSearchOut[]>(this.API_URL, {
      params: httpParams,
    });
  }
}
