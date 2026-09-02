#!/usr/bin/env bash
set -euo pipefail

# Creates one SNS topic per Locus event type, one SQS queue (+ DLQ) per consuming service, and subscribes each queue to its topic.

REGION="us-east-1"

create_topic() {
  awslocal sns create-topic --name "$1" --region "$REGION" >/dev/null
}

create_queue_with_dlq() {
  local queue_name="$1"
  local dlq_name="${queue_name}-dlq"
  local dlq_arn
  awslocal sqs create-queue --queue-name "$dlq_name" --region "$REGION" >/dev/null
  dlq_arn=$(awslocal sqs get-queue-attributes \
    --queue-url "http://localhost:4566/000000000000/$dlq_name" \
    --attribute-names QueueArn --region "$REGION" \
    --query "Attributes.QueueArn" --output text)
  awslocal sqs create-queue --queue-name "$queue_name" --region "$REGION" \
    --attributes "{\"RedrivePolicy\":\"{\\\"deadLetterTargetArn\\\":\\\"$dlq_arn\\\",\\\"maxReceiveCount\\\":\\\"5\\\"}\"}" >/dev/null
}

subscribe_queue_to_topic() {
  local topic_name="$1"
  local queue_name="$2"
  local topic_arn queue_arn
  topic_arn=$(awslocal sns list-topics --region "$REGION" \
    --query "Topics[?ends_with(TopicArn, ':$topic_name')].TopicArn" --output text)
  queue_arn=$(awslocal sqs get-queue-attributes \
    --queue-url "http://localhost:4566/000000000000/$queue_name" \
    --attribute-names QueueArn --region "$REGION" \
    --query "Attributes.QueueArn" --output text)
  # RawMessageDelivery=true: without it, the SQS message body is SNS's own wrapper JSON with our
  # event envelope embedded as an escaped string field, forcing every consumer to unwrap it twice.
  awslocal sns subscribe --topic-arn "$topic_arn" --protocol sqs \
    --notification-endpoint "$queue_arn" --region "$REGION" \
    --attributes '{"RawMessageDelivery":"true"}' >/dev/null
}

# event-type:consumer1,consumer2,... — mirrors technical-spec.md §7's event catalog.
EVENTS=(
  "user-registered:notification"
  "password-reset-requested:notification"
  "session-completed:analytics,accountability,goal,notification"
  "session-abandoned:analytics"
  "distraction-logged:analytics"
  "goal-deadline-approaching:notification"
  "streak-broken:notification,accountability"
  "weekly-summary-due:notification"
  "accountability-partner-activity-update:notification"
  "user-profile-updated:notification,analytics"
  "user-deleted:session,distraction,goal,analytics,accountability,notification"
)

for entry in "${EVENTS[@]}"; do
  topic="${entry%%:*}"
  consumers="${entry##*:}"
  create_topic "$topic"
  IFS=',' read -ra consumer_list <<< "$consumers"
  for consumer in "${consumer_list[@]}"; do
    queue="${consumer}-${topic}-queue"
    create_queue_with_dlq "$queue"
    subscribe_queue_to_topic "$topic" "$queue"
  done
done

echo "Locus: SNS topics and SQS queues provisioned in LocalStack."
