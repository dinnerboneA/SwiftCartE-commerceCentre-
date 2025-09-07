# SwiftCartE-commerceCentre-

SwiftCartSimulation.java is MAIN

SwiftCart is built to operate with minimal human intervention. It consists of 
the following six key sections: 
1. Order Intake System 
a. Orders arrive from the online platform at a rate of 1 order every 500ms. 
b. Each order is verified for payment, inventory availability, and shipping address.

2. Picking Station 
a. Robotic arms pick items from shelves and place them into order bins. 
b. Up to 4 orders can be picked at a time. 
c. Orders are verified for missing items.

3. Packing Station 
a. Completed bins are packed into shipping boxes (1 order at a time). 
b. A scanner checks each box to ensure contents match the order.

4. Labelling Station 
a. Each box is assigned a shipping label with destination and tracking. 
b. Boxes pass through a quality scanner (1 at a time). 

5. Sorting Area 
a. Boxes are sorted into batches of 6 boxes based on regional zones. 
b. Batches are loaded into transport containers (30 boxes per container). 

6. Loading Bay & Transport 
a. 3 autonomous loaders (AGVs) transfer containers to 2 outbound loading bays. 
b. Trucks take up to 18 containers and leave for delivery hubs. 
c. If both bays are occupied, incoming trucks must wait. 

 
Defective Orders 
• Orders may be rejected at any stage (e.g., out-of-stock items, packing errors, 
mislabelling). 
• Defective orders are removed by a reject handler and logged. 

Capacity Constraints 
• The loading bay has space for only 5 containers. If full, packing must pause. 

Autonomous Loaders 
• Only 3 loaders are available and can break down randomly (simulate with thread stalls). 
• Trucks can only be loaded when a loader and bay are free. 

Concurrent Activities 
• Loaders and outbound trucks operate concurrently. 
• Simulate congestion: e.g., 1 truck is waiting while both loading bays are in use. 

The Statistics 
When all trucks have departed for the day, the system should print a detailed report: 
Confirm that all orders, boxes, and containers have been cleared. 
Maximum, minimum and average loading and wait time per truck. 
      

Total number of orders processed, rejected, boxes packed, containers shipped and trucks 
dispatched 


Deliverables: 

Simulate 600 orders. 
Orders arrive every 500ms. 
Thread classes for order intake, picking, packing, labelling, sorting, loader, and truck. 
Random faults in loaders (simulate breakdowns). 
Rejection logic at multiple stages. 
Blocking/waiting when container capacity or loading bays are full. 
Logging for each major activity per thread. 



