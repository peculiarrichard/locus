const MAILPIT_ORIGIN = process.env.MAILPIT_ORIGIN ?? 'http://localhost:8025'

interface MailpitMessage {
  ID: string
  To: { Address: string }[]
  Snippet: string
}

// Polls Mailpit for the most recent message to a given address and pulls the verification token
// out of its snippet — real email delivery, not a stub, matching how every prior phase in this
// project verified email-driven flows.
export async function getLatestVerificationToken(toAddress: string, timeoutMs = 15_000): Promise<string> {
  const deadline = Date.now() + timeoutMs
  while (Date.now() < deadline) {
    const res = await fetch(`${MAILPIT_ORIGIN}/api/v1/messages?limit=25`)
    const data = (await res.json()) as { messages: MailpitMessage[] }
    const match = data.messages.find((m) => m.To.some((t) => t.Address === toAddress))
    if (match) {
      const tokenMatch = /token=([^\s"&]+)/.exec(match.Snippet)
      if (tokenMatch) {
        return tokenMatch[1]
      }
    }
    await new Promise((resolve) => setTimeout(resolve, 500))
  }
  throw new Error(`No verification email found for ${toAddress} within ${timeoutMs}ms`)
}
