FunctionHood: Compute distance-k neighbours of any monotone Boolean Function
=========================================================

The aim of this project is to, given a monotone Boolean function, be able to compute 
its closely related functions without the need to generate all possible O(2^2^n) Boolean functions.


How to use it?
--------------

To compile it you will need is java6 JDK and [maven](http://maven.apache.org/).

* grab the source from github
* run "mvn package" to compile and package it
* you can use the jar in the "target" subdirectory.

You can either integrate the .jar file in your tool, or launch the GUI to compute immediate neighbour functions, as follows:

    java -jar FunctionHood-0.1.jar

Licence
-------

This code is available under GPL-3.0.


Authors
-------

José E. R. Cury

Pedro T. Monteiro

Claudine Chaouiya
