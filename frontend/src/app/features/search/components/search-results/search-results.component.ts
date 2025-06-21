import { Component } from '@angular/core';
import { SearchStateService } from '../../../../core/services/search-state.service';
import { AsyncPipe } from '@angular/common';
import { SearchResultItemComponent } from '../search-result-item/search-result-item.component';
import { Observable } from 'rxjs';
import { RepositoriesSearchOut } from '../../../../core/models/repositories-search-out.model';
import { CommonModule } from '@angular/common';

@Component({
  selector: 'app-search-results',
  standalone: true,
  imports: [CommonModule, AsyncPipe, SearchResultItemComponent],
  templateUrl: './search-results.component.html',
  styleUrl: './search-results.component.css'
})
export class SearchResultsComponent {
  results$: Observable<RepositoriesSearchOut[]>;
  loading$: Observable<boolean>;
  error$: Observable<string | null>;

  constructor(private searchState: SearchStateService) {
    this.results$ = this.searchState.results$;
    this.loading$ = this.searchState.loading$;
    this.error$ = this.searchState.error$;
  }
}
