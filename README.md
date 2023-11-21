# CSC-401-P2P-Project
NCSU CSC-401 Project 1 
1/4
CSC 401 – Data and Computer Communications Networks
Project #1
Spring 2023
Project Objectives
In this project, you will implement a simple peer-to-peer (P2P) system with a centralized index (CI). Although this
P2P-CI system is rather elementary, in the process I expect that you will develop a good understanding of P2P and
client-server systems and build a number of fundamental skills related to writing Internet applications, including:
• becoming familiar with network programming and the socket interface,
• creating server processes that wait for connections,
• creating client processes that contact a well-known server and exchange data over the Internet,
• defining a simple application protocol and making sure that peers and server follow precisely the
specifications for their side of the protocol in order to accomplish particular tasks,
• creating and managing a centralized index at the server based on information provided by the peers, and
• implementing a concurrent server that is capable of carrying out communication with multiple clients
simultaneously.
Peer-to-Peer with Centralized Index (P2P-CI) System for Downloading RFCs
Internet protocol standards are defined in documents called “Requests for Comments” (RFCs). RFCs are available
for download from the IETF web site (http://www.ietf.org/). Rather than using this centralized server for
downloading RFCs, you will build a P2P-CI system in which peers who wish to download an RFC that they do not
have in their hard drive, may download it from another active peer who does. All communication among peers or
between a peer and the server will take place over TCP. Specifically, the P2P-CI system will operate as follows;
additional details on each component of the system will be provided shortly.
