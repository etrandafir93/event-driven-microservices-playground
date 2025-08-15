curl -X POST "http://localhost:8081/api/v1/orders" ^
     -H "Content-Type: application/json" ^
     -d "{\"username\": \"john_doe\", \"products\": {\"TV-55-SAM-QLED\": 2,\"TV-55-SAM-QLED\": 2,\"TV-55-SAM-QLED\": 2,\"TV-55-SAM-QLED\": 2}}"