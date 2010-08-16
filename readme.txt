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

2010.8.16, version 1.1
- initial checkin to github

