# Demo Service - Consistency Challenges

This module demonstrates **consistency challenges** when performing multiple operations across different systems within a single transaction.

## The Problem

`OrderService.createOrder()` performs **5 operations**:

1. **Database** - Save Order and OrderLines to PostgreSQL
2. **Kafka** - Publish OrderCreated event
3. **File System** - Save order backup to disk
4. **HTTP** - Notify Loyalty Service API v2
5. **Email** - Send order confirmation

### Why This Is Hard

`@Transactional` **only covers database operations**! It cannot:
- Rollback Kafka messages once sent
- Delete files once written
- Undo HTTP calls
- Recall emails

### Failure Scenarios

**Scenario 1**: File write fails after DB save and Kafka publish
- Order in DB ✓
- Kafka sent ✓
- File backup ✗

**Scenario 2**: Loyalty service fails
- Order in DB ✓
- Kafka sent ✓
- File saved ✓
- No loyalty points ✗

**Scenario 3**: Email fails (20% random failure rate)
- Everything else succeeds ✓
- Customer gets no email ✗

## Running

Start infrastructure:
```bash
docker-compose up -d postgres kafka
docker exec -it postgres psql -U postgres -c "CREATE DATABASE demo_db;"
```

Start service:
```bash
cd demo
mvn spring-boot:run
```

Create order:
```bash
curl -X POST http://localhost:8084/api/orders \
  -H "Content-Type: application/json" \
  -d '{"customerId": "CUST-123", "customerEmail": "test@example.com"}'
```

Run it multiple times to see the random email failures!

## Key Takeaway

This shows why you need:
- **Outbox Pattern** - For reliable Kafka publishing
- **Inbox Pattern** - For idempotent consumption
- **Saga Pattern** - For distributed transactions
- **Idempotency** - For safe retries

See the other services in this project for proper implementations! 🎯
