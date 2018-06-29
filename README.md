# tftp-server
A sample TFTP server written in Java that handles WRQ operation

In order to do this I had to reference the RFC for TFTP (https://tools.ietf.org/html/rfc1350) and code to those standards.

The most difficult part of understanding the RFC is understanding some of the components of the requests that were sent.
For example, in the WRQ there is a field which specifies the mode of the request.
I had difficulty figuring out how the mode is used and how to incorporate that into the Java service.
Another question I had when reading the RFC is whether the service can handle multiple requests at once, which would therefore change my Java service to use a multithreaded model.
There was no mention of that in the RFC, so I assumed that the service should be able to support concurrent requests.
This is what added the most complexity to the code.

If I could do this differently, I would have drawn a flow diagram beforehand detailing all the different scenarios that had to be covered. I think I may have missed something.
I would have spent more time looking at different design approaches before deciding on one.
There is also some more refactoring I could have done.
Also if time had permitted, I would have written a client to test out my code.
