import { test, expect } from '@playwright/test';
import { MOCK_SEARCH_RESULTS } from './mock-data';

test('should allow a user to search for repositories and see results', async ({ page }) => {
  // Mock the API response
  await page.route('**/api/search**', route => {
    route.fulfill({
      status: 200,
      contentType: 'application/json',
      body: JSON.stringify(MOCK_SEARCH_RESULTS),
    });
  });

  // Navigate to the app
  await page.goto('/');

  // Fill in the search form
  await page.getByLabel('Keywords').fill('angular');

  // Show optional fields
  await page.getByRole('button', { name: /optional filters/i }).click();

  await page.getByLabel(/Language/).fill('typescript');
  await page.getByLabel(/Created After/).fill('2020-01-01');
  await page.getByLabel(/Result Pages/).fill('5');

  // Click the search button
  await page.getByRole('button', { name: 'Search' }).click();

  // Wait for results to be visible
  await expect(page.getByText('Test Repo 1')).toBeVisible();
  await expect(page.getByText('Test Repo 2')).toBeVisible();

  // Get the results container
  const resultsContainer = page.locator('app-search-results');

  // Scroll through the results
  await resultsContainer.evaluate(node => node.scrollTop = node.scrollHeight);

  // Optional: Add a small delay to visually confirm scrolling in headed mode
  await page.waitForTimeout(1000);

  // Check that the last repo is visible after scrolling
  await expect(page.getByText('Test Repo 6')).toBeVisible();
}); 