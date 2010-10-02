PurePlay - a clone of the Play! Framework with the Groovy part removed, thus pure Java based. 

Objectives:

To make a faster and smaller Play clone by removing any dependencies on the Groovy language. 

Groovy is used in Play for 

# view template
# error reporting
# route table parsing

- PurePlay uses Japid as the template engine, therefore the view layer is pure Java.
- PurePlay simplifies the error reporting pages, such as the 4xx and the 5xx pages. The result is less sophisticated than the Play's error pages but is still effective.
- Removing Groovy syntax support in the route file means some fancy hack used by modules like CRUD will not work. The direct result is CRUD is not supported in PurePlay. This is a limitation one must take into account when considering using PurePlay.

PurePlay is based off the Play! 1.1 bazaar repository and is synchronized with it consistently.  


-- Bing Ran (bing_ran@hotmail.com)

History:

2010.10.2, version 1.2alpha

- experiment implementation of Fast Play: 
-- run NettyInvocation directly from the PlayHandler to circumvent the Invoker layer, 20-30% performance gain.
-- Special plugin StaticRouterPlugin that creates a Java class that tranlates the routing rules
in the routes files to statically linked controller action calls. The removal of the reflection based action
invocation yields 50%+ performance gain in the low level HTTP processing cycle.   
-  removed the HTTP1.1 only keep-alive constraint. HTTP1.0 Connection: keep-alive was added.
2010.8.16, version 1.1
	- initial checkin to github
2010.8.17, version 1.1
	- updated the akka module, scala module and added java sample controllers to the akka smaple app.
2010/8/23:
	1. changed the UrlEncodedParser to use "_body" as the key in the params to store the full body text, to avoid possible collision with forms.
	2. added synchronization to the instance initialization of WaitForTasksCompletion in Invoker

