## PROJECT CONTRIBUTORS

Hoang Trung Hieu (hieu.ht183915@sis.hust.edu.vn)

Nguyen Tien Thanh (nguyenthanh@soict.hust.edu.vn)

Nguyen Khanh Van (vannk@soict.hust.edu.vn)

Bui Manh Cuong

Le Vinh Nhon (nhon.lv176841@sis.hust.edu.vn)

Nguyen Chi Hieu

## LICENSE / USE

The javatuples directory contains a collection of network topologies, which are not our work. Other code is original contributions of the above contributors, and is released under the MIT LICENSE (see "./LICENSE"). 

We would appreciate you citing this code and the most relevant of our associated research publications below.

## BACKGROUND

The following research paper explains the ideas behind this tool:

*A Reduction Model For Simulating Large-Scale Interconnection Network* (Nguyen Tien Thanh, Nguyen Khanh Van, Bui Manh Cuong)

## CONTENTS AND STRUCTURE

Directory listing:

* common: contains some data structures and some utility classes for standard inputs and outputs

* config: provides a class which contains all constants in this project

* custom: code for creating the fat-tree graph and and its routing algorithm

* events: contains all the events for the simulator and actions for these events

* infrastructure: provides essentials for infrastructure of this project

* javatuples: contains tuples of network elements

* network: contains all elements of the topology such as layers, hosts, switches, buffers, routing tables, ...

* results: you will find the results of the program here

* routing: code for routing algorithm and routing path

* simulator: code for create the simulator and its actions

* weightedloadexperiment: contains some ways to pair source hosts and destination hosts, GUI of the throughput grapha and main method

## INSTRUCTIONS FOR USE

1. This project is written by java code, so you can run it on IDEs such as Eclipse, Netbeans, IntelliJ IDEA, ...

2. Before running , you need to add the library *SSJ* into your project. SSJ is a Java library for stochastic simulation, developed under the supervision of Pierre L'Ecuyer in the *Simulation and Optimization Laboratory, Department of Computer Science and Operations Research* at Université de Montréal. You can find this library in https://github.com/umontreal-simul/ssj

3. You can change the way to pair source host and destination host in the main method of this project, which is in the package *weightedloadexperiment*.

## NOTES

* This README, another README in upperBound, our papers mentioned above, and the comments in the code are the entirety of the documentation available. You can reach us at the email addresses mentioned above for other queries. Help is not promised, but if we have time, we can look into it.
