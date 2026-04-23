
Question 1:

By default, JAX-RS creates a new instance of a resource class for every incoming 
HTTP request. This is known as per-request scope. This means any instance variables 
inside the resource class are not shared between requests and are discarded after 
the response is sent.

This has an important implication for data management. Because each resource instance 
is fresh, you cannot store shared data (like rooms or sensors) as instance variables 
inside the resource class itself — they would be lost after every request. To solve 
this, we use a singleton DataStore class (using the Singleton design pattern) that 
lives for the entire lifetime of the application. All resource classes access the 
same shared DataStore instance. We also use ConcurrentHashMap instead of a regular 
HashMap to prevent race conditions when multiple requests try to read or write data 
at the same time.

Question 2: 

HATEOAS (Hypermedia as the Engine of Application State) means that API responses 
include links to related resources and available actions, rather than just raw data. 
For example, our discovery endpoint returns links to /api/v1/rooms and /api/v1/sensors, 
telling clients where to go next without needing to hardcode URLs.

This benefits client developers significantly because they do not need to memorise 
or hardcode endpoint paths — the API itself guides them through available actions. 
It also means that if the API changes its URL structure, clients that follow links 
dynamically will continue to work without modification, unlike clients that rely on 
static documentation.

Question 3:

Returning only IDs uses less network bandwidth since the response payload is much 
smaller. However, it forces the client to make additional HTTP requests to fetch 
details for each room individually, which increases latency and server load, 
especially if there are many rooms.

Returning full room objects uses more bandwidth per response but means the client 
has everything it needs in a single request, reducing round trips. For most use cases, 
returning full objects is preferable unless the list is extremely large, in which case 
pagination with full objects would be the best approach.

Question 4:

Yes, DELETE is idempotent in our implementation. Idempotent means that making the 
same request multiple times produces the same result as making it once. If a client 
sends DELETE /api/v1/rooms/LIB-301 and the room exists, it will be deleted and a 
200 OK is returned. If the same request is sent again, the room no longer exists 
and a 404 Not Found is returned. The end state of the server is the same in both 
cases — the room does not exist — which satisfies the definition of idempotency. 
The response code may differ but the server state does not change on the second call.

Question 5:

If a client sends a request with a Content-Type header of text/plain or 
application/xml instead of application/json, JAX-RS will automatically reject 
the request before it even reaches our method. The framework returns an HTTP 
415 Unsupported Media Type response, indicating that the server cannot process 
the format provided. This protects the API from malformed or unexpected input 
without requiring any manual checking inside our resource methods.

question 6:

Using a query parameter (e.g. /api/v1/sensors?type=CO2) is superior to a path 
parameter (e.g. /api/v1/sensors/type/CO2) for filtering because query parameters 
are optional by nature. This means the same endpoint /api/v1/sensors can return 
all sensors when no filter is provided, or a filtered subset when a query parameter 
is included. 

With a path-based approach, you would need a separate endpoint for filtered vs 
unfiltered results, which violates REST principles by creating multiple URLs for 
the same resource. Query parameters are also the universally recognised convention 
for filtering, sorting, and searching collections, making the API more intuitive 
for developers.

question 7:

The sub resource locator pattern allows us to delegate handling of nested paths 
to a dedicated class. 

This approach manages complexity effectively in large APIs. If every nested endpoint 
was defined in a single resource class, that class would become extremely large and 
difficult to maintain. By splitting responsibilities into focused classes, each class 
has a single clear purpose — SensorResource manages sensors, SensorReadingResource 
manages readings. This follows the Single Responsibility Principle, makes the code 
easier to test, and allows different developers to work on different resource classes 
simultaneously without conflicts.

Question 8:

A 404 Not Found typically means the URL endpoint itself does not exist. However, 
when a client POSTs a valid JSON body to a valid endpoint but includes a roomId 
that references a non-existent room, the endpoint was found successfully — the 
problem is with the content of the request body. HTTP 422 Unprocessable Entity 
is more accurate because it signals that the request was well-formed and reached 
the correct endpoint, but the server cannot process it due to a semantic error 
in the data — specifically, a reference to a resource that does not exist.

Question 9:

Exposing raw Java stack traces is a significant security risk for several reasons. 
Stack traces reveal the internal structure of the application, including package 
names, class names, method names, and line numbers. An attacker can use this 
information to identify which frameworks and libraries are being used and look up 
known vulnerabilities for those specific versions. Stack traces can also reveal 
file paths on the server, database query structures, and logic flow, all of which 
help an attacker map the system and craft targeted exploits. Our GlobalExceptionMapper 
prevents this by catching all unexpected errors and returning a generic 500 response 
with no internal details.

Question 10:

Using a JAX-RS filter for logging means the logging logic is defined in one place 
and applied automatically to every request and response in the entire API. This 
follows the DRY principle (Don't Repeat Yourself). If logging was added manually 
to every resource method, it would mean duplicating the same code dozens of times, 
making it easy to forget to add it to new methods and making it harder to change 
the logging format later. Filters also run at the framework level, meaning they 
capture every request including ones that fail before reaching a resource method, 
giving more complete observability.





