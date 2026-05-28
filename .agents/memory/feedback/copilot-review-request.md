---
name: copilot-review-request
description: How to request or re-request a Copilot PR review programmatically — GraphQL botIds is the only reliable path
metadata:
  type: feedback
  since: 2026-05-25
---

Use the GraphQL `requestReviews` mutation with `botIds` for both initial
requests and re-requests:

```bash
gh api graphql -f query='
mutation {
  requestReviews(input: {
    pullRequestId: "PR_NODE_ID",
    botIds: ["BOT_kgDOCnlnWA"]
  }) {
    pullRequest { id number }
  }
}'
```

- `PR_NODE_ID`: `gh api repos/SpineEventEngine/REPO/pulls/NUMBER --jq '.node_id'`
- `BOT_kgDOCnlnWA`: fixed node ID for the Copilot PR reviewer bot (stable)

**Why:** The REST endpoint (`POST .../requested_reviewers` with
`reviewers[]=Copilot`) silently no-ops on re-requests — it only works for
the first-ever request on a PR. The GraphQL `userIds` field also fails
because Copilot is a Bot, not a User. `botIds` is the correct field and
works for both initial and re-requests.

**How to apply:** Any time a Copilot review needs to be requested or
re-requested, use the GraphQL mutation above. Do not use the REST endpoint
or `@copilot review` comments.
