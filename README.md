# ZeroKnowledge
An implementation of a Zero Knowledge Protocol for the Discrete Logarithm.

The program uses TCP and peer-to-peer to simulate a Zero Knowledge proof for the discrete logarithm.

The server application is the verifier (they want to determine if the client knows some fixed private "passcode" data,
but the server wants to not store even a cryptographic hash of passwords as that might open up side-channel attacks
through database frequency of certain hashes).

The client application is the prover (they need to convince the server that they know the relevant "passcode" data).
