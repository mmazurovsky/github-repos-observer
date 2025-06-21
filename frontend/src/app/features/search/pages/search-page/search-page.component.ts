import { Component } from '@angular/core';
import { finalize } from 'rxjs';
import { RepositoriesSearchIn } from '../../../../core/models/repositories-search-in.model';
import { SearchApiService } from '../../../../core/services/search-api.service';
import { SearchStateService } from '../../../../core/services/search-state.service';
import { SearchBarComponent } from '../../components/search-bar/search-bar.component';
import { SearchResultsComponent } from '../../components/search-results/search-results.component';
import { CommonModule } from '@angular/common';

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
  ) {}

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
        console.error(err);
        this.state.setError('An error occurred while fetching repositories. Please ensure your API token is valid and try again.');
      }
    });
  }
}
