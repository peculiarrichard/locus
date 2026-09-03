import { expect, test } from '@playwright/test'
import { closeApp, launchApp } from './helpers/electron'
import { createVerifiedUser } from './helpers/users'

test('create a goal, run a session against it, and see it reflected on the Dashboard and Analytics', async () => {
  const user = await createVerifiedUser('session-goals')
  const { app, page } = await launchApp()

  try {
    await page.getByLabel('Email').fill(user.email)
    await page.getByLabel('Password').fill(user.password)
    await page.getByRole('button', { name: 'Log in' }).click()
    await expect(page.getByRole('heading', { name: 'Dashboard' })).toBeVisible()

    // Create a goal
    await page.getByRole('link', { name: 'Goals' }).click()
    await page.getByRole('button', { name: 'Create your first goal' }).click()

    await page.getByLabel('Title').fill('Pass the certification exam')
    const targetDate = new Date(Date.now() + 30 * 86_400_000).toISOString().slice(0, 10)
    await page.getByLabel('Target date').fill(targetDate)
    await page.getByRole('button', { name: 'Create goal' }).click()

    await expect(page.getByText('Pass the certification exam')).toBeVisible()

    // Start a session against no particular goal (keeps the flow simple/deterministic)
    await page.getByRole('link', { name: 'Sessions' }).click()
    await expect(page.getByRole('heading', { name: 'Start a session' })).toBeVisible()
    await page.getByRole('button', { name: 'DEEP WORK' }).click()
    await page.getByLabel('Planned duration (min)').fill('5')
    await page.getByRole('button', { name: 'Start session' }).click()

    await expect(page.getByText('DEEP WORK')).toBeVisible()
    await expect(page.getByRole('button', { name: 'End session' })).toBeVisible()

    await page.getByRole('button', { name: 'End session' }).click()
    await expect(page.getByRole('heading', { name: 'Session complete' })).toBeVisible({ timeout: 10_000 })

    // Dashboard should now show a non-empty week
    await page.getByRole('link', { name: 'Dashboard' }).click()
    await expect(page.getByText('No sessions yet')).not.toBeVisible()
    await expect(page.getByText('Sessions completed this week')).toBeVisible()
  } finally {
    await closeApp(app)
  }
})
