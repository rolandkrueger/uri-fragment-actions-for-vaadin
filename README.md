URI Fragment Actions for Vaadin
===============================

This project provides a [Vaadin](https://vaadin.com/home) add-on which enhances the standard Vaadin Navigator component with a sophisticated interpreter for URI fragments. Using this add-on, you can design the URI fragments used by your application in an arbitrarily complex hierarchy. Furthermore, your URI fragments can contain any number of parameter values which will be interpreted and converted automatically so that these values can be handled in a type-safe way.

For example, it is possible to use URI fragments like the following:

```text
#!/home
#!/admin/settings
#!/admin/users
#!/admin/users/id/4711/showHistory/startDate/2017-01-01/endDate/2017-01-31
#!/admin/users/id/4711/profile/activeTabsheet/address
```

As you can see, these URI fragments form a hierarchy where each individual path element can have an arbitrary number of parameters.

This add-on requires Java 8 as a minimum.
  
Background
----------
In the standard implementation, Vaadin's Navigator component only allows the handling of URI fragments which are very simple. In short, the first part of a URI fragment up to the first occurence of the '/' character is interpreted as a view name. The rest of the URI fragment starting with but excluding the '/' character is interpreted as a parameter string. The view name is used to find a class which implements class `com.vaadin.navigator.View`. If such a class is found, it is instantiated and passed to a `com.vaadin.navigator.ViewDisplay` in order to have it shown in the browser. If the URI fragment contains a parameter string, this is passed as is to the code which is responsible for displaying the View. There is no support for further disassembling and interpreting the parameter string. It is the sole responsibility of the developer to find a way to extract meaningful data from this parameter string.
   
There is similarly no support for the opposite way: creating a parameterized URI fragment for a particular `View` to be used in a `Link` component. This has to be done by hand. In doing so, great care has to be taken by the developer to implement this process in a way that is safe with refactorings.
 
URI Fragment Actions
--------------------

This add-on makes this task a no-brainer. The interpretation of a hierarchical URI fragment with deeply nested sub-structure and the extraction and type-safe data conversion of parameter values are all managed by the add-on. Furthermore, URI fragments for usage in links which carry the current state of the application can be generated in a way which is robust against refactorings. This is because the structure of the complete URI fragment hierarchy of an application is only defined once in one central place. Changing the structure of this hierarchy (e. g., when parts of the hierarchy need to be renamed) can be easily done in this central spot without affecting any code that generates URI fragments for links.
   
Library uri-fragment-rouing
---------------------------
The basis for this feature is established by the library [uri-fragment-routing](https://github.com/rolandkrueger/uri-fragment-routing). This library is responsible for  the URI fragment interpretation process, the extraction and conversion of parameter values and the management of the URI fragment hierarchy.

This Vaadin add-on only consists of a single wrapper class around a common Vaadin Navigator and the boilerplate code to make the URI fragment interpretation process of the library `uri-fragment-routing` available for Vaadin applications.
  
This library uses a different approach for interpreting URI fragments than the standard Vaadin Navigator. While the Navigator tries to extract a view name from the URI fragment, the `uri-fragment-routing` interprets the complete URI fragment and tries to resolve it into an *action command class*. If such a class could be found for a given URI fragment, it is instantiated and executed. Thus, this library uses the *Command Design Pattern*, while the hierarchical URI fragments are interpreted using the *Chain of Responsibility Design Pattern*.

For details about the correct usage of the library `uri-fragment-routing`, please consult the documentation provided on the library's [project page](https://github.com/rolandkrueger/uri-fragment-routing).