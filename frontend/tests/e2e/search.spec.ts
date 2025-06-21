import { expect, test } from '@playwright/test';
import { MOCK_SEARCH_RESULTS } from './mock-data';

test('should allow a user to search for repositories and see results', async ({ page }) => {
  // Mock the API response with artificial delay to simulate loading
  await page.route('**/api/search**', async route => {
    // Add 2-3 second delay to simulate real API loading time
    await new Promise(resolve => setTimeout(resolve, 2500));

    route.fulfill({
      status: 200,
      contentType: 'application/json',
      body: JSON.stringify(MOCK_SEARCH_RESULTS),
    });
  });

  // Navigate to the app
  await page.goto('/');

  // Pause to see the initial page load
  await page.waitForTimeout(2000);

  // Fill in the search form
  await page.getByLabel('Keywords').fill('angular');

  // Pause after entering keywords
  await page.waitForTimeout(1500);

  // Show optional fields
  await page.getByRole('button', { name: /optional filters/i }).click();

  // Pause to see optional fields expand
  await page.waitForTimeout(1000);

  await page.getByLabel(/Language/).fill('typescript');
  await page.waitForTimeout(500);

  await page.getByLabel(/Created After/).fill('2020-01-01');
  await page.waitForTimeout(500);

  await page.getByLabel(/Result Pages/).fill('5');

  // Pause before clicking search to see all inputs filled
  await page.waitForTimeout(2000);

  // Click the search button
  await page.getByRole('button', { name: 'Search' }).click();

  // Pause to see loading state
  await page.waitForTimeout(1500);

  // Wait for results to be visible
  await expect(page.getByText('Test Repo 1')).toBeVisible();
  await expect(page.getByText('Test Repo 2')).toBeVisible();

  // Pause to observe the search results
  await page.waitForTimeout(3000);

  // Get the results container
  const resultsContainer = page.locator('app-search-results');

  // Scroll through the results
  await resultsContainer.evaluate(node => node.scrollTop = node.scrollHeight);

  // Pause to visually confirm scrolling in headed mode
  await page.waitForTimeout(2000);

  // Check that the last repo is visible after scrolling
  await expect(page.getByText('Test Repo 6')).toBeVisible();

  // Final pause to see the completed test
  await page.waitForTimeout(2000);
}); 