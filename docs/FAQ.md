<p align="center">
<img src="./cdpg.png" width="300">
</p>

# Frequently Asked Questions (FAQs)

1. What is the purpose of the auditing server?
   - The auditing server records and manages access logs for various servers in CDPG, ensuring that all access and operations on the system are traceable.

2. How do I set up the auditing server?
   - Setting up the auditing server involves configuring the server environment, connecting to the message broker (such as RabbitMQ), and setting up any necessary database connections (e.g., PostgreSQL, Immudb). Detailed setup instructions can be found [here](SETUP-and-Installation.md)

3. What databases are used in the auditing server, and why?
   - The auditing server uses PostgreSQL for fast read access and ImmuDB for immutable data storage, ensuring that audit logs are both accessible and tamper-proof.

4. How is data stored in the auditing server?
   - Data is stored in both PostgreSQL and ImmuDB, with PostgreSQL being used for fast reads and ImmuDB for append-only, immutable data storage. This dual approach ensures that the data is both accessible and tamper-proof.

5. What happens if the auditing server goes down?
   - In case of downtime, the auditing server uses RabbitMQ to temporarily store messages, which will be processed once the server is back online.

6. How can I verify that data in ImmuDB has not been altered?
   - ImmuDB provides a way to verify data integrity by hashing logs, allowing you to confirm that records remain unchanged since their initial storage.

7. How can I troubleshoot common issues with the auditing server?
   - Check server logs, RabbitMQ message queues, and database health for common issues. Also, consult the error logs and RabbitMQ’s status to diagnose connectivity issues.

