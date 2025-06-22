import { AsyncPipe, CommonModule } from '@angular/common';
import { Component } from '@angular/core';
import { MatIconModule } from '@angular/material/icon';
import { Observable } from 'rxjs';
import { RepositoriesSearchOut } from '../../../../core/models/repositories-search-out.model';
import { SearchStateService } from '../../../../core/services/search-state.service';
import { SearchResultItemComponent } from '../search-result-item/search-result-item.component';

@Component({
  selector: 'app-search-results',
  standalone: true,
  imports: [CommonModule, AsyncPipe, MatIconModule, SearchResultItemComponent],
  templateUrl: './search-results.component.html',
  styleUrl: './search-results.component.css',
})
export class SearchResultsComponent {
  results$: Observable<RepositoriesSearchOut[]>;
  loading$: Observable<boolean>;
  error$: Observable<string | null>;
  hasSearched$: Observable<boolean>;

  constructor(private searchState: SearchStateService) {
    this.results$ = this.searchState.results$;
    this.loading$ = this.searchState.loading$;
    this.error$ = this.searchState.error$;
    this.hasSearched$ = this.searchState.hasSearched$;
  }

  trackByName(index: number, item: RepositoriesSearchOut): string {
    return item.name;
  }
}
