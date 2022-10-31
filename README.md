# mini-bank
 Scala Mini-banking-Api project with Akka, Cats and Cassandra

# Demo
Available Routes

Adding a new bank account

`curl -v -X POST http://localhost:8080/bank\
   -H 'Content-Type: application/json'\
   -d '{"user":"rcardin", "currency":"EUR", "balance": 1000.0}'`
   
Updating the balance of a bank account

`curl -v -X PUT http://localhost:8080/bank/{bank account id}\
   -H 'Content-Type: application/json'\
   -d '{"currency":"EUR", "amount": 500.0}'`
   
Retrieving the details of a bank account

`curl -v http://localhost:8080/bank/{bank account id}`
