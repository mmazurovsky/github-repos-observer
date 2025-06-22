import { RepositoriesSearchOut } from './repositories-search-out.model';

export const MOCK_SEARCH_RESULTS: RepositoriesSearchOut[] = [
  {
    name: 'Test Repo 1',
    url: 'http://example.com/repo1',
    language: 'TypeScript',
    created: '2023-01-01',
    stars: 100,
    forks: 50,
    recency: 'recent',
    popularityScore: '9.5',
  },
  {
    name: 'Test Repo 2',
    url: 'http://example.com/repo2',
    language: 'Java',
    created: '2022-05-15',
    stars: 200,
    forks: 150,
    recency: 'old',
    popularityScore: '8.7',
  },
];
