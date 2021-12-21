# Cloudsim Scheduling Algorithm Implimentation

## Algorithm Name
**Task scheduling using Weighted Active Monitoring Load Distribution technique.**

## Explanation
This algorithm creates VM‟s of different processing power and allocates
weighted count according to the computing power of the VM. WALB maintains index table
of VM‟s, associated weighted count and number of request currently allocated to each
VM. When a request to allocate a VM arrives from the Data Center Controller, this
algorithm identifies the least loaded and most powerful VM according to the weight
assigned and returns its VM id to the Data Center Controller. The Data Center Controller
sends a request to the identified VM and notifies the algorithm of allocation. The
algorithm increases the count by one for that VM. When VM finishes processing,
algorithm decreases the count of that VM by one. The experimental result shows that the
proposed algorithm achieves better performance factors such as response time and
processing time, but the algorithm does not consider process duration for each individual
request.
