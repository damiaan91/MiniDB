# MiniDB and AOPChat with Aspects
======

## Package Structure
* **AOPChat** - A simple client-server chat program.
* **AOPChatAspacts** - The server of AOPChat with aspects.
* **aspects.core** - A libarary which houses the core functionalities to create aspect for authentication, statistics and logging.
* **minidb.aspects** - The MiniDB application with aspects.
* **minidb.core.test** - Junit test for the MiniDB application.
* **minidb.core** - The MiniDB application, which simulates a simple database.

## Installation

The easiest way the run the programs is to import the packages in a ecplise workspace an follow the relevant instructions bellow.

Start AOPChat base program:
* Run the EchoServer in AOPChat to start a chat server.
* Run the ChatGUI in AOPChat to start a chat client.

Start AOPChat with aspects:
* Run the AspectedServer in AOPChatAspects to start a chat server with aspect.
* Run the ChatGUI in AOPChat to start a chat client (the aspects are only used on the server, so normal clients works with the AspectedServer).

Start MiniDB base program:
* Run the Client in minidb.core to start a minidb instance (login: admin/admin).

Start MiniDB with aspects:
* Run the MiniDBWithAspects in minidb.aspects to start a minidb instance with aspects (login: admin/admin).
