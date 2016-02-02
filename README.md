### Filetransfer
Simple CLI-based server and client programs to transfer files between a server and connected clients.  
Start the server program from the folder containing files to transfer and set the port to a valid number.  
The client is started with an address and port. If successfully connected to the server, the client can then send or retrieve files from it.
Additional commands allows the client to view all the files in the current directory, and to reset to another server.

####TODO
- Basically no security measures are in place, which means the server has no control over who connects to the server, or what they are sending and retrieving from it.
  - [ ] Password protection of server
  - [ ] Restrict server to handle certain file extensions
  - [ ] Encryption
- [ ] Graphical user interface
