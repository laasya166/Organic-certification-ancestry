# Contracts (agree before coding)

## Kafka
Topic `soa.s03.t08.events` (key = blendId), dead-letter `soa.s03.t08.events.DLT`.

BlendCreated v1:
```json
{
  "eventId": "7d1c2f0e-3a4b-4c55-9d1a-2b8f6e9a1c01",
  "aggregateId": "101",
  "schemaVersion": 1,
  "occurredAt": "2026-10-27T09:30:00Z",
  "correlationId": "c0ffee00-1111-2222-3333-444455556666",
  "processedAt": "2026-10-27",
  "outputLotId": "X1",
  "inputLotIds": ["A", "B", "C"]
}
```
Java record: `common/.../event/BlendCreatedEvent.java`.

## Certificate validity (certificate-service)
`GET /certificates/validity?lotIds=A,B,C&on=2026-10-27`
```json
[
  {"lotId": "A", "valid": true,  "reason": null},
  {"lotId": "B", "valid": true,  "reason": null},
  {"lotId": "C", "valid": false, "reason": "NO_CERTIFICATE"}
]
```
Reasons: `NO_CERTIFICATE`, `EXPIRED`, `NOT_YET_VALID`, `REVOKED`.

## Validation result (blend-validation-service)
`GET /validations/blends/{blendId}` -> `{ "blendId": "101", "status": "UNCERTIFIED", "reasons": ["C: NO_CERTIFICATE"], "evaluatedAt": "..." }`
Status values: `ORGANIC_ELIGIBLE`, `UNCERTIFIED`.

## JWT
HS256, secret from env `JWT_SECRET`. Claims: `sub` (username), `roles` (array: `processor` and/or `certificationReviewer`), `exp`.
- processor: create lots and blends, read own records
- certificationReviewer: attach/revoke certificates, read all validations

## Seed data (fixed demo date 2026-10-27)
Lots: A oats 500kg, B almonds 200kg, C raisins 100kg, D hazelnuts 150kg.
Certificates: A 2026-01-01..2026-12-31; B 2026-03-01..2027-02-28; C none; D expired (validTo 2026-06-30).
Scenarios: A+B -> ORGANIC_ELIGIBLE; A+B+C -> UNCERTIFIED; A+D -> UNCERTIFIED (expired).
