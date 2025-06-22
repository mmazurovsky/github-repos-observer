import { CommonModule } from '@angular/common';
import { HttpErrorResponse } from '@angular/common/http';
import { Component } from '@angular/core';
import { finalize } from 'rxjs';
import { RepositoriesSearchIn } from '../../../../core/models/repositories-search-in.model';
import { SearchApiService } from '../../../../core/services/search-api.service';
import { SearchStateService } from '../../../../core/services/search-state.service';
import { SearchBarComponent } from '../../components/search-bar/search-bar.component';
import { SearchResultsComponent } from '../../components/search-results/search-results.component';

@Component({
  selector: 'app-search-page',
  standalone: true,
  imports: [CommonModule, SearchBarComponent, SearchResultsComponent],
  templateUrl: './search-page.component.html',
  styleUrl: './search-page.component.css'
})
export class SearchPageComponent {

  constructor(
    private searchApi: SearchApiService,
    public state: SearchStateService
  ) { }

  handleSearch(params: RepositoriesSearchIn): void {
    this.state.setLoading(true);
    this.state.setError(null);
    this.state.setResults([]);

    this.searchApi.searchRepositories(params).pipe(
      finalize(() => this.state.setLoading(false))
    ).subscribe({
      next: (results) => {
        this.state.setResults(results);
      },
      error: (err) => {
        console.error('Search error:', err);

        let errorMessage = 'An unexpected error occurred while fetching repositories.';

        if (err instanceof HttpErrorResponse) {
          // Try to extract error message from the response body
          if (err.error && err.error.error) {
            errorMessage = err.error.error;
          } else if (err.error && typeof err.error === 'string') {
            errorMessage = err.error;
          } else if (err.message) {
            errorMessage = err.message;
          }
        } else if (err && err.message) {
          errorMessage = err.message;
        }

        this.state.setError(errorMessage);
      }
    });
  }
}
