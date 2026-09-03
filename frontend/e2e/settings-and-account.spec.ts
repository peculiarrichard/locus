import { expect, test } from '@playwright/test'
import { closeApp, launchApp } from './helpers/electron'
import { createVerifiedUser } from './helpers/users'

const GATEWAY_ORIGIN = process.env.LOCUS_GATEWAY_ORIGIN ?? 'http://localhost:8080'

test('devices list shows the current session, and account deletion via the re-auth dialog actually deletes the account', async () => {
  const user = await createVerifiedUser('account-deletion')
  const { app, page } = await launchApp()

  try {
    await page.getByLabel('Email').fill(user.email)
    await page.getByLabel('Password').fill(user.password)
    await page.getByRole('button', { name: 'Log in' }).click()
    await expect(page.getByRole('heading', { name: 'Dashboard' })).toBeVisible()

    await page.getByRole('link', { name: 'Settings' }).click()
    await page.getByRole('link', { name: 'Security' }).click()
    await expect(page.getByText('Unknown device')).toBeVisible()

    await page.getByRole('link', { name: 'Account', exact: true }).click()
    await page.getByRole('button', { name: 'Delete my account' }).click()
    await expect(page.getByRole('heading', { name: 'Delete your account' })).toBeVisible()

    await page.getByLabel('Confirm your password').fill(user.password)
    await page.getByRole('button', { name: 'Permanently delete' }).click()

    await expect(page.getByRole('heading', { name: 'Welcome back' })).toBeVisible({ timeout: 10_000 })

    const loginRes = await fetch(`${GATEWAY_ORIGIN}/api/v1/auth/login`, {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify(user)
    })
    expect(loginRes.status).toBe(401)
  } finally {
    await closeApp(app)
  }
})

test('distraction-frequency and history analytics screens render their zero-states for a fresh account', async () => {
  const user = await createVerifiedUser('analytics-zero-state')
  const { app, page } = await launchApp()

  try {
    await page.getByLabel('Email').fill(user.email)
    await page.getByLabel('Password').fill(user.password)
    await page.getByRole('button', { name: 'Log in' }).click()
    await expect(page.getByRole('heading', { name: 'Dashboard' })).toBeVisible()

    await page.getByRole('link', { name: 'Analytics' }).click()
    await page.getByRole('link', { name: 'Distractions' }).click()
    await expect(page.getByText('No distractions logged yet.')).toBeVisible()

    await page.getByRole('link', { name: 'History' }).click()
    await expect(page.getByText('No sessions in this range yet.')).toBeVisible()
  } finally {
    await closeApp(app)
  }
})
