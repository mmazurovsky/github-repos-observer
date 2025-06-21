import { ComponentFixture, TestBed } from '@angular/core/testing';
import { of, throwError } from 'rxjs';
import { SearchPageComponent } from '@app/features/search/pages/search-page/search-page.component';
import { SearchApiService } from '@app/core/services/search-api.service';
import { SearchStateService } from '@app/core/services/search-state.service';
import { MOCK_SEARCH_RESULTS } from '@app/core/models/repositories-search-out.stub';
import { RepositoriesSearchIn } from '@app/core/models/repositories-search-in.model';
import { SearchBarComponent } from '@app/features/search/components/search-bar/search-bar.component';
import { SearchResultsComponent } from '@app/features/search/components/search-results/search-results.component';
import { ReactiveFormsModule } from '@angular/forms';
import { SearchResultItemComponent } from '@app/features/search/components/search-result-item/search-result-item.component';

describe('SearchPageComponent', () => {
  let component: SearchPageComponent;
  let fixture: ComponentFixture<SearchPageComponent>;
  let searchApiSpy: jasmine.SpyObj<SearchApiService>;
  let stateService: SearchStateService;

  const mockSearchParams: RepositoriesSearchIn = { keywords: 'test' };

  beforeEach(async () => {
    searchApiSpy = jasmine.createSpyObj('SearchApiService', ['searchRepositories']);

    await TestBed.configureTestingModule({
      imports: [
        SearchPageComponent
      ],
      providers: [
        { provide: SearchApiService, useValue: searchApiSpy },
        SearchStateService // Use real state service to check state changes
      ]
    })
    .compileComponents();

    fixture = TestBed.createComponent(SearchPageComponent);
    component = fixture.componentInstance;
    stateService = TestBed.inject(SearchStateService);
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });

  it('should set results when API returns successfully with data', (done) => {
    searchApiSpy.searchRepositories.and.returnValue(of(MOCK_SEARCH_RESULTS));
    spyOn(stateService, 'setResults').and.callThrough();

    stateService.results$.subscribe((results: any) => {
      if (results.length > 0) {
        expect(results).toEqual(MOCK_SEARCH_RESULTS);
        done();
      }
    });

    component.handleSearch(mockSearchParams);
    expect(stateService.setResults).toHaveBeenCalledWith(MOCK_SEARCH_RESULTS);
  });

  it('should set an empty array when API returns successfully with no data', (done) => {
    searchApiSpy.searchRepositories.and.returnValue(of([]));
    spyOn(stateService, 'setResults').and.callThrough();

    let callCount = 0;
    stateService.results$.subscribe((results: any) => {
      callCount++;
      // The second time this is called is after the API call completes.
      if (callCount === 2) {
        expect(results).toEqual([]);
        done();
      }
    });

    component.handleSearch(mockSearchParams);
    // Initially called with [], then called again with the empty response
    expect(stateService.setResults).toHaveBeenCalledWith([]);
    expect(stateService.setResults).toHaveBeenCalledTimes(2);
  });

  it('should set an error when API call fails', (done) => {
    searchApiSpy.searchRepositories.and.returnValue(throwError(() => new Error('API Error')));
    spyOn(stateService, 'setError').and.callThrough();

    stateService.error$.subscribe((error: any) => {
      if (error) {
        expect(error).toContain('An error occurred');
        done();
      }
    });

    component.handleSearch(mockSearchParams);
    expect(stateService.setError).toHaveBeenCalledWith(jasmine.stringMatching(/An error occurred/));
  });

  it('should set loading state correctly during API call', () => {
    searchApiSpy.searchRepositories.and.returnValue(of(MOCK_SEARCH_RESULTS));
    spyOn(stateService, 'setLoading').and.callThrough();

    component.handleSearch(mockSearchParams);

    expect(stateService.setLoading).toHaveBeenCalledWith(true);
    // The finalize operator ensures this is called after the response
    expect(stateService.setLoading).toHaveBeenCalledWith(false);
  });
});
